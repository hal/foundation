# HasIdentifier Usage Scan

Scan of all PFJ `HasIdentifier` factory method usages across the codebase (excluding `test-suite/` and `target/`).

## Approach

1. Found all PFJ classes with identifier-accepting static factory methods via `HasIdentifier` implementations
2. Used Serena's `find_referencing_symbols` to locate every call site
3. Classified each as CENTRALIZED, RAW_STRING, or DYNAMIC

---

## Local classes with `identifier()` — `ColumnProvider`

The codebase has no classes directly implementing PFJ's `HasIdentifier`. However, the local interface `ColumnProvider` (`ui/.../finder/ColumnProvider.java`) defines its own `identifier()` method. All 10 implementations return an `ID` constant that already references `Identifiers.XXX`:

| Class | `ID` constant |
|-------|---------------|
| `ConfigurationColumn` | `Identifiers.CONFIGURATION_COLUMN` |
| `SubsystemColumn` | `Identifiers.SUBSYSTEM_COLUMN` |
| `InterfaceColumn` | `Identifiers.INTERFACE_COLUMN` |
| `PathColumn` | `Identifiers.PATH_COLUMN` |
| `SystemPropertyColumn` | `Identifiers.SYSTEM_PROPERTY_COLUMN` |
| `SocketBindingGroupColumn` | `Identifiers.SOCKET_BINDING_GROUP_COLUMN` |
| `SocketBindingTypeColumn` | `Identifiers.SOCKET_BINDING_TYPE` |
| `InboundColumn` | `Identifiers.SOCKET_BINDING_INBOUND` |
| `OutboundLocalColumn` | `Identifiers.SOCKET_BINDING_OUTBOUND_LOCAL` |
| `OutboundRemoteColumn` | `Identifiers.SOCKET_BINDING_OUTBOUND_REMOTE` |

All centralized — no action needed.

---

## CENTRALIZED (25 usages) — already using `Identifiers.XXX`

### NavigationItem

| File | Line | Identifier |
|------|------|------------|
| `op/.../NavigationProducer.java` | 41-51 | `Identifiers.NAV_DASHBOARD`, `NAV_CONFIGURATION`, `NAV_DEPLOYMENTS`, `NAV_RUNTIME`, `NAV_MODEL_BROWSER`, `NAV_TASKS` |

### Tab

| File | Line | Identifier |
|------|------|------------|
| `ui/.../ResourceTabs.java` | 80-88 | `Identifiers.DATA_TAB`, `ATTRIBUTES_TAB`, `OPERATIONS_TAB`, `CAPABILITIES_TAB` |
| `op/.../CredentialStoreTabs.java` | 61 | `Identifiers.CREDENTIAL_STORE_ALIASES_TAB` |

### FinderColumn (ID constants)

| File | Line | Identifier |
|------|------|------------|
| `op/.../ConfigurationColumn.java` | 48, 64 | `Identifiers.CONFIGURATION_COLUMN` |
| `op/.../SubsystemColumn.java` | 50, 66 | `Identifiers.SUBSYSTEM_COLUMN` |
| `op/.../SocketBindingTypeColumn.java` | 44, 59 | `Identifiers.SOCKET_BINDING_TYPE` |
| `op/.../SystemPropertyColumn.java` | 33 | `Identifiers.SYSTEM_PROPERTY_COLUMN` |
| `op/.../PathColumn.java` | 33 | `Identifiers.PATH_COLUMN` |
| `op/.../InterfaceColumn.java` | 63 | `Identifiers.INTERFACE_COLUMN` |
| `op/.../InboundColumn.java` | 30 | `Identifiers.SOCKET_BINDING_INBOUND` |
| `op/.../OutboundLocalColumn.java` | 30 | `Identifiers.SOCKET_BINDING_OUTBOUND_LOCAL` |
| `op/.../OutboundRemoteColumn.java` | 30 | `Identifiers.SOCKET_BINDING_OUTBOUND_REMOTE` |
| `op/.../SocketBindingGroupColumn.java` | 35 | `Identifiers.SOCKET_BINDING_GROUP_COLUMN` |

### FinderItem

