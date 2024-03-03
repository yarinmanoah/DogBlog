package com.example.dogblog.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

public class SignalUtils {
    private static SignalUtils signalUtilsGenerator = null;

    private final Context context;
    private static Vibrator v;


    public SignalUtils(Context context) {
        this.context = context;
    }

    public static void init(Context context){
        if(signalUtilsGenerator == null){
            signalUtilsGenerator = new SignalUtils(context);
            v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static SignalUtils getInstance(){
        return signalUtilsGenerator;
    }

    public void toast(String string){
        Toast
                .makeText(context, string, Toast.LENGTH_SHORT)
                .show();
    }

    public void vibrate(long milliseconds){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else {
            //deprecated in API 26
            v.vibrate(milliseconds);
        }
    }
}
