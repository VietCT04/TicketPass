package com.ticketpass.api.auth;

final class DisplayNameNormalizer {

    private DisplayNameNormalizer() {
    }

    static String normalizeForStorage(String displayName) {
        String normalized = displayName.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("display_name must not be blank");
        }
        return normalized;
    }
}