| File | Line | Identifier |
|------|------|------------|
| `op/.../ConfigurationColumn.java` | 66-90 | `Identifiers.SUBSYSTEMS_ITEM`, `INTERFACES_ITEM`, `SOCKET_BINDINGS_ITEM`, `PATHS_ITEM`, `SYSTEM_PROPERTIES_ITEM` |
| `op/.../SocketBindingTypeColumn.java` | 61-69 | `Identifiers.INBOUND_ITEM`, `OUTBOUND_LOCAL_ITEM`, `OUTBOUND_REMOTE_ITEM` |

### WizardStep

| File | Line | Identifier |
|------|------|------------|
| `ui/.../AddResourceDialogs.java` | 124, 164 | `Identifiers.SELECT_LOCATION_STEP`, `Identifiers.ADD_RESOURCE_STEP` |

### Tr (table header rows)

| File | Line | Identifier |
|------|------|------------|
| `ui/.../AttributesTable.java` | 78 | `Identifiers.ATTRIBUTES_HEAD` |
| `ui/.../OperationsTable.java` | 123 | `Identifiers.OPERATIONS_HEAD` |
| `ui/.../CapabilitiesTable.java` | 109 | `Identifiers.CAPABILITIES_HEAD` |
| `op/.../EndpointTable.java` | 61, 77 | `Identifiers.ENDPOINT_EMPTY`, `Identifiers.ENDPOINT_HEAD` |
| `op/.../ExpressionsSection.java` | 118 | `Identifiers.EXPRESSIONS_HEAD` |
| `op/.../ResourcesSection.java` | 242 | `Identifiers.RESOURCES_HEAD` |

### MenuItem

| File | Line | Identifier |
|------|------|------------|
| `op/.../NotificationElements.java` | 78, 81, 84 | `Identifiers.MARK_ALL_READ`, `SELECT_ALL`, `CLEAR_ALL` |
| `op/.../NotificationListener.java` | 199, 201 | `Identifiers.MARK_ALL_READ`, `CLEAR_ALL` |
| `op/.../ResourcesSection.java` | 172, 174, 222 | `Identifiers.SELECT_FILTERED`, `SELECT_ALL`, `SELECT_NONE` |

### FormGroup

| File | Line | Identifier |
|------|------|------------|
| `op/.../EndpointForm.java` | 75-99 | `Identifiers.ENDPOINT_NAME`, `ENDPOINT_URL`, `ENDPOINT_PROTOCOL`, `ENDPOINT_HOST`, `ENDPOINT_PORT` |
| `op/.../NewExpressionModal.java` | 89 | `Identifiers.NEW_EXPRESSION` |

### DescriptionListGroup

| File | Line | Identifier |
|------|------|------------|
| `op/.../OverviewCard.java` | 180, 199, 256 | `Identifiers.CONSOLE_VERSION`, `OPERATION_MODE`, `CONFIG_FILE` |
| `op/.../StatusCard.java` | 157 | `Identifiers.RUNTIME_CONFIGURATION_STATE` |
| `op/.../RuntimeCard.java` | 213-248 | `Identifiers.HOST_NAME`, `HOST_ARCH`, `HOST_CORES`, `JVM_NAME`, `JVM_VENDOR`, `JVM_VERSION` |

---

## RAW STRING (3 usages) — refactoring candidates

### 1. BreadcrumbItem in ResourceBreadcrumb

**File:** `ui/src/main/java/org/jboss/hal/ui/resource/shell/ResourceBreadcrumb.java`
**Lines:** 86, 93

```java
bc.addItem(breadcrumbItem("root", "/")
        .onClick((event, item) -> {
```

Used twice (once for empty template, once for non-empty). The constant `Identifiers.BREADCRUMB_ROOT` (`"root"`) already exists.

### 2. BreadcrumbItem in TasksPage

**File:** `op/console/src/main/java/org/jboss/hal/op/task/TasksPage.java`
**Line:** 131

```java
.addItem(breadcrumbItem("all-tasks", "All tasks")
        .onClick((e, c) -> {
```

The constant `Identifiers.ALL_TASKS` (`"all-tasks"`) already exists.

