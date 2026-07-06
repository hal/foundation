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
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateResolverTest {

    @Test
    void applySingle() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/b=c");
        TemplateResolver a = resolver("a");
        AddressTemplate result = template.apply(a);
        assertEquals("/a=a/b=c", result.template);
    }

    @Test
    void applyChain() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/{b}/{c}");
        AddressTemplate result = template
                .apply(resolver("a"))
                .apply(resolver("b"))
                .apply(resolver("c"));
        assertEquals("/a=a/b=b/c=c", result.template);
    }

    @Test
    void applyPartialChain() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/{b}/{c}");
        AddressTemplate result = template
                .apply(resolver("a"))
                .apply(resolver("c"));
        assertEquals("/a=a/{b}/c=c", result.template);
    }

    @Test
    void applyOnRoot() {
        AddressTemplate result = AddressTemplate.root().apply(resolver("a"));
        assertEquals("/", result.template);
    }

    @Test
    void applyDoesNotMutate() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/b=c");
        template.apply(resolver("a"));
        assertEquals("/{a}/b=c", template.template);
    }

    @Test
    void resolve() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/{b}/{c}");

        TemplateResolver a = resolver("a");
        TemplateResolver b = resolver("b");
        TemplateResolver c = resolver("c");

        TemplateResolver aa = a.andThen(a);
        TemplateResolver aaa = a.andThen(a).andThen(a);

        TemplateResolver ab = a.andThen(b);
        TemplateResolver ac = a.andThen(c);
        TemplateResolver abc = a.andThen(b).andThen(c);
        TemplateResolver acb = a.andThen(c).andThen(b);

        TemplateResolver ba = b.andThen(a);
        TemplateResolver bc = b.andThen(c);
        TemplateResolver bac = b.andThen(a).andThen(c);
        TemplateResolver bca = b.andThen(c).andThen(a);

        TemplateResolver ca = c.andThen(a);
        TemplateResolver cb = c.andThen(b);
        TemplateResolver cab = c.andThen(a).andThen(b);
        TemplateResolver cba = c.andThen(b).andThen(a);

        assertEquals("/a=a/{b}/{c}", aa.resolve(template).template);
        assertEquals("/a=a/{b}/{c}", aaa.resolve(template).template);

        assertEquals("/a=a/{b}/{c}", a.resolve(template).template);
        assertEquals("/a=a/b=b/{c}", ab.resolve(template).template);
        assertEquals("/a=a/{b}/c=c", ac.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", abc.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", acb.resolve(template).template);

        assertEquals("/{a}/b=b/{c}", b.resolve(template).template);
        assertEquals("/a=a/b=b/{c}", ba.resolve(template).template);
        assertEquals("/{a}/b=b/c=c", bc.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", bac.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", bca.resolve(template).template);

        assertEquals("/{a}/{b}/c=c", c.resolve(template).template);
        assertEquals("/a=a/{b}/c=c", ca.resolve(template).template);
        assertEquals("/{a}/b=b/c=c", cb.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", cab.resolve(template).template);
        assertEquals("/a=a/b=b/c=c", cba.resolve(template).template);
    }

    private TemplateResolver resolver(String character) {
        return template -> {
            List<Segment> resolved = new ArrayList<>();
            for (Segment segment : template) {
                if (segment.containsPlaceholder() && segment.placeholder().equals(new Placeholder(character, null, false))) {
                    resolved.add(new Segment(character, character));
                } else {
                    resolved.add(segment);
                }
            }
            return AddressTemplate.of(resolved);
        };
    }
}
