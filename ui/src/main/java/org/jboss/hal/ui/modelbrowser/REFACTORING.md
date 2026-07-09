# Model Browser ↔ Resource Components Refactoring

## Context

The `ui/resource/` package provides reusable, composable components for rendering WildFly management resources. The model browser previously had its own tightly coupled duplicates. This refactoring eliminated most of that duplication so there's a single source of truth for L&F.

## What was done

### Deleted files

- `modelbrowser/ResourceDetails.java` — replaced by `resource/ResourceTabs`
- `modelbrowser/ResourceList.java` — replaced by `resource/ResourceList`

### Generalized resource components

| Component | What was added | Why |
|---|---|---|
| `ResourceBreadcrumb` | `SegmentHandler` with depth info, fluent `onSegmentClick()` | Model browser needs per-segment navigation based on depth relative to tree root |
| `ResourceHeader` | `customTitle(String\|HTMLElement)`, `showStability(boolean)`, `showDescription(boolean)`, lazy construction | Model browser shows "Child resources of `X`" for folders, suppresses stability/description for singleton folders |
| `ResourceTabs` | `initialSelection(String)`, `onSelect(BiConsumer)`, lazy construction | Model browser remembers last selected tab across resource selections |
| `ResourceList` | `missingChildren(List)`, wildcard template detection, `ChildResource` made public, OUIA support | Model browser uses `read-children-names` for folders (auto-detected from `=*` template); missing singletons provided externally from tree node data |
| `ResourceShell` | `contentCss(String...)` | Model browser needs `hal-c-model-browser__detail-content` CSS class on content section |

### Rewritten `ModelBrowserDetail`

Reduced from ~233 lines to ~140 lines. Now composes `ResourceShell` + `ResourceBreadcrumb` + `ResourceHeader` + `ResourceTabs`/`ResourceList` instead of building everything inline. Only model-browser-specific logic remains: breadcrumb navigation callbacks, custom folder titles, missing singleton computation.

## Remaining duplication

The remaining duplication is between `ModelBrowserEngine` (serves the **tree**) and `ResourceList` (serves the **detail panel**). They parse the same DMR results but produce different data structures for different consumers.

### DMR operation selection

- **`ModelBrowserEngine.readChildrenOperation()`** (lines 75-82): checks `ModelBrowserNode.Type` to choose `read-children-names` (folder) vs `read-children-types` (resource)
- **`ResourceList.load()`**: checks `wildcardTemplate` (`template.last().value.equals("*")`) to make the same choice

Same logic, different input: node type enum vs template pattern. Both are correct — the `*` wildcard in the template encodes the same information as `FOLDER`/`SINGLETON_FOLDER`.

### Child result parsing

- **`ModelBrowserEngine.parseChildren()`** (lines 107-152): parses DMR result into `ModelBrowserNode` objects with type classification (`SINGLETON_FOLDER`, `FOLDER`, `SINGLETON_RESOURCE`, `RESOURCE`), builds a tree structure with `children` lists
- **`ResourceList.parseChildNames()` + `parseChildTypes()`**: parses DMR result into flat `ChildResource` objects with a `singleton` flag

Same DMR results, different output shapes. Tree needs hierarchical `ModelBrowserNode` with type enums; list needs flat `ChildResource` with boolean flags.

### Missing singleton detection

- **`ModelBrowserEngine.parseChildren()`** (lines 140-150): compares `parent.children` (all possible singletons from tree building) with the parse result to detect non-existing singletons inline
- **`ResourceList.addMissingChildren()`**: receives pre-computed missing list via `missingChildren()` builder method, merges after loading

Same concept, different data flow. Tree does it inline during parse; list receives externally computed data from `ModelBrowserDetail.missingChildrenFor()`.

### Why this duplication remains

`ModelBrowserEngine` serves the tree component — it builds `ModelBrowserNode` objects that become `TreeViewItem`s. `ResourceList` serves the detail panel — it builds `ChildResource` objects that become `DataListItem`s. The DMR operations and parsing logic are the same, but the output types and consumers are different.

A possible future step: extract the "which DMR operation to use" decision and the DMR result parsing into a shared utility that both `ModelBrowserEngine` and `ResourceList` can use, parameterized by the output type. This would require a design that doesn't leak `ModelBrowserNode` or its `Type` enum outside the model browser package.
