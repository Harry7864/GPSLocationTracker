package com.example.gpslocationtracker.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.gpslocationtracker.R;
import com.google.gson.Gson;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tools {
    Context context;
    Dialog dialog;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public Tools() {
    }

    /*@RequiresApi(api = Build.VERSION_CODES.M)
    public static void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(activity.getColor(R.color.colorPrimaryDark));
        }
    }*/

    public Tools(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        mSharedPreferences = context.getSharedPreferences(VariableBag.PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }



    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setWhiteNavigationBar(@NonNull Dialog dialog) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Window window = dialog.getWindow();
            if (window != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                GradientDrawable dimDrawable = new GradientDrawable();
                // ...customize your dim effect here

                GradientDrawable navigationBarDrawable = new GradientDrawable();
                navigationBarDrawable.setShape(GradientDrawable.RECTANGLE);
                navigationBarDrawable.setColor(Color.rgb(226, 226, 226));

                Drawable[] layers = {dimDrawable, navigationBarDrawable};

                LayerDrawable windowBackground = new LayerDrawable(layers);
                windowBackground.setLayerInsetTop(1, metrics.heightPixels);

                window.setBackgroundDrawable(windowBackground);
            }
        }
    }


    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(color));
        }
    }


    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    public static void displayImageRound(final Context ctx, final ImageView img, @DrawableRes int drawable) {
        try {
            Glide.with(ctx).asBitmap().load(drawable).into(new BitmapImageViewTarget(img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(ctx.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    img.setImageDrawable(circularBitmapDrawable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static void displayImageBG(Context ctx, ImageView img, String drawable) {
        try {
            Glide.with(ctx).load(drawable).into(img);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void displayImageOriginal(Context ctx, ImageView img, String drawable) {
        try {
            Glide.with(ctx).load(drawable).apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public static void displayImageOriginal(Context ctx, ImageView img, @DrawableRes int drawable) {
        try {
            Glide.with(ctx).load(drawable)
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void displayImage(Context ctx, ImageView img, String url) {
        Log.e("##", "" + url);
        try {
            Glide.with(ctx).load(url).apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                    .into(img);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("##", e.getMessage());

        }
    }



    public static void displayImageProfile(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url).apply(new RequestOptions().placeholder(R.drawable.app_icon).error(R.drawable.app_icon))
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    public static void displayImageBanner(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url).apply(new RequestOptions().placeholder(R.drawable.app_icon).error(R.drawable.app_icon))
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();


        }
    }


    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        nested.post(new Runnable() {
            @Override
            public void run() {
                nested.scrollTo(500, targetView.getBottom());
            }
        });
    }



    public static void displayImageViewURL(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url)
                    .into(img);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void toast(Context ctx, String msg, int type) {
        //type 0 info, 1 for Error ,2 for Sucess,3 for warning

            Toast.makeText(ctx, "" + msg, Toast.LENGTH_SHORT).show();

    }

    public static void log(String tag, String msg) {
        Log.e(tag, "" + msg);
    }

    public void showLoading() {
        dialog.setContentView(R.layout.loading_layout);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void stopLoading() {
        dialog.dismiss();
    }

    public static String capFirstLetter(String str) {
        String cap = str.substring(0, 1).toUpperCase() + str.substring(1);
        return cap;
    }

    public static String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }
        return capMatcher.appendTail(capBuffer).toString();
    }

    public static String Decode(String code) {
        byte[] decodeValue = Base64.decode(code, Base64.DEFAULT);
        return new String(decodeValue);
    }

    public static String encode(String code) {
        byte[] decodeValue = Base64.encode(code.getBytes(), Base64.DEFAULT);
        return new String(decodeValue);
    }

    public void setObject(String key, Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        mSharedPreferences.edit().putString(key, json).commit();
    }

    public <GenericClass> GenericClass getObject(String key, Class<GenericClass> object) {
        try {
            Gson gson = new Gson();
            String json = mSharedPreferences.getString(key, "");
            return gson.fromJson(json, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean CompareDate(String startdate, String endDate){
        Log.e("##",startdate);
        Log.e("##",endDate);

        boolean status=false;
        Date date1 = null,date2=null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {

            date1 = sdf.parse(startdate);
            date2 = sdf.parse(endDate);

            if (date1.compareTo(date2) < 0){
                status=true;
            }else if (date1.compareTo(date2) > 0) {
                status=false;
            }else if (date1.compareTo(date2) == 0) {
                status=true;
            } else {
                status=false;
            }

        } catch (ParseException e) {
            Log.e("##",e.getLocalizedMessage());
            e.printStackTrace();
        }


        return status;

    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    public static String getLeaveSubtype(String subtype) {
        switch (subtype) {
            case "FH":
                return "First Half";
            case "SH":
                return "Second Half";
            case "F":
                return "Full Day";
            default:
                return "";

        }

    }


    public static String replaceKeyWhiteSpace(String json) {
        return json.replaceAll("[\\s\t\n]", "_");
    }

    public static void hideSoftKeyboard(@NonNull Activity activity) {
        @SuppressLint("WrongConstant") InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
        if (activity.getCurrentFocus() != null && inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