### 3. DescriptionListGroup in InterfaceColumn

**File:** `op/console/src/main/java/org/jboss/hal/op/configuration/InterfaceColumn.java`
**Line:** 117

```java
dl.appendChild(descriptionListGroup("sbg")
        .addTerm(descriptionListTerm(sentenceCase(SOCKET_BINDING_GROUP)))
```

No matching constant exists yet. Needs a new `Identifiers` constant (e.g. `SOCKET_BINDING_GROUP_DLG`).

---

## DYNAMIC (51 usages) — inherently runtime-computed, not candidates

### Domain data identifiers (resource names, attribute names, etc.)

| Component | File | Line | Context |
|-----------|------|------|---------|
| `Alert` | `op/.../NotificationListener.java` | 65 | `alert(event.notification.severity(), event.notification.id, event.notification.title)` |
| `DataListItem` | `op/.../AliasManager.java` | 197 | `dataListItem(aliasId)` where `aliasId = Id.build(alias)` |
| `DataListItem` | `op/.../HealthCard.java` | 94 | `dataListItem(nameId)` where `nameId = Id.build(name)` |
| `DataListItem` | `ui/.../ResourceList.java` | 417 | `dataListItem(childId)` where `childId = Id.build(child.name)` |
| `FinderItem` | `op/.../SubsystemColumn.java` | 68 | `finderItem(Id.build(node.asString()))` — subsystem resource names |
| `FinderItem` | `ui/.../FinderBricks.java` | 103 | `finderItem(Id.build(node.asString()))` — child resource names |
| `ListItem` | `op/.../DocumentationCard.java` | 89, 100 | `listItem(Id.build("general-resources", nu[0]))`, `listItem(Id.build("get-help", nu[0]))` |
| `NotificationDrawerItem` | `op/.../NotificationListener.java` | 202 | `notificationDrawerItem(notification.severity(), notification.id, notification.title)` |
| `Tr` | `op/.../EndpointTable.java` | 115 | `tr(endpoint.id)` — endpoint domain data |
| `Tr` | `ui/.../AttributeRow.java` | 73 | `tr(attribute.name())` — attribute name from management model |
| `Tr` | `ui/.../OperationsTable.java` | 132 | `tr(operation.name())` — operation name from management model |
| `Tr` | `ui/.../CapabilitiesTable.java` | 168, 183 | `tr(capability.name())` — capability name from management model |

### Template-derived identifiers

| Component | File | Line | Context |
|-----------|------|------|---------|
| `BreadcrumbItem` | `ui/.../ResourceBreadcrumb.java` | 104 | `breadcrumbItem(current.identifier(), ...)` — `AddressTemplate.identifier()` |
| `TreeViewItem` | `ui/.../ModelBrowserEngine.java` | 165 | `treeViewItem(mbn.identifier)` — `ModelBrowserNode.identifier` from template |

### Composite / scoped IDs

| Component | File | Line | Context |
|-----------|------|------|---------|
| `DescriptionListGroup` | `ui/.../FindResource.java` | 140 | `descriptionListGroup(Id.build(scopeId, "address-help"))` |
| `DescriptionListGroup` | `ui/.../FindResource.java` | 154 | `descriptionListGroup(Id.build(scopeId, "type-help"))` |
| `DescriptionListGroup` | `ui/.../FindResource.java` | 165 | `descriptionListGroup(Id.build(scopeId, "name-help"))` |
| `DescriptionListGroup` | `ui/.../FindResource.java` | 178 | `descriptionListGroup(Id.build(comparisonId, "contains-help"))` |
| `DescriptionListGroup` | `ui/.../FindResource.java` | 190 | `descriptionListGroup(Id.build(comparisonId, "equals-help"))` |
| `FormGroup` | `ui/.../FindResource.java` | 219 | `formGroup(nameId)` where `nameId = Id.build(baseId, "name")` |
| `FormGroup` | `ui/.../FindResource.java` | 247 | `formGroup(rootId)` where `rootId = Id.build(baseId, "root")` |
| `FormGroup` | `ui/.../FindResource.java` | 255 | `formGroup(excludeId)` where `excludeId = Id.build(baseId, "exclude")` |
| `FormGroup` | `ui/.../FindResource.java` | 264 | `formGroup(scopeId)` where `scopeId = Id.build(baseId, "scope")` |
| `FormGroup` | `ui/.../FindResource.java` | 272 | `formGroup(comparisonId)` where `comparisonId = Id.build(baseId, "comparison")` |
| `Tr` | `ui/.../CapabilitiesTable.java` | 120 | `tr(Id.unique("empty"))` — placeholder row |
| `Tr` | `ui/.../CapabilitiesTable.java` | 211 | `tr(Id.build(capability.name(), "references", "head"))` |
| `Tr` | `ui/.../CapabilitiesTable.java` | 217 | `tr(Id.build(capability.name(), "references", attribute))` |
| `Tr` | `op/.../ExpressionsSection.java` | 140 | `tr(expressionId)` where `expressionId = Id.build(expression)` |

