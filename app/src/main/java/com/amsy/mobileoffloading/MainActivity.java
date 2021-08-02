package com.amsy.mobileoffloading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSIONS_REQUEST_CODE = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] requiredPermissions =  checkPermissions();
        if (requiredPermissions.length > 0) {
            askPermissions(requiredPermissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Please provide all necessary permissions", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    finish();
                }
            }
        }
    }

    private String[] checkPermissions() {
        ArrayList<String> requiredPermissions = new ArrayList<>();
        try {
            String packageName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] permissions = packageInfo.requestedPermissions;

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    requiredPermissions.add(permission);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String[] _requiredPermissions = new String[requiredPermissions.size()];
        _requiredPermissions = requiredPermissions.toArray(_requiredPermissions);
        return _requiredPermissions;
    }

    private void askPermissions(String[] requiredPermissions) {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }

    public void onClickMaster(View view) {
        Intent intent = new Intent(getApplicationContext(), MasterDiscovery.class);
        startActivity(intent);
    }

    public void onClickSlave(View view) {
        Intent intent = new Intent(getApplicationContext(), WorkerAdvertisement.class);
        startActivity(intent);
    }
}