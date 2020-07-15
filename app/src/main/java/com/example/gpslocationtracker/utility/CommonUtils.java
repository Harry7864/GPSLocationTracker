package com.example.gpslocationtracker.utility;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    public final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$");

    public final static Pattern EMAIL_ADDRESS_PATTERN_1 = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    public final static Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9-]+[_A-Za-z0-9-]*(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    public final static Pattern INVALID_EMAIL_PATTERN = Pattern.compile("^[0-9-]+[_0-9-]*(\\.[_0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    public final static Pattern PASSWORD_VALIDATION_1 = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z*\\d]).{6,20})");
    public final static Pattern PASSWORD_VALIDATION = Pattern.compile("[A-Za-z0-9\\#\\'\\*\\+\\-\\:\\=\\@\\^\\_\\`]+$");
    public final static Pattern WEB_URL_PATTERN = Patterns.WEB_URL;
    public final static Pattern NUMERIC_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
    public final static Pattern FIRST_LAST_NAME_PATTERN = Pattern.compile("^[A-Za-z]+[A-Za-z0-9-\\.\\-\\_\\']*$");
    public final static Pattern MOBILE_NUMBER = Pattern.compile("^([0-9\\+]|\\(\\d{1,3}\\))[0-9\\-\\. ]{3,15}$");


    private static final boolean YES = true;
    private static final boolean NO = false;

    private static final String EMAIL_PATTERN_1 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String EMAIL_PATTERN_2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+";



    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceName() {
        return Build.MODEL;
    }

    public static String getAppVersion(Context context) {
        String version = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /*public static String bindRupeeSymbol(Context context, String str) {
        return context.getResources().getString(R.string.Rs) + " " + str;
    }*/

    public static String convertToSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c",
                count / Math.pow(1000, exp),
                "kmgtpe".charAt(exp - 1));
    }

    public static InputFilter getEditTextFilter() {
        return new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) // put your condition here
                        sb.append(c);
                    else
                        keepOriginal = false;
                }
                if (keepOriginal)
                    return null;
                else {
                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
                Matcher ms = ps.matcher(String.valueOf(c));
                return ms.matches();
            }
        };
    }

    public static String reverseWords(String input, String splitSeparator, String appendSeparator) {
        Deque<String> words = new ArrayDeque<>();
        for (String word : input.split(splitSeparator)) {
            if (!word.isEmpty()) {
                words.addFirst(word);
            }
        }
        StringBuilder result = new StringBuilder();
        while (!words.isEmpty()) {
            result.append(words.removeFirst());
            if (!words.isEmpty()) {
                result.append(appendSeparator);
            }
        }
        return result.toString();
    }
