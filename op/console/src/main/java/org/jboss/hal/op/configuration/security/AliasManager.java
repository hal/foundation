package org.jboss.hal.op.configuration.security;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.op.configuration.security.AliasList.aliasList;

class AliasManager implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AliasManager aliasManager(Dispatcher dispatcher, AddressTemplate template) {
        return new AliasManager(dispatcher, template);
    }

    // ------------------------------------------------------ instance

    private final Dispatcher dispatcher;
    private final AddressTemplate template;
    private final HTMLElement root;
    private final AliasList aliasList;

    AliasManager(Dispatcher dispatcher, AddressTemplate template) {
        this.dispatcher = dispatcher;
        this.template = template;
        boolean supportsSetSecret = "credential-store".equals(template.last().key);
        this.aliasList = aliasList(supportsSetSecret);
        this.root = div().add(aliasList).element();
        loadAliases();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    private void loadAliases() {
        Operation operation = new Operation.Builder(template.resolve(), "read-aliases").build();
        dispatcher.execute(operation, result -> {
            List<String> aliases = new ArrayList<>();
            for (ModelNode node : result.asList()) {
                aliases.add(node.asString());
            }
            aliasList.show(aliases);
        });
    }
}
