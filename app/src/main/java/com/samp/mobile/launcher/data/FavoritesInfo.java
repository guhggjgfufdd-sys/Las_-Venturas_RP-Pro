package com.samp.mobile.launcher.data;

import android.content.Context;

import com.samp.mobile.launcher.config.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class FavoritesInfo {
    private static boolean bLoaded = false;
    private static final ArrayList<FavoriteServerData> serverList = new ArrayList<>();

    private static void resetToLockedServer(Context context) {
        serverList.clear();
        serverList.add(new FavoriteServerData(1, 1, Config.SERVER_HOST, Config.SERVER_PORT));
        bLoaded = true;
        Save(context);
    }

    public static void Load(Context context) {
        // This launcher is intentionally single-server. Any old/custom entries are discarded.
        resetToLockedServer(context);
    }

    public static void Save(Context context) {
        try {
            File file = new File(context.getExternalFilesDir(null), "SAMP/favorites.json");
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            JSONObject root = new JSONObject();
            JSONArray servers = new JSONArray();
            for (FavoriteServerData server : serverList) {
                JSONObject item = new JSONObject();
                item.put("id", server.id);
                item.put("serverid", server.serverid);
                item.put("ip", server.ip);
                item.put("port", server.port);
                servers.put(item);
            }
            root.put("servers", servers);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(root.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean AddServer(Context context, int id, int serverId, String host, int port) {
        if (!Config.isAllowedServer(host, port)) return false;
        if (!bLoaded) Load(context);
        return false;
    }

    public static boolean RemoveServer(Context context, String host, int port) {
        // The official server cannot be removed from this launcher.
        return false;
    }

    public static boolean IsServerExists(Context context, int id, int serverId, String host, int port, boolean queried) {
        if (!bLoaded) Load(context);
        if (!Config.isAllowedServer(host, port)) return false;
        for (FavoriteServerData server : serverList) {
            if (server.ip.equals(host) && server.port == port) {
                if (queried) server.queried = true;
                return true;
            }
        }
        return false;
    }

    public static FavoriteServerData GetServerByIpAndPort(Context context, String host, int port) {
        if (!bLoaded) Load(context);
        for (FavoriteServerData server : serverList) {
            if (server.ip.equals(host) && server.port == port) return server;
        }
        return null;
    }

    public static ArrayList<FavoriteServerData> getServerList(Context context) {
        if (!bLoaded) Load(context);
        return new ArrayList<>(serverList);
    }
}
