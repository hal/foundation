# ModelGraph Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rename and restructure MGT-related classes to follow the `ModelGraph` prefix convention and separate concerns (connection, search, capability) before the Model Catalog feature.

**Architecture:** The monolithic `ModelGraphConnection` class is split into three focused classes: `ModelGraphConnection` (connectivity/ping), `ModelGraphSearch` (search API), and `ModelGraphCapability` (capability references API). Each is an `@ApplicationScoped` CDI bean. The `ModelGraphToolsIndicator` is renamed to `ModelGraphIndicator`. All classes stay in the same package (`org.jboss.hal.core.mgt` for core, `org.jboss.hal.ui.component` for the indicator). Constants in `Urls` are renamed to match.

**Tech Stack:** Java 17, J2CL, Crysknife CDI, JsInterop, Elemento

**Spec:** `docs/superpowers/specs/2026-10-22-explorer-design.md` (section "Implementation Context")

## Global Constraints

- All renames must use IntelliJ's AST-based refactoring via JetBrains MCP (not text replacement)
- Build verification: `mvn install -Dquickly` after each task
- No functional changes — purely mechanical refactoring (rename, extract, update references)
- Preserve all existing Javadoc, updating class/method names within it
- Preserve Apache 2.0 license headers on all new files

## Review Focus

1. **CDI injection sites broken** — any class injecting `ModelGraphConnection` must be updated to inject the correct replacement (`ModelGraphConnection`, `ModelGraphSearch`, or both). Missing an injection site causes a runtime NPE.
2. **`UIContext` accessor rename missed** — `UIContext.modelGraphTools()` must split into separate accessors. Callers using `uic().modelGraphTools().search(...)` must switch to the new accessor. A missed caller compiles (if the old method exists) but calls the wrong service.
3. **`Urls` constant rename incomplete** — `MODEL_GRAPH_TOOLS_API` and `MODEL_GRAPH_TOOLS_PAGE` must be renamed. Any reference to the old constant name causes a compile error (good — caught at build time).
4. **OUIA component type string** — `ModelGraphToolsIndicator` has `ouiaComponentType()` returning `"hal/ModelGraphToolsIndicator"`. This is a test automation contract — renaming it may break external test selectors. Verify whether this string should change.
5. **Singleton factory method rename** — `ModelGraphToolsIndicator.modelGraphToolsIndicator()` is a static factory. All call sites must update to the new name.

---

### Task 1: Rename `ModelGraphConnection` → `ModelGraphConnection` and update all references

This task renames the class and updates all 7 files that reference it. The class keeps all its current methods temporarily — extraction happens in Tasks 2 and 3.

**Files:**
- Rename: `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphTools.java` → `ModelGraphConnection.java`
- Modify: `ui/src/main/java/org/jboss/hal/ui/UIContext.java` (field, constructor param, accessor)
- Modify: `ui/src/main/java/org/jboss/hal/ui/component/ModelGraphToolsIndicator.java` (reference in `refresh()`)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/UniversalSearchAsyncItems.java` (constructor param, field)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/MgtSearchAsyncItems.java` (constructor param, field)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/UniversalSearchBox.java` (references)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/package-info.java` (Javadoc reference)

**Interfaces:**
- Consumes: nothing (first task)
- Produces: `ModelGraphConnection` class with all original methods (`available()`, `ping()`, `search()`, `capabilityReferences()`, `apiEndpoint()`, `port()`)

- [ ] **Step 1: Use IntelliJ rename refactoring** to rename `ModelGraphConnection` → `ModelGraphConnection`

  Use `mcp__idea__rename_refactoring` on `ModelGraphConnection` in `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphTools.java`. This handles the class name, file name, constructor, logger, and all import statements across the codebase.

- [ ] **Step 2: Update `UIContext` accessor name**

  In `UIContext.java`, rename the field `modelGraphTools` → `modelGraphConnection`, the constructor parameter, and the accessor method `modelGraphTools()` → `modelGraphConnection()`. Update the Javadoc on the accessor.

