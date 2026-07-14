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
package org.jboss.hal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.core.Humanize.CaseType.CAPITAL;
import static org.jboss.hal.core.Humanize.CaseType.SENTENCE;

/** Generates human-readable text from terms used in the management model. */
public class Humanize {

    // ------------------------------------------------------ factory

    /**
     * Converts a given name into a sentence-cased, human-readable text. This method processes the input by capitalizing the
     * first letter of the name and ensuring the rest of the text is in lowercase, suitable for display purposes.
     *
     * @param name The input name to be converted into a sentence case. Must not be null.
     * @return A sentence-cased, human-readable version of the input name.
     */
    public static String sentenceCase(String name) {
        return new Humanize(SENTENCE).label(name);
    }

    /**
     * Converts a list of names into a sentence-cased, human-readable enumeration. The method handles edge cases such as an
     * empty list, a single name, or two names. For lists with more than two names, the items are separated by commas, with the
     * last item joined using the specified conjunction.
     *
     * @param names       The list of names to be converted into a sentence-cased enumeration. This can be null or empty.
     * @param conjunction The conjunction to use between the final two names when the list has more than one name (e.g., "and"
     *                    or "or").
     * @return A sentence-cased enumeration string. If the input list is null or empty, an empty string is returned. If the list
     * contains one name, the sentence-cased version of the name is returned. If the list has multiple names, a comma-separated
     * string with the conjunction is returned, formatted in sentence case.
     */
    public static String sentenceCase(List<String> names, String conjunction) {
        return new Humanize(SENTENCE).enumeration(names, conjunction);
    }

    /**
     * Converts a given name into a capital-cased, human-readable text. This method processes the input string by capitalizing
     * each word, ensuring the resulting text conforms to a title-like format suitable for display purposes.
     *
     * @param name The input name to be converted into a capital-cased text. Must not be null.
     * @return A capital-cased, human-readable text derived from the input name.
     */
    public static String capitalCase(String name) {
        return new Humanize(CAPITAL).label(name);
    }

    /**
     * Converts a list of names into a human-readable, capital-cased enumeration. Each name in the list is processed to ensure
     * it appears in capital case, and the names are formatted into a single string using commas and the specified conjunction.
     *
     * @param names       The list of names to be enumerated and converted to a capital case. The list can be null or empty.
     * @param conjunction The conjunction to use between the last two names in the enumeration (e.g., "and" or "or"). Must not
     *                    be null or empty.
     * @return A capital-cased string representing the enumeration of the provided names. If the input list is null or empty, an
     * empty string is returned. If the list has one name, its capital-cased version is returned. For multiple names, they are
     * joined in a comma-separated, capital-cased format with the conjunction applied to the last two names.
     */
    public static String capitalCase(List<String> names, String conjunction) {
        return new Humanize(CAPITAL).enumeration(names, conjunction);
    }

