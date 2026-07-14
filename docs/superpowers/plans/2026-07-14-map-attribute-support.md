# Map Attribute Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add pipeline support for free-form key-value map attributes (222 occurrences in WildFly 40), rendering them as Label Group chips in the view and providing a Text Input Group editor in the form.

**Architecture:** A new `MapMatcher` detects OBJECT attributes with simple VALUE_TYPE (like STRING) and a new `MapProvider` creates `MapViewItem` (Label Group) and `MapFormItem` (FilterInput with `key=value` parsing). Save uses granular `map-put`/`map-remove` DMR operations.

**Tech Stack:** Java (J2CL), PatternFly Java (Label, LabelGroup, FilterInput), DMR operations, JUnit 5

## Global Constraints

- All new classes are package-private (no `public` modifier) unless they need to be referenced from outside the package
- Follow existing pipeline patterns exactly (see `CredentialReferenceMatcher`/`CredentialReferenceProvider` as the reference implementation)
- Immutable data patterns — never mutate input collections
- Apache 2.0 license headers on all new files (copy from any existing file)
- 4-space indent, UTF-8, max line length 128, LF line endings

---

## File Map

### New files

| File | Responsibility |
|------|---------------|
| `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapMatcher.java` | Claims OBJECT attributes with simple VALUE_TYPE |
| `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapProvider.java` | Creates MapViewItem / MapFormItem for matched groups |
| `ui/src/main/java/org/jboss/hal/ui/resource/view/MapViewItem.java` | Read-only Label Group rendering of key=value entries |
| `ui/src/main/java/org/jboss/hal/ui/resource/form/MapFormItem.java` | FilterInput + Label Group editing with change tracking |
| `ui/src/test/java/org/jboss/hal/ui/resource/pipeline/MapMatcherTest.java` | Matcher unit tests |

### Modified files

| File | Change |
|------|--------|
| `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/AttributeMatcher.java` | Add `hasSimpleValueType()` static helper |
| `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/Pipeline.java` | Register MapMatcher and MapProvider |
| `ui/src/test/java/org/jboss/hal/ui/resource/pipeline/TestData.java` | Add `mapAttribute()` factory method |
| `dmr/src/main/java/org/jboss/hal/dmr/ModelDescriptionConstants.java` | Add `KEY` constant |
| `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/COVERAGE.md` | Update coverage numbers |

---

### Task 1: Detection — `hasSimpleValueType()` helper and `MapMatcher`

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/AttributeMatcher.java`
- Modify: `ui/src/test/java/org/jboss/hal/ui/resource/pipeline/TestData.java`
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapMatcher.java`
- Create: `ui/src/test/java/org/jboss/hal/ui/resource/pipeline/MapMatcherTest.java`

**Interfaces:**
- Consumes: `AttributeMatcher.partition()`, `AttributeDescription`, `ModelType`, `ModelDescriptionConstants.TYPE`, `ModelDescriptionConstants.VALUE_TYPE`
- Produces: `AttributeMatcher.hasSimpleValueType(AttributeDescription)` — static method returning boolean; `MapMatcher` — implements `AttributeMatcher`; `TestData.mapAttribute(String name)` — factory method for tests

- [ ] **Step 1: Add `mapAttribute()` factory method to `TestData.java`**

Add this method to `TestData.java` after the existing `simpleRecordObject` method:

```java
static AttributeDescription mapAttribute(String name) {
    ModelNode desc = new ModelNode();
    desc.get(TYPE).set("OBJECT");
    desc.get(VALUE_TYPE).set("STRING");
    return new AttributeDescription(new Property(name, desc));
}
```

This creates an OBJECT attribute whose VALUE_TYPE is the simple type `STRING` — exactly how map attributes appear in the WildFly management model. Note the difference from `simpleRecordObject()` which sets `VALUE_TYPE` to an OBJECT with named sub-attributes.

