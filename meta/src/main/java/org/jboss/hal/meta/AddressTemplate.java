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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;

import static java.util.stream.Collectors.joining;

/**
 * Template for a DMR address which can contain variable parts.
 * <p>
 * An address template can be defined using the following EBNF:
 * <pre>
 * AddressTemplate = "/" | Segment ;
 * Segment         = Tuple | Segment "/" Tuple ;
 * Tuple           = Placeholder | Key "=" Value ;
 * Placeholder     = "{" Alphanumeric "}" ;
 * Key             = Alphanumeric ;
 * Value           = Placeholder | Alphanumeric | "*" ;
 * </pre>
 * <p>
 * Examples for valid address templates are
 * <pre>
 * /
 * subsystem=io
 * {selected.server}
 * {selected.server}/deployment=foo
 * subsystem=logging/logger={selection}
 * </pre>
 * <p>
 * To get a fully qualified address from an address template use the method {@link #resolve(StatementContext)}.
 */
public final class AddressTemplate implements Iterable<Segment> {

    // ------------------------------------------------------ factory

    /** Creates a new address template from an encoded string template. */
    public static AddressTemplate root() {
        return new AddressTemplate("/");
    }


    /** Creates a new address template from an encoded string template. */
    public static AddressTemplate of(String template) {
        return new AddressTemplate(withSlash(template));
    }

    /** Creates a new address template from a placeholder. */
    public static AddressTemplate of(Placeholder placeholder) {
        return AddressTemplate.of(String.join("/", placeholder.expression()));
    }

    // ------------------------------------------------------ instance

    private static final String[][] SPECIAL_CHARACTERS = new String[][]{
            new String[]{"/", "\\/"},
            new String[]{":", "\\:"},
            new String[]{"=", "\\="},
    };
    public final String template;
    private final LinkedList<Segment> segments;

    private AddressTemplate(List<Segment> segments) {
        this.segments = new LinkedList<>(segments);
        this.template = join(segments);
    }

    private AddressTemplate(String template) {
        assert template != null : "template must not be null";
        this.segments = parse(template);
        this.template = join(segments);
    }

    private LinkedList<Segment> parse(String template) {
        LinkedList<Segment> segments = new LinkedList<>();

        if (template.equals("/")) {
            return segments;
        }

        StringTokenizer tok = new StringTokenizer(template);
        while (tok.hasMoreTokens()) {
            String nextToken = tok.nextToken();
            if (nextToken.contains("=")) {
                String[] split = nextToken.split("=");
                segments.add(new Segment(split[0], split[1]));
            } else {
                segments.add(new Segment(nextToken));
            }
        }
        return segments;
    }

    private String join(List<Segment> segments) {
        return segments.stream().map(Segment::toString).collect(joining("/"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressTemplate)) {
            return false;
        }

        AddressTemplate that = (AddressTemplate) o;
        return template.equals(that.template);

    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result;
        return result;
    }

    /** @return the string representation of this address template */
    @Override
    public String toString() {
        return template.isEmpty() ? "/" : template;
    }

    // ------------------------------------------------------ append / sub and parent

    /**
     * Appends the specified encoded template to this template and returns a new template. If the specified template does not
     * start with a slash, '/' is automatically appended.
     *
     * @param template the encoded template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

    public AddressTemplate append(AddressTemplate template) {
        return append(template.toString());
    }

    /**
     * Works like {@link List#subList(int, int)} over the tokens of this template and throws the same exceptions.
     *
     * @param fromIndex low endpoint (inclusive) of the sub template
     * @param toIndex   high endpoint (exclusive) of the sub template
     * @return a new address template containing the specified tokens.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</tt>)
     */
    public AddressTemplate subTemplate(int fromIndex, int toIndex) {
        LinkedList<Segment> subSegments = new LinkedList<>(this.segments.subList(fromIndex, toIndex));
        return AddressTemplate.of(join(subSegments));
    }

