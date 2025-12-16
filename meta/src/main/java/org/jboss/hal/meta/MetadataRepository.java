/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.db.LRUCache;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Settings;

import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;

import static elemental2.core.Global.JSON;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Repository for metadata. Contains a first and second-level cache for metadata.
 * <p>
 * Metadata can be obtained synchronously via {@link #get(AddressTemplate)} or asynchronously via {@link #lookup(List)},
 * {@link #lookup(AddressTemplate, Consumer)}, and {@link #lookup(AddressTemplate)}.
 */
@ApplicationScoped
public class MetadataRepository {

    // TODO Do not cache specific resources (they have different attributes/operations depending on their state):
    //  /host=*
    //  /host=*/server=*
    //  /server-group=* ?
    //  anything else?

    // TODO Add support for 2nd level cache!
    private static final int FIRST_LEVEL_CACHE_SIZE = 500;
    private static final Logger logger = Logger.getLogger(MetadataRepository.class.getName());

    private final Settings settings;
    private final Dispatcher dispatcher;

    /**
     * Template resolver used by this metadata repository. The template resolver is applied
     * <ol>
     *     <li>to address templates in {@link #get(AddressTemplate)} and {@link #lookup(AddressTemplate)} to check if a metadata is in the cache and</li>
     *     <li>to resource addresses from the rrd-payload in {@link #addMetadata(Metadata)} when adding metadata to the cache</li>
     * </ol>
     */
    private final TemplateResolver resolver;

    /**
     * First level cache for metadata. Key is the resolved address template, value is the metadata
     */
    private final LRUCache<String, Metadata> cache;

    /**
     * Contains the mapping between the requested address template and the processed resource addresses from the rrd-payload.
     * Keys and values are resolved using the {@link #resolver} before they're added to the map.
     * <p>
     * This mapping is necessary for address templates like {@code core-service=*} or {@code subsystem=*} which result in
     * multiple rrd-results.
     * <p>
     * For simple address templates like {@code interface=*} or {@code core-service=management} no mapping is necessary.
     */
    private final Map<String, Set<String>> processedAddresses;

    @Inject
    public MetadataRepository(Settings settings,
            Dispatcher dispatcher,
            StatementContext statementContext) {
        this.settings = settings;
        this.dispatcher = dispatcher;
        this.resolver = new MetadataResolver(statementContext);
        this.cache = new LRUCache<>(FIRST_LEVEL_CACHE_SIZE);
        this.processedAddresses = new HashMap<>();

        cache.addRemovalHandler((address, __) -> logger.debug("LRU metadata for %s has been removed", address));
    }

    // ------------------------------------------------------ api

    /**
     * Performs a parallel lookup for metadata based on a list of address templates. Each template in the list is processed and
     * the associated metadata is retrieved.
     *
     * @param templates the list of address templates to perform the lookup for
     * @return a Promise that resolves to {@code Void} when all lookups have been completed
     */
    public Promise<Void> lookup(List<AddressTemplate> templates) {
        List<Task<FlowContext>> tasks = templates.stream()
                .map(template -> (Task<FlowContext>) context -> lookup(template).then(__ -> context.resolve()))
                .collect(toList());
        return Flow.parallel(new FlowContext(), tasks).then(context -> Promise.resolve((Void) null));
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template  the address template to perform the lookup for
     * @param onSuccess the consumer to accept the retrieved metadata
     */
    public void lookup(AddressTemplate template, Consumer<Metadata> onSuccess) {
        lookup(template).then(metadata -> {
            onSuccess.accept(metadata);
            return null;
        });
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template the address template to perform the lookup for
     * @return a Promise representing the lookup result, containing the metadata associated with the address template
     */
    public Promise<Metadata> lookup(AddressTemplate template) {
        String address = resolveTemplate(template);
        Metadata metadata = internalGet(address);
        if (metadata != null) {
            logger.debug("Lookup metadata for %s → %s from cache", template, address);
            return Promise.resolve(metadata);
        } else {
            Set<String> processed = processedInCache(address);
            if (processed.isEmpty()) {
                logger.debug("Process metadata for %s → %s", template, address);
                return process(template, singleton(address));
            } else if (processed.size() == 1) {
                address = processed.iterator().next();
                metadata = internalGet(address);
                if (metadata == null) {
                    logger.debug("Process metadata for %s → %s", template, address);
                    return process(template, singleton(address));
                } else {
                    logger.debug("Lookup metadata for %s → %s from cache", template, address);
                    return Promise.resolve(metadata);
                }
            } else {
                logger.debug("Metadata for %s → %s has been processed, but resulted in multiple metadata. " +
                        "Returning an empty metadata", template, address);
                return Promise.resolve(Metadata.undefined());
            }
        }
    }

    /**
     * Retrieves the {@link Metadata} associated with the provided {@link AddressTemplate}. The method first resolves the
     * address template and attempts to retrieve the corresponding metadata from an internal cache. If the metadata is not
     * found, it checks for processed entries and applies specific fallback logic to either retrieve or return an undefined
     * metadata object.
     *
     * @param template the {@link AddressTemplate} used to resolve and retrieve the associated metadata
     * @return the {@link Metadata} associated with the resolved address, or an undefined metadata object if no corresponding
     * metadata can be retrieved
     */
    public Metadata get(AddressTemplate template) {
        String address = resolveTemplate(template);
        Metadata metadata = internalGet(address);
        if (metadata != null) {
            logger.debug("Get metadata for %s → %s from cache", template, address);
            return metadata;
        } else {
            Set<String> processed = processedInCache(address);
            if (processed.isEmpty()) {
                logger.error("No metadata found for %s → %s. Returning an empty metadata", template, address);
                return Metadata.undefined();
            } else if (processed.size() == 1) {
                address = processed.iterator().next();
                metadata = internalGet(address);
                if (metadata == null) {
                    logger.error("No metadata found for %s → %s. Returning an empty metadata", template, address);
                    return Metadata.undefined();
                } else {
                    logger.debug("Get metadata for %s → %s from cache", template, address);
                    return metadata;
                }
            } else {
                logger.debug("Metadata for %s → %s has been processed, but resulted in multiple metadata. " +
                        "Returning an empty metadata", template, address);
                return Metadata.undefined();
            }
        }
    }

    // ------------------------------------------------------ js api

    private static MetadataRepository instance;

    @PostConstruct
    void init() {
        MetadataRepository.instance = this;
    }

    @JsMethod(name = "get")
    private static Object jsGet(String address) {
        if (instance != null) {
            Metadata metadata = instance.get(AddressTemplate.of(address));
            return JSON.parse(metadata.toJSONString());
        } else {
            logger.error("MetadataRepository not initialized");
            return JSON.parse("{\"error\": \"MetadataRepository not initialized\"}");
        }
    }

    @JsMethod(name = "lookup")
    private static Promise<Object> jsLookup(String address) {
        if (instance != null) {
            return instance.lookup(AddressTemplate.of(address))
                    .then(metadata -> Promise.resolve(JSON.parse(metadata.toJSONString())));
        } else {
            logger.error("MetadataRepository not initialized");
            return Promise.reject("MetadataRepository not initialized");
        }
    }

    @JsMethod(name = "dump")
    private static Object jsDump() {
        if (instance != null) {
            StringBuilder builder = new StringBuilder("{\"firstLevelCache\": ")
                    .append(instance.cache.size())
                    .append(", \"secondLevelCache\": 0, \"entries\": [");
            for (Iterator<Map.Entry<String, LRUCache.Node<String, Metadata>>> iterator = instance.cache.entries().iterator();
                    iterator.hasNext(); ) {
                Map.Entry<String, LRUCache.Node<String, Metadata>> entry = iterator.next();
                builder.append("\"")
                        .append(entry.getKey())
                        .append("\"");
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("], \"processed\": [");
            for (Iterator<Map.Entry<String, Set<String>>> iterator = instance.processedAddresses.entrySet().iterator();
                    iterator.hasNext(); ) {
                Map.Entry<String, Set<String>> entry = iterator.next();
                builder.append("{\"requested\": \"")
                        .append(entry.getKey())
                        .append("\", \"resolved\":");
                builder.append(entry.getValue().stream()
                        .map(s -> "\"" + s + "\"")
                        .collect(joining(",", "[", "]")));
                builder.append("}");
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]}");
            return JSON.parse(builder.toString());
        } else {
            logger.error("MetadataRepository not initialized");
            return JSON.parse("{\"error\": \"MetadataRepository not initialized\"}");
        }
    }

    // ------------------------------------------------------ internal

    void addMetadata(Metadata metadata) {
        logger.debug("Add metadata for %s", metadata.resourceAddress());
        cache.put(metadata.address(), metadata);
    }

    void addProcessedAddresses(String address, Set<String> processedAddresses) {
        logger.debug("Add processed addresses %s → %s", address, processedAddresses);
        this.processedAddresses.computeIfAbsent(address, k -> new HashSet<>()).addAll(processedAddresses);
    }

    private Set<String> processedInCache(String address) {
        return processedAddresses.getOrDefault(address, emptySet());
    }

    private Promise<Metadata> process(AddressTemplate template, Set<String> addresses) {
        String timer = logger.timeInfo("Metadata processing for " + template.template + " → " + addresses);
        List<Task<ProcessingContext>> tasks = new ArrayList<>();
        tasks.add(new RrdTask(settings, dispatcher));
        tasks.add(new UpdateTask(this));
        return Flow.sequential(new ProcessingContext(template, addresses), tasks)
                .then(context -> Promise.resolve(get(template)))
                .finally_(() -> logger.timeEnd(timer));
    }

    private String resolveTemplate(AddressTemplate template) {
        return resolver.resolve(template).template;
    }

    private Metadata internalGet(String address) {
        return cache.get(address);
    }
}