    public static String abbreviate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        // try to break at a word boundary
        int breakAt = text.lastIndexOf(' ', maxLength);
        if (breakAt <= 0) {
            breakAt = maxLength;
        }
        return text.substring(0, breakAt) + " …";
    }

    // ------------------------------------------------------ instance

    /**
     * Enum representing the type of casing to be applied to text.
     *
     * <ul>
     *   <li>SENTENCE: Capitalizes the first letter of the text and converts the rest to lowercase.</li>
     *   <li>CAPITAL: Capitalizes the first letter of each word in the text.</li>
     * </ul>
     */
    public enum CaseType {
        SENTENCE,
        CAPITAL
    }

    private static final String DELIMITER = " / ";
    private static final String QUOTE = "'";
    private static final String SPACE = " ";

    private static final Map<String, String> SPECIALS = new HashMap<>();

    static {
        SPECIALS.put("ajp", "AJP");
        SPECIALS.put("ccm", "CCM");
        SPECIALS.put("crls", "CRLs");
        SPECIALS.put("dn", "DN");
        SPECIALS.put("ear", "EAR");
        SPECIALS.put("ee", "EE");
        SPECIALS.put("ejb", "EJB");
        SPECIALS.put("ejb3", "EJB3");
        SPECIALS.put("giop", "GIOP");
        SPECIALS.put("gss", "GSS");
        SPECIALS.put("ha", "HA");
        SPECIALS.put("http", "HTTP");
        SPECIALS.put("https", "HTTPS");
        SPECIALS.put("http2", "HTTP/2");
        SPECIALS.put("id", "ID");
        SPECIALS.put("iiop", "IIOP");
        SPECIALS.put("iiop-ssl", "IIOP SSL");
        SPECIALS.put("io", "IO");
        SPECIALS.put("ip", "IP");
        SPECIALS.put("jaas", "JAAS");
        SPECIALS.put("jacc", "JACC");
        SPECIALS.put("jaspi", "JASPI");
        SPECIALS.put("jaxrs", "JAX-RS");
        SPECIALS.put("jberet", "JBeret");
        SPECIALS.put("jboss", "JBoss");
        SPECIALS.put("jdbc", "JDBC");
        SPECIALS.put("jca", "JCA");
        SPECIALS.put("jdr", "JDA");
        SPECIALS.put("jgroups", "JGroups");
        SPECIALS.put("jms", "JMS");
        SPECIALS.put("jmx", "JMX");
        SPECIALS.put("jndi", "JNDI");
        SPECIALS.put("jpa", "JPA");
        SPECIALS.put("jsf", "JSF");
        SPECIALS.put("json", "JSON");
        SPECIALS.put("jsse", "JSSE");
        SPECIALS.put("jsr", "JSR");
        SPECIALS.put("jta", "JTA");
        SPECIALS.put("jts", "JTS");
        SPECIALS.put("jvm", "JVM");
        SPECIALS.put("jwt", "JWT");
        SPECIALS.put("lra", "LRA");
        SPECIALS.put("mcp", "MCP");
        SPECIALS.put("mdb", "MDB");
        SPECIALS.put("mbean", "MBean");
        SPECIALS.put("microprofile", "MicroProfile");
        SPECIALS.put("oauth2", "OAuth 2");
        SPECIALS.put("ocsp", "OCSP");
        SPECIALS.put("oidc", "OIDC");
        SPECIALS.put("openapi", "OpenAPI");
        SPECIALS.put("otp", "OTP");
        SPECIALS.put("rdn", "RDN");
        SPECIALS.put("sar", "SAR");
        SPECIALS.put("sasl", "SASL");
        SPECIALS.put("sfsb", "SFSB");
        SPECIALS.put("slsb", "SLSB");
        SPECIALS.put("sni", "SNI");
        SPECIALS.put("sql", "SQL");
        SPECIALS.put("ssl", "SSL");
        SPECIALS.put("tcp", "TCP");
        SPECIALS.put("tls", "TLS");
        SPECIALS.put("ttl", "TTL");
        SPECIALS.put("tx", "TX");
        SPECIALS.put("udp", "UDP");
        SPECIALS.put("uri", "URI");
        SPECIALS.put("url", "URL");
        SPECIALS.put("uuid", "UUID");
        SPECIALS.put("vm", "VM");
        SPECIALS.put("xa", "XA");
        SPECIALS.put("war", "WAR");
        SPECIALS.put("wsdl", "WSDL");
    }

    private final CaseType caseType;

    Humanize(CaseType caseType) {
        this.caseType = caseType;
    }

    /**
     * Converts a given name into a human-readable text. If the name contains a dot (.), it is split into parts, and each part
     * is recursively labeled and joined with {@value #DELIMITER}. Otherwise, the method processes the name by replacing dashes
     * with spaces, replacing special characters, and capitalizing words as needed.
     *
     * @param name The input name to be converted into a human-readable text.
     * @return A human-readable text based on the input name.
     */
    public String label(String name) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return String.join(DELIMITER, stream(parts).map(this::label).collect(joining(DELIMITER)));
        } else {
            String label = name;
            label = label.replace('-', ' ');
            label = replaceSpecial(label);
            label = capitalize(label);
            return label;
        }
    }

    /**
     * Creates a human-readable enumeration from a list of names, separated by a conjunction. Handles edge cases such as a
     * single name or two names in the list. If the list contains more than two names, all names except the last are separated
     * by commas, and the last name is joined using the specified conjunction.
     *
     * @param names       The list of names to be enumerated. This can be null or empty.
     * @param conjunction The conjunction to use between the final two names in the enumeration (e.g., "and" or "or").
     * @return A formatted enumeration string. If the input list is null or empty, an empty string is returned. If the list
     * contains a single name, it returns the name wrapped in quotes. For multiple names, it formats the list as a
     * comma-separated string with the conjunction applied to the last two names.
     */
    public String enumeration(List<String> names, String conjunction) {
        String enumeration = "";
        if (names != null && !names.isEmpty()) {
            LinkedList<String> ll = new LinkedList<>(names);
            int size = ll.size();
            if (size == 1) {
                return QUOTE + label(ll.getFirst()) + QUOTE;
            } else if (size == 2) {
                return QUOTE + label(ll.getFirst()) + QUOTE +
                        SPACE + conjunction + SPACE +
                        QUOTE + label(ll.getLast()) + QUOTE;
            } else {
                String last = ll.removeLast();
                enumeration = ll.stream()
                        .map(name -> QUOTE + label(name) + QUOTE)
                        .collect(joining(", "));
                enumeration = enumeration + SPACE + conjunction + SPACE + QUOTE + label(last) + QUOTE;
            }
        }
        return enumeration;
    }

    // ------------------------------------------------------ internal

    private String replaceSpecial(String label) {
        List<String> replacedParts = new ArrayList<>();
        for (String part : label.split(" ")) {
            String replaced = part;
            for (Map.Entry<String, String> entry : SPECIALS.entrySet()) {
                if (replaced.length() == entry.getKey().length()) {
                    replaced = replaced.replace(entry.getKey(), entry.getValue());
                }
            }
            replacedParts.add(replaced);
        }
        return String.join(SPACE, replacedParts);
    }

    private String capitalize(String str) {
        char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            char ch = buffer[i];
            if (Character.isWhitespace(ch)) {
                capitalizeNext = caseType == CAPITAL;
            } else if (capitalizeNext) {
                buffer[i] = Character.toUpperCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }
}
