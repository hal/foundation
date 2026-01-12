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
package org.jboss.hal.op.task;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.task.Task;
import org.patternfly.style.Size;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.p;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.wizard.Wizard.wizard;
import static org.patternfly.component.wizard.WizardHeader.wizardHeader;
import static org.patternfly.component.wizard.WizardHeaderDescription.wizardHeaderDescription;
import static org.patternfly.component.wizard.WizardHeaderTitle.wizardHeaderTitle;
import static org.patternfly.component.wizard.WizardStep.wizardStep;
import static org.patternfly.component.wizard.WizardStepType.review;
import static org.patternfly.icon.IconSets.fas.chartLine;

@Dependent
public class StatisticsTask implements Task {

    public static final String TASK_ID = StatisticsTask.class.getName();
    private final Dispatcher dispatcher;
    private final ModelTree modelTree;

    @Inject
    public StatisticsTask(Dispatcher dispatcher, ModelTree modelTree) {
        this.dispatcher = dispatcher;
        this.modelTree = modelTree;
    }

    @Override
    public String id() {
        return TASK_ID;
    }

    @Override
    public String title() {
        return "Statistics";
    }

    @Override
    public Element icon() {
        return chartLine().element();
    }

    @Override
    public HTMLElement summary() {
        return content(p)
                .add("Enable / disable statistics for all or a selection of subsystems.")
                .element();
    }

    @Override
    public HTMLElement moreInfo() {
        return content()
                .add(p().text("This task lets you manage statistics in various subsystems."))
                .element();
    }

    @Override
    public void run() {
        new FindStatisticsEnabledAttributes(dispatcher, modelTree).find(AddressTemplate.root());
        modal().size(Size.md)
                .add(wizard().height(400)
                        .addHeader(wizardHeader()
                                .addTitle(wizardHeaderTitle("Manage statistics"))
                                .addDescription(wizardHeaderDescription(
                                        "This wizard lets you manage statistics in various subsystems.")))
                        .addItem(wizardStep("wizard-modal-step-0", "Step 1")
                                .add(p().text("Step 1 content")))
                        .addItem(wizardStep("wizard-modal-step-1", "Step 2")
                                .add(p().text("Step 2 content")))
                        .addItem(wizardStep("wizard-modal-step-4", "Review", review)
                                .add(p().text("Review content"))))
                .appendToBody()
                .open();
    }
}