- [ ] **Step 3: Update all callers of `uic().modelGraphTools()`**

  Search for `modelGraphTools()` calls. Update:
  - `ModelGraphToolsIndicator.java:146` — `uic().modelGraphTools().ping()` → `uic().modelGraphConnection().ping()`
  - Any other callers found by the search

- [ ] **Step 4: Update Javadoc references**

  In `ModelGraphConnection.java`, update the class Javadoc: replace "model graph tools (MGT)" with "model graph (MGT)" where it refers to the class purpose. Update log messages from "Model graph tools" to "Model graph" where they describe the service.

- [ ] **Step 5: Build and verify**

  Run: `mvn install -Dquickly`
  Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

  ```bash
  git add -A
  git commit -m "refactor: rename ModelGraphTools to ModelGraphConnection"
  ```

---

### Task 2: Extract search methods into `ModelGraphSearch`

Extract the `search()` methods and their supporting types from `ModelGraphConnection` into a new `ModelGraphSearch` class. `ModelGraphConnection` retains only `available()`, `ping()`, `apiEndpoint()`, and `port()`.

**Files:**
- Create: `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphSearch.java`
- Modify: `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphConnection.java` (remove `search()` methods and `LIMIT` constant)
- Modify: `ui/src/main/java/org/jboss/hal/ui/UIContext.java` (add `ModelGraphSearch` field, constructor param, accessor)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/MgtSearchAsyncItems.java` (change from `ModelGraphConnection` to `ModelGraphSearch`)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/UniversalSearchAsyncItems.java` (change constructor to take both `ModelGraphConnection` and `ModelGraphSearch`)
- Modify: `op/console/src/main/java/org/jboss/hal/op/search/UniversalSearchBox.java` (update instantiation)

**Interfaces:**
- Consumes: `ModelGraphConnection` from Task 1 (for `apiEndpoint()` — but `ModelGraphSearch` needs its own endpoint access)
- Produces: `ModelGraphSearch` with `search(String term): Promise<SearchResult[]>` and `search(String term, int limit): Promise<SearchResult[]>`

