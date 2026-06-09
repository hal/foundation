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

/**
 * Factory methods for rendering error messages and {@link ModelNode} values as truncatable PatternFly code blocks.
 * Escaped forward slashes ({@code \/}) in the input are unescaped before rendering.
 */
public final class CodeBricks {

    /** Renders an error message as a code block truncated to 5 lines. */
    public static CodeBlock errorCode(String error) {
        return errorCode(error, 5);
    }

    /**
     * Renders an error message as a code block truncated to the specified number of lines.
     *
     * @param error the error message text
     * @param lines the maximum number of visible lines before truncation
     * @return a code block component displaying the error
     */
    public static CodeBlock errorCode(String error, int lines) {
        return codeBlock()
                .truncate(lines)
                .code(error.replace("\\/", "/"));
    }

    /** Renders a {@link ModelNode} as a JSON code block truncated to 5 lines. */
    public static CodeBlock modelNodeCode(ModelNode modelNode) {
        return modelNodeCode(modelNode, 5);
    }

    /**
     * Renders a {@link ModelNode} as a JSON code block truncated to the specified number of lines. Undefined model
     * nodes produce an empty code block.
     *
     * @param modelNode the model node to render as JSON
     * @param lines     the maximum number of visible lines before truncation
     * @return a code block component displaying the JSON representation
     */
    public static CodeBlock modelNodeCode(ModelNode modelNode, int lines) {
        String code = modelNode.isDefined() ? modelNode.toJSONString().replace("\\/", "/") : "";
        return codeBlock()
                .truncate(lines)
                .code(code);
    }

    private CodeBricks() {
    }
}
