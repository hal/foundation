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
 * The model browser UI component for exploring and navigating the WildFly management resource tree interactively.
 * <p>
 * The model browser provides a split-pane interface with a tree view on the left showing the resource hierarchy and a
 * detail panel on the right displaying attributes, operations, and capabilities for the selected resource. Users can
 * navigate through the management model, filter resources, and inspect resource metadata.
 * <p>
 * The main entry point is {@link org.jboss.hal.ui.modelbrowser.ModelBrowser}, which coordinates the tree view, detail
 * panels, and filtering components.
 *
 * @see org.jboss.hal.ui.modelbrowser.ModelBrowser
 * @see org.jboss.hal.ui.modelbrowser.AttributesFilter
 * @see org.jboss.hal.ui.modelbrowser.OperationsFilter
 */
package org.jboss.hal.ui.modelbrowser;