- [ ] **Step 2: Write the `MapMatcherTest`**

Create `ui/src/test/java/org/jboss/hal/ui/resource/pipeline/MapMatcherTest.java`:

```java
package org.jboss.hal.ui.resource.pipeline;

import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.pipeline.AttributeMatcher.MatchResult;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.ui.resource.pipeline.TestData.credentialReference;
import static org.jboss.hal.ui.resource.pipeline.TestData.mapAttribute;
import static org.jboss.hal.ui.resource.pipeline.TestData.pool;
import static org.jboss.hal.ui.resource.pipeline.TestData.simpleRecordObject;
import static org.jboss.hal.ui.resource.pipeline.TestData.stringAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapMatcherTest {

    private final MapMatcher matcher = new MapMatcher();

    @Test
    void claimsMapAttribute() {
        List<AttributeDescription> pool = pool(
                stringAttribute("name"),
                mapAttribute("properties"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        assertEquals("properties", result.groups().get(0).primary().name());
        assertEquals(2, result.remaining().size());
    }

    @Test
    void claimsMultipleMapAttributes() {
        List<AttributeDescription> pool = pool(
                mapAttribute("properties"),
                mapAttribute("params"),
                stringAttribute("jndi-name"));
        MatchResult result = matcher.match(pool);

        assertEquals(2, result.groups().size());
        assertEquals(1, result.remaining().size());
        assertEquals("jndi-name", result.remaining().get(0).name());
    }

    @Test
    void ignoresStructuredObject() {
        List<AttributeDescription> pool = pool(
                credentialReference("credential-reference"),
                simpleRecordObject("file", "path", "relative-to"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertTrue(result.groups().isEmpty());
        assertEquals(3, result.remaining().size());
    }

    @Test
    void ignoresSimpleTypes() {
        List<AttributeDescription> pool = pool(
                stringAttribute("name"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertTrue(result.groups().isEmpty());
        assertEquals(2, result.remaining().size());
    }

    @Test
    void emptyPool() {
        MatchResult result = matcher.match(pool());

        assertTrue(result.groups().isEmpty());
        assertTrue(result.remaining().isEmpty());
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn test -pl ui -Dtest=MapMatcherTest -P quick-build`
Expected: Compilation failure — `MapMatcher` and `hasSimpleValueType` do not exist yet.

- [ ] **Step 4: Add `hasSimpleValueType()` to `AttributeMatcher.java`**

Add this static method after the existing `hasObjectValueType` method (around line 106):

```java
/**
 * Tests whether the given attribute is an OBJECT whose VALUE_TYPE is a simple scalar type (STRING, INT, LONG, etc.).
 * This identifies free-form key-value map attributes — the complement of {@link #hasObjectValueType}.
 */
static boolean hasSimpleValueType(AttributeDescription description) {
    try {
        ModelType type = description.get(TYPE).asType();
        if (type != ModelType.OBJECT) {
            return false;
        }
        if (!description.hasDefined(VALUE_TYPE)) {
            return false;
        }
        ModelNode valueTypeNode = description.get(VALUE_TYPE);
        if (valueTypeNode.getType() != ModelType.TYPE) {
            return false;
        }
        return valueTypeNode.asType().simple();
    } catch (IllegalArgumentException e) {
        return false;
    }
}
```

- [ ] **Step 5: Create `MapMatcher.java`**

Create `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapMatcher.java`:

