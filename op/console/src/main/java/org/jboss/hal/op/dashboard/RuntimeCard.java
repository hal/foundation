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
package org.jboss.hal.op.dashboard;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.patternfly.chart.bullet.Bullet;
import org.patternfly.chart.utilization.DonutThreshold;
import org.patternfly.chart.utilization.DonutUtilization;
import org.patternfly.component.card.Card;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static java.lang.Math.round;
import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.flow.Flow.parallel;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_INFO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.op.dashboard.Dashboard.dashboardEmptyState;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.descriptionOnly;
import static org.jboss.hal.ui.Format.humanReadableBytes;
import static org.jboss.hal.ui.Format.percent;
import static org.patternfly.chart.Data.data;
import static org.patternfly.chart.LegendOrientation.vertical;
import static org.patternfly.chart.Padding.padding;
import static org.patternfly.chart.bullet.Bullet.bullet;
import static org.patternfly.chart.bullet.BulletData.bulletData;
import static org.patternfly.chart.utilization.DonutThreshold.donutThreshold;
import static org.patternfly.chart.utilization.DonutUtilization.donutUtilization;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.flex.AlignItems.stretch;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.layout.flex.JustifyContent.center;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Size.xl;

class RuntimeCard implements Attachable, AutoRefresh, DashboardCard {

    private static final String METADATA_KEY = "metadata";
    private static final String PAYLOAD_KEY = "payload";

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;
    private final Flex gallery;
    private final DonutThreshold memoryThreshold;
    private final DonutUtilization memoryUtilization;
    private final Bullet threadsBullet;
    private double intervalHandle;

    RuntimeCard(StatementContext statementContext, Dispatcher dispatcher, MetadataRepository metadataRepository) {
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.gallery = flex().gap(md).alignItems(stretch);

        this.memoryThreshold = donutThreshold()
                .style("width", "300px")
                .ariaTitle("Heap memory utilization")
                .ariaDesc("Utilization chart showing the heap memory usage in megabytes (MB).")
                .data(data("Warning at 75%", 75), data("Danger at 90%", 90))
                .labels(data -> data.x != null ? data.x : null)
                .padding(padding(0, 160, 0, 0))
                .height(150)
                .width(300);
        this.memoryUtilization = donutUtilization()
                .title("")
                .subTitle("")
                .labels(data -> data.x != null ? data.x + ": " + percent(data.y) : null)
                .legendData("Heap memory: 0", "Warning at 75%", "Danger at 90%")
                .legendOrientation(vertical)
                .thresholds(75, 90);
        this.threadsBullet = bullet()
                .ariaTitle("Threads")
                .ariaDesc("Bullet chart showing the number of daemon and non-daemon threads and the peak number of threads.")
                .comparativeErrorMeasureData(bulletData("Thread Peak", 0))
                .primarySegmentedMeasureData(bulletData("Daemon threads", 0), bulletData("Threads", 0))
                .qualitativeRangeData(bulletData("Low", 0), bulletData("Medium", 0), bulletData("High", 0))
                .labels(data -> data.name + ": " + data.y)
                .padding(padding(-150, 20, -75, 5))
                .height(75)
                .width(300)

        ;
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        // nop
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        stopAutoRefresh();
    }

    @Override
    public HTMLElement element() {
        return gallery.element();
    }

