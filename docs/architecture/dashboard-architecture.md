# Dashboard Architecture

## Purpose

The dashboard provides a brief summary of key WildFly information as the console's landing page. It uses a card-based layout where each card highlights a specific aspect of the running server. Some cards are universal, appearing in both standalone and domain mode, while others are specific to particular deployment modes or configurations.

## Design

### Card Architecture

Each dashboard card is an independent component that:

- Fetches its data via DMR operations through the `Dispatcher` service
- Renders as a PatternFly card with consistent styling and layout
- Supports both light and dark themes automatically
- Manages its own loading and error states
- Emits CDI events to coordinate with other cards or the console

The card-based architecture enables:

- **Asynchronous loading** — each card loads independently without blocking others
- **Fault isolation** — failures in one card do not prevent other cards from rendering
- **Extensibility** — new cards can be added as CDI beans without modifying existing code
- **Responsive layout** — cards wrap intelligently based on viewport size using PatternFly grid

### Card Lifecycle

```
1. Card bean instantiation (CDI injection)
2. Initialize (subscribe to relevant DMR operations)
3. Load data (execute DMR operation, handle Promise)
4. Render (PatternFly card with fetched data)
5. Update (when relevant DMR events fire or manual refresh)
6. Dispose (unsubscribe from events)
```

## Planned Card Set

The dashboard supports a rich set of cards that adapt to the server's mode and capabilities.

### Universal Cards

These cards appear in both standalone server and domain mode:

- **General Information** — WildFly name, version, configuration URL, operation mode, stability level
- **Deployments** — count of deployed applications and their statuses (deployed, disabled, failed)
- **Log File** — recent summary of log file with count of warnings and errors
- **Host** — operating system name and version, number of processor cores
- **JVM** — Java Virtual Machine name and version
- **Runtime** — WildFly uptime, running mode, health status (green/yellow/red)
- **Memory** — heap and non-heap memory usage with visual gauges
- **Threads** — thread pool statistics (min, max, current count)
- **Help** — quick links to official documentation, community chat, forums, and support resources

### Domain-Specific Cards

When connected to a domain controller, additional cards provide domain-wide visibility:

- **Hosts** — count of managed hosts in the domain, with status summary
- **Servers** — total server instances across all hosts, with status breakdown
- **Profiles** — count of available server profiles
- **Server Groups** — count of server groups and their member assignments

## Implementation

### Card Abstraction

Cards are `@Dependent` CDI beans implementing a card interface with standard methods:

```java
public interface DashboardCard {
    String id();           // unique identifier
    String title();        // display title
    int order();          // rendering order
    Promise<Void> load(); // fetch data, returns Promise
    HTMLElement render(); // return card element
    void dispose();       // cleanup (optional)
}
```

Cards are discovered automatically by Crysknife and loaded on the dashboard page in order specified by `order()`.

### Data Fetching

All cards use the `Dispatcher` service to execute DMR operations:

```java
Dispatcher dispatcher; // @Inject

Operation op = new Operation.Builder(address)
    .build();

dispatcher.execute(op)
    .then(result -> {
        // update internal state
        return null;
    })
    .catch_(error -> {
        // handle error gracefully
        return null;
    });
```

Cards emit CDI events (`@Inject Event<T>`) to notify the console of state changes or request refreshes.

### UI Rendering

All cards render using PatternFly Java bindings via Elemento, producing consistent markup and styling. Cards use the `Card` component with optional `CardTitle`, `CardBody`, and `CardFooter` sections:

```java
return card()
    .title("Memory")
    .body(
        memoryGauge("Heap", heapUsage),
        memoryGauge("Non-Heap", nonHeapUsage)
    )
    .element();
```

PatternFly's automatic theme system ensures cards adapt to light and dark modes without custom CSS.

### Error Handling

Cards handle errors gracefully by displaying a message within the card boundary:

- Network failures show a "Failed to load" message with optional retry
- Permission errors show a "Not authorized" message
- Missing data shows an empty state with relevant context
- No errors block other cards from rendering

## Current State & Open Work

The dashboard is implemented with the basic universal and domain-specific card set, with screenshots available in the [features section](../features/dashboard.md).

Open work is tracked in [#71](https://github.com/hal/foundation/issues/71):

- Refine the card set based on community feedback (`help wanted` label)
- Additional domain-mode cards (e.g., deployment status summary, server group health)
- Card customization (pin/unpin, hide/show, reorder)
- Real-time card refresh based on server events or user polling interval
- Card drilldown (clicking a card navigates to detailed views)

The card-based architecture provides a solid foundation for expanding dashboard capabilities and enables community contributions for new card types.
