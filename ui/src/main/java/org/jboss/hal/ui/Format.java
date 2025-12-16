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

import org.jboss.elemento.intl.NumberFormat;
import org.jboss.elemento.intl.NumberFormatOptions;

import static org.jboss.elemento.intl.Format.percent;
import static org.jboss.elemento.intl.Format.unit;

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
        String[] units = new String[]{"byte", "kilobyte", "megabyte", "gigabyte", "terabyte"}; // NON-NLS
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        NumberFormat numberFormat = new NumberFormat("en-US", NumberFormatOptions.create()
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
    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public static String duration(long duration) {
        if (duration < 1000) {
            return duration + " ms"; // NON-NLS
        }

        duration /= 1000;

        int sec = (int) duration % 60;
        duration /= 60;

        int min = (int) duration % 60;
        duration /= 60;

        int hour = (int) duration % 24;
        duration /= 24;

        int day = (int) duration;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day)
                    .append(" ")
                    .append(day > 1 ? "days" : "day");
        }
        if (hour > 0 || (day > 0)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(hour)
                    .append(" ")
                    .append(hour > 1 ? "hours" : "hour");
        }
        if (min > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(min)
                    .append(" ")
                    .append(min > 1 ? "minutes" : "minute");
        }
        if (sec > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(sec)
                    .append(" ")
                    .append(sec > 1 ? "seconds" : "second");
        }
        return sb.toString();
    }

    public static String percent(double value) {
        NumberFormat numberFormat = new NumberFormat("en-US", NumberFormatOptions.create()
                .style(percent));
        double failSafeValue = (value > 1) ? value / 100 : value;
        return numberFormat.format(failSafeValue);
    }
}
