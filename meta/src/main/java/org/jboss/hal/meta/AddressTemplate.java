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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ValueEncoder;
import org.jboss.hal.meta.WildcardResolver.Direction;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.jboss.hal.dmr.ValueEncoder.ENCODED_SLASH;

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
 * <strong>Resolving</strong><br/>
 * To get a fully qualified {@link ResourceAddress} from an address template use one of the <code>resolve()</code> methods and a
 * {@link TemplateResolver}. In general, you should prefer address templates over {@linkplain ResourceAddress resource addresses}.
 * <p>
 * <strong>Encoding</strong><br/>
 * Some characters in values must be encoded using the backslash character:
 * <ul>
 *     <li><code>/</code> → <code>\/</code></li>
 *     <li><code>=</code> → <code>\=</code></li>
 *     <li><code>:</code> → <code>\:</code></li>
 * </ul>
 * When creating a template from a {@linkplain AddressTemplate#of(String) string},
 * or {@linkplain AddressTemplate#append(String) appending} a string, you must take care of the encoding.
 * If you create a template from {@linkplain AddressTemplate#of(List) segments}, the encoding is done for you.
 */
public final class AddressTemplate implements Iterable<Segment> {

    // ------------------------------------------------------ factory

    /**
     * Creates a new root address template, which represents the empty address.
     */
    public static AddressTemplate root() {
        return new AddressTemplate(emptyList());
    }

    /**
     * Creates a new address template from an <strong>encoded</strong> string template. Special characters in the template must
     * be encoded.
     */
    public static AddressTemplate of(String template) {
        return new AddressTemplate(parse(template));
    }

    /** Creates a new address template from a placeholder. */
    public static AddressTemplate of(Placeholder placeholder) {
        if (placeholder != null) {
            return new AddressTemplate(parse("/" + placeholder.expression()));
        } else {
            return new AddressTemplate(emptyList());
        }
    }

    public static AddressTemplate of(Segment segment) {
        return of(singletonList(segment));
    }

    /**
     * Creates a new address template from an existing template.
     */
    public static AddressTemplate of(AddressTemplate template) {
        return template != null ? new AddressTemplate(template.segments) : new AddressTemplate(emptyList());
    }

    /**
     * Creates a new address template from a list of segments. Special characters in segment values must not be encoded.
     */
    public static AddressTemplate of(List<Segment> segments) {
        return segments != null ? new AddressTemplate(segments) : new AddressTemplate(emptyList());
    }

    public static AddressTemplate of(ResourceAddress address) {
        return of(address.toString());
    }

    // ------------------------------------------------------ instance

    /** The string representation of this address template. If the template contains special characters, they're encoded. */
    public final String template;
    private final LinkedList<Segment> segments;

    private AddressTemplate(List<Segment> segments) {
        this.segments = segments.stream()
                .filter(Predicate.not(segment -> segment == Segment.EMPTY))
                .collect(toCollection(LinkedList::new));
        this.template = "/" + this.segments.stream()
                .map(Segment::toString)
                .collect(joining("/"));
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

    /**
     * @return the string representation of this address template. If the template contains special characters, they're encoded.
     */
    @Override
    public String toString() {
        return template;
    }

    // ------------------------------------------------------ append / sub and parent

    /**
     * Appends the specified <strong>encoded</strong> template to this template and returns a new template. Special characters
     * in the value must not be encoded.
     *
     * @param key   the key to append to the template
     * @param value the value to append to the template
     * @return a new template
     */
    public AddressTemplate append(String key, String value) {
        return append(key + "=" + ValueEncoder.encode(value));
    }

    /**
     * Appends the specified <strong>encoded</strong> template to this template and returns a new template. Special characters
     * in the template must be encoded. If the specified template does not start with a slash, '/' is automatically appended.
     *
     * @param template the <strong>encoded</strong> template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String first;
        String second;
        if (isEmpty()) {
            first = "";
        } else {
            first = this.template.substring(1);
        }
        if (template != null && template.startsWith("/") && !template.startsWith(ENCODED_SLASH)) {
            second = template.substring(1);
        } else {
            second = template;
        }
        return AddressTemplate.of(first + "/" + second);
    }

    /**
     * Appends the specified template to this template and returns a new template.
     *
     * @return a new template
     */
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
        return new AddressTemplate(subSegments);
    }

    /** @return the parent address template or the root template */
    public AddressTemplate parent() {
        if (isEmpty() || size() == 1) {
            return AddressTemplate.of("/");
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    public AddressTemplate anonymiseLast() {
        if (isEmpty()) {
            return AddressTemplate.of("");
        } else if (!"*".equals(last().value)) {
            return parent().append(last().key, "*");
        } else {
            return AddressTemplate.of(this);
        }
    }

    // ------------------------------------------------------ properties

    /** @return the first segment or {@link Segment#EMPTY} if this address template is empty. */
    public Segment first() {
        if (!segments.isEmpty()) {
            return segments.getFirst();
        }
        return Segment.EMPTY;
    }

    /** @return the last segment or {@link Segment#EMPTY} if this address template is empty. */
    public Segment last() {
        if (!segments.isEmpty()) {
            return segments.getLast();
        }
        return Segment.EMPTY;
    }

    /**
     * Checks if the address template is fully qualified. An address template is considered fully qualified if none of its
     * segments contain a placeholder or if none of its segments have a value of "*".
     *
     * @return true if the address template is fully qualified, false otherwise.
     */
    public boolean fullyQualified() {
        for (Segment segment : this) {
            if (segment.containsPlaceholder() || "*".equals(segment.value)) {
                return false;
            }
        }
        return true;
    }

    /** @return true if this template contains no tokens, false otherwise */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /** @return the number of tokens */
    public int size() {
        return segments.size();
    }

    public List<Segment> segments() {
        return unmodifiableList(segments);
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    public String identifier() {
        if (isEmpty()) {
            return "root";
        } else {
            String safeTemplate = template
                    .replace("/", "-s-")
                    .replace("=", "-e-")
                    .replace(":", "-c-")
                    .replace("*", "-w-");
            return Id.build(safeTemplate);
        }
    }

    // ------------------------------------------------------ resolve

    /** Resolve this template using the {@link NoopResolver} */
    public ResourceAddress resolve() {
        return resolve(new NoopResolver());
    }

    /** Resolve this template using the {@link WildcardResolver} */
    public ResourceAddress resolve(Direction direction, String first, String... more) {
        return resolve(new WildcardResolver(direction, first, more));
    }

    /** Resolve this template using the {@link StatementContextResolver} */
    public ResourceAddress resolve(StatementContext context) {
        return resolve(new StatementContextResolver(context));
    }

    /**
     * Resolves the given template using the provided resolver.
     *
     * @param resolver the resolver used to resolve the template
     * @return the resolved ResourceAddress
     */
    public ResourceAddress resolve(TemplateResolver resolver) {
        if (isEmpty()) {
            return ResourceAddress.root();
        } else {
            ModelNode model = new ModelNode();
            AddressTemplate resolved = resolver.resolve(this);
            for (Segment segment : resolved) {
                // Do *not* encode values: the model node will be encoded as DMR!
                model.add(segment.key, segment.value);
            }
            return new ResourceAddress(model);
        }
    }

    // ------------------------------------------------------ internal

    private static LinkedList<Segment> parse(String template) {
        LinkedList<Segment> segments = new LinkedList<>();

        if (template != null && !template.trim().isEmpty()) {
            String trimmed = template.trim();
            if (!trimmed.equals("/")) {
                trimmed = trimmed.startsWith("/") && !trimmed.startsWith(ENCODED_SLASH)
                        ? trimmed.substring(1)
                        : trimmed;
                trimmed = trimmed.endsWith("/") && !trimmed.endsWith(ENCODED_SLASH)
                        ? trimmed.substring(0, trimmed.length() - 1)
                        : trimmed;

                // split template by '/'
                int current = 0;
                boolean backslash = false;
                LinkedList<String> unparsedSegments = new LinkedList<>();
                for (int i = 0; i < trimmed.length(); i++) {
                    char c = trimmed.charAt(i);
                    if (c == '\\') {
                        backslash = true;
                    } else if (c == '/') {
                        if (!backslash) {
                            unparsedSegments.add(trimmed.substring(current, i));
                            current = i + 1;
                        }
                        backslash = false;
                    } else {
                        backslash = false;
                    }
                }
                unparsedSegments.add(trimmed.substring(current));

                // pre-validate segments
                LinkedList<String> preValidatedSegments = new LinkedList<>();
                for (String us : unparsedSegments) {
                    if (us == null || us.isEmpty() || "{}".equals(us) ||
                            us.startsWith("=") || (us.endsWith("=") && !us.endsWith("\\=")) ||
                            (us.contains("{") && !us.contains("}")) ||
                            (us.contains("}") && !us.contains("{")) ||
                            (!us.contains("=") && !us.contains("{") && !us.contains("}"))) {
                        break; // stop after the first invalid segment
                    }
                    preValidatedSegments.add(us);
                }

                // split segments by '='
                for (String pvs : preValidatedSegments) {
                    String key = null;
                    String value;
                    backslash = false;
                    for (int i = 0; i < pvs.length(); i++) {
                        char c = pvs.charAt(i);
                        if (c == '\\') {
                            backslash = true;
                        } else if (c == '=') {
                            if (!backslash) {
                                key = pvs.substring(0, i);
                                value = pvs.substring(i + 1);
                                segments.add(new Segment(key, ValueEncoder.decode(value)));
                            }
                            backslash = false;
                        } else {
                            backslash = false;
                        }
                    }
                    if (key == null) {
                        segments.add(new Segment(pvs));
                    }
                }
            }
        }

        // 2. post-validate segments and remove everything after the first invalid segment
        boolean invalid = false;
        for (Iterator<Segment> iterator = segments.iterator(); iterator.hasNext(); ) {
            Segment segment = iterator.next();
            boolean emptyKey = segment.key == null || segment.key.isEmpty() || "{}".equals(segment.key);
            boolean emptyValue = segment.value == null || segment.value.isEmpty() || "{}".equals(segment.value);
            boolean placeholder = segment.containsPlaceholder();
            invalid = invalid ||
                    ((emptyKey && emptyValue) || ((emptyKey || emptyValue) && !placeholder));
            if (invalid) {
                iterator.remove();
            }
        }
        return segments;
    }
}
