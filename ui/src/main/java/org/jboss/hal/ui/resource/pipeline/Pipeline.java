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
package org.jboss.hal.ui.resource.pipeline;

import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.pipeline.AttributeHandler.MatchResult;
import org.jboss.hal.ui.resource.view.ViewItem;

/**
 * Transforms resource metadata into view or form items through a two-tier architecture:
 * <ol>
 *     <li><b>Handlers</b> — registered {@link AttributeHandler}s scan the attribute pool in priority order, claiming
 *         {@link AttributeMatch}es of related attributes. Each handler both claims and produces items for its matches,
 *         performing resolution internally. Handlers bridge the description world (metadata) and the value world
 *         (resolved snapshots with RBAC state).</li>
 *     <li><b>Providers</b> — registered {@link ItemProvider}s handle unclaimed attributes and child attributes delegated by
 *         handlers. Providers operate in the value world only — they receive already-resolved attributes. Providers are tried
 *         in registration order; first match wins.</li>
 * </ol>
 * <p>
 * Two entry points:
 * <ul>
 *     <li><b>Full pipeline</b> — {@link #viewItems(PipelineContext)} and {@link #formItems(PipelineContext)} process all
 *         attributes in the resource metadata.</li>
 *     <li><b>Child pipeline</b> — {@link #viewItem(PipelineContext, ResolvedAttribute)} and
 *         {@link #formItem(PipelineContext, ResolvedAttribute)} produce items for single resolved attributes, used by handlers
 *         to delegate child attributes to the provider chain.</li>
 * </ul>
 *
 * @see AttributeHandler
 * @see ItemProvider
 */
public final class Pipeline {

    private static final Pipeline instance;

    static {
        // Order matters: handlers run in sequence and each one claims matching attributes from the remaining pool.
        // Earlier handlers take priority — if CredentialReferenceHandler claims an attribute, FlatteningHandler never sees it.
        // FlatteningHandler MUST be last because it flattens any remaining composite attributes that weren't claimed
        // by a specialized handler. Inserting a new handler after FlatteningHandler means it would never see any
        // composite attributes (they'd already be flattened into scalar items).
        List<AttributeHandler> handlers = List.of(
                new CredentialReferenceHandler(),
                new TimeUnitHandler(),
                new FileHandler(),
                new PathRelativeToHandler(),
                new MapHandler(),
                new FlatteningHandler()
        );
        // First matching provider wins. RelativeToProvider handles the special case of path-relative-to siblings;
        // DefaultProvider is the catch-all fallback for everything else.
        List<ItemProvider> providers = List.of(
                new RelativeToProvider(),
                new DefaultProvider()
        );
        instance = new Pipeline(handlers, providers);
    }

    /** Returns the shared pipeline instance with all handlers and providers registered in the correct priority order. */
    public static Pipeline instance() {
        return instance;
    }

    private final List<AttributeHandler> handlers;
    private final List<ItemProvider> providers;

    Pipeline(List<AttributeHandler> handlers, List<ItemProvider> providers) {
        this.handlers = handlers;
        this.providers = providers;
    }

    // ------------------------------------------------------ full pipeline (top-level entry points)

    /** Runs the full pipeline and produces view items for all attributes in the resource metadata. */
    public List<ViewItem> viewItems(PipelineContext context) {
        return viewItems(context, context.resourceDescription().attributes());
    }

