# Architecture Overview

HAL Foundation is a management console for WildFly and JBoss, written entirely in Java but running in the browser. The Java source compiles to JavaScript via J2CL, bundles with Vite, and optionally packages inside a Quarkus HTTP server for standalone deployment. The result is a fully client-side application with enterprise-grade tooling behind the scenes.

## Technical Stack

The stack bridges Java's type safety and tooling with browser-native deployment:

- **Java 21** provides the source language with modern features and strong types
- **J2CL** transpiles Java to JavaScript, enabling Java development with browser deployment
- **Crysknife CDI** brings Jakarta CDI-compatible dependency injection to J2CL — beans, qualifiers, events, and producers all work at compile time
- **Elemento** abstracts the DOM with fluent Java APIs for building HTML elements
- **PatternFly Java** wraps PatternFly 6 components in Java bindings, delivering consistent enterprise UI patterns
- **Maven** orchestrates the build through a multi-level profile system that controls edition and packaging
- **Vite + PNPM** handle frontend bundling and CSS dependency management
- **Quarkus** provides an optional HTTP server for standalone deployment, with native binary support via GraalVM

## Module Structure

The codebase is organized into focused Maven modules, each owning a single responsibility. Lower modules know nothing about higher ones, and the dependency graph flows strictly downward:

- **environment** — server environment detection, endpoints, operation mode, and stability level tracking
- **dmr** — WildFly DMR (Dynamic Model Representation) protocol types, operations, and composite operations
- **meta** — management model metadata, address templates with placeholder resolution, security contexts, and resource descriptions
- **event** — CDI-based application events and browser-level custom events for UI communication
- **resources** — internationalization bundles, CSS class constants, OUIA IDs for test automation, and external URLs
- **db** — PouchDB bindings for local database operations and LRU cache implementation
- **mgt** — Model Graph Tools (MGT) integration — REST client, DTOs, availability events, catalog search, and endpoint management for the MGT sidecar container
- **core** — high-level CRUD operations, dispatcher for DMR execution, notification system, and human-readable label generation
- **task** — task interface and CDI-based discovery for guided multi-step operations
- **ui** — PatternFly component utilities, brick pattern factories, model browser, resource views, forms, and attribute pipeline
- **op/console** — halOP application entry point, bootstrap flow, Vite build configuration, and console assembly

## Dependency Injection

HAL uses Crysknife CDI, a Jakarta CDI-compatible container that works within J2CL's compile-time constraints. Beans are discovered at compile time: `@ApplicationScoped` for singletons, `@Dependent` for per-injection instances, and `@Inject` for dependency resolution. The container supports standard CDI features including qualifiers, events, and producers, so the programming model feels familiar to any Jakarta EE developer.

## Bootstrap Flow

When the console starts, it runs a sequential Elemento Flow that configures the application before any UI appears. Each step is a Promise-returning function that may execute DMR operations, update CDI beans, or modify the DOM. If any step fails, the flow halts and displays an error page.

1. **SetLogLevel** — configure logging based on URL parameters or environment
2. **SelectEndpoint** — determine the WildFly management endpoint (embedded, remote, or user-selected)
3. **SingleSignOnSupport** — initialize SSO authentication if required
4. **ReadEnvironment** — query the server for product version, operation mode, and capabilities
5. **ReadHostNames** — fetch domain controller topology in domain mode
6. **FindDomainController** — identify the active domain controller
7. **ReadStability** — determine server stability level (default, community, preview, experimental)
8. **LoadSettings** — restore user preferences from browser cookies
9. **SetTitle** — update the browser title with server and product information

## Data Layer

All communication with the WildFly management API uses DMR operations. The `Dispatcher` service executes `Operation` and `Composite` instances against the management endpoint and returns Promises for asynchronous result handling. Operations are built using the builder pattern with type-safe parameter methods.

PouchDB provides local caching for frequently accessed metadata such as resource descriptions and security contexts. The `MetadataRepository` uses a two-level cache — an in-memory LRU for fast repeated access plus PouchDB for persistence across sessions — to minimize network round-trips during navigation.

## Key Services

