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

/**
 * Attribute-to-item pipeline that transforms resource metadata into view and form items.
 *
 * <h2>Pipeline Stages</h2>
 * <ol>
 *     <li><b>Group</b> — {@link org.jboss.hal.ui.resource.pipeline.AttributeMatcher}s scan the attribute pool in priority
 *         order, claiming groups of related attributes into {@link org.jboss.hal.ui.resource.pipeline.AttributeGroup}s.</li>
 *     <li><b>Itemize</b> — {@link org.jboss.hal.ui.resource.pipeline.ItemProvider}s resolve each group against the
 *         {@link org.jboss.hal.ui.resource.pipeline.PipelineContext} into
 *         {@link org.jboss.hal.ui.resource.pipeline.ResolvedAttribute}s and create
 *         {@link org.jboss.hal.ui.resource.pipeline.ViewItem}s or
 *         {@link org.jboss.hal.ui.resource.pipeline.FormItem}s. Providers are tried in registration order; first match
 *         wins.</li>
 * </ol>
 *
 * <h2>Provider Chain</h2>
 * <ol>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.CredentialReferenceProvider} — composite: credential-reference</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.TimeUnitProvider} — composite: time-unit</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.FileProvider} — composite: file</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.PathRelativeToProvider} — sibling group: path + relative-to</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.RelativeToProvider} — standalone: relative-to (FIP only)</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.FlatteningProvider} — unclaimed simpleRecord OBJECTs → n sub-attribute
 *         items</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.DefaultItemProvider} — everything else: type-based dispatch</li>
 * </ol>
 *
 * <h2>Type Relationships</h2>
 * <pre>
 * AttributeDescription  — raw metadata from the management model (no values, no RBAC)
 *         ↓ stage 1 matchers group them
 * AttributeGroup        — 1..n descriptions that belong together (still no values)
 *         ↓ stage 2 providers resolve against PipelineContext
 * ResolvedAttribute     — 1 description + its current value + readable/writable (snapshot)
 *         ↓ passed to item constructors
 * OldFormItem      — holds 1..n ResolvedAttributes, renders UI, produces operations
 * OldViewItem      — holds 1..n ResolvedAttributes, renders read-only display
 * </pre>
 * <p>
 * {@link org.jboss.hal.ui.resource.pipeline.AttributeGroup} is the stage 1 → stage 2 contract (descriptions only).
 * {@link org.jboss.hal.ui.resource.pipeline.ResolvedAttribute} is the stage 2 → item contract (descriptions + values + RBAC).
 * The split happens at the provider: it receives a group, resolves each description against the context, and passes resolved
 * attributes to the item constructor.
 *
 * <h2>Use Cases</h2>
 *
 * <h3>Single attribute (e.g. a STRING {@code enabled})</h3>
 * <pre>
 * Stage 1: AttributeGroup([enabled])              — 1 description
 * Stage 2: resolve → ResolvedAttribute(enabled)   — 1 resolved
 * Item:    holds 1 ResolvedAttribute
 *          operations() → 1 write-attribute(name="enabled", value=X)
 * </pre>
 *
 * <h3>Composite OBJECT kept as unit (e.g. {@code credential-reference})</h3>
 * <pre>
 * Stage 1: AttributeGroup([credential-reference])              — 1 description (the OBJECT)
 * Stage 2: resolve → ResolvedAttribute(credential-reference)   — 1 resolved
 *          Sub-attributes (store, alias, clear-text) are INSIDE the value ModelNode
 *          and the description's valueTypeAttributeDescriptions()
 * Item:    holds 1 ResolvedAttribute
 *          operations() → 1 write-attribute(name="credential-reference", value={store:X, alias:Y, ...})
 * </pre>
 *
 * <h3>Flattened simple-record OBJECT (e.g. an unclaimed {@code {foo, bar}} OBJECT)</h3>
 * <pre>
 * Stage 1: AttributeGroup([my-record])                          — 1 description (the OBJECT)
 * Stage 2: FlatteningProvider detects simpleRecord, flattens:
 *          → ResolvedAttribute(foo) with fqn="my-record.foo"    — nested description
 *          → ResolvedAttribute(bar) with fqn="my-record.bar"
 * Items:   2 items, each holds 1 ResolvedAttribute
 *          operations() → 1 write-attribute(name="my-record.foo", value=X) each (FQN path)
 * </pre>
 *
 * <h3>Sibling group (e.g. {@code path} + {@code relative-to})</h3>
 * <pre>
 * Stage 1: AttributeGroup([path, relative-to])                  — 2 descriptions
 * Stage 2: resolve each:
 *          → ResolvedAttribute(path)
 *          → ResolvedAttribute(relative-to)
 * Item:    1 item, holds 2 ResolvedAttributes
 *          operations() → 2 write-attribute ops (one per attribute)
 * </pre>
 *
 * <h3>Summary</h3>
 * <table>
 *     <caption>Data flow per use case</caption>
 *     <tr><th>Use case</th><th>AttributeGroup</th><th>ResolvedAttributes</th><th>Items</th><th>Operations</th></tr>
 *     <tr><td>Single attribute</td><td>1 desc</td><td>1 resolved</td><td>1 item, 1 resolved</td><td>1 op</td></tr>
 *     <tr><td>Composite (credential-ref)</td><td>1 desc (OBJECT)</td><td>1 resolved</td><td>1 item, 1 resolved</td><td>1 op (whole OBJECT)</td></tr>
 *     <tr><td>Flattened simple-record</td><td>1 desc (OBJECT)</td><td>n resolved</td><td>n items, each 1 resolved</td><td>n ops (FQN paths)</td></tr>
 *     <tr><td>Sibling group</td><td>n descs</td><td>n resolved</td><td>1 item, n resolved</td><td>n ops (separate attrs)</td></tr>
 * </table>
 *
 * <p>
 * Entry point: {@link org.jboss.hal.ui.resource.pipeline.Pipeline#create()}.
 *
 * @see org.jboss.hal.ui.resource.pipeline.Pipeline
 */
package org.jboss.hal.ui.resource.pipeline;
