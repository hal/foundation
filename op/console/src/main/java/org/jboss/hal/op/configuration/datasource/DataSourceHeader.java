package org.jboss.hal.op.configuration.datasource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.ui.resource.shell.ResourceHeader;
import org.jboss.hal.ui.resource.extension.ResourceHeaderProvider;
import org.patternfly.component.IconPosition;
import org.patternfly.icon.PredefinedIcon;

import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JNDI_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.ui.brick.JndiBricks.renderJndiName;
import static org.patternfly.icon.IconSets.fas.database;
import static org.patternfly.token.Token.globalIconColorDisabled;
import static org.patternfly.token.Token.globalIconColorStatusSuccessDefault;

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
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        return dispatcher.execute(operation).then(result -> {
            boolean enabled = result.hasDefined(ENABLED) && result.get(ENABLED).asBoolean();
            // --pf-t--global--icon--color--disabled
            // --pf-t--global--icon--color--status--success--default
            PredefinedIcon icon = database().style("color",
                    enabled ? globalIconColorStatusSuccessDefault.var : globalIconColorDisabled.var);
            defaultHeader.iconAndText(icon, template.last().value, IconPosition.start);
            defaultHeader.textDelegate().append(small(enabled ? "enabled" : "disabled").element());
            if (result.hasDefined(JNDI_NAME)) {
                defaultHeader.add(renderJndiName(result.get(JNDI_NAME).asString()));
            }
            return Promise.resolve(defaultHeader);
        });
    }
}
