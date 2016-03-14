package edu.berkeley.cs160.represent.represent.utils;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


/**
 * Created by Avi on 10/10/15.
 */
public class ImageUtils {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "ImageUtils";

    public static String path;

    public static String imgPath;

    public static String getMechImgPath() {
        return mechImgPath;
    }

    public static void setMechImgPath(String mechImgPath) {
        ImageUtils.mechImgPath = mechImgPath;
    }

    public static String getImgPath() {
        return imgPath;
    }

    public static void setImgPath(String imgPath) {
        ImageUtils.imgPath = imgPath;
    }

    public static String mechImgPath;

//    public static void loadImage(Context context, File file, ImageView target) {
//        if (file == null) {
//            Picasso.with(context)
//                    .load(R.drawable.ic_person_black_48dp)
//                    .fit()
//                    .centerCrop()
//                    .into(target);
//        } else {
//            Picasso.with(context)
//                    .load(file)
//                    .fit()
//                    .centerCrop()
//                    .into(target);
//        }
//    }

    public static void loadImageFromUrl(Context context, String url, ImageView target) {
        Picasso.with(context)
                .load(url)
                .fit()
                .into(target);
    }

    public static void loadImageFromUrlWithoutFitting(Context context, String url, ImageView target) {
        Picasso.with(context)
                .load(url)
                .into(target);
    }

    public static void loadImage(Context context, int resId, ImageView target) {
        Picasso.with(context)
                .load(resId)
                .fit()
                .centerCrop()
                .into(target);
    }


}
