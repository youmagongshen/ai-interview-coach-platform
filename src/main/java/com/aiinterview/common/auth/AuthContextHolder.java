package com.aiinterview.common.auth;

public final class AuthContextHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new AuthException("unauthorized");
        }
        return userId;
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}