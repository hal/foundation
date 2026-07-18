package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;

import elemental2.dom.HTMLElement;

/** Functional interface for rendering a defined attribute value as an HTML element. */
@FunctionalInterface
interface DefinedValue {

    HTMLElement element(PipelineContext context, ResolvedAttribute attribute);
}
