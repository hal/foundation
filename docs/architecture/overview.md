# Architecture Overview

HAL Foundation is a modern Java-based management console for WildFly and JBoss, compiled to JavaScript via J2CL and packaged for web deployment. The architecture balances enterprise requirements with web-native patterns, leveraging dependency injection, asynchronous operations, and a layered module structure.

## Technical Stack

- **Java 21**: Source language with modern language features and type safety
- **J2CL**: Java-to-JavaScript transpiler enabling Java development with browser deployment
- **Crysknife CDI**: Jakarta CDI-compatible dependency injection framework that works with J2CL
- **Elemento**: DOM abstraction layer providing fluent Java APIs for HTML element construction
- **PatternFly Java**: Java bindings for PatternFly 6 components, delivering consistent enterprise UI patterns
- **Maven**: Build orchestration with multi-level profile system for edition and packaging control
- **Vite + PNPM**: Frontend bundling and dependency management for CSS and static assets
- **Quarkus**: Optional HTTP server for standalone deployment with native binary support via GraalVM

## Module Structure

The codebase is organized into focused Maven modules, each with a single responsibility:

- **environment**: Server environment detection, endpoints, operation mode, and stability level tracking
- **dmr**: WildFly DMR (Dynamic Model Representation) protocol types, operations, and composite operations
- **meta**: Management model metadata, address templates with placeholder resolution, security contexts, and resource descriptions
- **event**: CDI-based application events and browser-level custom events for UI communication
- **resources**: Internationalization bundles, CSS class constants, OUIA IDs for test automation, and external URLs
- **db**: PouchDB bindings for local database operations and LRU cache implementation
- **core**: High-level CRUD operations, dispatcher for DMR execution, notification system, and human-readable label generation
- **task**: Task interface and CDI-based discovery for guided multi-step operations
- **ui**: PatternFly component utilities, brick pattern factories, model browser, resource views, forms, and attribute pipeline
- **op/console**: halOP application entry point, bootstrap flow, Vite build configuration, and console assembly

## Dependency Injection

HAL uses Crysknife CDI, a Jakarta CDI-compatible container that works with J2CL's compile-time constraints. Beans are discovered at compile time using `@ApplicationScoped` for singletons, `@Dependent` for per-injection instances, and `@Inject` for dependency resolution. The container supports standard CDI features including qualifiers, events, and producers.

## Bootstrap Flow

The console initializes via a sequential Elemento Flow that configures the application before rendering the UI:

1. **SetLogLevel**: Configure logging based on URL parameters or environment
2. **SelectEndpoint**: Determine WildFly management endpoint (embedded, remote, or user-selected)
3. **SingleSignOnSupport**: Initialize SSO authentication if required
4. **ReadEnvironment**: Query server for product version, operation mode, and capabilities
5. **ReadHostNames**: Fetch domain controller topology in domain mode
6. **FindDomainController**: Identify the active domain controller
7. **ReadStability**: Determine server stability level (default, community, preview, experimental)
8. **LoadSettings**: Restore user preferences from browser cookies
9. **SetTitle**: Update browser title with server and product information

Each step is a Promise-returning function that may execute DMR operations, update CDI beans, or modify the DOM. Failures halt the flow and display an error page.

## Data Layer

All communication with WildFly management API uses DMR operations. The `Dispatcher` service executes `Operation` and `Composite` instances against the management endpoint, returning Promises for asynchronous result handling. Operations are built using the builder pattern with type-safe parameter methods.

PouchDB provides local caching for frequently accessed metadata such as resource descriptions and security contexts. The `MetadataRepository` uses a two-level cache (in-memory LRU + PouchDB) to minimize network round-trips during navigation.

## Key Services

### Dispatcher

An `@ApplicationScoped` CDI bean that executes DMR operations against the WildFly management API. Wraps the HTTP transport layer, handles request serialization and response parsing, and returns Promises for asynchronous operation. Supports both single operations and composite batches with atomic rollback on failure.

### MetadataRepository

Stores and retrieves management model metadata using a two-level cache. The first level is an in-memory LRU cache for fast repeated access. The second level is PouchDB for persistent storage across sessions. Metadata lookup is keyed by `AddressTemplate`, and results are automatically populated into the cache for future requests.

### StatementContext

Resolves placeholders in resource addresses based on current user selections. For example, `{selected.profile}` resolves to the currently selected domain profile name. The context tracks selections made in the UI (profiles, hosts, servers) and provides a fluent API for template resolution via `StatementContextResolver`.

### CrudOperations

An abstract CRUD layer over the DMR protocol. Provides create, read, update, and delete methods with automatic notification generation on success or failure. Operations accept `AddressTemplate` instances for address construction and emit CDI events to notify the UI of resource changes. Simplifies common management tasks with consistent error handling and user feedback.

### Notifications

Manages the lifecycle of `Notification` instances using an LRU cache with configurable capacity. Supports sending, reading, clearing, and removing notifications. Fires CDI events (`NotificationAdded`, `NotificationRead`, `NotificationsCleared`, `NotificationRemoved`) to update the UI masthead badge and notification drawer.

## UI Patterns

### Brick Pattern

A brick is a `final` utility class with a `private` constructor and only `static` factory methods. Each brick groups related methods by domain, producing small, reusable PatternFly-based UI elements. The naming convention is `<Domain>Bricks` (e.g., `AttributeBricks`, `CodeBricks`). Brick classes that are used across multiple packages live in `org.jboss.hal.ui.brick`, while domain-specific bricks live alongside their consumers.

### Filter System

The filter package provides a generic filtering infrastructure with type-safe filter implementations for common criteria (text, enum, boolean, number ranges). Filters expose PatternFly toolbar UI components and apply predicate logic to collections. Filters can be composed and combined for multi-criteria searches.

### Stability Labels

WildFly features are tagged with stability levels (default, community, preview, experimental). The `StabilityLabel` component displays these levels with appropriate PatternFly label colors and icons, providing visual feedback on feature maturity.

## Event Architecture

HAL uses two distinct event mechanisms:

- **ApplicationEvent**: CDI events for cross-component communication. Fired with `@Inject Event<T>` and observed with `@Observes`. Used for application-wide notifications such as resource changes, setting updates, and navigation events.
- **UIEvent**: Browser-level custom events dispatched on HTML elements. Used for UI-local communication within a component subtree. Provides a namespaced event type system and integrates with browser event bubbling.

Both event types are marker interfaces with specific implementations per use case. CDI events are preferred for business logic coordination, while UI events are used for DOM-level interactions.

## Asynchronous Operations

All DMR operations return Elemento `Promise` instances, enabling asynchronous, non-blocking execution. The Promise API supports chaining with `then`, error handling with `catch`, and parallel composition with `all`. UI components subscribe to Promise completion to update displays, show notifications, or navigate to new resources.

## Localization

The `resources` module contains the `L10nBundle` interface, which provides access to translated strings. All user-facing text is externalized into property files and accessed via generated constants. The `Humanize` utility converts technical model terms (e.g., `ejb-connection-pool`) into human-readable labels (e.g., `EJB Connection Pool`) with special handling for common abbreviations (HTTP, SSL, JPA).

## Test Automation

The console uses OUIA (Open UI Automation) IDs to provide stable selectors for test automation. The `OuiaIds` class in the `resources` module centralizes both static and dynamically composed IDs. All PatternFly components are tagged with OUIA attributes, enabling reliable test scripts that survive UI refactoring.
