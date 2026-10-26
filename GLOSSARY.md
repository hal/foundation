# Glossary

Terms used in the HAL Foundation codebase. Each entry gives the canonical term and its
precise meaning. Code, comments, and documentation should use these terms consistently.

## Model Catalog

The top-level page that exposes the full MGT data surface through a faceted search
interface. Users type a text query and narrow results by selecting facet values in a
sidebar. Not to be confused with the quick search (UniversalSearchBox) or the Model
Browser.

## Facet

A named category of filterable properties in the Model Catalog (e.g., "Data Type",
"Stability"). Each facet contains multiple **facet values**.

## Facet Value

One selectable option within a facet (e.g., "STRING" within the "Data Type" facet).
Displayed with a count showing how many results match that value given all other active
filters.

## Filter

An active selection made by the user — a facet value that has been clicked and is
constraining the result set. Shown as a removable chip in the toolbar.

## Live Filter

A runtime check that cross-references Model Catalog results with the connected WildFly
server. Unlike facets (which query the static MGT Neo4j database), live filters perform
DMR operations against the live server. Examples: "Exists" (does the resource exist on
the server), "Defined" (does the attribute have a value). Deferred to phase 2.

## Subtree Filter

The address-prefix facet in the Model Catalog (filter key: `scope`). Constrains results
to a subtree of the management model (e.g., `/subsystem=elytron`). Called "subtree
filter" in code to avoid overloading the word "scope".

## MGT

Abbreviation for Model Graph Tools — the external Neo4j-backed service that indexes the
WildFly management model. The HAL codebase uses the `ModelGraph` prefix for all classes
that interact with MGT.

## ModelGraphConnection

The CDI bean that manages connectivity to the MGT REST API. Handles URL discovery,
availability checks (`available()`), and ping. Formerly `ModelGraphConnection`.

## ModelGraphCatalog

The CDI bean (`@ApplicationScoped`) that queries the MGT `/api/catalog` endpoint. Takes a
`CatalogFilter` and returns a `Promise<CatalogResponse>` with paginated results and facet
counts. Follows the same async `fetch` pattern as `ModelGraphSearch`.

## CatalogFilter

Plain Java class representing the 30+ query parameters for the catalog API. Builder-style
setters configure filters; `toQueryString()` produces the URL query string. Only non-null
values are included.

## ModelGraphType

Enum representing the four MGT node types: `resource`, `attribute`, `operation`,
`capability`. Shared by both `SearchResult` and `CatalogResult` (unified from the former
`SearchResultType`).
