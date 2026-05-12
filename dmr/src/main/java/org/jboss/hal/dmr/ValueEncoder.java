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
package org.jboss.hal.dmr;

/**
 * Encodes and decodes special characters ({@code /}, {@code :}, {@code =}) in DMR address values.
 *
 * <p>
 * Resource addresses in the WildFly management model use {@code /}, {@code :}, and {@code =} as structural delimiters.
 * When these characters appear in actual resource names or keys, they must be escaped to avoid ambiguity. This class
 * provides methods to encode values before constructing addresses and decode values when extracting names from addresses.
 *
 * <p>
 * Encoding replaces special characters with their escaped equivalents:
 * <ul>
 * <li>{@code /} becomes {@code \/}</li>
 * <li>{@code :} becomes {@code \:}</li>
 * <li>{@code =} becomes {@code \=}</li>
 * </ul>
 */
public class ValueEncoder {

    public static final String ENCODED_SLASH = "\\/";
    private static final String[][] SPECIAL_CHARACTERS = new String[][]{
            new String[]{"/", ENCODED_SLASH},
            new String[]{":", "\\:"},
            new String[]{"=", "\\="},
    };

    public static String encode(String value) {
        String localValue = value;
        if (localValue != null && !localValue.isEmpty()) {
            boolean encode = false;
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (localValue.contains(specialCharacter[0])) {
                    encode = true;
                    break;
                }
            }
            if (encode) {
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[0], special[1]);
                }
            }
        }
        return localValue;
    }

    public static String decode(String value) {
        String localValue = value;
        if (localValue != null && !localValue.isEmpty()) {
            boolean decode = false;
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (localValue.contains(specialCharacter[1])) {
                    decode = true;
                    break;
                }
            }
            if (decode) {
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[1], special[0]);
                }
            }
        }
        return localValue;
    }
}
