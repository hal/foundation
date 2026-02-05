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
package org.jboss.hal.op.endpoint;

import org.jboss.elemento.IsElement;
import org.patternfly.component.form.Form;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.switch_.Switch;
import org.patternfly.core.ObservableValue;

import elemental2.core.TypeError;
import elemental2.dom.HTMLElement;
import elemental2.dom.URL;

import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.ValidationStatus.success;
import static org.patternfly.component.ValidationStatus.warning;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.link;

class EndpointForm implements IsElement<HTMLElement> {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "9990";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String DEFAULT_URL = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT;

    private final Form form;
    private final FormGroupControl nameControl;
    private final TextInput nameInput;
    private final Switch secureSwitch;
    private final TextInput hostInput;
    private final TextInput portInput;
    private final FormGroupControl urlControl;
    private final TextInput urlInput;
    private final ObservableValue<String> url;
    private Endpoint endpoint;
    private boolean newEndpoint;

    EndpointForm(EndpointStorage storage) {
        this.form = form().horizontal()
                .addGroup(formGroup("mi-name").required()
                        .addLabel(formGroupLabel("Name"))
                        .addControl(nameControl = formGroupControl()
                                .addControl(nameInput = textInput("mi-name")
                                        .applyTo(input -> input.autocomplete("off")))))
                .addGroup(formGroup("mi-protocol").required()
                        .addLabel(formGroupLabel("Protocol").noPaddingTop())
                        .addControl(formGroupControl()
                                .add(secureSwitch = switch_("mi-protocol", "mi-protocol", false)
                                        .label("HTTPS"))))
                .addGroup(formGroup("mi-host").required()
                        .addLabel(formGroupLabel("Host"))
                        .addControl(formGroupControl()
                                .addControl(hostInput = textInput("mi-host")
                                        .placeholder(DEFAULT_HOST))))
                .addGroup(formGroup("mi-port").required()
                        .addLabel(formGroupLabel("Port"))
                        .addControl(formGroupControl()
                                .addControl(portInput = textInput(number, "mi-port")
                                        .applyTo(input -> {
                                            input.min(0);
                                            input.max(65535);
                                        })
                                        .placeholder(DEFAULT_PORT))))
                .addGroup(formGroup("mi-url")
                        .addLabel(formGroupLabel("URL"))
                        .addControl(urlControl = formGroupControl()
                                .add(inputGroup()
                                        .addText(inputGroupText().icon(link()))
                                        .addItem(inputGroupItem().fill()
                                                .addControl(urlInput = textInput("mi-url")
                                                        .readonly()))
                                        .addItem(inputGroupItem()
                                                .addButton(button("Ping").control()
                                                        .onClick((event, component) -> ping()))))));

        endpoint = null;
        newEndpoint = true;
        url = ov(DEFAULT_URL).subscribe((current, previous) -> urlInput.value(current));
        nameInput.onChange((e, c, value) -> {
            if (newEndpoint) {
                if (storage.findByName(value) != null) {
                    nameControl.setHelperText(helperText("Management interface with that name already exists", warning));
                    nameInput.validated(warning);
                } else {
                    nameControl.removeHelperText();
                    nameInput.resetValidation();
                }
            }
        });
        secureSwitch.onChange((e, c, value) -> url.set(buildUrl(value, hostInput.value(), portInput.value())));
        hostInput.onChange((e, c, value) -> url.set(buildUrl(secureSwitch.value(), value, portInput.value())));
        portInput.onChange((e, c, value) -> url.set(buildUrl(secureSwitch.value(), hostInput.value(), value)));
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    boolean isValid() {
        if (nameInput.value() == null || nameInput.value().isEmpty()) {
            nameControl.setHelperText(helperText("Must not be empty", error));
            nameInput.validated(error);
            return false;
        } else {
            nameInput.resetValidation();
            return true;
        }
    }

    Endpoint endpoint() {
        if (newEndpoint) {
            return Endpoint.endpoint(nameInput.value(), url.get());
        } else {
            endpoint.name = nameInput.value();
            endpoint.url = urlInput.value();
            return endpoint;
        }
    }

    void reset() {
        nameControl.removeHelperText();
        nameInput.resetValidation();
        urlControl.removeHelperText();
        urlInput.resetValidation();
    }

    void show(Endpoint endpoint) {
        this.endpoint = endpoint;
        this.newEndpoint = endpoint == null;
        if (newEndpoint) {
            nameInput.value("");
            secureSwitch.value(false);
            hostInput.value("");
            portInput.value("");
            url.change(DEFAULT_URL);
        } else {
            URL u = new URL(endpoint.url);
            nameInput.value(endpoint.name);
            secureSwitch.value(u.protocol.equals("https:"));
            hostInput.value(u.hostname);
            portInput.value(u.port);
            url.change(endpoint.url);
        }
    }

    private String buildUrl(boolean secure, String host, String port) {
        StringBuilder builder = new StringBuilder();
        builder.append("http");
        if (secure) {
            builder.append("s");
        }
        builder.append("://");
        if (host == null || host.isEmpty()) {
            builder.append(DEFAULT_HOST);
        } else {
            builder.append(host);
        }
        builder.append(":");
        if (port == null || port.isEmpty()) {
            builder.append(DEFAULT_PORT);
        } else {
            builder.append(port);
        }
        return builder.toString();
    }

    private void ping() {
        String urlValue = url.get();
        String failSafeUrl = urlValue.endsWith("/") ? urlValue.substring(0, urlValue.length() - 1) : urlValue;
        Endpoint.ping(failSafeUrl)
                .then(valid -> {
                    if (valid) {
                        urlControl.setHelperText(helperText("Valid management interface", success));
                        urlInput.validated(success);
                    } else {
                        urlControl.setHelperText(helperText("Not a valid management interface", warning));
                        urlInput.validated(warning);
                    }
                    return null;
                })
                .catch_(failure -> {
                    String message = failure instanceof TypeError
                            ? "Management endpoint not accessible"
                            : "Not a valid management interface";
                    urlControl.setHelperText(helperText(message, error));
                    urlInput.validated(error);
                    return null;
                });
    }
}
