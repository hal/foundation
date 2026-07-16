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
package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/** View item for file composite attributes ({@code {path, relative-to}}). Shows "path relative to dir" or just "path". */
public class FileViewItem extends AbstractViewItem {

    public FileViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        ResolvedAttribute path = attribute.child(PATH);
        ResolvedAttribute relativeTo = attribute.child(RELATIVE_TO);
        HTMLElement pathEl = Pipeline.instance().viewItem(path, context).valueElement();
        HTMLElement rtEl = Pipeline.instance().viewItem(relativeTo, context).valueElement();
        if (path.value().isDefined() && relativeTo.value().isDefined()) {
            return span()
                    .add(pathEl)
                    .add(span().text(" relative to ").style("color", "var(--pf-t--global--text--color--placeholder)"))
                    .add(rtEl)
                    .element();
        } else if (path.value().isDefined()) {
            return pathEl;
        } else {
            return plainText(attribute);
        }
    }
}
