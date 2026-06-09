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
 *
 * <h2>Creating and Appending</h2>
 * There are two families of methods, following the naming convention of
 * <a href="https://www.gwtproject.org/javadoc/latest/com/google/gwt/safehtml/shared/SafeHtmlUtils.html">SafeHtmlUtils</a>:
 * <dl>
 *     <dt><strong>{@code of(...)} / {@code append(...)}</strong> — safe, encoding handled for you</dt>
 *     <dd>Accept <em>decoded</em> (raw) values. Special characters ({@code /}, {@code :}, {@code =}) in the value are encoded
 *     automatically. Use these methods when working with dynamic, runtime, or user-provided values. Examples:
 *     <ul>
 *         <li>{@link #of(String, String)} — single key/value segment</li>
 *         <li>{@link #of(Segment)}, {@link #of(List)}, {@link #of(ResourceAddress)} — from structured objects</li>
 *         <li>{@link #of(Placeholder)} — from a placeholder</li>
 *         <li>{@link #append(String, String)} — append a key/value segment</li>
 *         <li>{@link #append(Segment)}, {@link #append(AddressTemplate)}, {@link #append(Placeholder)} — append structured
 *         objects</li>
 *     </ul>
 *     </dd>
 *     <dt><strong>{@code ofTrusted(...)} / {@code appendTrusted(...)}</strong> — caller vouches, no encoding applied</dt>
 *     <dd>Accept an <em>encoded</em> template string. The caller is responsible for correct escaping of special characters in
 *     values (e.g., {@code \/}, {@code \:}, {@code \=}). No encoding or validation of values is performed. Use these methods
 *     for static, compile-time template literals where you control the content. Examples:
 *     <ul>
 *         <li>{@link #ofTrusted(String)} — create from a trusted template string</li>
 *         <li>{@link #appendTrusted(String)} — append a trusted template string</li>
 *     </ul>
 *     </dd>
 * </dl>
 *
 * <h2>Resolving</h2>
 * To get a fully qualified {@link ResourceAddress} from an address template use one of the <code>resolve()</code> methods and a
 * {@link TemplateResolver}. In general, you should prefer address templates over
 * {@linkplain ResourceAddress resource addresses}.
 *
 * <h2>Encoding</h2>
 * Internally, segment values are stored in <em>decoded</em> form. Encoding only happens at serialization time (in
 * {@link Segment#toString()} and {@link #toString()}). The following characters are encoded:
 * <ul>
 *     <li><code>/</code> → <code>\/</code></li>
 *     <li><code>=</code> → <code>\=</code></li>
 *     <li><code>:</code> → <code>\:</code></li>
 * </ul>
 */
public final class AddressTemplate implements Iterable<Segment> {

    // ------------------------------------------------------ trusted factory (no encoding)

    /**
     * Creates an address template from a <strong>trusted, encoded</strong> template string.
     * <p>
     * The caller vouches that the string is correctly formatted. Special characters in values ({@code /}, {@code :}, {@code =})
     * must already be escaped with a backslash (e.g., {@code \/}, {@code \:}, {@code \=}). <strong>No encoding is
     * applied.</strong>
     * <p>
     * Use this method for static, compile-time template literals. For dynamic or user-provided values, use
     * {@link #of(String, String)} instead, which handles encoding automatically.
     *
     * @param template the trusted, encoded template string
     * @return a new address template
     */
    public static AddressTemplate ofTrusted(String template) {
        return new AddressTemplate(parseSegments(template));
    }

    // ------------------------------------------------------ safe factories (encoding handled)

    /**
     * Creates a new root address template, which represents the empty address.
     */
    public static AddressTemplate root() {
        return new AddressTemplate(emptyList());
    }

    /**
     * Creates a new address template with a single segment from the given key and value. <strong>The value is automatically
     * encoded</strong> — callers must <strong>not</strong> pre-encode the value.
     *
     * @param key   the resource type (e.g., {@code "subsystem"}, {@code "deployment"})
     * @param value the resource name, which may contain special characters ({@code /}, {@code :}, {@code =})
     * @return a new address template
     */
    public static AddressTemplate of(String key, String value) {
        return of(singletonList(new Segment(key, value)));
    }

    /**
     * Creates a new address template from a placeholder.
     */
    public static AddressTemplate of(Placeholder placeholder) {
        if (placeholder != null) {
            return new AddressTemplate(parseSegments("/" + placeholder.expression()));
        } else {
            return new AddressTemplate(emptyList());
        }
    }

    /**
     * Creates a new address template from a single segment. The segment stores its value in decoded form — encoding is handled
     * at serialization time.
     */
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
     * Creates a new address template from a list of segments. Segments store their values in decoded form — encoding is handled
     * at serialization time.
     */
    public static AddressTemplate of(List<Segment> segments) {
        return segments != null ? new AddressTemplate(segments) : new AddressTemplate(emptyList());
    }

    /**
     * Creates a new address template from a resource address by extracting its property list directly. No encode/decode
     * round-trip is performed.
     */
    public static AddressTemplate of(ResourceAddress address) {
        if (address != null && address.isDefined()) {
            List<Segment> segments = address.asPropertyList().stream()
                    .map(p -> new Segment(p.getName(), p.getValue().asString()))
                    .collect(toCollection(LinkedList::new));
            return new AddressTemplate(segments);
        }
        return root();
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
     * @return the string representation of this address template (same as {@link #template}). If the template contains special
     * characters, they're encoded.
     * @see AddressTemplate#template
     */
    @Override
    public String toString() {
        return template;
    }

    // ------------------------------------------------------ trusted append (no encoding)

    /**
     * Appends the specified <strong>trusted, encoded</strong> template string to this template and returns a new template.
     * <p>
     * The caller vouches that the string is correctly formatted. Special characters in values must already be escaped with a
     * backslash (e.g., {@code \/}, {@code \:}, {@code \=}). <strong>No encoding is applied.</strong>
     * <p>
     * For dynamic or user-provided values, use {@link #append(String, String)} instead, which handles encoding automatically.
     *
     * @param template the trusted, encoded template string (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    public AddressTemplate appendTrusted(String template) {
        if (template == null || template.isEmpty()) {
            return this;
        }
        LinkedList<Segment> newSegments = new LinkedList<>(this.segments);
        newSegments.addAll(parseSegments(template));
        return new AddressTemplate(newSegments);
    }

    // ------------------------------------------------------ safe append (encoding handled)

    /**
     * Appends a key-value segment to this template and returns a new template. <strong>The value is automatically
     * encoded</strong> — callers must <strong>not</strong> pre-encode the value.
     *
     * @param key   the resource type
     * @param value the resource name, which may contain special characters ({@code /}, {@code :}, {@code =})
     * @return a new address template with the appended segment
     */
    public AddressTemplate append(String key, String value) {
        String cleanKey = (key != null && key.startsWith("/") && !key.startsWith(ENCODED_SLASH))
                ? key.substring(1) : key;
        LinkedList<Segment> newSegments = new LinkedList<>(this.segments);
        newSegments.add(new Segment(cleanKey, value));
        return new AddressTemplate(newSegments);
    }

    /**
     * Appends a segment to this template and returns a new template. The segment stores its value in decoded form — encoding is
     * handled at serialization time.
     *
     * @param segment the segment to append
     * @return a new address template with the appended segment
     */
    public AddressTemplate append(Segment segment) {
        LinkedList<Segment> newSegments = new LinkedList<>(this.segments);
        newSegments.add(segment);
        return new AddressTemplate(newSegments);
    }

    /**
     * Appends the specified template to this template and returns a new template.
     *
     * @return a new template
     */
    public AddressTemplate append(AddressTemplate template) {
        LinkedList<Segment> newSegments = new LinkedList<>(this.segments);
        newSegments.addAll(template.segments);
        return new AddressTemplate(newSegments);
    }

    /**
     * Appends a placeholder to this template and returns a new template.
     *
     * @param placeholder the placeholder to append
     * @return a new template
     */
    public AddressTemplate append(Placeholder placeholder) {
        if (placeholder != null) {
            LinkedList<Segment> newSegments = new LinkedList<>(this.segments);
            newSegments.addAll(parseSegments(placeholder.expression()));
            return new AddressTemplate(newSegments);
        }
        return this;
    }

    // ------------------------------------------------------ sub and parent

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
            return root();
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    /** Returns a copy of this template with the last segment's value replaced by {@code *}, or this template if already anonymous. */
    public AddressTemplate anonymiseLast() {
        if (isEmpty()) {
            return root();
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

    /** @return an unmodifiable list of segments in this address template */
    public List<Segment> segments() {
        return unmodifiableList(segments);
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    /** @return a sanitized, DOM-safe identifier derived from this template, suitable for use as an HTML element ID */
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

    private static LinkedList<Segment> parseSegments(String template) {
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
                                segments.add(new Segment(key, org.jboss.hal.dmr.ValueEncoder.decode(value)));
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
