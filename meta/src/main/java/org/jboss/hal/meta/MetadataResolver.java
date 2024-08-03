package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.meta.Placeholder.SELECTED_RESOURCE;

/**
 * Template resolver for the {@link MetadataRepository}.
 * <p>
 * This resolver
 * <ol>
 *     <li>
 *         resolves the template against the {@link StatementContext} and then
 *     </li>
 *     <li>
 *         replaces the values of the segments with "*" depending on the operation mode,
 *         the key of the segment, the index of the segment, and the length of the template:
 *         <table>
 *             <colgroup>
 *                 <col style="width: 30%;">
 *                 <col style="width: 30%;">
 *                 <col style="width: 40%;">
 *             </colgroup>
 *             <thead><tr><th>Segment</th><th>Replacement</th><th>Condition</th></tr></thead>
 *             <tbody>
 *                 <tr><td>host=foo</td><td>host=*</td><td>domain mode && index == 0 && length > 1</td></tr>
 *                 <tr><td>server-group=foo</td><td>server-group=*</td><td>domain mode && index == 0 && length > 1</td></tr>
 *                 <tr><td>profile=foo</td><td>profile=*</td><td>domain mode && index == 0</td></tr>
 *                 <tr><td>server=foo</td><td>server=*</td><td>domain mode && index == 1</td></tr>
 *                 <tr><td>server-config=foo</td><td>server-config=*</td><td>domain mode && index == 1</td></tr>
 *                 <tr><td>deployment=foo</td><td>deployment=*</td><td>(standalone mode && index == 0 && length > 1) || (domain mode && index == 1 && length > 2)</td></tr>
 *                 <tr><td>foo={selected.resource}</td><td>foo=*</td><td>always</td></tr>
 *                 <tr><td>&lt;anything else&gt;</td><td>no replacement</td><td>always</td></tr>
 *             </tbody>
 *         </table>
 *     </li>
 * </ol>
 */
class MetadataResolver implements TemplateResolver {

    private final StatementContext statementContext;
    private final StatementContextResolver statementContextResolver;

    MetadataResolver(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.statementContextResolver = new StatementContextResolver(statementContext);
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        if (!template.isEmpty()) {
            int index = 0;
            int length = template.size();
            List<Segment> segments = new ArrayList<>();
            AddressTemplate resolved = statementContextResolver.resolve(template);

            for (Segment segment : resolved) {
                String key = segment.key;
                String value = segment.value;
                if (segment.containsPlaceholder() && SELECTED_RESOURCE.equals(segment.placeholder())) {
                    value = "*";
                } else {
                    if (statementContext.standalone()) {
                        if (DEPLOYMENT.equals(key) && index == 0 && length > 1) {
                            value = "*";
                        }
                    } else {
                        switch (key) {
                            case HOST:
                            case SERVER_GROUP:
                                if (length > 1 && index == 0) {
                                    value = "*";
                                }
                                break;

                            case PROFILE:
                                if (index == 0) {
                                    value = "*";
                                }
                                break;

                            case SERVER:
                            case SERVER_CONFIG:
                                if (index == 1) {
                                    value = "*";
                                }
                                break;

                            case DEPLOYMENT:
                                if (index == 1 && length > 2) {
                                    value = "*";
                                }
                                break;

                            default:
                                break;
                        }
                    }
                }
                segments.add(new Segment(key, value));
                index++;
            }
            return AddressTemplate.of(segments);
        }
        return template;
    }
}
