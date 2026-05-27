package com.access.control.util;

import com.access.control.entity.User;

public final class UserSanitizer {

    private UserSanitizer() {
    }

    public static User withoutPassword(User user) {
        if (user == null) {
            return null;
        }
        user.setPassword(null);
        return user;
    }
}
