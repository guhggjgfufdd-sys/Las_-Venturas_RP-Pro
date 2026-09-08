package com.samp.mobile.launcher.config;

import android.app.Activity;

public class Config {

    public static Activity currentContext;

    public static final String SERVER_NAME = "Las Venturas RP";
    public static final String SERVER_HOST = "142.132.203.47";
    public static final int SERVER_PORT = 21299;
    public static final String SERVER_ADDRESS = SERVER_HOST + ":" + SERVER_PORT;

    // The archive is streamed to a temporary file and verified before extraction.
    public static final String GAME_ARCHIVE_URL = "https://drive.usercontent.google.com/download?id=1JW2RscmpHNz8g51phxjjKkubTV2ZDEmD&export=download&confirm=t&uuid=492ecdc7-c09d-449b-923d-a7e6b51762ba";
    public static final long GAME_ARCHIVE_SIZE_BYTES = 716092010L;
    // Replace this with a full 64-character SHA-256 when the archive is republished with its final checksum.
    public static final String GAME_ARCHIVE_SHA256 = "e06ce14e8e8ebc4bcb246aa57d9c2efdbcc99ad7e9c218";
    public static final String[] REQUIRED_GAME_FILES = {
            "Text/american.dxt",
            "Textures/fonts/RussianFont.png"
    };

    public static boolean isAllowedServer(String host, int port) {
        return SERVER_HOST.equals(host) && SERVER_PORT == port;
    }
}
