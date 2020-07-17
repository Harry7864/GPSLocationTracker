package com.example.gpslocationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.gpslocationtracker.utility.PreferenceManager;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    PreferenceManager preferenceManager;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 5;

    String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    String rationale = "Please provide Some permission so that you can use it...";
    Permissions.Options options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        preferenceManager = new PreferenceManager(this);
        options = new Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Warning");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Permissions.check(SplashActivity.this, permissions, rationale, options, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        if (preferenceManager.getLoginSession()) {
                            startActivity(new Intent(SplashActivity.this, InOutActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });
            }
        },3000);

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<String>();
            List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsNeeded.add("Write Storage");
            }
            if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionsNeeded.add("Read Storage");
            }
            if (!addPermission(permissionsList, Manifest.permission.CAMERA)) {
                permissionsNeeded.add("Camera");
            }
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionsNeeded.add("Access Fine Location");
            }if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                permissionsNeeded.add("Access Coarse Location");
            }if (!addPermission(permissionsList, Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND)) {
                permissionsNeeded.add("COMPANION_RUN_IN");
            }if (!addPermission(permissionsList, Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND)) {
                permissionsNeeded.add("COMPANION_USE_DATA");
            }
            if (permissionsList.size() > 0)
            {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (preferenceManager.getLoginSession()) {
                            startActivity(new Intent(SplashActivity.this, InOutActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= 23)

            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);

                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    perms.put(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND, PackageManager.PERMISSION_GRANTED);

                }

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                        || perms.get(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND) == PackageManager.PERMISSION_GRANTED
                        || perms.get(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (preferenceManager.getLoginSession()) {
                                startActivity(new Intent(SplashActivity.this, InOutActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                    }, SPLASH_TIME_OUT);
                    return;
                } else {
                    // Permission Denied
                    Log.d("###", "Some permissions are not granted ask again ");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        showDialogOK("Some Permissions are required for Open Camera",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                checkPermission();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                // proceed with logic by disabling the related features or quit the app.
                                                dialog.dismiss();
                                                finish();
                                                break;
                                        }
                                    }
                                });
                    } else {
                        explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?");
                    }
//                    if (Build.VERSION.SDK_INT >= 23) {
//                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName())));
//                        finish();
//                    }

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void explain(String msg) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        dialog.create().dismiss();
                        finish();
                    }
                });
        dialog.show();
    }
    //        public boolean isCheckPermission (Context ctx){
//            if (Build.VERSION.SDK_INT >= 23) {
//                if (ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED
//                        && ctx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                        && ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    Log.v("##", "Permission is granted1");
//                    return true;
//                } else {
//
//                    Log.v("##", "Permission is revoked1");
//                    ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
//                    return false;
//                }
//            } else { //permission is automatically granted on sdk<23 upon installation
//                Log.v("##", "Permission is granted1");
//                return true;
//            }
//        }
}