### Attribute-derived identifiers (view/form items)

These all use an `identifier` field/parameter derived from `AttributeDescription.name()`:

| Component | File | Line | Context |
|-----------|------|------|---------|
| `DescriptionListGroup` | `ui/.../CredentialReferenceViewItem.java` | 57 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../DefaultViewItem.java` | 71 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../FileViewItem.java` | 38 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../ListSimpleRecordViewItem.java` | 57 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../MapViewItem.java` | 44 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../PathRelativeToViewItem.java` | 40 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `ui/.../TimeUnitViewItem.java` | 41 | `descriptionListGroup(identifier)` |
| `DescriptionListGroup` | `op/.../Dashboard.java` | 62 | `descriptionListGroup(attribute)` — attribute name variable |
| `FormGroup` | `ui/.../DefaultFormItem.java` | 99 | `formGroup(identifier)` |
| `FormGroup` | `ui/.../ListSimpleRecordFormItem.java` | 99 | `formGroup(identifier)` |
| `FormGroup` | `ui/.../PathRelativeToFormItem.java` | 69 | `formGroup(identifier)` |

### String concatenation for sub-rows

| Component | File | Line | Context |
|-----------|------|------|---------|
| `Tr` | `ui/.../ListSimpleRecordFormItem.java` | 173 | `tr(identifier + "-head")` |
| `Tr` | `ui/.../ListSimpleRecordFormItem.java` | 185 | `tr(identifier + "-" + index)` |
| `Tr` | `ui/.../ListSimpleRecordViewItem.java` | 71 | `tr(identifier() + "-head")` |
| `Tr` | `ui/.../ListSimpleRecordViewItem.java` | 79 | `tr(identifier() + "-" + index)` |

### Menu items with computed identifiers

| Component | File | Line | Context |
|-----------|------|------|---------|
| `MenuItem` | `op/.../MgtSearchAsyncItems.java` | 193 | `menuItem(identifier)` — computed via `Id.build(result.type, result.name, normalizedAddr)` |
| `MenuItem` | `op/.../ResourcesSection.java` | 541, 557, 574 | `menuItem(identifier)` — computed via `Id.build(template, ...)` |

### Other dynamic identifiers

| Component | File | Line | Context |
|-----------|------|------|---------|
| `BreadcrumbItem` | `op/.../TasksPage.java` | 138 | `breadcrumbItem(task.id(), task.title())` — task domain ID |
| `SimpleListItem` | `ui/.../AddResourceDialogs.java` | 135 | `simpleListItem(resolved)` — resolved template path |
| `Tab` | `ui/.../ResourceTabs.java` | 166 | `tab(descriptor.id, descriptor.title)` — tab descriptor ID |
| `ToggleGroupItem` | `ui/.../CredentialReferenceControl.java` | 93-99 | `Mode.UNDEFINED.name()`, `Mode.CLEAR_TEXT.name()`, etc. |
| `Tr` | `op/.../ResourcesSection.java` | 489 | `tr(templateId)` — computed via `Id.build(template, ...)` |
| `TreeViewItem` | `ui/.../ModelBrowserEngine.java` | 165 | `treeViewItem(mbn.identifier)` — `ModelBrowserNode.identifier` |
