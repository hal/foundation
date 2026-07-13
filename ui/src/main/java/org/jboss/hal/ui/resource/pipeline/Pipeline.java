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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.pipeline.AttributeMatcher.MatchResult;

/**
 * Transforms resource metadata into view or form items through a two-stage pipeline:
 * <ol>
 *     <li><b>Group</b> — registered {@link AttributeMatcher}s scan the attribute pool in priority order,
 *         claiming groups of related attributes. Unclaimed attributes become single-attribute groups.</li>
 *     <li><b>Itemize</b> — for each {@link AttributeGroup}, registered {@link ItemProvider}s are tried in order. The first
 *         match creates the item(s). Unmatched groups are handled by the {@link DefaultItemProvider} catch-all.</li>
 * </ol>
 * <p>
 * One pipeline, two entry points: {@link #viewItems(PipelineContext)} and {@link #formItems(PipelineContext)}. Stage 1
 * (grouping) is identical for both. Stage 2 calls {@link ItemProvider#viewItems(AttributeGroup, PipelineContext)} or
 * {@link ItemProvider#formItems(AttributeGroup, PipelineContext)} depending on the entry point.
 *
 * @see AttributeMatcher
 * @see ItemProvider
 * @see AttributeGroup
 */
public final class Pipeline {

    /**
     * Creates a pipeline with all matchers and providers registered in the correct priority order.
     * <p>
     * Matcher order (stage 1): composite matchers first (credential-reference, time-unit, file), then sibling matchers
     * (path+relative-to).
     * <p>
     * Provider order (stage 2): specific providers first (credential-reference, time-unit, file, path+relative-to, standalone
     * relative-to), then the default catch-all.
     */
    public static Pipeline create() {
        // Order is important!
        List<AttributeMatcher> matchers = List.of(
                new CredentialReferenceMatcher(),
                new TimeUnitMatcher(),
                new FileMatcher(),
                new PathRelativeToMatcher()
        );
        // Order is important!
        List<ItemProvider> providers = List.of(
                new CredentialReferenceProvider(),
                new TimeUnitProvider(),
                new FileProvider(),
                new PathRelativeToProvider(),
                new RelativeToProvider(),
                new FlatteningProvider(),
                new DefaultItemProvider()
        );
        return new Pipeline(matchers, providers);
    }

    private final List<AttributeMatcher> matchers;
    private final List<ItemProvider> providers;

    Pipeline(List<AttributeMatcher> matchers, List<ItemProvider> providers) {
        this.matchers = matchers;
        this.providers = providers;
    }

    /** Runs the pipeline and produces view items for all attributes in the resource metadata. */
    public List<ViewItem> viewItems(PipelineContext context) {
        List<AttributeGroup> groups = group(context);
        return itemizeView(groups, context);
    }

    /** Runs the pipeline and produces form items for all attributes in the resource metadata. */
    public List<FormItem> formItems(PipelineContext context) {
        List<AttributeGroup> groups = group(context);
        return itemizeForm(groups, context);
    }

    // ------------------------------------------------------ stage 1: group

    private List<AttributeGroup> group(PipelineContext context) {
        List<AttributeDescription> pool = new ArrayList<>();
        Map<String, Integer> originalOrder = new HashMap<>();
        int index = 0;
        for (AttributeDescription ad : context.resourceDescription().attributes()) {
            pool.add(ad);
            originalOrder.put(ad.name(), index++);
        }

        List<AttributeGroup> groups = new ArrayList<>();
        List<AttributeDescription> remaining = pool;
        for (AttributeMatcher matcher : matchers) {
            MatchResult result = matcher.match(remaining);
            groups.addAll(result.groups());
            remaining = result.remaining();
        }

        for (AttributeDescription ad : remaining) {
            groups.add(AttributeGroup.single(ad));
        }

        groups.sort((g1, g2) -> {
            int pos1 = originalOrder.getOrDefault(g1.primary().name(), Integer.MAX_VALUE);
            int pos2 = originalOrder.getOrDefault(g2.primary().name(), Integer.MAX_VALUE);
            return Integer.compare(pos1, pos2);
        });

        return groups;
    }

    // ------------------------------------------------------ stage 2: itemize

    private List<ViewItem> itemizeView(List<AttributeGroup> groups, PipelineContext context) {
        List<ViewItem> items = new ArrayList<>();
        for (AttributeGroup group : groups) {
            for (ItemProvider provider : providers) {
                if (provider.matches(group)) {
                    List<ViewItem> result = provider.viewItems(group, context);
                    if (result != null) {
                        items.addAll(result);
                        break;
                    }
                }
            }
        }
        return items;
    }

    private List<FormItem> itemizeForm(List<AttributeGroup> groups, PipelineContext context) {
        List<FormItem> items = new ArrayList<>();
        for (AttributeGroup group : groups) {
            for (ItemProvider provider : providers) {
                if (provider.matches(group)) {
                    List<FormItem> result = provider.formItems(group, context);
                    if (result != null) {
                        items.addAll(result);
                        break;
                    }
                }
            }
        }
        return items;
    }
}