```java
package org.jboss.hal.ui.resource.pipeline;

import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasSimpleValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.partition;

/**
 * Matcher that claims OBJECT attributes with a simple VALUE_TYPE (STRING, INT, LONG, etc.) — free-form key-value maps.
 * These are arbitrary {@code {String → String}} maps like {@code properties}, {@code params}, {@code configuration}, etc.
 * <p>
 * Must run after all composite matchers (which claim known structured OBJECTs) and before {@link FlatteningProvider}
 * (which handles simpleRecord OBJECTs with structured VALUE_TYPE).
 */
class MapMatcher implements AttributeMatcher {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasSimpleValueType(ad));
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `mvn test -pl ui -Dtest=MapMatcherTest -P quick-build`
Expected: All 5 tests PASS.

- [ ] **Step 7: Run existing matcher tests to verify no regressions**

Run: `mvn test -pl ui -Dtest="CredentialReferenceMatcherTest,FileMatcherTest,PathRelativeToMatcherTest,TimeUnitMatcherTest,MapMatcherTest" -P quick-build`
Expected: All tests PASS.

- [ ] **Step 8: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/pipeline/AttributeMatcher.java \
       ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapMatcher.java \
       ui/src/test/java/org/jboss/hal/ui/resource/pipeline/MapMatcherTest.java \
       ui/src/test/java/org/jboss/hal/ui/resource/pipeline/TestData.java
git commit -m "feat: add MapMatcher for free-form key-value map attributes"
```

---

### Task 2: View — `MapViewItem`

**Files:**
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/view/MapViewItem.java`

**Interfaces:**
- Consumes: `AbstractViewItem`, `ResolvedAttribute`, `PipelineContext`, PatternFly `Label`, `LabelGroup`
- Produces: `MapViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context)` — constructor matching existing view item pattern

- [ ] **Step 1: Create `MapViewItem.java`**

Create `ui/src/main/java/org/jboss/hal/ui/resource/view/MapViewItem.java`:

```java
package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.hal.dmr.Property;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.style.Color.grey;

/**
 * View item for free-form key-value map attributes. Renders the map entries as a compact PatternFly Label Group
 * with {@code key=value} labels.
 */
public class MapViewItem extends AbstractViewItem {

    public MapViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        return labelGroup()
                .numLabels(5)
                .addItems(attribute.value().asPropertyList(),
                        (Property entry) -> label(entry.getName() + "=" + entry.getValue().asString(), grey)
                                .compact())
                .element();
    }
}
```

The `numLabels(5)` shows the first 5 entries with a "+N more" collapse for larger maps. Each entry is rendered as a compact grey label with `key=value` text.

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl ui -P op`
Expected: Compilation succeeds. (No unit test for view items — they require DOM/browser; verified via integration test later.)

- [ ] **Step 3: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/view/MapViewItem.java
git commit -m "feat: add MapViewItem for read-only key-value map display"
```

---

### Task 3: Form — `MapFormItem`

**Files:**
- Modify: `dmr/src/main/java/org/jboss/hal/dmr/ModelDescriptionConstants.java`
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/form/MapFormItem.java`

**Interfaces:**
- Consumes: `AbstractFormItem`, `ResolvedAttribute`, `PipelineContext`, `FilterInput`, `ModelDescriptionConstants.MAP_PUT_OPERATION`, `ModelDescriptionConstants.MAP_REMOVE_OPERATION`, `ModelDescriptionConstants.KEY`, `ModelDescriptionConstants.NAME`, `ModelDescriptionConstants.VALUE`
- Produces: `MapFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context)` — constructor matching existing form item pattern. Overrides `operations(ResourceAddress)` to produce `map-put`/`map-remove` instead of `write-attribute`.

- [ ] **Step 1: Add `KEY` constant to `ModelDescriptionConstants.java`**

Add this constant in alphabetical order (around the K section):

```java
String KEY = "key";
```

- [ ] **Step 2: Create `MapFormItem.java`**

Create `ui/src/main/java/org/jboss/hal/ui/resource/form/MapFormItem.java`:

