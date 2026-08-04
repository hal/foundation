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
 * Attribute-to-item pipeline with a two-tier architecture.
 *
 * <h2>Overview</h2>
 * <p>
 * The pipeline transforms resource metadata ({@link org.jboss.hal.meta.description.AttributeDescription}s) into
 * {@link org.jboss.hal.ui.resource.view.ViewItem}s and {@link org.jboss.hal.ui.resource.form.FormItem}s through two tiers:
 *
 * <h3>Handlers ({@link org.jboss.hal.ui.resource.pipeline.AttributeHandler})</h3>
 * <p>
 * Handlers bridge the <em>description world</em> (metadata only) and the <em>value world</em> (resolved snapshots with RBAC
 * state). Each handler both claims attributes from the pool and produces items for its claimed matches — no separate matcher
 * and provider needed.
 * <p>
 * Registered handlers (in priority order):
 * <ol>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.CredentialReferenceHandler} — OBJECT with {store, alias, clear-text}</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.TimeUnitHandler} — OBJECT with {time, unit}</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.FileHandler} — OBJECT with {path, relative-to}</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.PathRelativeToHandler} — sibling path + relative-to STRING pairs</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.MapHandler} — OBJECT with simple scalar VALUE_TYPE</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.FlatteningHandler} — simpleRecord OBJECTs (all simple sub-attributes)</li>
 * </ol>
 *
 * <h3>Providers ({@link org.jboss.hal.ui.resource.pipeline.ItemProvider})</h3>
 * <p>
 * Providers operate in the value world only — they receive already-resolved
 * {@link org.jboss.hal.ui.resource.ResolvedAttribute}s and produce leaf-level items. Providers are the common exit path for
 * both unclaimed top-level attributes and child attributes delegated by handlers.
 * <p>
 * Registered providers (in order):
 * <ol>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.RelativeToProvider} — standalone *relative-to attributes (form only)</li>
 *     <li>{@link org.jboss.hal.ui.resource.pipeline.DefaultProvider} — type-based dispatch catch-all</li>
 * </ol>
 *
 * <h2>Data Flow</h2>
 * <pre>
 * Description world                    Value world
 * (metadata only)                      (description + value + RBAC)
 *
 * AttributeDescription ─┐
 *                       ├─ match() ──→ AttributeMatch ──→ handler.viewItems(ctx, match)
 * AttributeDescription ─┘                                        │
 *                                                          resolve(ctx, desc)
 *                                                                │
 *                                                                ▼
 *                                                        ResolvedAttribute
 *                                                                │
 *                                                     pipeline.viewItem(ctx, ra)
 *                                                                │
 *                                                                ▼
 *                                                        ItemProvider chain
 * </pre>
 *
 * <h2>Entry Points</h2>
 * <ul>
 *     <li><b>Full pipeline</b> — {@code Pipeline.instance().viewItems(context)} /
 *         {@code Pipeline.instance().formItems(context)}</li>
 *     <li><b>Child pipeline</b> — {@code Pipeline.instance().viewItem(context, resolvedAttribute)} /
 *         {@code Pipeline.instance().formItem(context, resolvedAttribute)}</li>
 * </ul>
 *
 * @see org.jboss.hal.ui.resource.pipeline.Pipeline
 * @see org.jboss.hal.ui.resource.pipeline.AttributeHandler
 * @see org.jboss.hal.ui.resource.pipeline.ItemProvider
 */
package org.jboss.hal.ui.resource.pipeline;
