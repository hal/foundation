# Pipeline Support (WildFly 40)

Overview of attribute support in the UI resource [pipeline](pipeline.md).

Total attributes: **5,803** (4,118 configuration + 1,685 runtime)

## Handlers

| | Handler | Pattern | Count | Priority |
|---|---|---|---|---|
| :white_check_mark: | [Default Item Provider](pipeline/default-item-provider.md) | STRING, BOOLEAN, INT, LONG, DOUBLE, BYTES, LIST of simple type | 5,384 | — |
| :white_check_mark: | [Credential Reference](pipeline/credential-reference.md) | credential-reference family | 49 | — |
| :white_check_mark: | [Time Unit](pipeline/time-unit.md) | keepalive-time | 8 | — |
| :white_check_mark: | [File](pipeline/file.md) | file (logging) | 8 | — |
| :white_check_mark: | [Path / Relative-To](pipeline/path-relative-to.md) | path + relative-to siblings | 31 | — |
| :white_check_mark: | [Relative-To](pipeline/relative-to.md) | standalone relative-to | 1 | — |
| :white_check_mark: | [Map](pipeline/map.md) | free-form key-value maps | 222 | — |
| :white_check_mark: | [Flattening](pipeline/flattening.md) | \*-column (Infinispan JDBC) + other simpleRecord OBJECTs | ~80 | — |
| | [List of Simple Records](pipeline/list-of-simple-records.md) | LIST of OBJECT with simple-type sub-attributes | 19 | HIGH |
| | [List of Nested Lists](pipeline/list-of-nested-lists.md) | LIST of OBJECT with nested LIST sub-attributes | 8 | MEDIUM |
| | [List of Nested Objects](pipeline/list-of-nested-objects.md) | LIST of OBJECT with nested OBJECT sub-attributes | 1 | MEDIUM |
| | [Complex Object](pipeline/complex-object.md) | Complex/recursive OBJECTs (not lists) | 7 | LOW |

## Coverage

~93% of all attributes (~5,387 of 5,803) are covered by the pipeline.

| Storage | Covered | Not covered | Coverage |
|---|---|---|---|
| Configuration (4,118) | ~3,909 | ~209 | ~95% |
| Runtime (1,685) | ~1,478 | ~207 | ~88% |

Runtime attributes are read-only, so even uncovered ones render acceptably as plain text or JSON display.

## Multi-type Resources

Resources with 5+ different attribute types — ideal for comprehensive testing across multiple handlers.

| Resource | Types present | Notes |
|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | STRING, BOOLEAN, INT, LONG, OBJECT | Credential-reference + maps + enums + simple types; available out of the box |
| `/core-service=management/management-interface=http-interface` | STRING, BOOLEAN, INT, LIST, OBJECT | Includes `allowed-origins` (LIST), `http-upgrade` (simpleRecord), `console-enabled` (BOOLEAN) |
| `/core-service=platform-mbean/type=runtime` | STRING, BOOLEAN, LONG, LIST, OBJECT | Read-only; includes `system-properties` (map), `input-arguments` (LIST) |
| `/subsystem=jaxrs` | STRING, BOOLEAN, INT, LIST, OBJECT | JAX-RS subsystem with diverse attribute types |
| `/subsystem=jgroups/channel=ee/protocol=UFC` | STRING, BOOLEAN, INT, LONG, DOUBLE | One of the few resources with DOUBLE attributes |
