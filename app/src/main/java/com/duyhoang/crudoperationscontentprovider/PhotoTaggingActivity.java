package com.duyhoang.crudoperationscontentprovider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PhotoTaggingActivity extends BaseActivity {

    Button btnTakePhoto;
    String[] photoTaggingPermissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA};
    List<String> deniedPermissions;
    boolean isFirstTimeRun = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_tagging);

        btnTakePhoto = (Button)findViewById(R.id.button_take_a_photo);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFirstTimeRun)
                {
                    requestRuntimePermissions(PhotoTaggingActivity.this, photoTaggingPermissions, MY_PHOTO_TAGGING_CODE);
                    isFirstTimeRun = false;
                }
                else
                {
                    if(isAllPhotoTaggingRuntimePermissionsGranted()){
                        Toast.makeText(PhotoTaggingActivity.this, "Go ahead, do you stuff", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        requestRuntimePermissions(PhotoTaggingActivity.this, deniedPermissions.toArray(new String[deniedPermissions.size()]), MY_PHOTO_TAGGING_CODE);
                    }
                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        deniedPermissions = new ArrayList<>();
        if(requestCode == MY_PHOTO_TAGGING_CODE){
            if(isAllPhotoTaggingRuntimePermissionsGranted())
            {
                Toast.makeText(this, "Go ahead, do your stuff", Toast.LENGTH_SHORT).show();
            }
            else
            {
                for(int i = 0; i < permissions.length; i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                        deniedPermissions.add(permissions[i]);
                }
            }
        }
    }

    private boolean isAllPhotoTaggingRuntimePermissionsGranted() {
        for(String p: photoTaggingPermissions){
            if(ActivityCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }
}
