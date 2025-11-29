 # New features

This document highlights the new features of the management console.

## Header

- Notification badge / drawer
- Theme, contrast selector
- Endpoint selector

## Stability level

- Highlight of stability levels for resources, attributes, operations and parameters.

Examples:

- TODO

## Dashboard

Pending...

## Model browser

The model browser has been greatly improved:

- History of selected resources and buttons to go backwards and forwards
- Search the management model for resource addresses, types and names
- Go to arbitrary resources by specifying the resource address
- Clickable breadcrumbs that reflect the currently selected resource address
- Copy the current address to the clipboard
- Descriptions in resources list views
- Follow capability references
- Resizable tree view

### Data

- Filter by name, status (defined/undefined, required/not required, deprecated/not deprecated) and mode (storage/access type)
- Attribute description as popovers
- Links for referenced capabilities (popup for multiple references)
- Support for simple nested attributes
- Support for complex attributes in a read-only view
- Info of allowed values
- Syntax highlighting for expressions

### Attributes

- Filter by name, type, status (required/not required, deprecated/not deprecated) and mode (storage/access type)
- Support for nested attributes

### Operations

- Filter by name, signature (parameters/no parameters, return value/no return value) and status (deprecated/not deprecated)
- Omit/show global operations (remembered as a user setting)
- Execute operations

### Capabilities

- New tab that shows the capabilities of the selected resource

## JavaScript API

Some classes expose a JavaScript API. You have to call the methods using the fully qualified name.

### Metadata

- `org.jboss.hal.meta.MetadataRepository.get(String address)` return the metadata as JSON
- `org.jboss.hal.meta.MetadataRepository.lookup(address)` lookup the metadata as JSON and return a promise
- `org.jboss.hal.meta.MetadataRepository.dump()` dump internal data of the metadata repository

### Logging

See https://hal-console.gitbook.io/elemento/logger#controlling-log-levels-from-javascript