### Dispatcher

The central service for talking to WildFly. The `Dispatcher` is an `@ApplicationScoped` CDI bean that wraps the HTTP transport layer, handles request serialization and response parsing, and returns Promises. It supports both single operations and composite batches with atomic rollback on failure.

### MetadataRepository

The metadata cache. It stores and retrieves management model metadata using a two-level strategy: an in-memory LRU cache for fast repeated access, and PouchDB for persistent storage across sessions. Lookups are keyed by `AddressTemplate`, and results automatically populate the cache for future requests.

### StatementContext

The placeholder resolver. It resolves placeholders in resource addresses based on current user selections — for example, `{selected.profile}` resolves to the currently selected domain profile name. The context tracks selections made in the UI (profiles, hosts, servers) and provides a fluent API for template resolution via `StatementContextResolver`.

### CrudOperations

The CRUD layer. It provides create, read, update, and delete methods over the DMR protocol with automatic notification generation on success or failure. Operations accept `AddressTemplate` instances for address construction and emit CDI events to notify the UI of resource changes.

### Notifications

The notification system. It manages the lifecycle of `Notification` instances using an LRU cache with configurable capacity. It supports sending, reading, clearing, and removing notifications, and fires CDI events (`NotificationAdded`, `NotificationRead`, `NotificationsCleared`, `NotificationRemoved`) to update the UI masthead badge and notification drawer.

## UI Patterns

### Brick Pattern

A brick is a `final` utility class with a `private` constructor and only `static` factory methods. Each brick groups related methods by domain, producing small, reusable PatternFly-based UI elements. The naming convention is `<Domain>Bricks` (e.g., `AttributeBricks`, `CodeBricks`). Bricks used across multiple packages live in `org.jboss.hal.ui.brick`, while domain-specific bricks live alongside their consumers.

### Filter System

The filter package provides a generic filtering infrastructure with type-safe implementations for common criteria (text, enum, boolean, number ranges). Filters expose PatternFly toolbar UI components and apply predicate logic to collections. They can be composed and combined for multi-criteria searches.

### Stability Labels

WildFly features are tagged with stability levels (default, community, preview, experimental). The `StabilityLabel` component displays these levels with appropriate PatternFly label colors and icons, providing visual feedback on feature maturity.

## Event Architecture

HAL uses two distinct event mechanisms, each suited to a different scope:

- **ApplicationEvent** — CDI events for cross-component communication. Fired with `@Inject Event<T>` and observed with `@Observes`. Used for application-wide concerns like resource changes, setting updates, and navigation events.
- **UIEvent** — browser-level custom events dispatched on HTML elements. Used for UI-local communication within a component subtree. Provides a namespaced event type system and integrates with browser event bubbling.

CDI events are the default for business logic coordination. UI events are for DOM-level interactions where CDI's global scope would be too broad.

## Asynchronous Operations

All DMR operations return browser-native `Promise` instances for asynchronous, non-blocking execution. The Promise API supports chaining with `then`, error handling with `catch`, and parallel composition with `all`. UI components subscribe to Promise completion to update displays, show notifications, or navigate to new resources.

For multi-step asynchronous workflows, HAL uses Elemento's [Flow API](https://hal.github.io/elemento/flow.html) — a sequential execution model where each step returns a Promise, and the flow halts on failure. The bootstrap sequence is the primary example of this pattern.

## Localization

The `resources` module contains the `L10nBundle` interface for accessing translated strings. All user-facing text is externalized into property files and accessed via generated constants. The `Humanize` utility converts technical model terms (e.g., `ejb-connection-pool`) into human-readable labels (e.g., `EJB Connection Pool`) with special handling for common abbreviations (HTTP, SSL, JPA).

## Test Automation

The console uses OUIA (Open UI Automation) IDs to provide stable selectors for test automation. The `OuiaIds` class in the `resources` module centralizes both static and dynamically composed IDs. All PatternFly components are tagged with OUIA attributes, enabling reliable test scripts that survive UI refactoring. See [Test Automation](../development/test-automation.md) and the [dave documentation](https://hal.github.io/dave/) for details on the test suite.