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

/** Generates human-readable labels from terms used in the management model. */
public class LabelBuilder {

    // ------------------------------------------------------ factory

    public static String labelBuilder(String name) {
        return new LabelBuilder().label(name);
    }

    public static String labelBuilder(List<String> names, String conjunction) {
        return new LabelBuilder().enumeration(names, conjunction);
    }

    // ------------------------------------------------------ instance

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
        SPECIALS.put("wsdl", "WSDL");
    }

    public String label(String name) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return String.join(" / ", stream(parts).map(this::label).collect(joining(" / ")));
        } else {
            String label = name;
            label = label.replace('-', ' ');
            label = replaceSpecial(label);
            label = capitalize(label);
            return label;
        }
    }

    /**
     * Turns a list of names from the management model into a human readable enumeration wrapped in quotes and separated with
     * commas. The last name is separated with the specified conjunction.
     *
     * @return The list of names as human-readable string or an empty string if the names are null or empty.
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
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toUpperCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }
}
