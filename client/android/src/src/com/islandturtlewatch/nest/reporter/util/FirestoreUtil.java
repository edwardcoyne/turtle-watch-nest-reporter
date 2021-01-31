package com.islandturtlewatch.nest.reporter.util;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class FirestoreUtil {
    // Static only.
    private FirestoreUtil() {}

    public static String UserPath() {
        final String username =
                FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        return "/season/" + Calendar.getInstance().get(Calendar.YEAR) + "/user/" + username;
    }
}
