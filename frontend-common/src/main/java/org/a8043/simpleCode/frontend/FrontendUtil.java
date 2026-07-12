package org.a8043.simpleCode.frontend;

public class FrontendUtil {
    public static String formatDuration(long millis) {
        if (millis < 0) {
            millis = 0;
        }

        long totalSeconds = millis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        boolean hasValue = false;

        if (days > 0) {
            sb.append(days).append(I18n.get("time.day"));
            hasValue = true;
        }

        if (hours > 0) {
            if (hasValue) {
                sb.append(" ");
            }
            sb.append(hours).append(I18n.get("time.hour"));
            hasValue = true;
        }

        if (minutes > 0) {
            if (hasValue) {
                sb.append(" ");
            }
            sb.append(minutes).append(I18n.get("time.minute"));
            hasValue = true;
        }

        if (seconds > 0 || !hasValue) {
            if (hasValue) {
                sb.append(" ");
            }
            sb.append(seconds).append(I18n.get("time.second"));
        }

        return sb.toString();
    }
}
