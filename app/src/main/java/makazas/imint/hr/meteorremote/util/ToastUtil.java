package makazas.imint.hr.meteorremote.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    public static void showShortToastWithMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToastWithMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
