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
package org.jboss.hal.ui;

import org.jboss.elemento.intl.Duration;
import org.jboss.elemento.intl.NumberFormat;
import org.jboss.elemento.intl.Unit;

import static org.jboss.elemento.intl.DurationFormat.durationFormat;
import static org.jboss.elemento.intl.DurationFormatOptions.durationFormatOptions;
import static org.jboss.elemento.intl.Format.long_;
import static org.jboss.elemento.intl.Format.percent;
import static org.jboss.elemento.intl.Format.unit;
import static org.jboss.elemento.intl.NumberFormat.numberFormat;
import static org.jboss.elemento.intl.NumberFormatOptions.numberFormatOptions;
import static org.jboss.elemento.intl.Unit.byte_;
import static org.jboss.elemento.intl.Unit.gigabyte;
import static org.jboss.elemento.intl.Unit.kilobyte;
import static org.jboss.elemento.intl.Unit.megabyte;
import static org.jboss.elemento.intl.Unit.terabyte;
import static org.jboss.hal.ui.UIContext.uic;

public class Format {

    /**
     * Converts a file size in bytes into a human-readable string format using appropriate units such as bytes, kilobytes,
     * megabytes, gigabytes, or terabytes.
     *
     * @param size the file size in bytes
     * @return a formatted string representing the file size in a human-readable format with units
     */
    public static String humanReadableBytes(long size) {
        if (size <= 0) {
            return "0";
        }
        Unit[] units = new Unit[]{byte_, kilobyte, megabyte, gigabyte, terabyte};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        NumberFormat numberFormat = numberFormat(uic().settings().locale(), numberFormatOptions()
                .style(unit)
                .unit(units[digitGroups])
                .maximumFractionDigits(0));
        return numberFormat.format(size / Math.pow(1024, digitGroups));
    }

    /**
     * Formats the elapsed time in milliseconds to a human-readable format, for example, "1 minute, 16 seconds".
     *
     * @param duration in milliseconds
     * @return The string representation of the human-readable format.
     */
    public static String duration(long duration) {
        if (duration < 1000) {
            Duration d = Duration.duration().milliSeconds((int) duration);
            return durationFormat(uic().settings().locale(), durationFormatOptions().style(long_)).format(d);
        }

        duration /= 1000;
        int sec = (int) duration % 60;
        duration /= 60;
        int min = (int) duration % 60;
        duration /= 60;
        int hour = (int) duration % 24;
        duration /= 24;
        int day = (int) duration;
        Duration d = Duration.duration().days(day).hours(hour).minutes(min).seconds(sec);
        return durationFormat(uic().settings().locale(), durationFormatOptions().style(long_))
                .format(d);
    }

    public static String percent(double value) {
        NumberFormat numberFormat = numberFormat(uic().settings().locale(), numberFormatOptions()
                .style(percent));
        double failSafeValue = (value > 1) ? value / 100 : value;
        return numberFormat.format(failSafeValue);
    }
}