    /** @return the parent address template or the root template */
    public AddressTemplate parent() {
        if (isEmpty() || size() == 1) {
            return AddressTemplate.of("/");
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    // ------------------------------------------------------ properties

    /** @return the first segment or null if this address template is empty. */
    public Segment first() {
        if (!segments.isEmpty() && segments.getFirst().hasKey()) {
            return segments.getFirst();
        }
        return null;
    }

    /** @return the last segment or null if this address template is empty. */
    public Segment last() {
        if (!segments.isEmpty() && segments.getFirst().hasKey()) {
            return segments.getLast();
        }
        return null;
    }

    /** @return true if this template contains no tokens, false otherwise */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** @return the number of tokens */
    public int size() {
        return segments.size();
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    // ------------------------------------------------------ wildcards & resolve

    public AddressTemplate wildcards(String first, String... rest) {
        List<String> wildcards = new ArrayList<>();
        wildcards.add(first);
        if (rest != null) {
            wildcards.addAll(Arrays.asList(rest));
        }

        List<Segment> processedSegments = new ArrayList<>();
        Iterator<String> wi = wildcards.iterator();
        for (Segment segment : segments) {
            if (wi.hasNext() && segment.hasKey() && "*".equals(segment.value)) {
                processedSegments.add(new Segment(segment.key, wi.next()));
            } else {
                processedSegments.add(new Segment(segment.key, segment.value));
            }
        }
        return AddressTemplate.of(join(processedSegments));
    }

    public ResourceAddress resolve(StatementContext context) {
        return resolve(context, null);
    }

    public ResourceAddress resolve(StatementContext context, SegmentResolver resolver) {
        if (isEmpty()) {
            return ResourceAddress.root();
        } else {
            ModelNode model = new ModelNode();
            for (int i = 0; i < segments.size(); i++) {
                Segment segment = segments.get(i);
                Segment resolved = resolver == null
                        ? context.resolve(segment)
                        : resolver.resolve(context, this, segment, i == 0, i == segments.size() - 1, i);
                model.add(resolved.key, decodeValue(resolved.value));
            }
            return new ResourceAddress(model);
        }
    }

    // ------------------------------------------------------ internal

    private static String withSlash(String template) {
        if (template != null && !template.startsWith("/")) {
            return "/" + template;
        }
        return template;
    }

    static String encodeValue(String value) {
        boolean encode = false;
        if (value != null) {
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (value.contains(specialCharacter[0])) {
                    encode = true;
                    break;
                }
            }
            if (encode) {
                String localValue = value;
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[0], special[1]);
                }
                return localValue;
            }
        }
        return value;
    }

    private static String decodeValue(String value) {
        boolean decode = false;
        if (value != null) {
            for (String[] specialCharacter : SPECIAL_CHARACTERS) {
                if (value.contains(specialCharacter[1])) {
                    decode = true;
                    break;
                }
            }
            if (decode) {
                String localValue = value;
                for (String[] special : SPECIAL_CHARACTERS) {
                    localValue = localValue.replace(special[1], special[0]);
                }
                return localValue;
            }
        }
        return value;
    }

    private static class StringTokenizer {

        private final String delim;
        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s) {
            this.s = s;
            this.delim = "/";
            len = s.length();
        }

        String nextToken() {
            if (!hasMoreTokens()) {
                throw new NoSuchElementException();
            }
            String result = next;
            next = null;
            return result;
        }

        boolean hasMoreTokens() {
            if (next != null) {
                return true;
            }
            // skip leading delimiters
            while (pos < len && delim.indexOf(s.charAt(pos)) != -1) {
                pos++;
            }

            if (pos >= len) {
                return false;
            }

            int p0 = pos++;
            while (pos < len && delim.indexOf(s.charAt(pos)) == -1) {
                pos++;
            }

            next = s.substring(p0, pos++);
            return true;
        }
    }
}
