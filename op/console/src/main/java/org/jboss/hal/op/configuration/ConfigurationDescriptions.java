package org.jboss.hal.op.configuration;

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

public interface ConfigurationDescriptions {

    // TODO Replace hard-coded previews with HTML templates based on https://handlebarsjs.com/
    // language=html
    SafeHtml CONFIGURATION_DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "Configure subsystems and global resources such as interfaces, socket bindings, paths and system properties.");
    // language=html
    SafeHtml CONFIGURATION_DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "View and modify the configuration for each available subsystem. For example, add a data source, configure a messaging provider, or set up application security.");
    // language=html
    SafeHtml INTERFACE_DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "A logical name for a network interface / IP address / host name to which sockets can be bound. The <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code> configurations all include a section where interfaces can be declared. Other sections of the configuration can then reference those interfaces by their logical name, rather than having to include the full details of the interface (which may vary on different machines).");
    // language=html
    SafeHtml INTERFACE_DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "An interface configuration includes the logical name of the interface as well as information specifying the criteria to use for resolving the actual physical address to use.");
    // language=html
    SafeHtml PATH_DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "A logical name for a filesystem path. The <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code> configurations all include a section where paths can be declared. Other sections of the configuration can then reference those paths by their logical name, rather than having to include the full details of the path (which may vary on different machines).");
    // language=html
    SafeHtml PATH_DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "For example, the logging subsystem configuration includes a reference to the <code>jboss.server.log.dir</code> path that points to the server&#39;s <code>log</code> directory.");
    // language=html
    SafeHtml SOCKET_BINDING_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "A socket binding is a named configuration for a socket. The <code>domain.xml</code> and <code>standalone.xml</code> configurations both include a section where named socket configurations can be declared. Other sections of the configuration can then reference those sockets by their logical name, rather than having to include the full details of the socket configuration (which may vary on different machines).");
    // language=html
    SafeHtml SUBSYSTEM_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "A set of subsystem configurations. A subsystem is an added set of capabilities added to the core server by an extension. As such a subsystem provides servlet handling capabilities, an EJB container, JTA support, etc.");
    // language=html
    SafeHtml SYSTEM_PROPERTY_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "System property values can be set in a number of places in <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code>. The values in <code>standalone.xml</code> are set as part of the server boot process. Values in <code>domain.xml</code> and <code>host.xml</code> are applied to servers when they are launched.");
}
