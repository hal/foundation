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
package org.jboss.hal.ui.modelbrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.icon.IconSets;
import org.patternfly.icon.PredefinedIcon;

/**
 * Holds data necessary to create tree view items from the management model. Acts as a layer between the
 * {@link org.jboss.hal.dmr.ModelNode}s and the {@link org.patternfly.component.tree.TreeViewItem}s.
 */
class ModelBrowserNode {

    static final String ROOT_ID = "root";

    enum Type {
        SINGLETON_FOLDER(IconSets.fas::listUl, null),

        FOLDER(IconSets.fas::folder, IconSets.fas::folderOpen),

        SINGLETON_RESOURCE(IconSets.fas::fileLines, null),

        RESOURCE(IconSets.fas::fileLines, null);

        final Supplier<PredefinedIcon> icon;
        final Supplier<PredefinedIcon> expandedIcon;

        Type(Supplier<PredefinedIcon> icon, Supplier<PredefinedIcon> expandedIcon) {
            this.icon = icon;
            this.expandedIcon = expandedIcon;
        }
    }

    final String identifier;
    final AddressTemplate template;
    final String name;
    final Type type;
    final List<ModelBrowserNode> children;
    boolean exists;

    ModelBrowserNode(AddressTemplate template, String name, Type type) {
        this.identifier = template.identifier();
        this.template = template;
        this.name = name != null ? SafeHtmlUtils.htmlEscape(name) : null;
        this.type = type;
        this.children = new ArrayList<>();
        this.exists = true;
    }

    ModelBrowserNode copy(Consumer<ModelBrowserNode> modifyCopy) {
        ModelBrowserNode copy = new ModelBrowserNode(template, name, type);
        modifyCopy.accept(copy);
        return copy;
    }
}
