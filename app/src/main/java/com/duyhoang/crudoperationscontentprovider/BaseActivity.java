package com.duyhoang.crudoperationscontentprovider;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rogerh on 5/3/2018.
 */

public class BaseActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    protected int MY_PHOTO_TAGGING_CODE = 10;

    protected String[] deniedPermissionPhotoTagging;

    protected void requestRuntimePermissions(final Activity activity, final String[] permissions, final int customConstantCode){
        if(permissions.length == 1){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])){
                Snackbar.make(findViewById(android.R.id.content), "App needs permission to work", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Enable", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(activity, permissions, customConstantCode);
                            }
                        })
                        .show();
            }
            else
                ActivityCompat.requestPermissions(activity, permissions, customConstantCode);
        }
        else if(permissions.length > 1 && customConstantCode == MY_PHOTO_TAGGING_CODE){
            int nDeniedPermission = getDeniedPermissionsAmongPhotoTagging(activity, permissions).length;
            if(nDeniedPermission == 1){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermissionPhotoTagging[0])){
                    Snackbar.make(findViewById(android.R.id.content), "App needs permission granted to work", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(activity, deniedPermissionPhotoTagging, customConstantCode);
                                }
                            })
                            .show();
                }
                else{
                    ActivityCompat.requestPermissions(activity, deniedPermissionPhotoTagging, customConstantCode);
                }

            }
            else if(nDeniedPermission == 2){
                if(isFirstTimeRequest2PermissionPhotoTagging()){
                    Snackbar.make(findViewById(android.R.id.content), "App needs permissions to work", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(activity, deniedPermissionPhotoTagging,  customConstantCode);
                                }
                            })
                            .show();
                }
                else
                    ActivityCompat.requestPermissions(activity, deniedPermissionPhotoTagging, customConstantCode);
            }

        }


    }

    private boolean isFirstTimeRequest2PermissionPhotoTagging() {
        SharedPreferences sp = getSharedPreferences("MY_PHOTO_TAGGING_FILE", Activity.MODE_PRIVATE);
        boolean isFirstTime = sp.getBoolean("first_time_request_permissions", true);
        if(isFirstTime){
            sp.edit().putBoolean("first_time_request_permissons", false).commit();
        }
        return  isFirstTime;
    }

    protected String[] getDeniedPermissionsAmongPhotoTagging(Activity activity, String[] permssions) {
        List<String> deniedPermissions = new ArrayList<>();
        for(String p: permssions){
            if(ActivityCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_DENIED)
                deniedPermissions.add(p);
        }
        deniedPermissionPhotoTagging = deniedPermissions.toArray(new String[deniedPermissions.size()]);
        return deniedPermissionPhotoTagging;
    }

}