    /** Runs the full pipeline and produces view items for the given attributes. */
    public List<ViewItem> viewItems(PipelineContext context, Iterable<AttributeDescription> attributes) {
        List<AttributeDescription> pool = toPool(attributes);
        Map<String, Integer> originalOrder = originalOrder(pool);

        List<HandledMatch> handledMatches = new ArrayList<>();
        List<AttributeDescription> remaining = pool;

        for (AttributeHandler handler : handlers) {
            MatchResult result = handler.match(remaining);
            for (AttributeMatch match : result.matches()) {
                handledMatches.add(new HandledMatch(handler, match));
            }
            remaining = result.remaining();
        }

        List<ItemOrMatch> sorted = sortByOriginalOrder(handledMatches, remaining, originalOrder);
        List<ViewItem> items = new ArrayList<>();
        for (ItemOrMatch entry : sorted) {
            if (entry.handledMatch != null) {
                List<ViewItem> result = entry.handledMatch.handler.viewItems(context, entry.handledMatch.match);
                if (result != null) {
                    items.addAll(result);
                }
            } else {
                ResolvedAttribute ra = ResolvedAttribute.resolve(context, entry.unclaimed);
                ViewItem item = provideViewItem(context, ra);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /** Runs the full pipeline and produces form items for all attributes in the resource metadata. */
    public List<FormItem> formItems(PipelineContext context) {
        return formItems(context, context.resourceDescription().attributes());
    }

    /** Runs the full pipeline and produces form items for the given attributes (also used for operation parameters). */
    public List<FormItem> formItems(PipelineContext context, Iterable<AttributeDescription> attributes) {
        List<AttributeDescription> pool = toPool(attributes);
        Map<String, Integer> originalOrder = originalOrder(pool);

        List<HandledMatch> handledMatches = new ArrayList<>();
        List<AttributeDescription> remaining = pool;

        for (AttributeHandler handler : handlers) {
            MatchResult result = handler.match(remaining);
            for (AttributeMatch match : result.matches()) {
                handledMatches.add(new HandledMatch(handler, match));
            }
            remaining = result.remaining();
        }

        List<ItemOrMatch> sorted = sortByOriginalOrder(handledMatches, remaining, originalOrder);
        List<FormItem> items = new ArrayList<>();
        for (ItemOrMatch entry : sorted) {
            if (entry.handledMatch != null) {
                List<FormItem> result = entry.handledMatch.handler.formItems(context, entry.handledMatch.match);
                if (result != null) {
                    items.addAll(result);
                }
            } else {
                ResolvedAttribute ra = ResolvedAttribute.resolve(context, entry.unclaimed);
                FormItem item = provideFormItem(context, ra);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    // ------------------------------------------------------ child pipeline (recursive entry points)

    /**
     * Produces a single view item for an already-resolved attribute by running it through the provider chain. Used by handlers
     * to delegate child or sibling attributes.
     */
    public ViewItem viewItem(PipelineContext context, ResolvedAttribute ra) {
        return provideViewItem(context, ra);
    }

    /**
     * Produces a single form item for an already-resolved attribute by running it through the provider chain. Used by handlers
     * to delegate child or sibling attributes.
     */
    public FormItem formItem(PipelineContext context, ResolvedAttribute ra) {
        return provideFormItem(context, ra);
    }

    // ------------------------------------------------------ provider chain

    private ViewItem provideViewItem(PipelineContext context, ResolvedAttribute ra) {
        for (ItemProvider provider : providers) {
            if (provider.handles(ra)) {
                ViewItem item = provider.viewItem(context, ra);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    private FormItem provideFormItem(PipelineContext context, ResolvedAttribute ra) {
        for (ItemProvider provider : providers) {
            if (provider.handles(ra)) {
                FormItem item = provider.formItem(context, ra);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------ internal

    private List<AttributeDescription> toPool(Iterable<AttributeDescription> attributes) {
        List<AttributeDescription> pool = new ArrayList<>();
        for (AttributeDescription ad : attributes) {
            pool.add(ad);
        }
        return pool;
    }

    private Map<String, Integer> originalOrder(List<AttributeDescription> pool) {
        Map<String, Integer> order = new HashMap<>();
        int index = 0;
        for (AttributeDescription ad : pool) {
            order.put(ad.name(), index++);
        }
        return order;
    }

    private List<ItemOrMatch> sortByOriginalOrder(List<HandledMatch> handledMatches,
            List<AttributeDescription> unclaimed, Map<String, Integer> originalOrder) {
        List<ItemOrMatch> all = new ArrayList<>();
        for (HandledMatch hm : handledMatches) {
            all.add(new ItemOrMatch(hm, null));
        }
        for (AttributeDescription ad : unclaimed) {
            all.add(new ItemOrMatch(null, ad));
        }
        all.sort((a, b) -> {
            String nameA = a.primaryName();
            String nameB = b.primaryName();
            int posA = originalOrder.getOrDefault(nameA, Integer.MAX_VALUE);
            int posB = originalOrder.getOrDefault(nameB, Integer.MAX_VALUE);
            return Integer.compare(posA, posB);
        });
        return all;
    }

    private record HandledMatch(AttributeHandler handler, AttributeMatch match) {}

    private record ItemOrMatch(HandledMatch handledMatch, AttributeDescription unclaimed) {
        String primaryName() {
            if (handledMatch != null) {
                return handledMatch.match.primary().name();
            }
            return unclaimed.name();
        }
    }
}
