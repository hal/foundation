package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;

import elemental2.dom.HTMLElement;

@FunctionalInterface
interface DefinedValue {

    HTMLElement element(PipelineContext context, ResolvedAttribute attribute);
}
