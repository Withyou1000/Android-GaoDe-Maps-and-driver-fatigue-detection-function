package com.example.myapplication.Method;
import android.content.Context;
import android.widget.Toast;
public class Show {
    private static Toast cur;
    public static void show(Context context, String s) {
        if(cur!=null)
        {
            cur.cancel();
        }
        cur=Toast.makeText(context, s, Toast.LENGTH_LONG);
        cur.show();
    }
}
