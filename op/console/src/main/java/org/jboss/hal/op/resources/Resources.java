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
package org.jboss.hal.op.resources;

import org.kie.j2cl.tools.processors.annotations.GWT3Resource;
import org.kie.j2cl.tools.processors.common.resources.ClientBundle;
import org.kie.j2cl.tools.processors.common.resources.TextResource;

/** Client bundle providing static resources such as the HAL logo for the halOP console. */
@GWT3Resource
public interface Resources extends ClientBundle {

    /** Singleton instance of the resource bundle. */
    Resources INSTANCE = ResourcesImpl.INSTANCE;

    /** Returns the HAL logo as an SVG text resource. */
    @Source("hal-logo.svg")
    TextResource logo();
}