```java
package org.jboss.hal.ui.resource.form;

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_PUT_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/**
 * Form item for free-form key-value map attributes. Uses a {@link FilterInput} with {@code key=value} parsing.
 * Each entry appears as a removable label. Produces granular {@code map-put} / {@code map-remove} DMR operations
 * for changed entries.
 */
public class MapFormItem extends AbstractFormItem {

    private final Map<String, String> originalEntries;
    private /*final*/ FilterInput filterInput;

    public MapFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        this.originalEntries = new LinkedHashMap<>();
        if (attribute.value().isDefined()) {
            for (Property property : attribute.value().asPropertyList()) {
                originalEntries.put(property.getName(), property.getValue().asString());
            }
        }
        defaultSetup();
    }

    // ------------------------------------------------------ setup

    @Override
    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(mapControl());
    }

    @Override
    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = inputGroup()
                    .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                    .addItem(inputGroupItem().fill().add(mapControl()))
                    .element();
        }
        return nativeContainer;
    }

    private FilterInput mapControl() {
        filterInput = filterInput(identifier)
                .applyTo(inputElement -> {
                    inputElement.autocomplete("off");
                    inputElement.placeholder("key=value");
                })
                .textToLabel(this::parseKeyValue)
                .allowDuplicates(false);
        if (attribute.value().isDefined()) {
            setLabels(originalEntries);
        } else if (attribute.description().nillable()) {
            filterInput.placeholder(UNDEFINED);
        }
        return filterInput;
    }

    private Label parseKeyValue(String text) {
        int eqIndex = text.indexOf('=');
        if (eqIndex <= 0) {
            return null;
        }
        String key = text.substring(0, eqIndex);
        String value = text.substring(eqIndex + 1);
        if (value.isEmpty()) {
            return null;
        }
        return Label.label(key + "=" + value).compact().closable();
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        if (filterInput != null) {
            filterInput.resetValidation();
        }
    }

    @Override
    public boolean validate() {
        if (requiredOnItsOwn()) {
            if (inputMode == InputMode.NATIVE) {
                if (currentEntries().isEmpty()) {
                    filterInput.validated(error);
                    formGroupControl.addHelperText(requiredHelperText());
                    return false;
                }
            }
        } else if (inputMode == InputMode.EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ modification tracking

    @Override
    boolean isNativeModifiedForNew() {
        return !currentEntries().isEmpty();
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        Map<String, String> current = currentEntries();
        if (wasDefined) {
            return !originalEntries.equals(current);
        } else {
            return !current.isEmpty();
        }
    }

    // ------------------------------------------------------ data

    @Override
    public ModelNode modelNode() {
        if (inputMode == InputMode.NATIVE) {
            Map<String, String> entries = currentEntries();
            if (entries.isEmpty()) {
                return new ModelNode();
            }
            ModelNode modelNode = new ModelNode();
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                modelNode.get(entry.getKey()).set(entry.getValue());
            }
            return modelNode;
        } else if (inputMode == InputMode.EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ operations

    @Override
    public List<Operation> operations(ResourceAddress address) {
        if (!isModified()) {
            return java.util.Collections.emptyList();
        }
        Map<String, String> current = currentEntries();
        List<Operation> operations = new ArrayList<>();

        // Entries to remove: in original but not in current
        for (String key : originalEntries.keySet()) {
            if (!current.containsKey(key)) {
                operations.add(new Operation.Builder(address, MAP_REMOVE_OPERATION)
                        .param(NAME, attribute.name())
                        .param(KEY, key)
                        .build());
            }
        }

        // Entries to add or update: in current but not in original, or value changed
        for (Map.Entry<String, String> entry : current.entrySet()) {
            String originalValue = originalEntries.get(entry.getKey());
            if (originalValue == null || !originalValue.equals(entry.getValue())) {
                operations.add(new Operation.Builder(address, MAP_PUT_OPERATION)
                        .param(NAME, attribute.name())
                        .param(KEY, entry.getKey())
                        .param(VALUE, entry.getValue())
                        .build());
            }
        }

        return operations;
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToNativeMode() {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setLabels(originalEntries);
        } else if (attribute.description().nillable()) {
            filterInput.placeholder(UNDEFINED);
        }
    }

    // ------------------------------------------------------ internal

    private void setLabels(Map<String, String> entries) {
        filterInput.labelGroup().clear();
        List<String> labelTexts = entries.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(toList());
        filterInput.labelGroup().addItems(labelTexts,
                text -> filterInput.textToLabel().apply(text));
    }

    private Map<String, String> currentEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        for (Label label : filterInput.labelGroup().items()) {
            String text = label.text();
            int eqIndex = text.indexOf('=');
            if (eqIndex > 0) {
                entries.put(text.substring(0, eqIndex), text.substring(eqIndex + 1));
            }
        }
        return entries;
    }
}
```

