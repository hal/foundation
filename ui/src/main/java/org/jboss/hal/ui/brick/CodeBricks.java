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
package org.jboss.hal.ui.brick;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.codeblock.CodeBlock;

import static org.patternfly.component.codeblock.CodeBlock.codeBlock;

/** Code block rendering for errors and model nodes. */
public final class CodeBricks {

    public static CodeBlock errorCode(String error) {
        return errorCode(error, 5);
    }

    public static CodeBlock errorCode(String error, int lines) {
        return codeBlock()
                .truncate(lines)
                .code(error.replace("\\/", "/"));
    }

    public static CodeBlock modelNodeCode(ModelNode modelNode) {
        return modelNodeCode(modelNode, 5);
    }

    public static CodeBlock modelNodeCode(ModelNode modelNode, int lines) {
        String code = modelNode.isDefined() ? modelNode.toJSONString().replace("\\/", "/") : "";
        return codeBlock()
                .truncate(lines)
                .code(code);
    }

    private CodeBricks() {
    }
}
