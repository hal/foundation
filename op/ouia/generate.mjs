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

import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const idsJavaPath = resolve(
  __dirname,
  "../../resources/src/main/java/org/jboss/hal/resources/Ids.java",
);
const outputPath = resolve(__dirname, "src/ids.ts");

// ------------------------------------------------------ Id.build() port

/**
 * Port of Elemento's Id.asId() — sanitizes a single string part into an HTML-safe ID segment.
 */
function asId(text) {
  const parts = text.split(/[-\s]/);
  const sanitized = [];
  for (const part of parts) {
    if (part) {
      let s = part.replaceAll(/\s+/g, "");
      s = s.replaceAll(/[^a-zA-Z0-9\-_]/g, "");
      s = s.replaceAll("_", "-");
      if (s.length > 0) {
        sanitized.push(s);
      }
    }
  }
  if (sanitized.length === 0) {
    return null;
  }
  return sanitized
    .filter((s) => s && s.trim().length > 0)
    .map((s) => s.toLowerCase())
    .join("-");
}

/**
 * Port of Elemento's Id.build() — builds an HTML-safe ID from one or more string parts.
 */
function build(id, ...additionalIds) {
  if (!id || id.trim().length === 0) {
    throw new Error("ID must not be null or empty.");
  }
  const ids = [id];
  for (const additionalId of additionalIds) {
    if (additionalId && additionalId.trim().length !== 0) {
      ids.push(additionalId);
    }
  }
  return ids
    .map(asId)
    .filter((s) => s !== null)
    .join("-");
}

// ------------------------------------------------------ parse Ids.java

function parseIdsJava(source) {
  const constants = [];
  const methods = [];

  const constantRegex =
    /String\s+([A-Z][A-Z0-9_]*)\s*=\s*"([^"]+)"\s*;/g;
  let match;
  while ((match = constantRegex.exec(source)) !== null) {
    constants.push({ name: match[1], value: match[2] });
  }

  const methodRegex =
    /static\s+String\s+(\w+)\s*\(([^)]*)\)\s*\{([^}]+)\}/g;
  while ((match = methodRegex.exec(source)) !== null) {
    const methodName = match[1];
    const paramsStr = match[2].trim();
    const body = match[3].trim();

    const params = paramsStr
      .split(",")
      .map((p) => p.trim())
      .filter((p) => p.length > 0)
      .map((p) => {
        const parts = p.split(/\s+/);
        const type = parts[0];
        const name = parts[1];
        const varargs = type.endsWith("...");
        return { type, name, varargs };
      });

    const buildCallRegex = /Id\.build\(([^)]+)\)/;
    const buildMatch = buildCallRegex.exec(body);
    if (buildMatch) {
      const args = buildMatch[1]
        .split(",")
        .map((a) => a.trim());
      methods.push({ name: methodName, params, buildArgs: args });
    }
  }

  return { constants, methods };
}

// ------------------------------------------------------ emit TypeScript

function emitTypeScript({ constants, methods }) {
  const lines = [
    "// Generated from Ids.java — do not edit",
    "",
    "// ------------------------------------------------------ ID builder",
    "",
    "function asId(text: string): string | null {",
    '    const parts = text.split(/[-\\s]/);',
    "    const sanitized: string[] = [];",
    "    for (const part of parts) {",
    "        if (part) {",
    '            let s = part.replaceAll(/\\s+/g, "");',
    '            s = s.replaceAll(/[^a-zA-Z0-9\\-_]/g, "");',
    '            s = s.replaceAll("_", "-");',
    "            if (s.length > 0) {",
    "                sanitized.push(s);",
    "            }",
    "        }",
    "    }",
    "    if (sanitized.length === 0) {",
    "        return null;",
    "    }",
    "    return sanitized",
    "        .filter((s) => s && s.trim().length > 0)",
    "        .map((s) => s.toLowerCase())",
    '        .join("-");',
    "}",
    "",
    "export function buildId(id: string, ...additionalIds: string[]): string {",
    '    if (!id || id.trim().length === 0) {',
    '        throw new Error("ID must not be null or empty.");',
    "    }",
    "    const ids: string[] = [id];",
    "    for (const additionalId of additionalIds) {",
    "        if (additionalId && additionalId.trim().length !== 0) {",
    "            ids.push(additionalId);",
    "        }",
    "    }",
    "    return ids",
    "        .map(asId)",
    "        .filter((s): s is string => s !== null)",
    '        .join("-");',
    "}",
  ];

  if (constants.length > 0) {
    lines.push("");
    lines.push(
      "// ------------------------------------------------------ static IDs",
    );
    lines.push("");
    const sorted = [...constants].sort((a, b) => a.name.localeCompare(b.name));
    for (const { name, value } of sorted) {
      lines.push(`export const ${name} = "${value}";`);
    }
  }

  if (methods.length > 0) {
    lines.push("");
    lines.push(
      "// ------------------------------------------------------ dynamic IDs",
    );
    lines.push("");
    for (const { name, params, buildArgs } of methods) {
      const paramNames = new Set(params.map((p) => p.name));
      const varargsParam = params.find((p) => p.varargs);
      const tsParams = params
        .map((p) =>
          p.varargs
            ? `...${p.name}: string[]`
            : `${p.name}: string`,
        )
        .join(", ");
      const tsArgs = buildArgs
        .map((arg) => {
          const trimmed = arg.replace(/^"|"$/g, "");
          if (arg.startsWith('"')) return arg;
          if (paramNames.has(arg)) {
            return params.find((p) => p.name === arg)?.varargs
              ? `...${arg}`
              : arg;
          }
          if (varargsParam) {
            const nonVarargsParams = params
              .filter((p) => !p.varargs)
              .map((p) => p.name);
            return [...nonVarargsParams, `...${varargsParam.name}`].join(", ");
          }
          return arg;
        })
        .join(", ");
      lines.push(
        `export function ${name}(${tsParams}): string {`,
      );
      lines.push(`    return buildId(${tsArgs});`);
      lines.push("}");
      lines.push("");
    }
  }

  return lines.join("\n") + "\n";
}

// ------------------------------------------------------ main

const source = readFileSync(idsJavaPath, "utf-8");
const parsed = parseIdsJava(source);
const output = emitTypeScript(parsed);

mkdirSync(dirname(outputPath), { recursive: true });
writeFileSync(outputPath, output, "utf-8");

console.log(
  `Generated ${outputPath} (${parsed.constants.length} constants, ${parsed.methods.length} functions)`,
);