Key design decisions:
- `originalEntries` snapshot taken at construction time for change tracking
- `parseKeyValue()` returns `null` for invalid input (no `=`, empty key, empty value), which `FilterInput` treats as "reject the input"
- `operations()` produces granular `map-put`/`map-remove` instead of `write-attribute`
- `currentEntries()` parses the label texts back into a map for comparison

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl dmr,ui -P op`
Expected: Compilation succeeds.

- [ ] **Step 4: Commit**

```bash
git add dmr/src/main/java/org/jboss/hal/dmr/ModelDescriptionConstants.java \
       ui/src/main/java/org/jboss/hal/ui/resource/form/MapFormItem.java
git commit -m "feat: add MapFormItem with key=value parsing and map-put/map-remove operations"
```

---

### Task 4: Pipeline Integration — `MapProvider` and Registration

**Files:**
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapProvider.java`
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/Pipeline.java`
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/COVERAGE.md`

**Interfaces:**
- Consumes: `ItemProvider`, `AttributeMatch`, `PipelineContext`, `ResolvedAttribute.resolve()`, `MapViewItem`, `MapFormItem`, `AttributeMatcher.hasSimpleValueType()`
- Produces: `MapProvider` — implements `ItemProvider`; pipeline integration complete

- [ ] **Step 1: Create `MapProvider.java`**

Create `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapProvider.java`:

```java
package org.jboss.hal.ui.resource.pipeline;

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.MapFormItem;
import org.jboss.hal.ui.resource.view.MapViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasSimpleValueType;

/**
 * Provider for free-form key-value map attributes. Matches OBJECT attributes with a simple VALUE_TYPE
 * (detected by {@link MapMatcher} in stage 1). Creates {@link MapViewItem} for read-only display
 * and {@link MapFormItem} for editing.
 */
class MapProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeMatch group) {
        return group.isSingle() && hasSimpleValueType(group.primary());
    }

    @Override
    public List<ViewItem> viewItems(AttributeMatch group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new MapViewItem(ra.fqn(), ra, context));
    }

    @Override
    public List<FormItem> formItems(AttributeMatch group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new MapFormItem(ra.fqn(), ra, context));
    }
}
```

- [ ] **Step 2: Register `MapMatcher` and `MapProvider` in `Pipeline.create()`**

In `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/Pipeline.java`, update the `create()` method.

Add `MapMatcher` as the last matcher (after `PathRelativeToMatcher`):

```java
List<AttributeMatcher> matchers = List.of(
        new CredentialReferenceMatcher(),
        new TimeUnitMatcher(),
        new FileMatcher(),
        new PathRelativeToMatcher(),
        new MapMatcher()
);
```

Add `MapProvider` after `RelativeToProvider` and before `FlatteningProvider`:

```java
List<ItemProvider> providers = List.of(
        new CredentialReferenceProvider(),
        new TimeUnitProvider(),
        new FileProvider(),
        new PathRelativeToProvider(),
        new RelativeToProvider(),
        new MapProvider(),
        new FlatteningProvider(),
        new DefaultItemProvider()
);
```

Update the Javadoc for the `create()` method to mention map:

