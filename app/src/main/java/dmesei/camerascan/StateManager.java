package dmesei.camerascan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import dmesei.camerascan.Scanned.ScannedItem;

import static android.content.Context.MODE_PRIVATE;

public class StateManager {
    public static void saveState(Activity context, List list) {
        final SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);

        Log.d("DEBUG SAVESTATE", json);

        editor.putString("lista",json);
        editor.apply();
    }

    public static List<ScannedItem> loadState(Activity context) {
        final SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("lista", "");

        Log.d("DEBUG LOADSTATE", json);

        ArrayList<ScannedItem> elementos = gson.fromJson(json, new TypeToken<List<ScannedItem>>(){}.getType());
        return elementos;
    }
}
