# Attribute Group Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display WildFly management model attribute groups in HAL's resource views and forms, with a toolbar toggle for flat vs grouped layout.

**Architecture:** Strategy-based rendering inside `ResourceData.load()`. A boolean `grouped` flag on `ResourceData` controls whether items are added flat (current behavior) or wrapped in `FormFieldGroup` (form) / `ExpandableSection` (view). The flag survives view/edit transitions and is toggled from the toolbar.

**Tech Stack:** PatternFly Java (`FormFieldGroup`, `FormFieldGroupHeader`, `FormFieldGroupBody`, `ExpandableSection`, `ExpandableSectionToggle`, `ExpandableSectionContent`), Elemento, `Humanize.capitalCase()` for group titles.

## Global Constraints

- Java 21, compiled via J2CL — no reflection, no `java.util.stream.Collectors.groupingBy`
- PatternFly Java 0.9.3 components only
- All new code follows existing HAL patterns: factory methods, `static import` style, no `new` in calling code
- Immutable data — `ResourceAttribute` fields are `final`; grouping produces new collections, never mutates
- Group title via `Humanize.capitalCase()` (already exists in codebase)
- `modifier(filtered)` CSS class used for filter hide/show (existing pattern)

---

### Task 1: Fix `ResourceAttribute.grouped()` ordering and add `hasGroups()`

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/ResourceAttribute.java:120-145`

**Interfaces:**
- Produces:
  - `static Map<String, List<ResourceAttribute>> grouped(List<ResourceAttribute> attributes)` — returns ungrouped first (key `"ungrouped"`), then named groups a-z
  - `static boolean hasGroups(List<ResourceAttribute> attributes)` — returns `true` if any attribute has a non-null `group`

- [ ] **Step 1: Fix `grouped()` ordering — ungrouped first**

Replace the `grouped()` method body in `ResourceAttribute.java` (lines 126-144):

```java
public static Map<String, List<ResourceAttribute>> grouped(List<ResourceAttribute> attributes) {
    List<ResourceAttribute> ungrouped = new ArrayList<>();
    TreeMap<String, List<ResourceAttribute>> groups = new TreeMap<>();
    for (ResourceAttribute attribute : attributes) {
        if (attribute.group == null) {
            ungrouped.add(attribute);
        } else {
            groups.computeIfAbsent(attribute.group, k -> new ArrayList<>()).add(attribute);
        }
    }
    LinkedHashMap<String, List<ResourceAttribute>> result = new LinkedHashMap<>();
    if (!ungrouped.isEmpty()) {
        result.put("ungrouped", ungrouped);
    }
    result.putAll(groups);
    return result;
}
```

- [ ] **Step 2: Add `hasGroups()` method**

Add after the `grouped()` method:

```java
/** Returns {@code true} if any attribute in the list has a non-null attribute group. */
public static boolean hasGroups(List<ResourceAttribute> attributes) {
    for (ResourceAttribute attribute : attributes) {
        if (attribute.group != null) {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 3: Remove the TODO comment**

Remove lines 124-125:

```java
// TODO Make use of attribute groups and turn them into form field groups
//  https://www.patternfly.org/components/forms/form/design-guidelines#field-groups
```

- [ ] **Step 4: Build and verify**

Run: `mvn compile -pl ui -P op`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/ResourceAttribute.java
git commit -m "feat: fix grouped() ordering and add hasGroups() to ResourceAttribute"
```

---

### Task 2: Add `ResourceView` constructor accepting external `DescriptionList`

The grouped view mode creates multiple `DescriptionList` instances (one per group) placed inside `ExpandableSection` containers. `ResourceView` currently creates its own `DescriptionList` internally. A new package-private constructor lets grouped rendering supply an external `DescriptionList`.

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/view/ResourceView.java:103-113`

**Interfaces:**
- Produces: `ResourceView(DescriptionList dl)` — package-private constructor wrapping an external description list

- [ ] **Step 1: Add new constructor**

Add a package-private constructor after the existing `public ResourceView()` constructor (after line 113):

```java
ResourceView(DescriptionList dl) {
    this.items = new LinkedHashMap<>();
    this.aur = new AurHandler<>(this);
    this.dl = dl;
}
```

- [ ] **Step 2: Build and verify**

Run: `mvn compile -pl ui -P op`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/view/ResourceView.java
git commit -m "feat: add ResourceView constructor accepting external DescriptionList"
```

---

### Task 3: Add `addFieldGroup()` to `ResourceForm`

`ResourceForm` currently only accepts `FormItem` (which wraps `FormGroup`). For grouped rendering, it needs to also accept `FormFieldGroup` containers that hold the grouped `FormGroup` elements visually. Items are still tracked in the flat `items` map for validation and data operations.

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/form/ResourceForm.java:90-95`

**Interfaces:**
- Consumes: `FormFieldGroup` from PatternFly Java
- Produces: `ResourceForm addFieldGroup(FormFieldGroup fieldGroup)` — adds a field group container to the form's DOM

- [ ] **Step 1: Add import**

Add to `ResourceForm.java`:

```java
import org.patternfly.component.form.FormFieldGroup;
```

- [ ] **Step 2: Add `addFieldGroup()` method**

Add in the `// ------------------------------------------------------ add` section, after the `add(FormItem)` method:

```java
public ResourceForm addFieldGroup(FormFieldGroup fieldGroup) {
    form.addFieldGroup(fieldGroup);
    return this;
}
```

- [ ] **Step 3: Build and verify**

Run: `mvn compile -pl ui -P op`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/form/ResourceForm.java
git commit -m "feat: add addFieldGroup() to ResourceForm"
```

---

### Task 4: Add grouped flag to `ResourceData` and wire grouped rendering

This is the core task. It adds the `grouped` boolean to `ResourceData`, modifies `load()` to support grouped layout for both VIEW and EDIT states, and updates `onFilterChanged()` to hide empty group containers.

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/data/ResourceData.java`

**Interfaces:**
- Consumes:
  - `ResourceAttribute.grouped(List<ResourceAttribute>)` from Task 1
  - `ResourceAttribute.hasGroups(List<ResourceAttribute>)` from Task 1
  - `ResourceView(DescriptionList)` from Task 2
  - `ResourceForm.addFieldGroup(FormFieldGroup)` from Task 3
  - `Humanize.capitalCase(String)` (existing in codebase)
- Produces:
  - `void toggleGrouped()` — toggles the `grouped` flag and reloads the current state
  - `boolean isGrouped()` — returns current grouped flag value
  - `boolean supportsGrouping()` — returns whether current resource has attribute groups

- [ ] **Step 1: Add imports**

Add these imports to `ResourceData.java`:

```java
import java.util.Map;

import org.jboss.hal.ui.resource.form.FormItem;
import org.patternfly.component.expandable.ExpandableSection;
import org.patternfly.component.form.FormFieldGroup;
import org.patternfly.component.form.FormFieldGroupBody;
import org.patternfly.component.list.DescriptionList;

import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.ui.resource.ResourceAttribute.hasGroups;
import static org.jboss.hal.ui.resource.ResourceAttribute.grouped;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.form.FormFieldGroup.formFieldGroup;
import static org.patternfly.component.form.FormFieldGroupBody.formFieldGroupBody;
import static org.patternfly.component.form.FormFieldGroupHeader.formFieldGroupHeader;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;
```

- [ ] **Step 2: Add instance fields**

Add after the existing `private boolean inlineEdit;` field (line 129):

```java
private boolean grouped;
private boolean supportsGrouping;
private List<ResourceItem<?>> allItems;
private List<HTMLElement> groupContainers;
```

- [ ] **Step 3: Initialize fields in constructor**

In the constructor, after `this.inlineEdit = false;` (line 143), add:

```java
this.grouped = false;
this.supportsGrouping = false;
this.allItems = new ArrayList<>();
this.groupContainers = new ArrayList<>();
```

- [ ] **Step 4: Modify `load()` — VIEW branch for grouped rendering**

Replace the VIEW branch inside `load()` (lines 213-219) with:

```java
if (state == VIEW) {
    allItems.clear();
    groupContainers.clear();
    supportsGrouping = hasGroups(resourceAttributes);
    if (grouped && supportsGrouping) {
        HTMLContainerBuilder<HTMLDivElement> viewContainer = div();
        Map<String, List<ResourceAttribute>> groups = grouped(resourceAttributes);
        for (Map.Entry<String, List<ResourceAttribute>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<ResourceAttribute> groupAttributes = entry.getValue();
            DescriptionList dl = descriptionList()
                    .orientation(breakpoints(
                            sm, vertical,
                            md, horizontal,
                            lg, horizontal,
                            xl, horizontal,
                            _2xl, horizontal));
            for (ResourceAttribute ra : groupAttributes) {
                ViewItem vi = viewItem(template, metadata, ra);
                dl.addItem(vi.descriptionListGroup);
                allItems.add(vi);
            }
            if ("ungrouped".equals(groupName)) {
                viewContainer.add(dl);
            } else {
                ExpandableSection es = expandableSection()
                        .addToggle(expandableSectionToggle(capitalCase(groupName)))
                        .addContent(expandableSectionContent().add(dl));
                es.expand();
                viewContainer.add(es);
                groupContainers.add(es.element());
            }
        }
        items = null;
        rootContainer.add(viewContainer);
    } else {
        ResourceView resourceView = new ResourceView();
        for (ResourceAttribute ra : resourceAttributes) {
            resourceView.addItem(viewItem(template, metadata, ra));
        }
        items = resourceView;
        rootContainer.add(items);
    }
```

Note: In the grouped VIEW branch, `ViewItem`s are added directly to the `DescriptionList` via their `descriptionListGroup` field (which is the `DescriptionListGroup` element) rather than through a `ResourceView` wrapper. The `allItems` list tracks all items for filtering. `items` is set to `null` since there's no single `HasItems` container.

- [ ] **Step 5: Modify `load()` — EDIT branch for grouped rendering**

Replace the EDIT branch inside `load()` (lines 221-227) with:

```java
} else if (state == EDIT) {
    allItems.clear();
    groupContainers.clear();
    supportsGrouping = hasGroups(resourceAttributes);
    resourceForm = new ResourceForm(template);
    if (grouped && supportsGrouping) {
        Map<String, List<ResourceAttribute>> groups = grouped(resourceAttributes);
        for (Map.Entry<String, List<ResourceAttribute>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<ResourceAttribute> groupAttributes = entry.getValue();
            if ("ungrouped".equals(groupName)) {
                for (ResourceAttribute ra : groupAttributes) {
                    FormItem fi = formItem(template, metadata, ra,
                            new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED));
                    resourceForm.addItem(fi);
                    allItems.add(fi);
                }
            } else {
                FormFieldGroupBody ffgBody = formFieldGroupBody();
                for (ResourceAttribute ra : groupAttributes) {
                    FormItem fi = formItem(template, metadata, ra,
                            new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED));
                    resourceForm.addItem(fi);
                    ffgBody.addGroup(fi.formGroup);
                    allItems.add(fi);
                }
                FormFieldGroup ffg = formFieldGroup(true)
                        .addHeader(formFieldGroupHeader(capitalCase(groupName)))
                        .addBody(ffgBody);
                resourceForm.addFieldGroup(ffg);
                groupContainers.add(ffg.element());
            }
        }
    } else {
        for (ResourceAttribute ra : resourceAttributes) {
            resourceForm.addItem(formItem(template, metadata, ra,
                    new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED)));
        }
    }
    items = resourceForm;
}
```

- [ ] **Step 6: Update `onFilterChanged()` for group-container visibility**

Replace the `onFilterChanged` method (lines 276-300) with:

```java
private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
    if ((state == VIEW || state == EDIT) && isAttached(element())) {
        logger.debug("Filter attributes: %s", filter);
        int matchingItems;
        Iterable<? extends ResourceItem<?>> filterItems;
        if (grouped && !allItems.isEmpty()) {
            filterItems = allItems;
        } else if (items != null) {
            filterItems = items;
        } else {
            filterItems = java.util.Collections.emptyList();
        }

        if (filter.defined()) {
            matchingItems = 0;
            for (ResourceItem<?> item : filterItems) {
                ResourceAttribute ra = item.resourceAttribute();
                if (ra != null) {
                    boolean match = filter.match(ra);
                    item.element().classList.toggle(modifier(filtered), !match);
                    if (match) {
                        matchingItems++;
                    }
                }
            }
            // Hide group containers where all items are filtered out
            for (HTMLElement container : groupContainers) {
                boolean hasVisibleItem = false;
                for (ResourceItem<?> item : filterItems) {
                    if (container.contains(item.element())
                            && !item.element().classList.contains(modifier(filtered))) {
                        hasVisibleItem = true;
                        break;
                    }
                }
                setVisible(container, hasVisibleItem);
            }
            toggle(noMatch, rootContainer.element(), matchingItems == 0);
        } else {
            matchingItems = total.get();
            toggle(noMatch, rootContainer.element(), false);
            for (ResourceItem<?> item : filterItems) {
                item.element().classList.remove(modifier(filtered));
            }
            for (HTMLElement container : groupContainers) {
                setVisible(container, true);
            }
        }
        visible.set(matchingItems);
    }
}
```

- [ ] **Step 7: Add toggle and query methods**

Add after the `cancel()` method in the `// ------------------------------------------------------ actions` section:

```java
void toggleGrouped() {
    grouped = !grouped;
    removeChildrenFrom(rootContainer);
    load(state);
}

boolean isGrouped() {
    return grouped;
}

boolean supportsGrouping() {
    return supportsGrouping;
}
```

- [ ] **Step 8: Clear `allItems` and `groupContainers` in `changeState()`**

In the `changeState()` method, after `removeChildrenFrom(rootContainer);` (line 351), add:

```java
allItems.clear();
groupContainers.clear();
```

- [ ] **Step 9: Add missing import for `ViewItem`**

Verify this import exists, add if missing:

```java
import org.jboss.hal.ui.resource.view.ViewItem;
```

- [ ] **Step 10: Build and verify**

Run: `mvn compile -pl ui -P op`
Expected: BUILD SUCCESS

- [ ] **Step 11: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/data/ResourceData.java
git commit -m "feat: add grouped rendering support to ResourceData"
```

---

### Task 5: Add grouped toggle to `ResourceDataToolbar`

Add a toggle button to the toolbar that switches between flat and grouped layout. The button is only visible when the resource has attribute groups. It appears in both view and edit action groups.

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/resource/data/ResourceDataToolbar.java`

**Interfaces:**
- Consumes: `ResourceData.toggleGrouped()`, `ResourceData.isGrouped()`, `ResourceData.supportsGrouping()` from Task 4

- [ ] **Step 1: Add imports**

Add to `ResourceDataToolbar.java`:

```java
import static org.patternfly.icon.IconSets.fas.layerGroup;
import static org.patternfly.icon.IconSets.fas.list;
```

- [ ] **Step 2: Add instance fields**

Add after the existing `private ToolbarItem editItem;` field (line 96):

```java
private final String groupToggleId;
private ToolbarItem groupToggleItem;
```

- [ ] **Step 3: Initialize `groupToggleId` in constructor**

In the constructor, after `this.editId = Id.unique("edit");` (line 102), add:

```java
this.groupToggleId = Id.unique("group-toggle");
```

- [ ] **Step 4: Add the toggle to `viewActionGroup()`**

In the `viewActionGroup()` method, insert before the `resetItem` assignment (line 177):

```java
groupToggleItem = toolbarItem()
        .add(button().id(groupToggleId).plain()
                .icon(resourceData.isGrouped() ? list() : layerGroup())
                .onClick((e, b) -> resourceData.toggleGrouped()))
        .add(tooltip(By.id(groupToggleId),
                resourceData.isGrouped() ? "Flat layout" : "Grouped layout"));
setVisible(groupToggleItem, resourceData.supportsGrouping());
```

Modify the `viewActionGroup` construction (line 192) to include the toggle:

```java
viewActionGroup = toolbarGroup(actionGroupPlain).css(modifier("align-right"))
        .addItem(groupToggleItem)
        .addItem(refreshItem)
        .addItem(resetItem)
        .addItem(editItem);
```

- [ ] **Step 5: Add toggle to `editActionGroup()`**

In the `editActionGroup()` method, insert before the `saveItem` assignment (line 200):

```java
groupToggleItem = toolbarItem()
        .add(button().id(groupToggleId).plain()
                .icon(resourceData.isGrouped() ? list() : layerGroup())
                .onClick((e, b) -> resourceData.toggleGrouped()))
        .add(tooltip(By.id(groupToggleId),
                resourceData.isGrouped() ? "Flat layout" : "Grouped layout"));
setVisible(groupToggleItem, resourceData.supportsGrouping());
```

Modify the `editActionGroup` construction (line 208) to include the toggle:

```java
editActionGroup = toolbarGroup(buttonGroup).css(modifier("align-right"))
        .addItem(groupToggleItem)
        .addItem(saveItem)
        .addItem(cancelItem);
```

- [ ] **Step 6: Build and verify**

Run: `mvn compile -pl ui -P op`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/data/ResourceDataToolbar.java
git commit -m "feat: add grouped layout toggle to ResourceDataToolbar"
```

---

### Task 6: Manual integration test

Test the grouped layout with a real WildFly server by navigating to a resource with attribute groups.

**Files:** None (manual testing)

- [ ] **Step 1: Build halOP**

Run: `mvn install -P op,standalone`

- [ ] **Step 2: Start a WildFly server**

Start a WildFly server that has the `messaging-activemq` subsystem configured (full profile). Start with `--stability=experimental`.

- [ ] **Step 3: Start halOP standalone and connect**

Run the built standalone server JAR, connect to the WildFly instance.

- [ ] **Step 4: Test grouped layout**

Navigate to `messaging-activemq/server=default` in the model browser. This resource has 10+ attribute groups (journal, security, cluster, debug, management, statistics, transaction, etc.).

Verify:
1. The toggle button appears in the toolbar (layerGroup icon)
2. Clicking it switches to grouped view — ungrouped attributes on top, then named groups as expandable sections
3. Each group title is humanized (e.g., "Journal", "Security", "Cluster")
4. Groups are expanded by default and can be collapsed
5. Switching to edit mode preserves the grouped flag
6. In edit mode, groups appear as `FormFieldGroup` with headers
7. Filtering hides individual items and hides empty group containers
8. Clicking the toggle again returns to flat layout
9. Resources without groups (e.g., `subsystem=io`) don't show the toggle

- [ ] **Step 5: Test edge cases**

1. Toggle grouped in view mode → switch to edit → verify grouped persists
2. Toggle grouped in edit mode → switch to view → verify grouped persists
3. Apply a filter that hides all items in one group → verify group container hides
4. Clear the filter → verify all groups reappear
5. Collapse a group, apply a filter matching items inside → verify item count is correct
6. Navigate to a different resource and back → verify default is flat

- [ ] **Step 6: Commit any fixes**

If any issues are found during testing, fix and commit individually.
