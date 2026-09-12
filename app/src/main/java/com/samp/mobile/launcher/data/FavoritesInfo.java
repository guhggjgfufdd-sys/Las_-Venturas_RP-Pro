package com.samp.mobile.launcher.data;

import android.content.Context;
import android.os.Environment;

import com.samp.mobile.launcher.util.Util;
import com.samp.mobile.launcher.config.ServerConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

/* renamed from: ru.unisamp_mobile.launcher.FavoritesInfo */
public class FavoritesInfo {
    private static boolean bLoaded = false;
    private static ArrayList<FavoriteServerData> serverList = new ArrayList<>();

    public static void Load(Context mContext) {
        ClearFavorites();
        serverList.add(new FavoriteServerData(1, 1, ServerConfig.HOST, ServerConfig.PORT));
        bLoaded = true;
        Save(mContext);
    }
    public static void Save(Context context) {
        try {
            File file = new File(context.getExternalFilesDir(null), "SAMP/favorites.json");
            if (file.exists()) {
                file.delete();
            }
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("servers", new JSONArray());
            JSONArray jSONArray = jSONObject.getJSONArray("servers");
            for (int i = 0; i < serverList.size(); i++) {
                FavoriteServerData favoriteServerData = serverList.get(i);
                jSONArray.put(i, new JSONObject());
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                jSONObject2.put("id", favoriteServerData.id);
                jSONObject2.put("serverid", favoriteServerData.serverid);
                jSONObject2.put("ip", favoriteServerData.ip);
                jSONObject2.put("port", favoriteServerData.port);
            }
            new File(context.getExternalFilesDir(null), "SAMP/").mkdirs();
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(jSONObject.toString());
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean AddServer(Context context, int i, int i2, String str, int i3) {
        if (!ServerConfig.HOST.equals(str) || ServerConfig.PORT != i3) {
            return false;
        }
        if (!bLoaded) {
            Load(context);
        }
        return false;
    }
    public static boolean RemoveServer(Context context, String str, int i) {
        return false;
    }
    public static boolean IsServerExists(Context context, int i, int i2, String str, int i3, boolean z) {
        if (!bLoaded) {
            Load(context);
        }
        Iterator<FavoriteServerData> it = serverList.iterator();
        while (it.hasNext()) {
            FavoriteServerData next = it.next();
            if (next.id == i && next.serverid == i2 && i != 0 && i2 != 0 && next.ip.equals("")) {
                if (z) {
                    next.queried = true;
                }
                next.ip = str;
                next.port = i3;
                Save(context);
                return true;
            } else if (next.ip.equals(str) && next.port == i3) {
                if (z) {
                    next.queried = true;
                }
                return true;
            }
        }
        return false;
    }

    public static FavoriteServerData GetServerByIpAndPort(Context context, String str, int i) {
        if (!bLoaded) {
            Load(context);
        }
        Iterator<FavoriteServerData> it = serverList.iterator();
        while (it.hasNext()) {
            FavoriteServerData next = it.next();
            if (next.ip.equals(str) && next.port == i) {
                return next;
            }
        }
        return null;
    }

    public static ArrayList<FavoriteServerData> getServerList(Context context) {
        if (!bLoaded) {
            Load(context);
        }
        return new ArrayList<>(serverList);
    }

    static void ClearFavorites() {
        serverList.clear();
    }
}
