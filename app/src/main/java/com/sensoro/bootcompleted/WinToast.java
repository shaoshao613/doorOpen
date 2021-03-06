package com.sensoro.bootcompleted;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class WinToast {
    
    public static void toast(Context context, int textRes) {
        CharSequence text = context.getResources().getText(textRes);
        makeText(context, text).show();
    }
    
    public static void toast(Context context, CharSequence sequence) {
        Toast toast = makeText(context, sequence);
        if (toast != null) {
            toast.show();
        }
    }
    
    public static void toastWithCat(Context context, int textRes, boolean isHappy) {
        CharSequence text = context.getResources().getText(textRes);
        toastWithCat(context, text, isHappy);
    }
    
    public static void toastWithCat(Context context, CharSequence text, boolean isHappy) {
        Toast result = new Toast(context);
        
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.ui_toast, null);
        result.setView(v);
        ImageView iv = (ImageView) v.findViewById(android.R.id.icon);
        
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        tv.setText(text);
        
        result.setGravity(Gravity.CENTER, 0, 0);
        result.setDuration(Toast.LENGTH_SHORT);
        result.show();
    }
    
    public static Toast makeText(final Context context, final CharSequence text) {
        // ((Activity) context).runOnUiThread(new Runnable() {
        //
        // @Override
        // public void run() {
        final Toast resultInner = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.ui_toast, null);
        resultInner.setView(v);
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        tv.setText(text);
        
        resultInner.setGravity(Gravity.CENTER, 0, 0);
        resultInner.setDuration(Toast.LENGTH_SHORT);
        // }
        // });
        
        return resultInner;
    }
}
