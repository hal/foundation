# Task Framework

## What It Does

Tasks let users complete complex management operations quickly by combining multiple steps across different subsystems into a single guided UI. Instead of navigating to individual resources one by one — say, toggling `statistics-enabled` across a dozen subsystems — a task presents a focused interface for the entire operation.

## How It Works

### The Task Interface

Each task implements `org.jboss.hal.task.Task` and provides everything the framework needs to discover, display, and run it:

- **id()** — unique identifier, typically the fully qualified class name
- **title()** — human-readable display name
- **icon()** — PatternFly icon for visual identification
- **summary()** — HTML description of the task's purpose and effect
- **elements()** — the task's UI elements (forms, lists, custom widgets)
- **run()** — execution logic that performs the actual operations
- **enabled()** — optional conditional enablement based on server state

### Discovery

Tasks are `@Dependent` CDI beans discovered automatically at build time by Crysknife. The console loads all task implementations from the classpath and displays them on the tasks page. External modules can contribute tasks by placing implementation JARs on the classpath — no explicit registration is required.

### Code Example

```java
@Dependent
public class MyTask implements Task {

    public String id() {
        return MyTask.class.getName();
    }

    public String title() {
        return "Configure Logging";
    }

    public Element icon() {
        return clipboard().element();
    }

    public HTMLElement summary() {
        return content(p).add("Configures the logging subsystem.").element();
    }

    public Iterable<HTMLElement> elements() {
        return List.of();
    }

    public void run() {
        // execute the task logic
    }
}
```

### Reference Implementation

The `statistics-enabled` task demonstrates the framework in practice. It discovers all resources that define a `statistics-enabled` attribute, presents them in a filterable list, and lets users enable or disable statistics across multiple resources at once — with expression support for fine-grained selection. This implementation serves as a blueprint for new task development.

## Current State & Open Work

The task framework and task page are fully implemented. The `statistics-enabled` task is the reference implementation.

Open work is tracked in [#189](https://github.com/hal/foundation/issues/189):

- Define additional tasks (SSL configuration, datasource setup, logging configuration, etc.)
- Community contributions welcome (`help wanted` label)