    // ------------------------------------------------------ refresh

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void refresh() {
        removeChildrenFrom(gallery);

        AddressTemplate productInfoTmpl = AddressTemplate.of("{domain.controller}");
        Task<FlowContext> productInfoTask = context -> metadataRepository.lookup(productInfoTmpl)
                .then(metadata -> context.resolve(METADATA_KEY, metadata));
        Operation productInfoOp = new Operation.Builder(productInfoTmpl.resolve(statementContext), PRODUCT_INFO).build();

        AddressTemplate memoryTmpl = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=memory");
        Operation memoryOp = new Operation.Builder(memoryTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();

        AddressTemplate threadsTmpl = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=threading");
        Operation threadsOp = new Operation.Builder(threadsTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();

        Task<FlowContext> payloadTask = context -> dispatcher.execute(new Composite(productInfoOp, memoryOp, threadsOp))
                .then(result -> context.resolve(PAYLOAD_KEY, result));

        parallel(new FlowContext(), asList(productInfoTask, payloadTask)).subscribe(context -> {
            if (context.isSuccessful()) {
                Metadata metadata = context.get(METADATA_KEY);
                ModelNode returnValue = metadata.resourceDescription()
                        .operations()
                        .get(PRODUCT_INFO)
                        .returnValue();
                AttributeDescriptions attributeDescriptions = new AttributeDescriptions(
                        returnValue.get(VALUE_TYPE).get("summary").get(VALUE_TYPE));
                CompositeResult payload = context.get(PAYLOAD_KEY);
                ModelNode summary = payload.step(0).get(RESULT).asList().get(0).get("summary");
                ModelNode memory = payload.step(1).get(RESULT).get("heap-memory-usage");
                ModelNode threads = payload.step(2).get(RESULT);
                gallery.addItem(flexItem().flex(_1).add(hostInfoCard(summary, attributeDescriptions)));
                gallery.addItem(flexItem().flex(_1).add(jvmInfoCard(summary, attributeDescriptions)));
                gallery.addItem(flexItem().flex(_1).add(memoryCard()));
                updateMemoryThreads(memory, threads);
                intervalHandle = startAutoRefresh();
            } else {
                error(context.failureReason());
            }
        });
    }

    // ------------------------------------------------------ cards

    private Card hostInfoCard(ModelNode modelNode, AttributeDescriptions ad) {
        AttributeDescriptions cpuAd = ad.get("host-cpu").valueTypeAttributeDescriptions();
        return card().fullHeight()
                .addTitle(cardTitle()
                        .run(ct -> ct.textDelegate().appendChild(title(2, xl, "Host").element())))
                .addBody(cardBody().add(descriptionList()
                        .addItem(descriptionListGroup("host-name")
                                .addTerm(descriptionListTerm("Name")
                                        .help(attributeDescriptionPopover("Name", ad.get("host-operating-system"),
                                                descriptionOnly)
                                                .placement(auto)))
                                .addDescription(descriptionListDescription(modelNode.get("host-operating-system").asString())))
                        .addItem(descriptionListGroup("host-arch")
                                .addTerm(descriptionListTerm("Architecture")
                                        .help(attributeDescriptionPopover("Architecture", cpuAd.get("host-cpu-arch"),
                                                descriptionOnly)
                                                .placement(auto)))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(modelNode, "host-cpu.host-cpu-arch").asString())))
                        .addItem(descriptionListGroup("host-cores")
                                .addTerm(descriptionListTerm("Cores")
                                        .help(attributeDescriptionPopover("Cores", cpuAd.get("host-core-count"),
                                                descriptionOnly)
                                                .placement(auto)))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(modelNode, "host-cpu.host-core-count").asString())))));
    }

    private Card jvmInfoCard(ModelNode modelNode, AttributeDescriptions ad) {
        AttributeDescriptions jvmAd = ad.get("jvm").valueTypeAttributeDescriptions();
        return card().fullHeight()
                .addTitle(cardTitle()
                        .run(ct -> ct.textDelegate().appendChild(title(2, xl, "JVM").element())))
                .addBody(cardBody().add(descriptionList()
                        .addItem(descriptionListGroup("jvm-name")
                                .addTerm(descriptionListTerm("Name"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(modelNode, "jvm.name").asString())))
                        .addItem(descriptionListGroup("jvm-version")
                                .addTerm(descriptionListTerm("Version")
                                        .help(attributeDescriptionPopover("Version", jvmAd.get("jvm-version"),
                                                descriptionOnly)
                                                .placement(auto)))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(modelNode, "jvm.jvm-version").asString())))
                        .addItem(descriptionListGroup("jvm-vendor")
                                .addTerm(descriptionListTerm("Vendor")
                                        .help(attributeDescriptionPopover("Vendor", jvmAd.get("jvm-vendor"),
                                                descriptionOnly)
                                                .placement(auto)))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(modelNode, "jvm.jvm-vendor").asString())))));
    }

    private Card memoryCard() {
        return card().fullHeight()
                .addTitle(cardTitle()
                        .run(ct -> ct.textDelegate().appendChild(title(2, xl, "Memory & Threads").element())))
                .addBody(cardBody()
                        .add(flex().rowGap(sm).justifyContent(center)
                                .addItem(flexItem()
                                        .add(memoryThreshold.addUtilization(memoryUtilization)))
                                .add(flexItem()
                                        .add(threadsBullet))));
    }

    // ------------------------------------------------------ auto refresh

    @Override
    public double interval() {
        return 1_500;
    }

    @Override
    public double handle() {
        return intervalHandle;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void autoRefresh() {
        if (memoryUtilization != null && threadsBullet != null) {
            AddressTemplate memoryTmpl = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=memory");
            Operation memoryOp = new Operation.Builder(memoryTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            AddressTemplate threadsTmpl = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=threading");
            Operation threadsOp = new Operation.Builder(threadsTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(new Composite(memoryOp, threadsOp))
                    .then(result -> {
                        ModelNode memory = result.step(0).get(RESULT).get("heap-memory-usage");
                        ModelNode threads = result.step(1).get(RESULT);
                        updateMemoryThreads(memory, threads);
                        return null;
                    })
                    .catch_(error -> {
                        error(String.valueOf(error));
                        return null;
                    });
        }
    }

    private void updateMemoryThreads(ModelNode heapMemoryNode, ModelNode threadsNode) {
        long used = heapMemoryNode.get("used").asLong();
        long max = heapMemoryNode.get("max").asLong();
        double usedPercent = (double) used / max * 100.0;
        String usedFormat = humanReadableBytes(used);
        String maxFormat = humanReadableBytes(max);
        memoryUtilization
                .title(usedFormat)
                .subTitle("of " + maxFormat)
                .data(data("Memory usage", usedPercent))
                .legendData("Memory: " + usedFormat, "Warning at 75%", "Danger at 90%");

        double daemonThreadCount = threadsNode.get("daemon-thread-count").asInt();
        double threadCount = threadsNode.get("thread-count").asInt();
        double peakThreadCount = threadsNode.get("peak-thread-count").asInt();
        double peakPlus20Percent = round(peakThreadCount + (peakThreadCount * 0.2));
        double lowSegment = round(peakPlus20Percent * 0.5);
        double mediumSegment = round(peakPlus20Percent * 0.75);
        threadsBullet
                .comparativeErrorMeasureData(
                        bulletData("Thread peak count", peakThreadCount))
                .primarySegmentedMeasureData(
                        bulletData("Daemon threads", daemonThreadCount),
                        bulletData("Threads", threadCount))
                .qualitativeRangeData(
                        bulletData("Low", lowSegment),
                        bulletData("Medium", mediumSegment),
                        bulletData("High", peakPlus20Percent))
                .maxDomain(peakPlus20Percent);
    }

    // ------------------------------------------------------ internals

    private void error(String error) {
        removeChildrenFrom(gallery);
        gallery.addItem(flexItem().flex(_1).add(dashboardEmptyState()
                .status(danger)
                .text("Runtime error")
                .addBody(emptyStateBody().add(errorCode(error)))));
    }
}
