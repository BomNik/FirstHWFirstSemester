package com.mipt.nikitabumagin.security;

public final class TokenMaskingUtils {

    private static final int VISIBLE_CHARS = 6;

    private TokenMaskingUtils() {
    }

    public static String mask(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        if (token.length() <= VISIBLE_CHARS * 2) {
            return "***";
        }
        return token.substring(0, VISIBLE_CHARS)
                + "***"
                + token.substring(token.length() - VISIBLE_CHARS);
    }
}
