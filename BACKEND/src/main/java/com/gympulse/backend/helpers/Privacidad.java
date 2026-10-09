package com.gympulse.backend.helpers;

public final class Privacidad {

    private Privacidad() {
    }

    public static String enmascararEmail(String email) {
        if (email == null) {
            return null;
        }
        String limpio = email.trim();
        int arroba = limpio.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        return limpio.charAt(0) + "***" + limpio.substring(arroba);
    }
}