**Design decision:** `ModelGraphSearch` needs access to the API endpoint URL. Rather than injecting `ModelGraphConnection`, it should inject `Environment` directly and compute the endpoint itself — same pattern, same private `apiEndpoint()` and `port()` methods. This avoids coupling `ModelGraphSearch` to `ModelGraphConnection` and keeps each class self-contained. Extract `apiEndpoint()` and `port()` into a shared package-private utility or duplicate them (they're 4 lines total). Prefer duplication over premature abstraction — extract a shared utility only if a third class needs it (which `ModelGraphCapability` will in Task 3, making this the right time to extract).

**Revised approach:** Create a package-private `ModelGraphEndpoint` helper class that computes the API endpoint URL from `Environment`. All three classes (`ModelGraphConnection`, `ModelGraphSearch`, `ModelGraphCapability`) inject or use it.

- [ ] **Step 1: Create `ModelGraphEndpoint`**

  Create `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphEndpoint.java`:
  - `@ApplicationScoped`
  - Inject `Environment`
  - Method: `StringBuilder api()` — returns the base API URL (current `apiEndpoint()` logic)
  - Move `HTTP_PORT_BASE` constant and `port(Version)` method here

- [ ] **Step 2: Create `ModelGraphSearch`**

  Create `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphSearch.java`:
  - `@ApplicationScoped`
  - Inject `ModelGraphEndpoint`
  - Move `search(String term)` and `search(String term, int limit)` from `ModelGraphConnection`
  - Move `LIMIT` constant
  - Own `Logger` instance

- [ ] **Step 3: Update `ModelGraphConnection`**

  - Remove `search()` methods, `LIMIT`, `apiEndpoint()`, `port()`, `HTTP_PORT_BASE`
  - Inject `ModelGraphEndpoint` instead of computing endpoint internally
  - Update `ping()` to use `endpoint.api().append("identity")`

- [ ] **Step 4: Add `ModelGraphSearch` to `UIContext`**

  In `UIContext.java`:
  - Add field `private final ModelGraphSearch modelGraphSearch`
  - Add constructor parameter
  - Add accessor `public ModelGraphSearch modelGraphSearch()`

- [ ] **Step 5: Update `ModelGraphSearchAsyncItems`**

  Change constructor parameter and field from `ModelGraphConnection` to `ModelGraphSearch`. Only `search()` is called here.

- [ ] **Step 6: Update `UniversalSearchAsyncItems`**

  - Change constructor to accept `ModelGraphConnection` (for `available()`) and `ModelGraphSearch` (passed to `ModelGraphSearchAsyncItems`)
  - Update `mgtAvailable` to use `ModelGraphConnection.available()`

- [ ] **Step 7: Update `UniversalSearchBox`**

  Update where `UniversalSearchAsyncItems` is instantiated to pass both `modelGraphConnection` and `modelGraphSearch` from `uic()`.

- [ ] **Step 8: Build and verify**

  Run: `mvn install -Dquickly`
  Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

  ```bash
  git add -A
  git commit -m "refactor: extract ModelGraphSearch from ModelGraphConnection"
  ```

---

### Task 3: Extract capability methods into `ModelGraphCapability`

Extract the `capabilityReferences()` method from `ModelGraphConnection` into a new `ModelGraphCapability` class.

**Files:**
- Create: `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphCapability.java`
- Modify: `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphConnection.java` (remove `capabilityReferences()`)
- Modify: `ui/src/main/java/org/jboss/hal/ui/UIContext.java` (add `ModelGraphCapability` field, constructor param, accessor)

**Interfaces:**
- Consumes: `ModelGraphEndpoint` from Task 2
- Produces: `ModelGraphCapability` with `capabilityReferences(String capability): Promise<CapabilityReference[]>`

- [ ] **Step 1: Create `ModelGraphCapability`**

  Create `core/src/main/java/org/jboss/hal/core/mgt/ModelGraphCapability.java`:
  - `@ApplicationScoped`
  - Inject `ModelGraphEndpoint`
  - Move `capabilityReferences(String capability)` from `ModelGraphConnection`
  - Own `Logger` instance

- [ ] **Step 2: Update `ModelGraphConnection`**

  Remove `capabilityReferences()`. `ModelGraphConnection` now has only `available()` and `ping()`.

- [ ] **Step 3: Add `ModelGraphCapability` to `UIContext`**

  In `UIContext.java`:
  - Add field `private final ModelGraphCapability modelGraphCapability`
  - Add constructor parameter
  - Add accessor `public ModelGraphCapability modelGraphCapability()`

- [ ] **Step 4: Update any callers of `capabilityReferences()`**

  Search for `capabilityReferences(` across the codebase. Update callers to use `uic().modelGraphCapability().capabilityReferences(...)` instead of the old path.

- [ ] **Step 5: Build and verify**

  Run: `mvn install -Dquickly`
  Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

  ```bash
  git add -A
  git commit -m "refactor: extract ModelGraphCapability from ModelGraphConnection"
  ```

---

### Task 4: Rename `ModelGraphToolsIndicator` → `ModelGraphIndicator` and rename URL constants

**Files:**
- Rename: `ui/src/main/java/org/jboss/hal/ui/component/ModelGraphToolsIndicator.java` → `ModelGraphIndicator.java`
- Modify: `resources/src/main/java/org/jboss/hal/resources/Urls.java` (rename constants)
- Modify: all files referencing `MODEL_GRAPH_TOOLS_API` or `MODEL_GRAPH_TOOLS_PAGE`
- Modify: all files referencing `modelGraphToolsIndicator()` factory method

**Interfaces:**
- Consumes: `ModelGraphConnection` (from Task 1, used in `refresh()`)
- Produces: `ModelGraphIndicator` with factory method `modelGraphIndicator()`

- [ ] **Step 1: Use IntelliJ rename refactoring** for `ModelGraphToolsIndicator` → `ModelGraphIndicator`

  Use `mcp__idea__rename_refactoring`. This renames the class, file, constructor, factory method, and all references.

- [ ] **Step 2: Rename factory method**

  Rename `modelGraphToolsIndicator()` → `modelGraphIndicator()`. Update all call sites.

- [ ] **Step 3: Update OUIA component type**

  In `ModelGraphIndicator.java`, update `ouiaComponentType()` return value from `"hal/ModelGraphToolsIndicator"` to `"hal/ModelGraphIndicator"`.

- [ ] **Step 4: Rename URL constants in `Urls.java`**

  - `MODEL_GRAPH_TOOLS_API` → `MODEL_GRAPH_API`
  - `MODEL_GRAPH_TOOLS_PAGE` → `MODEL_GRAPH_PAGE`
  - Update Javadoc on both constants

- [ ] **Step 5: Update all references to renamed constants**

  Search for `MODEL_GRAPH_TOOLS_API` and `MODEL_GRAPH_TOOLS_PAGE`. Update import statements and usages in:
  - `ModelGraphEndpoint.java` (from Task 2)
  - `ModelGraphIndicator.java`
  - Any other files

- [ ] **Step 6: Update indicator text strings**

  In `ModelGraphIndicator.java`, update UI-facing text:
  - Menu group label: "Model graph tools" → "Model graph" (or keep as-is if this is the user-facing product name)
  - Status messages: keep "Model graph tools are available/not available" or update — builder's call

- [ ] **Step 7: Build and verify**

  Run: `mvn install -Dquickly`
  Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

  ```bash
  git add -A
  git commit -m "refactor: rename ModelGraphToolsIndicator to ModelGraphIndicator and update URL constants"
  ```

---

### Task 5: Final verification and cleanup

**Files:**
- Verify: all files in `core/src/main/java/org/jboss/hal/core/mgt/`
- Verify: `ui/src/main/java/org/jboss/hal/ui/UIContext.java`
- Verify: `op/console/src/main/java/org/jboss/hal/op/search/` (all search files)

**Interfaces:**
- Consumes: all previous tasks
- Produces: clean build, no stale references

- [ ] **Step 1: Search for any remaining "ModelGraphTools" references**

  Search across the entire codebase for the string `ModelGraphConnection` (case-sensitive). This catches:
  - Stale imports
  - Javadoc references
  - Log messages
  - String literals

  Expected: zero matches

- [ ] **Step 2: Verify the final class structure**

  Confirm the `core/mgt` package contains:
  - `ModelGraphConnection.java` — `available()`, `ping()` only
  - `ModelGraphEndpoint.java` — API URL computation
  - `ModelGraphSearch.java` — `search()` methods
  - `ModelGraphCapability.java` — `capabilityReferences()`
  - `CapabilityReference.java` — JsInterop type (unchanged)
  - `CapabilityReferencesResponse.java` — JsInterop type (unchanged)
  - `ResourceRef.java` — JsInterop type (unchanged)
  - `SearchResponse.java` — JsInterop type (unchanged)
  - `SearchResult.java` — JsInterop type (unchanged)
  - `SearchResultType.java` — enum (unchanged)

  And `ui/component` contains:
  - `ModelGraphIndicator.java` (renamed from `ModelGraphToolsIndicator`)

- [ ] **Step 3: Full build with checks**

  Run: `mvn verify -P op`
  Expected: BUILD SUCCESS (includes tests and checks)

- [ ] **Step 4: Commit any cleanup**

  If Step 1 found stale references:
  ```bash
  git add -A
  git commit -m "refactor: clean up remaining ModelGraphTools references"
  ```
