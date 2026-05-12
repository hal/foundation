/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta;

import org.junit.jupiter.api.Test;

import static org.jboss.hal.meta.StatementContextFactory.domainStatementContext;
import static org.jboss.hal.meta.StatementContextFactory.standaloneStatementContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataResolverTest {

    @Test
    public void hostServer() {
        String[][] fixtures = new String[][]{
                // host placeholder
                new String[]{"/{domain.controller}", "/host=primary"},
                new String[]{"/{selected.host}", "/host=secondary"},
                // host/server placeholders
                new String[]{"/{domain.controller}/{selected.server}", "/host=primary/server=server1"},
                new String[]{"/{selected.host}/{selected.server}", "/host=secondary/server=server1"},
                new String[]{"/{selected.host}/server=*", "/host=secondary/server=*"},
                new String[]{"/{selected.host}/server=server1", "/host=secondary/server=server1"},
                new String[]{
                        "/{selected.host}/{selected.server}/subsystem=undertow/server=*/host=*",
                        "/host=secondary/server=server1/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/{selected.host}/{selected.server}/subsystem=undertow/server=default-server/host=default-host",
                        "/host=secondary/server=server1/subsystem=undertow/server=default-server/host=default-host",
                },
                // host/server-config placeholders
                new String[]{"/{selected.host}/{selected.server-config}", "/host=secondary/server-config=server2"},
                new String[]{"/{selected.host}/server-config=*", "/host=secondary/server-config=*"},
                new String[]{"/{selected.host}/server-config=server1", "/host=secondary/server-config=server1"},
                new String[]{
                        "/{selected.host}/{selected.server-config}/subsystem=undertow/server=*/host=*",
                        "/host=secondary/server-config=server2/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/{selected.host}/{selected.server-config}/subsystem=undertow/server=default-server/host=default-host",
                        "/host=secondary/server-config=server2/subsystem=undertow/server=default-server/host=default-host"
                },
                // host wildcard
                new String[]{"/host=*", "/host=*"},
                // host/server wildcards
                new String[]{"/host=*/server=*", "/host=*/server=*"},
                new String[]{
                        "/host=*/server=*/subsystem=undertow/server=*/host=*",
                        "/host=*/server=*/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/host=*/server=*/subsystem=undertow/server=default-server/host=default-host",
                        "/host=*/server=*/subsystem=undertow/server=default-server/host=default-host"
                },
                // host/server-config wildcards
                new String[]{"/host=*/server-config=*", "/host=*/server-config=*"},
                new String[]{
                        "/host=*/server-config=*/subsystem=undertow/server=*/host=*",
                        "/host=*/server-config=*/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/host=*/server-config=*/subsystem=undertow/server=default-server/host=default-host",
                        "/host=*/server-config=*/subsystem=undertow/server=default-server/host=default-host"
                },
                // host value
                new String[]{"/host=primary", "/host=primary"},
                // host/server values
                new String[]{"/host=primary/server=server1", "/host=primary/server=server1"},
                new String[]{
                        "/host=primary/server=server1/subsystem=undertow/server=server1/host=*",
                        "/host=primary/server=server1/subsystem=undertow/server=server1/host=*"
                },
                new String[]{
                        "/host=primary/server=server1/subsystem=undertow/server=default-server/host=default-host",
                        "/host=primary/server=server1/subsystem=undertow/server=default-server/host=default-host"
                },
                // host/server-config values
                new String[]{"/host=primary/server-config=server1", "/host=primary/server-config=server1"},
                new String[]{
                        "/host=primary/server-config=server1/subsystem=undertow/server=*/host=*",
                        "/host=primary/server-config=server1/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/host=primary/server-config=server1/subsystem=undertow/server=default-server/host=default-host",
                        "/host=primary/server-config=server1/subsystem=undertow/server=default-server/host=default-host"
                },
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void profile() {
        String[][] fixtures = new String[][]{
                new String[]{"/{selected.profile}", "/profile=*"},
                new String[]{"/{selected.profile}/subsystem=io", "/profile=*/subsystem=io"},
                new String[]{"/{selected.profile}/subsystem=io/worker=*", "/profile=*/subsystem=io/worker=*"},
                new String[]{"/{selected.profile}/subsystem=io/worker=default", "/profile=*/subsystem=io/worker=default"},
                new String[]{"/profile=*", "/profile=*"},
                new String[]{"/profile=*/subsystem=io", "/profile=*/subsystem=io"},
                new String[]{"/profile=*/subsystem=io/worker=*", "/profile=*/subsystem=io/worker=*"},
                new String[]{"/profile=*/subsystem=io/worker=default", "/profile=*/subsystem=io/worker=default"},
                new String[]{"/profile=full", "/profile=*"},
                new String[]{"/profile=full/subsystem=io", "/profile=*/subsystem=io"},
                new String[]{"/profile=full/subsystem=io/worker=*", "/profile=*/subsystem=io/worker=*"},
                new String[]{"/profile=full/subsystem=io/worker=default", "/profile=*/subsystem=io/worker=default"},
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (String[] fixture : fixtures) {
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template);
        }
    }

    @Test
    public void serverGroup() {
        String[][] fixtures = new String[][]{
                new String[]{"/{selected.server-group}", "/server-group=main-server-group"},
                new String[]{"/{selected.server-group}/jvm=*", "/server-group=main-server-group/jvm=*"},
                new String[]{"/{selected.server-group}/jvm=jvm1", "/server-group=main-server-group/jvm=jvm1"},
                new String[]{"/server-group=*", "/server-group=*"},
                new String[]{"/server-group=*/jvm=*", "/server-group=*/jvm=*"},
                new String[]{"/server-group=*/jvm=jvm1", "/server-group=*/jvm=jvm1"},
                new String[]{"/server-group=main-server-group", "/server-group=main-server-group"},
                new String[]{"/server-group=main-server-group/jvm=*", "/server-group=main-server-group/jvm=*"},
                new String[]{"/server-group=main-server-group/jvm=jvm1", "/server-group=main-server-group/jvm=jvm1"},
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void standaloneDeployment() {
        String[][] fixtures = new String[][]{
                new String[]{"/{selected.deployment}", "/deployment=hello-world"},
                new String[]{"/{selected.deployment}/subsystem=batch", "/deployment=*/subsystem=batch"},
                new String[]{"/deployment=*", "/deployment=*"},
                new String[]{"/deployment=*/subsystem=batch", "/deployment=*/subsystem=batch"},
                new String[]{"/deployment=kitchensink", "/deployment=kitchensink"},
                new String[]{"/deployment=kitchensink/subsystem=batch", "/deployment=*/subsystem=batch"},
        };
        MetadataResolver resolver = new MetadataResolver(standaloneStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void domainDeployment() {
        String[][] fixtures = new String[][]{
                new String[]{
                        "/server-group=main-server-group/{selected.deployment}",
                        "/server-group=main-server-group/deployment=hello-world"
                },
                new String[]{
                        "/server-group=main-server-group/{selected.deployment}/subsystem=batch",
                        "/server-group=main-server-group/deployment=*/subsystem=batch"
                },
                new String[]{
                        "/server-group=main-server-group/deployment=*",
                        "/server-group=main-server-group/deployment=*"},
                new String[]{
                        "/server-group=main-server-group/deployment=*/subsystem=batch",
                        "/server-group=main-server-group/deployment=*/subsystem=batch"
                },
                new String[]{
                        "/server-group=main-server-group/deployment=kitchensink",
                        "/server-group=main-server-group/deployment=kitchensink"},
                new String[]{
                        "/server-group=main-server-group/deployment=kitchensink/subsystem=batch",
                        "/server-group=main-server-group/deployment=*/subsystem=batch"
                },
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void selectedResource() {
        String[][] fixtures = new String[][]{
                new String[]{"/subsystem=logging/console-handler={selected.resource}",
                        "/subsystem=logging/console-handler=bar"},
        };
        MetadataResolver resolver = new MetadataResolver(standaloneStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void standalone() {
        // Combination of all fixtures from above.
        // Some templates don't make sense in standalone mode if no placeholder is used.
        // But they're included here to verify that they're not modified by the template resolver.
        String[][] fixtures = new String[][]{
                // ------------------------------------------------------ hostServer()
                // host placeholder
                new String[]{"/{domain.controller}", "/"},
                new String[]{"/{selected.host}", "/"},
                // host/server placeholders
                new String[]{"/{domain.controller}/{selected.server}", "/"},
                new String[]{"/{selected.host}/{selected.server}", "/"},
                new String[]{"/{selected.host}/server=*", "/server=*"},
                new String[]{"/{selected.host}/server=server1", "/server=server1"},
                new String[]{
                        "/{selected.host}/{selected.server}/subsystem=undertow/server=*/host=*",
                        "/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/{selected.host}/{selected.server}/subsystem=undertow/server=default-server/host=default-host",
                        "/subsystem=undertow/server=default-server/host=default-host"
                },
                // host/server-config placeholders
                new String[]{"/{selected.host}/{selected.server-config}", "/"},
                new String[]{"/{selected.host}/server-config=*", "/server-config=*"},
                new String[]{"/{selected.host}/server-config=server1", "/server-config=server1"},
                new String[]{
                        "/{selected.host}/{selected.server-config}/subsystem=undertow/server=*/host=*",
                        "/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/{selected.host}/{selected.server-config}/subsystem=undertow/server=default-server/host=default-host",
                        "/subsystem=undertow/server=default-server/host=default-host"
                },
                // host wildcard
                new String[]{"/host=*", "/host=*"},
                // host/server wildcards
                new String[]{"/host=*/server=*", "/host=*/server=*"},
                new String[]{
                        "/host=*/server=*/subsystem=undertow/server=*/host=*",
                        "/host=*/server=*/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/host=*/server=*/subsystem=undertow/server=default-server/host=default-host",
                        "/host=*/server=*/subsystem=undertow/server=default-server/host=default-host"
                },
                // host/server-config wildcards
                new String[]{"/host=*/server-config=*", "/host=*/server-config=*"},
                new String[]{
                        "/host=*/server-config=*/subsystem=undertow/server=*/host=*",
                        "/host=*/server-config=*/subsystem=undertow/server=*/host=*"},
                new String[]{
                        "/host=*/server-config=*/subsystem=undertow/server=default-server/host=default-host",
                        "/host=*/server-config=*/subsystem=undertow/server=default-server/host=default-host"
                },
                // host value
                new String[]{"/host=primary", "/host=primary"},
                // host/server values
                new String[]{"/host=primary/server=server1", "/host=primary/server=server1"},
                new String[]{
                        "/host=primary/server=server1/subsystem=undertow/server=server1/host=*",
                        "/host=primary/server=server1/subsystem=undertow/server=server1/host=*"
                },
                new String[]{
                        "/host=primary/server=server1/subsystem=undertow/server=default-server/host=default-host",
                        "/host=primary/server=server1/subsystem=undertow/server=default-server/host=default-host"
                },
                // host/server-config values
                new String[]{"/host=primary/server-config=server1", "/host=primary/server-config=server1"},
                new String[]{
                        "/host=primary/server-config=server1/subsystem=undertow/server=*/host=*",
                        "/host=primary/server-config=server1/subsystem=undertow/server=*/host=*"
                },
                new String[]{
                        "/host=primary/server-config=server1/subsystem=undertow/server=default-server/host=default-host",
                        "/host=primary/server-config=server1/subsystem=undertow/server=default-server/host=default-host"
                },
                // ------------------------------------------------------ profile()
                new String[]{"/{selected.profile}", "/"},
                new String[]{"/{selected.profile}/subsystem=io", "/subsystem=io"},
                new String[]{"/{selected.profile}/subsystem=io/worker=*", "/subsystem=io/worker=*"},
                new String[]{"/{selected.profile}/subsystem=io/worker=default", "/subsystem=io/worker=default"},
                new String[]{"/profile=*", "/profile=*"},
                new String[]{"/profile=*/subsystem=io", "/profile=*/subsystem=io"},
                new String[]{"/profile=*/subsystem=io/worker=*", "/profile=*/subsystem=io/worker=*"},
                new String[]{"/profile=*/subsystem=io/worker=default", "/profile=*/subsystem=io/worker=default"},
                new String[]{"/profile=full", "/profile=full"},
                new String[]{"/profile=full/subsystem=io", "/profile=full/subsystem=io"},
                new String[]{"/profile=full/subsystem=io/worker=*", "/profile=full/subsystem=io/worker=*"},
                new String[]{"/profile=full/subsystem=io/worker=default", "/profile=full/subsystem=io/worker=default"},
                // ------------------------------------------------------ serverGroup()
                new String[]{"/{selected.server-group}", "/"},
                new String[]{"/{selected.server-group}/jvm=*", "/jvm=*"},
                new String[]{"/{selected.server-group}/jvm=jvm1", "/jvm=jvm1"},
                new String[]{"/server-group=*", "/server-group=*"},
                new String[]{"/server-group=*/jvm=*", "/server-group=*/jvm=*"},
                new String[]{"/server-group=*/jvm=jvm1", "/server-group=*/jvm=jvm1"},
                new String[]{"/server-group=main-server-group", "/server-group=main-server-group"},
                new String[]{"/server-group=main-server-group/jvm=*", "/server-group=main-server-group/jvm=*"},
                new String[]{"/server-group=main-server-group/jvm=jvm1", "/server-group=main-server-group/jvm=jvm1"},
                // ------------------------------------------------------ standaloneDeployment()
                new String[]{"/{selected.deployment}", "/deployment=hello-world"},
                new String[]{"/{selected.deployment}/subsystem=batch", "/deployment=*/subsystem=batch"},
                new String[]{"/deployment=*", "/deployment=*"},
                new String[]{"/deployment=*/subsystem=batch", "/deployment=*/subsystem=batch"},
                new String[]{"/deployment=kitchensink", "/deployment=kitchensink"},
                new String[]{"/deployment=kitchensink/subsystem=batch", "/deployment=*/subsystem=batch"},
                // ------------------------------------------------------ selectedResource()
                new String[]{"/subsystem=logging/console-handler={selected.resource}",
                        "/subsystem=logging/console-handler=bar"},
        };
        MetadataResolver resolver = new MetadataResolver(standaloneStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void unknownPlaceholder() {
        String[][] fixtures = new String[][]{
                new String[]{
                        "/{domain.controller}/{selected.server}/subsystem=undertow/server=default-server/host=default-host/location={selected.resource}/filter-ref={unknown}",
                        "/host=primary/server=server1/subsystem=undertow/server=default-server/host=default-host/location=bar/filter-ref={unknown}"
                },
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }

    @Test
    public void special() {
        // any special test cases should go here
        String[][] fixtures = new String[][]{
                new String[]{
                        "/core-service=management/access=authorization/constraint=application-classification/type=datasources/classification=data-source/applies-to=\\/deployment\\=*\\/subdeployment\\=*\\/subsystem\\=datasources\\/data-source\\=*",
                        "/core-service=management/access=authorization/constraint=application-classification/type=datasources/classification=data-source/applies-to=\\/deployment\\=*\\/subdeployment\\=*\\/subsystem\\=datasources\\/data-source\\=*"
                },
        };
        MetadataResolver resolver = new MetadataResolver(domainStatementContext());
        for (int i = 0; i < fixtures.length; i++) {
            String[] fixture = fixtures[i];
            assertEquals(fixture[1], resolver.resolve(AddressTemplate.ofTrusted(fixture[0])).template,
                    String.format("fixtures[%d]", i));
        }
    }
}