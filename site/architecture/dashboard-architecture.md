# Dashboard Architecture

## What It Does

The dashboard is the console's landing page — a card-based layout that gives you a quick summary of key WildFly information at a glance. Each card highlights a specific aspect of the running server: memory usage, thread counts, deployments, and more. Some cards appear everywhere, while others show up only in domain mode or when specific capabilities are available.

## How It Works

### Card Architecture

Each dashboard card is an independent component. It fetches its own data via DMR operations, renders as a PatternFly card, manages its own loading and error states, and emits CDI events to coordinate with others. This independence brings four benefits:

- **Asynchronous loading** — each card loads without blocking others
- **Fault isolation** — a failure in one card doesn't prevent the rest from rendering
- **Extensibility** — new cards are CDI beans, added without modifying existing code
- **Responsive layout** — cards wrap based on viewport size using PatternFly grid

### Card Lifecycle

A card moves through a straightforward lifecycle: it's instantiated by CDI, initializes its subscriptions, loads data via DMR (returning a Promise), renders as a PatternFly card, and then updates whenever a refresh event arrives or the user triggers a reload.

```
Instantiation          CDI injection
      │
      ▼
  Initialize           subscribe to DMR ops
      │
      ▼
    Load               execute DMR, handle Promise
      │
      ▼
   Render              PatternFly card
      │
      ▼
   Update              DMR events / refresh
      │  ▲
      │  │ refresh
      ▼  │
   Dispose             unsubscribe
```

## Card Set

The dashboard supports a set of cards that adapt to the server's mode and capabilities.

### Universal Cards

These appear in both standalone and domain mode:

- **General Information** — WildFly name, version, configuration URL, operation mode, stability level
- **Deployments** — count of deployed applications and their statuses (deployed, disabled, failed)
- **Log File** — recent log summary with count of warnings and errors
- **Host** — operating system name and version, number of processor cores
- **JVM** — Java Virtual Machine name and version
- **Runtime** — WildFly uptime, running mode, health status
- **Memory** — heap and non-heap memory usage with visual gauges
- **Threads** — thread pool statistics (min, max, current count)
- **Help** — quick links to documentation, community chat, forums, and support

### Domain-Specific Cards

When connected to a domain controller, additional cards provide domain-wide visibility:

- **Hosts** — count of managed hosts with status summary
- **Servers** — total server instances across all hosts with status breakdown
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

Cards are discovered automatically by Crysknife and loaded on the dashboard page in the order specified by `order()`.

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

All cards render using PatternFly Java bindings via Elemento, producing consistent markup and styling:

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
- No single card's error blocks the others from rendering

## Current State & Open Work

The dashboard is implemented with the basic universal and domain-specific card set. Screenshots are available in the [features section](../features/dashboard.md).

Open work is tracked in [#71](https://github.com/hal/foundation/issues/71):

- Refine the card set based on community feedback (`help wanted` label)
- Additional domain-mode cards (e.g., deployment status summary, server group health)
- Card customization (pin/unpin, hide/show, reorder)
- Real-time card refresh based on server events or user polling interval
- Card drilldown (clicking a card navigates to detailed views)