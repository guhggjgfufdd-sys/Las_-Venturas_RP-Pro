package com.samp.mobile.launcher.config;

import android.app.Activity;

public class Config {

    public static Activity currentContext;

    public static final String SERVER_NAME = "Las Venturas RP";
    public static final String SERVER_HOST = "142.132.203.47";
    public static final int SERVER_PORT = 21299;
    public static final String SERVER_ADDRESS = SERVER_HOST + ":" + SERVER_PORT;

    public static boolean isAllowedServer(String host, int port) {
        return SERVER_HOST.equals(host) && SERVER_PORT == port;
    }
}