```java
/**
 * Creates a pipeline with all matchers and providers registered in the correct priority order.
 * <p>
 * Matcher order (stage 1): composite matchers first (credential-reference, time-unit, file), then sibling matchers
 * (path+relative-to), then map matcher (free-form key-value maps).
 * <p>
 * Provider order (stage 2): specific providers first (credential-reference, time-unit, file, path+relative-to, standalone
 * relative-to, map), then flattening for simpleRecord OBJECTs, then the default catch-all.
 */
```

- [ ] **Step 3: Update `COVERAGE.md`**

Replace the "Not yet covered" section header and the "Free-form key-value maps" subsection in `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/COVERAGE.md`:

In the "Covered" table, add a row:

```
| Free-form key-value maps | 222 | MapMatcher + MapProvider |
```

Update the total covered count from `5,165` to `5,387` and percentage from `89%` to `~93%`.

In the "Not yet covered" section, update the count from `638` to `~416` and percentage from `11%` to `~7%`. Remove the entire "1. Free-form key-value maps" subsection. Renumber the remaining subsections (LIST of OBJECT becomes 1, Complex/recursive OBJECTs becomes 2).

Update the "Coverage by storage type" table proportionally.

- [ ] **Step 4: Verify full compilation**

Run: `mvn compile -P op`
Expected: Compilation succeeds.

- [ ] **Step 5: Run all pipeline tests**

Run: `mvn test -pl ui -Dtest="CredentialReferenceMatcherTest,FileMatcherTest,PathRelativeToMatcherTest,TimeUnitMatcherTest,MapMatcherTest" -P quick-build`
Expected: All tests PASS.

- [ ] **Step 6: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/pipeline/MapProvider.java \
       ui/src/main/java/org/jboss/hal/ui/resource/pipeline/Pipeline.java \
       ui/src/main/java/org/jboss/hal/ui/resource/pipeline/COVERAGE.md
git commit -m "feat: integrate MapProvider into pipeline for key-value map attribute support"
```

---

### Task 5: Verify full build and manual testing

**Files:**
- No new files

**Interfaces:**
- Consumes: Everything from Tasks 1–4

- [ ] **Step 1: Run full build**

Run: `mvn clean verify -P op`
Expected: BUILD SUCCESS. All existing tests pass, no compilation errors.

- [ ] **Step 2: Start development mode for manual testing**

Terminal 1: `mvn j2cl:watch -P op` — wait for "Build Complete: ready for browser refresh"
Terminal 2: `cd op/console && pnpm run watch` — opens browser at http://localhost:1234

- [ ] **Step 3: Manual verification — View**

Navigate to a resource with map attributes. Good candidates:
- **JGroups protocol**: Configuration → JGroups → Stack → default → Protocol → any protocol → look for `Properties`
- **Datasource properties**: Configuration → Datasources → any datasource → look for `*-properties` attributes

Verify:
- Map entries appear as compact grey `key=value` labels
- Empty/undefined maps show "undefined"
- Labels overflow/collapse with "+N more" for maps with many entries

- [ ] **Step 4: Manual verification — Form**

Switch to edit mode on a resource with map attributes.

Verify:
- Text input shows `key=value` placeholder
- Typing `foo=bar` and pressing Enter creates a removable label
- Typing `foo` (no `=`) is rejected
- Typing `=bar` (empty key) is rejected
- Typing `foo=` (empty value) is rejected
- Typing `url=jdbc:h2:mem:test;MODE=MySQL` creates a label with the full value (split on first `=` only)
- Clicking the close button on a label removes it
- Saving creates granular `map-put`/`map-remove` operations (check browser dev tools Network tab for the DMR requests)

- [ ] **Step 5: Commit the spec and plan docs**

```bash
git add docs/superpowers/specs/2026-07-14-map-attribute-support-design.md \
       docs/superpowers/plans/2026-07-14-map-attribute-support.md
git commit -m "docs: add design spec and implementation plan for map attribute support"
```
