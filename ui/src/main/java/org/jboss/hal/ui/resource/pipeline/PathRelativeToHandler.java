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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.PathRelativeToFormItem;
import org.jboss.hal.ui.resource.view.PathRelativeToViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Handler for sibling path + relative-to attribute pairs. Claims pairs of top-level STRING attributes that are semantically
 * coupled and should be rendered as one unit. Resolves both attributes and uses name-based lookup (not positional).
 */
class PathRelativeToHandler implements AttributeHandler {

    private static final String RELATIVE_TO_SUFFIX = RELATIVE_TO;
    private static final String DIRECTORY = "directory";

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        List<AttributeMatch> groups = new ArrayList<>();
        Set<String> claimed = new HashSet<>();

        for (AttributeDescription ad : pool) {
            if (!ad.name().endsWith(RELATIVE_TO_SUFFIX)) {
                continue;
            }
            if (claimed.contains(ad.name())) {
                continue;
            }

            String prefix = ad.name().substring(0, ad.name().length() - RELATIVE_TO_SUFFIX.length());
            String siblingPathName = prefix.isEmpty() ? "path" : prefix + "path";

            AttributeDescription path = findInPool(pool, siblingPathName);
            if (path == null && prefix.isEmpty()) {
                path = findInPool(pool, DIRECTORY);
            }

            if (path != null && !claimed.contains(path.name())) {
                groups.add(AttributeMatch.multiple(path.name(), Arrays.asList(path, ad)));
                claimed.add(path.name());
                claimed.add(ad.name());
            }
        }

        List<AttributeDescription> remaining = new ArrayList<>();
        for (AttributeDescription ad : pool) {
            if (!claimed.contains(ad.name())) {
                remaining.add(ad);
            }
        }

        return new MatchResult(groups, remaining);
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute path = ResolvedAttribute.resolve(context, match.descriptions().get(0));
        ResolvedAttribute relativeTo = ResolvedAttribute.resolve(context, match.descriptions().get(1));
        return singletonList(new PathRelativeToViewItem(context, match.name(), path, relativeTo));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute path = ResolvedAttribute.resolve(context, match.descriptions().get(0));
        ResolvedAttribute relativeTo = ResolvedAttribute.resolve(context, match.descriptions().get(1));
        return singletonList(new PathRelativeToFormItem(context, match.name(), path, relativeTo));
    }

    private AttributeDescription findInPool(List<AttributeDescription> pool, String name) {
        for (AttributeDescription ad : pool) {
            if (ad.name().equals(name)) {
                return ad;
            }
        }
        return null;
    }
}
