package org.jboss.hal.op.configuration.datasource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.ui.resource.ResourceHeader;
import org.jboss.hal.ui.resource.extension.ResourceHeaderProvider;
import org.patternfly.component.IconPosition;
import org.patternfly.icon.IconSets;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JNDI_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.ui.brick.JndiBricks.renderJndiName;

@ApplicationScoped
public class DataSourceHeader implements ResourceHeaderProvider {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public DataSourceHeader(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    @Override
    public AddressTemplate scope() {
        return AddressTemplate.ofTrusted("subsystem=datasources/data-source=*");
    }

    @Override
    public Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata,
            ResourceHeader defaultHeader) {
        ResourceAddress address = template.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        return dispatcher.execute(operation).then(result -> {
            defaultHeader.iconAndText(IconSets.fas.database(), template.last().value, IconPosition.start);
            if (result.hasDefined(JNDI_NAME)) {
                defaultHeader.add(renderJndiName(result.get(JNDI_NAME).asString()));
            }
            return Promise.resolve(defaultHeader);
        });
    }
}
