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

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.layout.flex.Display.inlineFlex;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.md;

class DonutDemoCard implements DashboardCard {

    private static final SafeHtml HEAP_CODE = SafeHtmlUtils.fromSafeConstant(
            "<div class=\"pf-v5-c-chart\" style=\"pointer-events: none; touch-action: none; position: relative; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" role=\"img\" aria-labelledby=\"victory-container-29-title\" aria-describedby=\"victory-container-29-desc\" viewBox=\"0 0 175 175\" style=\"pointer-events: all; width: 100%; height: 100%;\"><title id=\"victory-container-29-title\">Donut utilization chart example</title><desc id=\"victory-container-29-desc\">Storage capacity</desc><g><path d=\"M0.779,-67.495A67.5,67.5,0,0,1,39.042,-55.063L33.752,-47.781A58.5,58.5,0,0,0,0.779,-58.495Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-theme--blue--ColorScale--100, #06c); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path><path d=\"M40.303,-54.147A67.5,67.5,0,1,1,-0.779,-67.495L-0.779,-58.495A58.5,58.5,0,1,0,35.013,-46.865Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-donut--threshold--first--Color, #f0f0f0); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path></g><text id=\"chart6-ChartLabel-title\" direction=\"inherit\" dx=\"0\" x=\"87.5\" y=\"86.175\"><tspan x=\"87.5\" dx=\"0\" dy=\"0\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--title--Fill, #151515); font-size: 24px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">10%</tspan><tspan x=\"87.5\" dx=\"0\" dy=\"13.5\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--subtitle--Fill, #b8bbbe); font-size: 14px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">of 512 MB</tspan></text></svg><div style=\"z-index: 99; position: absolute; top: 0px; left: 0px; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" viewBox=\"0 0 175 175\" style=\"overflow: visible; width: 100%; height: 100%;\"></svg></div></div>");

    private static final SafeHtml NON_HEAP_CODE = SafeHtmlUtils.fromSafeConstant(
            "<div class=\"pf-v5-c-chart\" style=\"pointer-events: none; touch-action: none; position: relative; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" role=\"img\" aria-labelledby=\"victory-container-33-title\" aria-describedby=\"victory-container-33-desc\" viewBox=\"0 0 175 175\" style=\"pointer-events: all; width: 100%; height: 100%;\"><title id=\"victory-container-33-title\">Donut utilization chart example</title><desc id=\"victory-container-33-desc\">Storage capacity</desc><g><path d=\"M0.779,-67.495A67.5,67.5,0,0,1,45.636,-49.736L39.474,-43.174A58.5,58.5,0,0,0,0.779,-58.495Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-theme--blue--ColorScale--100, #06c); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path><path d=\"M46.772,-48.669A67.5,67.5,0,1,1,-0.779,-67.495L-0.779,-58.495A58.5,58.5,0,1,0,40.611,-42.107Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-donut--threshold--first--Color, #f0f0f0); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path></g><text id=\"chart6-ChartLabel-title\" direction=\"inherit\" dx=\"0\" x=\"87.5\" y=\"86.175\"><tspan x=\"87.5\" dx=\"0\" dy=\"0\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--title--Fill, #151515); font-size: 24px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">12%</tspan><tspan x=\"87.5\" dx=\"0\" dy=\"13.5\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--subtitle--Fill, #b8bbbe); font-size: 14px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">of 744 MB</tspan></text></svg><div style=\"z-index: 99; position: absolute; top: 0px; left: 0px; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" viewBox=\"0 0 175 175\" style=\"overflow: visible; width: 100%; height: 100%;\"></svg></div></div>");

    private static final SafeHtml THREADS_CODE = SafeHtmlUtils.fromSafeConstant(
            "<div class=\"pf-v5-c-chart\" style=\"pointer-events: none; touch-action: none; position: relative; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" role=\"img\" aria-labelledby=\"victory-container-37-title\" aria-describedby=\"victory-container-37-desc\" viewBox=\"0 0 175 175\" style=\"pointer-events: all; width: 100%; height: 100%;\"><title id=\"victory-container-37-title\">Donut utilization chart example</title><desc id=\"victory-container-37-desc\">Storage capacity</desc><g><path d=\"M0.779,-67.495A67.5,67.5,0,0,1,48.669,-46.772L42.107,-40.611A58.5,58.5,0,0,0,0.779,-58.495Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-theme--blue--ColorScale--100, #06c); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path><path d=\"M49.736,-45.636A67.5,67.5,0,1,1,-0.779,-67.495L-0.779,-58.495A58.5,58.5,0,1,0,43.174,-39.474Z\" transform=\"translate(87.5, 87.5)\" role=\"presentation\" shape-rendering=\"auto\" style=\"fill: var(--pf-v5-chart-donut--threshold--first--Color, #f0f0f0); padding: 8px; stroke: var(--pf-v5-chart-pie--data--stroke--Color, transparent); stroke-width: 1;\"></path></g><text id=\"chart6-ChartLabel-title\" direction=\"inherit\" dx=\"0\" x=\"87.5\" y=\"86.175\"><tspan x=\"87.5\" dx=\"0\" dy=\"0\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--title--Fill, #151515); font-size: 24px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">26%</tspan><tspan x=\"87.5\" dx=\"0\" dy=\"13.5\" text-anchor=\"middle\" style=\"fill: var(--pf-v5-chart-donut--label--subtitle--Fill, #b8bbbe); font-size: 14px; text-anchor: middle; font-family: var(--pf-v5-chart-global--FontFamily, &quot;RedHatText&quot;, helvetica, arial, sans-serif); letter-spacing: var(--pf-v5-chart-global--letter-spacing, normal); stroke: transparent;\">of 50</tspan></text></svg><div style=\"z-index: 99; position: absolute; top: 0px; left: 0px; width: 100%; height: 100%;\"><svg width=\"175\" height=\"175\" viewBox=\"0 0 175 175\" style=\"overflow: visible; width: 100%; height: 100%;\"></svg></div></div>");

    private final Dispatcher dispatcher;
    private final HTMLElement root;

    DonutDemoCard(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.root = card()
                .addHeader(cardHeader()
                        .addTitle(cardTitle().text("Donut Demo"))
                        .addActions(refreshActions()))
                .addBody(cardBody()
                        .add(flex().display(inlineFlex).spaceItems(md)
                                .addItem(flexItem()
                                        .add(div().text("Heap"))
                                        .add(div().html(HEAP_CODE)))
                                .addItem(flexItem()
                                        .add(div().text("Non heap"))
                                        .add(div().html(NON_HEAP_CODE)))
                                .addItem(flexItem()
                                        .add(div().text("Threads"))
                                        .add(div().html(THREADS_CODE)))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void refresh() {
        AddressTemplate mbean = AddressTemplate.of("core-service=platform-mbean");
        AddressTemplate memory = mbean.append("type=memory");
        AddressTemplate threading = mbean.append("type=threading");
        Operation osOp = new Operation.Builder(memory.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation runtimeOp = new Operation.Builder(threading.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
    }
}