/*
    public static void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            activity.getWindow().setStatusBarColor(activity.getResources().getColor(R.color.colorPrimary));
        }
    }*/

    public static void buttonScaleTransition(RelativeLayout btn) {
        PropertyValuesHolder pvhx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0, 1);
        PropertyValuesHolder pvhy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(btn, pvhx, pvhy);
        objectAnimator.setDuration(500);
        objectAnimator.reverse();
        objectAnimator.start();
    }

    public static void buttonScaleTransition1(Button btn) {
        PropertyValuesHolder pvhx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0, 1);
        PropertyValuesHolder pvhy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(btn, pvhx, pvhy);
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*----------------
    This Method is used for check Special character in string
    * ----------------*/

    public static boolean checkSpecialCharacter(String value) {
        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(value);
        boolean bs = ms.matches();
        if (bs) {
            return true;
        }
        return bs;
    }

      /*----------------
    This Method is used to convert First letter of string to upper case
    * ----------------*/

    public static String capFirstLetter(String str) {
        String cap = str.substring(0, 1).toUpperCase() + str.substring(1);
        return cap;
    }

       /*----------------
    This Method is used to convert First letter of every word of string to upper case
    * ----------------*/

    public static String capitalize(String capString) {
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    /*----------------
    This Method is used for check Valid Email ID
    * ----------------*/

    public static boolean checkEmail(String email) {
        return !INVALID_EMAIL_PATTERN.matcher(email).matches() && EMAIL_PATTERN.matcher(email).matches();
    }

    /*----------------
    This Method is used for check FirstName and LastName
    * ----------------*/

    public static boolean checkFirstLastName(String name) {
        return FIRST_LAST_NAME_PATTERN.matcher(name).matches();
    }

    /*----------------
    This Method is used for check Password
    * ----------------*/

    public static boolean checkPassword(String password) {
        return PASSWORD_VALIDATION.matcher(password).matches();
        // return PASSWORD_VALIDATION.matcher(password).matches();
    }

    /*----------------
    This Method is used for check Empty Data in EditText
    * ----------------*/

    public static boolean nonEmpty(EditText editText) {
        if (editText != null && !(TextUtils.isEmpty(editText.getText().toString().trim()))) {
            return YES;
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    /*----------------
    This Method is used for check Special character in string with Message
    * ----------------*/


    public static boolean nonEmpty(Context context, EditText editText, String msg) {
        if (nonEmpty(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    /*----------------
    This Method is used for check Valid Email ID
    * ----------------*/

    public static boolean validateEmail(EditText editText) {
        if (nonEmpty(editText)) {
            String emailAsString = removeBlankSpace(editText.getText().toString());
            return emailAsString.matches(EMAIL_PATTERN_1)
                    || emailAsString.matches(EMAIL_PATTERN_2);

        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    /*----------------
    This Method is used for check Valid Email ID
    * ----------------*/

    public static boolean validateEmail(Context context, EditText editText, String msg) {
        if (validateEmail(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    /*----------------
    This Method is used for match Min Length
    * ----------------*/

    public static boolean matchMinLength(EditText editText, int length) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.length() >= length ? YES : NO;
        } else {
            Log.e("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean matchMinLength(Context context, EditText editText, int length, String msg) {
        if (matchMinLength(editText, length)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    /*----------------
    This Method is used for check No Special Character in String
    * ----------------*/

    public static boolean noSpecialCharacters(EditText editText) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.matches("[a-zA-Z0-9.? ]*");
        } else {
            Log.e("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean noSpecialCharacters(Context context, EditText editText, String msg) {
        if (noSpecialCharacters(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    /*----------------
    This Method is used for match Length
    * ----------------*/

    public static boolean matchLength(EditText editText, int length) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.length() == length;
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean matchLength(Context context, EditText editText, int length, String msg) {
        if (matchLength(editText, length)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    /*----------------
    This Method is used for check Mobile Validation
    * ----------------*/

    public static boolean mobileNumberValidation(EditText editText) {
        if (nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString().trim());
            return Patterns.PHONE.matcher(mobileNumber).matches();
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, String msg) {
        if (mobileNumberValidation(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean mobileNumberValidation(EditText editText, Pattern pattern) {
        if (nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString());
            return pattern.matcher(mobileNumber).matches();
        } else {
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, Pattern pattern, String msg) {
        if (mobileNumberValidation(editText, pattern)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean mobileNumberValidation(EditText editText, int minLength, int maxLength) {
        if (minLength > 0 && maxLength > 0 && nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString().trim());
            return mobileNumber.length() >= minLength && mobileNumber.length() <= maxLength;
        } else {
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, int minLength, int maxLength, String msg) {
        if (mobileNumberValidation(editText, minLength, maxLength)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean matchText(EditText baseEditText, EditText... editTexts) {
        if (nonEmpty(baseEditText)) {
            String matchString = baseEditText.getText().toString();
            for (EditText editText : editTexts) {
                if (editText == null || !(matchString.equals(editText.getText().toString()))) {
                    return NO;
                }
            }
        } else {
            return NO;
        }
        return YES;
    }

    public static boolean matchText(Context context, String msg, EditText baseEditText, EditText... editTexts) {
        if (matchText(baseEditText, editTexts)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean matchPassword(String password1, String password2) {
        if (password1.equals(password2)) {
            return YES;
        } else {
            return NO;
        }
    }

    public static boolean checkAllEditTexts(EditText... editTexts) {
        for (EditText edit : editTexts) {
            if (edit == null || !(edit.getText().toString().trim().length() > 0)) {
                return NO;
            }
        }
        return YES;
    }

    public static boolean checkAllEditTexts(Context context, String msg, EditText... editTexts) {
        if (checkAllEditTexts(editTexts)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String removeBlankSpace(String value) {
        value = value.replace(" ", "");
        return value;
    }

    public static boolean isValidUrl(String url) {
        Matcher m = WEB_URL_PATTERN.matcher(url);
        return m.matches();
    }

    public static boolean isValidNumeric(String number) {
        boolean isValid = false;
        CharSequence inputStr = number;
        Matcher matcher = NUMERIC_PATTERN.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isNullString(String string) {
        try {
            return string.trim().equalsIgnoreCase("null") || string.trim() == null || string.trim().length() < 0 || string.trim().equals("");
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isNotNullString(String string) {
        try {
            return string.trim() != null || string.trim().length() > 0 || !string.trim().equals("");
        } catch (Exception e) {
            return true;
        }
    }

    public static void showSoftKeyboard(Context context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Context context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getAgeFromBirthDay(String birthDate) {
        int y, m, d, age = 0;
        try {
            Calendar calBirth = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            calBirth.setTime(sdf.parse("" + birthDate));

            GregorianCalendar cal = new GregorianCalendar();
            y = cal.get(Calendar.YEAR);
            m = cal.get(Calendar.MONTH);
            d = cal.get(Calendar.DAY_OF_MONTH);
            cal.set(calBirth.get(Calendar.YEAR), (calBirth.get(Calendar.MONTH) + 1), calBirth.get(Calendar.DAY_OF_MONTH));
            age = y - cal.get(Calendar.YEAR);
            if ((m < cal.get(Calendar.MONTH))
                    || ((m == cal.get(Calendar.MONTH)) && (d < cal
                    .get(Calendar.DAY_OF_MONTH)))) {
                --age;
            }
            /*if (age < 0)
                throw new IllegalArgumentException("Age < 0");*/
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }

    /*---------------
     * Method for Make TextView Resizable
     * -------------*/

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                String text;
                int lineEndIndex;
                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    lineEndIndex = tv.getLayout().getLineEnd(0);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else {
                    lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                }
                tv.setText(text);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                        viewMore), TextView.BufferType.SPANNABLE);
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spannableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spannableText)) {
            ssb.setSpan(new MySpannable(true) {

                @Override
                public void onClick(View widget) {
                    tv.setLayoutParams(tv.getLayoutParams());
                    tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                    tv.invalidate();
                    if (viewMore) {
                        makeTextViewResizable(tv, -1, "View Less", false);
                    } else {
                        makeTextViewResizable(tv, 4, "View More", true);
                    }

                }
            }, str.indexOf(spannableText), str.indexOf(spannableText) + spannableText.length(), 0);

        }
        return ssb;

    }

    public static class MySpannable extends ClickableSpan {

        private boolean isUnderline = false;

        /**
         * Constructor
         */
        public MySpannable(boolean isUnderline) {
            this.isUnderline = isUnderline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {

            ds.setUnderlineText(isUnderline);
            ds.setColor(Color.parseColor("#0067b0"));

        }

        @Override
        public void onClick(View widget) {

        }
    }

    /**
     * Purpose: This method is used to convert Inputstream to string.
     * ----------------------------------------
     * -------------------------------------
     */
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }


    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception e) {

        }
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static Bitmap decodeSampledBitmapFromUri(String path, int reqWidth,
                                                    int reqHeight) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    public static boolean isValidEmailId(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public static boolean isValidPrice(String price) {

        return Pattern.compile("[0-9]+([,.][0-9]{1,2})?").matcher(price).matches();
    }
    // "[0-9]+([,.][0-9]{1,2})?";
    // ^([1-9][0-9]{0,2})?(\.[0-9]?)?$

    public static boolean isValidPercent(String percent) {

        return Pattern.compile("^[1-9]\\d{0,7}(?:\\.\\d{1,4})?$").matcher(percent).matches();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }

        return inSampleSize;
    }

    public static boolean mobileVarification(String phoneNumber) {

        System.out.println(phoneNumber.length());
        String regex = "^([0-9]|\\+|\\()([1-9|0-9||\\) ]*)([1-9|0-9])$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }
    //** METHOD FOR SAVING IMAGE *//*

    public static String downloadImage(Context cxt, Bitmap bmp) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ExpenseManager");
        myDir.mkdirs();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
        Calendar calendar = Calendar.getInstance();
        String date = formatter.format(new Date(calendar.getTimeInMillis()));
        //String fname = date + ".jpg";

        String fname = "fragment_profile.jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
            options.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap_temp = BitmapFactory.decodeFile(file.toString(), options);


            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, out);


            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("File Path : -", file.toString());

        return file.toString();
    }

    public synchronized static String saveContactsImage(Context cxt, Bitmap bmp, int i) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/ExpenseManager");
        myDir.mkdirs();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
        Calendar calendar = Calendar.getInstance();
        String date = formatter.format(new Date(calendar.getTimeInMillis()));
        //String fname = date + ".jpg";

        String fname = "contact" + i + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;
            options.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap_temp = BitmapFactory.decodeFile(file.toString(), options);


            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);


            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("File Path : -", file.toString());

        return file.toString();
    }

    public static void toShowError(EditText editTextObject, String msg) {
        editTextObject.setError(msg);
        editTextObject.setFocusableInTouchMode(true);
        editTextObject.setFocusable(true);
        editTextObject.requestFocus();
        editTextObject.setSelection(editTextObject.getText().toString().length());
    }

    public static String Base64Image(String imagepath) {
        String encodedImage = "";
        if (!imagepath.isEmpty()) {
            try {

                Bitmap bm = BitmapFactory.decodeFile(imagepath); //SB
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);// is
                byte[] b = baos.toByteArray();

                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                Log.d("sb", encodedImage);

            } catch (Exception ignored) {
            }

        }
        return encodedImage;
    }

    public static String bitmapToString(Bitmap bitmap) {
        String encodedImage = "";
        try {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d("sb", encodedImage);

        } catch (Exception ignored) {
        }
        return encodedImage;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected() && netInfo.isAvailable();
    }

    public static String Base64Encode(ByteArrayOutputStream bytes) {
        String encodeImage = null;
        byte[] imageBytes = bytes.toByteArray();
        encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodeImage;
    }

    public static boolean emailValidator(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static Typeface getFont(Context context, int tag) {
        if (tag == 100) {
            return Typeface.createFromAsset(context.getAssets(),
                    "fonts/Gravity-Regular.otf");
        } else if (tag == 101) {
            return Typeface.createFromAsset(context.getAssets(),
                    "fonts/Gravity-Bold.otf");
        }
        return Typeface.DEFAULT;
    }

    public static int getCameraPhotoOrientation(Uri imageUri, String imagePath, Context context) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static void AlertDialog(Context context, String title, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}

