package com.example.myapplication.Method;

import android.graphics.Bitmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void saveimage(String path, Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
