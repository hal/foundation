# Model Graph Tools

Model Graph Tools (MGT) is an optional companion for the HAL management console. It provides a searchable, graph-based representation of WildFly's management model — the same model the console uses to configure your server — and makes that knowledge available to both the console and AI-powered coding assistants.

## What Problem Does It Solve?

WildFly's management model is large. A single WildFly release defines hundreds of resources, thousands of attributes, and a web of capabilities that link them together. The console already lets you browse and edit this model, but answering questions like _"which attributes were deprecated between WildFly 38 and 39?"_ or _"what capabilities does the Elytron subsystem provide?"_ requires manually navigating many resources.

MGT solves this by importing the complete static metadata of a WildFly release into a [Neo4j](https://neo4j.com/) graph database. Every resource, attribute, operation, capability, and their relationships become graph nodes and edges that can be queried instantly. The console can then tap into this data to surface richer information — and AI tools like [Claude Code](https://docs.anthropic.com/en/docs/claude-code/overview) or [GitHub Copilot](https://github.com/features/copilot) can use it as a knowledge base when writing WildFly configuration code.

## How It Works

MGT runs as a lightweight container alongside your WildFly server. Each container image is built for a specific WildFly version and exposes a REST API on a port derived from that version number. For example:

| WildFly Version | MGT Port | REST API Base |
|---|---|---|
| 38.0 | 7380 | `http://localhost:7380/api/` |
| 39.0 | 7390 | `http://localhost:7390/api/` |
| 41.0 | 7410 | `http://localhost:7410/api/` |

The port formula is **7000 + major × 10 + minor**, so there is no guesswork involved.

When the console starts, it automatically checks whether a matching MGT container is running. If one is found, the **MGT indicator** in the toolbar lights up and additional features become available. If not, the console works exactly as before — MGT is purely additive.

## Starting MGT

The easiest way to work with MGT is the `mgt` CLI. It handles container management and port mapping for you.

### Install the CLI

::: code-group

```bash [Homebrew]
brew tap hpehl/tap
brew install mgt
```

```bash [Install Script]
curl -fsSL https://model-graph-tools.github.io/mgt/install.sh | sh
```

```bash [Cargo]
cargo install mgt
```

:::

### Start and Stop Containers

Start an MGT container for a specific WildFly version:

```bash
mgt start 41
```

This pulls the matching container image (if needed), maps the correct port automatically, and starts the container. You can start containers for multiple versions at once:

```bash
mgt start 38,39,41
```

Stop containers when you no longer need them:

```bash
mgt stop 41
mgt stop --all
```

List running containers with `mgt ps`.

### Using Docker or Podman Directly

If you prefer not to install the CLI, you can start a container directly — just make sure the port mapping follows the formula (**7000 + major × 10 + minor**):

```bash
docker run -d --name mgt-41 -p 7410:7474 quay.io/halconsole/mgt:41.0
```

For more details on the `mgt` CLI, see the [tooling repository](https://github.com/model-graph-tools/tooling).

## What MGT Offers

Once the MGT container is running and detected by the console, you gain access to several capabilities:

### For Console Users

- **Richer metadata**: The console can display additional context about resources, attributes, and operations drawn from the full management model graph.
- **Cross-version awareness**: MGT knows the complete model for a specific WildFly version, so the console can highlight version-specific details that the live server's management API alone does not expose.

### For AI-Assisted Development

MGT includes an [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server that lets AI coding assistants query the management model directly. This means tools like Claude Code can:

- Search for resources, attributes, and operations by name or pattern
- Look up allowed values and default settings
- Find deprecated features and their replacements
- Compare management model changes between WildFly versions
- Discover capability relationships between subsystems

This turns the AI assistant into a WildFly configuration expert that always has the correct, version-specific documentation at hand.

## Console Integration

The MGT indicator in the console toolbar shows the current status at a glance:

| Status | Meaning |
|---|---|
| **Available** | An MGT container matching the connected WildFly version is running and reachable |
| **Not available** | No matching MGT container was detected |

Clicking the indicator opens a panel where you can check availability again or learn more about setting up MGT.

## Available Versions

MGT container images are published for recent WildFly releases. Visit the [container registry on Quay.io](https://quay.io/repository/halconsole/mgt?tab=tags) to see all available tags.

## Further Reading

- [Model Graph Tools Website](https://model-graph-tools.github.io/) — project website with detailed documentation and guides
- [MGT on GitHub](https://github.com/model-graph-tools) — source code and issue tracker
- [HAL Foundation on GitHub](https://github.com/hal/foundation) — source code for the management console
- [WildFly Documentation](https://docs.wildfly.org/) — official WildFly documentation
