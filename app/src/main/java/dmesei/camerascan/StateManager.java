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

    private static Gson gson = new Gson();

    public static void saveState(Activity context, List list) {
        final SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        String json = gson.toJson(list);

        editor.putString("lista",json);
        editor.apply();
    }

    public static List<ScannedItem> loadState(Activity context) {
        final SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);

        String json = prefs.getString("lista", "");

        ArrayList<ScannedItem> elementos = gson.fromJson(json, new TypeToken<List<ScannedItem>>(){}.getType());
        return elementos == null ? new ArrayList<ScannedItem>() : elementos;
    }
}
