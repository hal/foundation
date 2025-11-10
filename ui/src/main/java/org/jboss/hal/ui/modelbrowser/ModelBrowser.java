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
package org.jboss.hal.ui.modelbrowser;

import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.AddResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.DeleteResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.style.Classes;

import elemental2.dom.AddEventListenerOptions;
import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DOMRect;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.dom.MutationRecord;
import elemental2.dom.TouchEvent;
import elemental2.webstorage.WebStorageWindow;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.mousemove;
import static org.jboss.elemento.EventType.mouseup;
import static org.jboss.elemento.EventType.resize;
import static org.jboss.elemento.EventType.touchcancel;
import static org.jboss.elemento.EventType.touchend;
import static org.jboss.elemento.EventType.touchmove;
import static org.jboss.elemento.EventType.touchstart;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.resources.LocalStorage.MODEL_BROWSER_TREE_WIDTH;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResource;
import static org.jboss.hal.ui.resource.ResourceDialogs.deleteResource;
import static org.patternfly.core.Aria.hidden;
import static org.patternfly.core.Aria.label;
import static org.patternfly.core.Aria.orientation;
import static org.patternfly.core.Roles.separator;
import static org.patternfly.style.Classes.handle;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.resizing;

public class ModelBrowser implements Attachable, IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelBrowser modelBrowser(AddressTemplate template) {
        return new ModelBrowser(template);
    }

    // ------------------------------------------------------ instance

    private static final double STEP_PX = 25;
    private static final double TREE_DEFAULT_WIDTH = 400;
    private static final String TREE_WIDTH = "--hal-c-model-browser-tree-width";
    private static final String TREE_MIN_WIDTH = "--hal-c-model-browser-tree-min-width";
    private static final String TREE_MAX_WIDTH = "--hal-c-model-browser-tree-max-width";
    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());

    final ModelBrowserTree tree;
    final ModelBrowserDetail detail;
    private final AddressTemplate template;
    private final HTMLElement root;
    private final HTMLElement splitter;
    private boolean dragging;
    private double containerLeft;
    private ModelBrowserNode rootMbn;
    private HandlerRegistration mouseDownHandler;
    private HandlerRegistration mouseMoveHandler;
    private HandlerRegistration touchStartHandler;
    private HandlerRegistration touchMoveHandler;
    private HandlerRegistration resizeHandler;

    public ModelBrowser(AddressTemplate template) {
        this.template = template;
        this.dragging = false;
        this.containerLeft = 0;
        this.tree = new ModelBrowserTree(this);
        this.detail = new ModelBrowserDetail(this);
        this.root = div().css(halComponent(modelBrowser))
                .add(tree)
                .add(splitter = div().css(halComponent(modelBrowser, Classes.splitter))
                        .role(separator)
                        .aria(orientation, "vertical")
                        .aria(label, "Resize")
                        .add(div().css(halComponent(modelBrowser, Classes.splitter, handle))
                                .aria(hidden, true))
                        .element())
                .add(detail)
                .element();

        AddResource.listen(root, this::add);
        DeleteResource.listen(root, this::delete);
        SelectInTree.listen(root, this::select);
        Attachable.register(this, this);
        load();
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        try {
            String savedWidth = WebStorageWindow.of(window).localStorage.getItem(MODEL_BROWSER_TREE_WIDTH);
            if (savedWidth != null) {
                double savedPx = parseCSSLength(trimOrEmpty(savedWidth), TREE_DEFAULT_WIDTH);
                double normalized = clampWidth(snapToStep(savedPx));
                root.style.setProperty(TREE_WIDTH, normalized + "px");
            }
        } catch (Exception e) {
            logger.error("Failed to read saved model tree width: %s", e.getMessage());
        }

        AddEventListenerOptions once = AddEventListenerOptions.create();
        once.setOnce(true);
        AddEventListenerOptions passive = AddEventListenerOptions.create();
        passive.setPassive(true);
        mouseDownHandler = bind(splitter, EventType.mousedown, event -> {
            event.preventDefault();
            startDrag();
            mouseMoveHandler = bind(window, mousemove, this::mouseMove);
            bind(window, mouseup, once, this::mouseUp);
        });
        touchStartHandler = bind(splitter, touchstart, passive, event -> {
            if (event.touches.length > 0) {
                startDrag();
                touchMoveHandler = bind(window, touchmove, passive, this::touchMove);
                bind(window, touchend, once, this::touchEnd);
                bind(window, touchcancel, once, this::touchEnd);
            }
        });
        resizeHandler = EventType.bind(window, resize, event -> {
            if (!dragging) {return;}
            // Adjust current width to respect new clamp range
            CSSStyleDeclaration styles = org.jboss.elemento.DomGlobal.window.getComputedStyle(root);
            double current = parseCSSLength(styles.getPropertyValue(TREE_WIDTH).trim(), TREE_DEFAULT_WIDTH);
            onMove(containerLeft + current);
        });
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        if (resizeHandler != null) {
            resizeHandler.removeHandler();
        }
        if (touchMoveHandler != null) {
            touchMoveHandler.removeHandler();
        }
        if (touchStartHandler != null) {
            touchStartHandler.removeHandler();
        }
        if (mouseMoveHandler != null) {
            mouseMoveHandler.removeHandler();
        }
        if (mouseDownHandler != null) {
            mouseDownHandler.removeHandler();
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    void home() {
        if (rootMbn != null) {
            tree.unselect();
            detail.show(rootMbn);
        }
    }

    void load() {
        if (template.fullyQualified()) {
            uic().metadataRepository().lookup(template, metadata -> {
                ResourceAddress address = template.resolve(uic().statementContext());
                Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
                uic().dispatcher().execute(operation, result -> {
                    String name = template.isEmpty() ? "Management Model" : template.last().value;
                    rootMbn = new ModelBrowserNode(template, name, RESOURCE);
                    tree.load(parseChildren(rootMbn, result, true));
                    detail.show(rootMbn);
                });
            });
        } else {
            // TODO Add error empty state
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", template);
        }
    }

    private void add(AddResource.Details details) {
        addResource(details.parent, details.child, details.singleton).then(__ -> {
            tree.select(details.parent.identifier());
            tree.reload();
            return null;
        });
    }

    private void delete(DeleteResource.Details details) {
        deleteResource(details.template)
                .then(__ -> {
                    tree.select(details.template.anonymiseLast().identifier());
                    tree.reload();
                    return null;
                })
                .catch_(error -> {
                    Notifications.error("Failed to delete resource", String.valueOf(error));
                    return null;
                });
    }

    private void select(SelectInTree.Details details) {
        if (rootMbn != null) {
            if (details.template != null) {
                // select by address template
                if (details.template.template.startsWith(rootMbn.template.template)) {
                    tree.select(details.template);
                } else {
                    logger.error("Unable to select %s: %s is not a sub-template of %s",
                            details.template, details.template, rootMbn.template);
                }
            } else if (details.identifier != null) {
                if (details.parentIdentifier != null) {
                    // select by parent/child identifier
                    tree.select(details.parentIdentifier, details.identifier);
                } else {
                    // select by identifier
                    tree.select(details.identifier);
                }
            }
        } else {
            logger.error("Unable to select %s: Root template is null!", template);
        }
    }

    // ------------------------------------------------------ splitter

    private void startDrag() {
        DOMRect rect = root.getBoundingClientRect();
        containerLeft = rect.left;
        dragging = true;
        root.classList.add(modifier(resizing));
    }

    private void endDrag() {
        if (!dragging) {return;}
        dragging = false;
        root.classList.remove(modifier(resizing));
    }

    private void mouseMove(MouseEvent event) {
        onMove(event.clientX);
    }

    private void mouseUp(MouseEvent event) {
        if (mouseMoveHandler != null) {
            mouseMoveHandler.removeHandler();
        }
        endDrag();
    }

    private void touchMove(TouchEvent event) {
        if (dragging && event.touches.length > 0) {
            event.preventDefault();
            onMove(event.touches.getAt(0).clientX);
        }
    }

    private void touchEnd(TouchEvent event) {
        if (touchMoveHandler != null) {
            touchMoveHandler.removeHandler();
        }
        endDrag();
    }

    private void onMove(double clientX) {
        if (!dragging) {return;}
        double raw = clientX - containerLeft;
        // Clamp to allowable range, snap to step, then clamp again to be safe
        double clamped = clampWidth(raw);
        double snapped = clampWidth(snapToStep(clamped));
        String value = snapped + "px";
        root.style.setProperty(TREE_WIDTH, value);
        try {
            WebStorageWindow.of(window).localStorage.setItem(MODEL_BROWSER_TREE_WIDTH, value);
        } catch (Exception ignored) {}
    }

    private double snapToStep(double px) {
        return Math.round(px / STEP_PX) * STEP_PX;
    }

    private double clampWidth(double px) {
        CSSStyleDeclaration styles = org.jboss.elemento.DomGlobal.window.getComputedStyle(root);
        double minPx = parseCSSLength(trimOrEmpty(styles.getPropertyValue(TREE_MIN_WIDTH)), 0);
        double maxPx = parseCSSLength(trimOrEmpty(styles.getPropertyValue(TREE_MAX_WIDTH)),
                Math.round(window.innerWidth * 0.7));
        return Math.max(minPx, Math.min(maxPx, px));
    }

    private double parseCSSLength(String value, double fallbackPx) {
        if (value == null || value.isEmpty()) {return fallbackPx;}
        String v = value.trim();
        try {
            if (v.endsWith("px")) {
                return Double.parseDouble(v.substring(0, v.length() - 2));
            }
            if (v.endsWith("vw")) {
                double pct = Double.parseDouble(v.substring(0, v.length() - 2));
                return window.innerWidth * (pct / 100.0);
            }
            return Double.parseDouble(v);
        } catch (Throwable t) {
            return fallbackPx;
        }
    }

    private static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
