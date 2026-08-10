# Resource Management

Managing WildFly resources efficiently requires clear presentation of attributes, their values, constraints, and relationships. The console provides several features to make resource configuration more intuitive and less error-prone.

## Attribute Groups

Attributes are organized using **attribute groups** defined in the resource description metadata. These groups logically cluster related configuration options, making it easier to find and understand the purpose of each attribute. For example, security-related attributes appear together, performance tuning options are grouped, and basic configuration is separated from advanced options.

## Auto-Grouping for Large Resources

When working with resources that define 20 or more attributes and no metadata-defined groups exist, the console applies **automatic alphabetical grouping**. Attributes are organized into letter-range sections (such as "A – D", "E – H"), reducing visual clutter and making it faster to locate specific attributes by name.

## Attribute Descriptions

Rather than displaying long descriptions inline, the console shows **attribute descriptions as popovers**. Hovering over an attribute name reveals its full description, including details about its purpose, valid values, and any constraints. This approach keeps the interface clean while ensuring documentation is always accessible.

## Capability References

Attributes that reference capabilities (such as a security domain, SSL context, or data source) display **links to the referenced resources**. Clicking these links navigates to the referenced resource, making it easy to verify or modify related configuration. When multiple references exist, a popup presents all targets.

## Nested Attributes

WildFly's management model supports complex nested attributes. The console handles both **simple nested attributes** (editable structures with a few levels of nesting) and **complex nested attributes** (read-only display of deeply nested structures). This ensures you can view and modify nested configuration without needing to resort to CLI commands.

## Allowed Values

For attributes with constrained value sets (enumerations or predefined options), the console displays the **allowed values** alongside the attribute. This eliminates guesswork and reduces configuration errors by showing exactly what values are valid.

## Expression Syntax Highlighting

WildFly expressions (such as `${jboss.bind.address:127.0.0.1}`) are highlighted with **syntax highlighting** to distinguish literal values from expressions. This visual distinction makes it clear when configuration uses environment variables, system properties, or vault expressions.
