package com.tu.courier.util;

import com.tu.courier.entity.User;

public final class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
