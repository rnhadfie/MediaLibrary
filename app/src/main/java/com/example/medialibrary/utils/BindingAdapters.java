package com.example.medialibrary.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    @BindingAdapter("imageBytes")
    public static void setImageBytes(ImageView imageView, byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageDrawable(null);
        }
    }
}
