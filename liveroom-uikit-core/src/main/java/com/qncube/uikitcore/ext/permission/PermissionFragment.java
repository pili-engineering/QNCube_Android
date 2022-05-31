package com.qncube.uikitcore.ext.permission;



import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;


public class PermissionFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 777;

    private FragmentAttachCallback fragmentAttachCallback;
    private PermissionCallback permissionCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.getActivity() == null) {
            return;
        }
        ArrayList<String> grantList = new ArrayList<>();
        ArrayList<String> deniedList = new ArrayList<>();
        ArrayList<String> alwaysDeniedList = new ArrayList<>();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //获得权限
                    grantList.add(permissions[i]);
                } else {
                    //点了'不再询问'的权限
                    boolean alwaysDenied = !ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), permissions[i]);
                    if (alwaysDenied) {
                        alwaysDeniedList.add(permissions[i]);
                    } else {
                        //拒绝的权限
                        deniedList.add(permissions[i]);
                    }
                }
            }
            permissionCallback.onComplete(grantList, deniedList, alwaysDeniedList);
            removeFragment();
        }
    }

    private void removeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(this).commit();
        }
    }


    void requestPermission(String[] permissions) {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (fragmentAttachCallback != null) {
            fragmentAttachCallback.onAttach();
        }
    }

    void setOnAttachCallback(FragmentAttachCallback fragmentAttachCallback) {
        this.fragmentAttachCallback = fragmentAttachCallback;
    }

    void setOnPermissionCallback(PermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }
}
