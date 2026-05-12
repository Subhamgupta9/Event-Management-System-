package com.ems.util;

public final class HtmlUtil {
    private HtmlUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (char current : value.toCharArray()) {
            switch (current) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(current);
            }
        }
        return escaped.toString();
    }

    public static String safeUrl(String value) {
        if (value == null || value.isBlank()) {
            return "#";
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("/") || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return escape(trimmed);
        }

        return "#";
    }
}
