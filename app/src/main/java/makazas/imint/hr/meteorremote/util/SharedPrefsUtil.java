package makazas.imint.hr.meteorremote.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utility class for loading/saving shared preferences
 *
 * @author David Takač
 * @version 1.0
 */
public class SharedPrefsUtil {


    /**
     * Saves {@code key} and {@code value} pair
     * in {@link SharedPreferences} of {@code context}.
     *
     * @param context whose {@code SharedPreferences} are used for saving of {@code key} and {@code pair}
     *
     * @param key under which {@code value} is saved
     *
     * @param value which is saved
     */
    public static void save(Context context, String key, String value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();
    }


    /**
     * Returns a preference stored in {@link SharedPreferences} of {@code context} under key {@¢ode key} if it exists. Otherwise
     * returns {@code defaultValue}.
     *
     * @param context whose {@code SharedPreferences} are used for getting of value
     *
     * @param key whose value is returned
     *
     * @param defaultValue value returned if {@code key} contains no mapping.
     *
     * @return {@code String} value stored under {@code key} in {@code SharedPreferences} of this {@code context} if it exists. Otherwise returns {@code defaultValue};
     */
    public static String get(Context context, String key, String defaultValue){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (ClassCastException ignorable) {
            return defaultValue;
        }
    }
}
