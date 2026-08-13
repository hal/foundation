# Features Overview

The HAL management console for WildFly provides a modern, feature-rich interface for managing your WildFly servers and domains. This section highlights the key capabilities that make working with WildFly's management model more efficient and intuitive.

## Header Enhancements

The console header provides quick access to essential controls. The **notification badge and drawer** keeps you informed of important events and alerts. The **theme and contrast selector** lets you customize the visual appearance to match your preferences or accessibility needs. The **endpoint selector** makes it easy to switch between different WildFly instances when managing multiple servers.

## Stability Level Awareness

WildFly's management model includes stability levels (experimental, preview, community, default) to indicate the maturity of features. The console highlights these stability levels across resources, attributes, operations, and parameters, making it clear when you're working with experimental features that may change in future releases.

## Navigation Improvements

Navigation has been refined for a smoother experience. The **finder selection syncs with navigation items**, so clicking a navigation item restores your last selection in that section. **Browser back and forward buttons** work correctly within the finder, making it natural to explore the management model and return to previous views.

## JavaScript API

For advanced users and automation scenarios, several classes expose a JavaScript API accessible from the browser console. The `MetadataRepository` provides methods to retrieve, lookup, and dump metadata for any resource address. Logging can be controlled programmatically using methods documented in the [Elemento Logger guide](https://hal-console.gitbook.io/elemento/logger#controlling-log-levels-from-javascript).

## Additional Resources

For a comprehensive demonstration of these features in action, watch the [WildFly Community Call presentation from December 2025](https://youtu.be/X43dkm1Zdm4).
