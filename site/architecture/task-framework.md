# Task Framework

## Purpose

Tasks enable users to complete complex management operations quickly by combining multiple steps across different subsystems and resources. Instead of navigating to individual resources one by one, a task presents a guided, focused UI for a specific use case.

## Design

### Task Interface

Each task implements `org.jboss.hal.task.Task` and provides:

- **id()** — unique identifier, typically the fully qualified class name
- **title()** — human-readable display name
- **icon()** — PatternFly icon element for visual identification
- **summary()** — HTML description of the task's purpose and effect
- **elements()** — the task's UI elements (forms, lists, custom widgets)
- **run()** — execution logic that performs the actual operations
- **enabled()** — optional conditional enablement based on server state

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

### Discovery

Tasks are `@Dependent` CDI beans discovered automatically at build time by Crysknife. The console loads all task implementations from the classpath and displays them on the tasks page. External modules can contribute tasks by placing implementation JARs on the classpath—no explicit registration is required.

### Reference Implementation

The `statistics-enabled` task demonstrates the framework in practice:

1. **Discovery** — discovers all resources that define a `statistics-enabled` attribute
2. **Presentation** — presents them in a filterable list for easy navigation
3. **Bulk Operation** — enables users to enable/disable statistics across multiple resources at once
4. **Expression Support** — supports filter expressions for fine-grained selection

This implementation serves as a blueprint for new task development.

## Current State & Open Work

The task framework and task page are fully implemented. The `statistics-enabled` task serves as the reference implementation.

Open work is tracked in [#189](https://github.com/hal/foundation/issues/189):

- Define additional tasks (SSL configuration, datasource setup, logging configuration, etc.)
- Community contributions welcome (`help wanted` label)

The framework provides a solid foundation for expanding task coverage to cover more advanced management scenarios.
