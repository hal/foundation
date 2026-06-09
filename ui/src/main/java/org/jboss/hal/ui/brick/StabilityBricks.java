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
package org.jboss.hal.ui.brick;

import java.util.function.Supplier;

import org.jboss.hal.env.Stability;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.style.Status;

import static org.jboss.hal.env.Stability.EXPERIMENTAL;
import static org.jboss.hal.env.Stability.PREVIEW;
import static org.patternfly.icon.IconSets.fas.circleInfo;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.triangleExclamation;

/**
 * Factory methods for mapping WildFly {@link Stability} levels to PatternFly statuses and icons. Used by the stability
 * label component to render consistent visual indicators across the console.
 * <ul>
 *     <li>{@link Stability#EXPERIMENTAL} — danger status, flask icon</li>
 *     <li>{@link Stability#PREVIEW} — warning status, triangle-exclamation icon</li>
 *     <li>All other levels — info status, circle-info icon</li>
 * </ul>
 */
public final class StabilityBricks {

    /** Maps a stability level to a PatternFly {@link Status} for colour-coding labels and badges. */
    public static Status stabilityStatus(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return Status.danger;
        } else if (stability == PREVIEW) {
            return Status.warning;
        }
        return Status.info;
    }

    /** Returns an icon representing the given stability level. */
    public static PredefinedIcon stabilityIcon(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return flask();
        } else if (stability == PREVIEW) {
            return triangleExclamation();
        }
        return circleInfo();
    }

    /** Returns a supplier that creates an icon for the given stability level. */
    public static Supplier<PredefinedIcon> stabilityIconSupplier(Stability stability) {
        return () -> stabilityIcon(stability);
    }

    private StabilityBricks() {
    }
}
