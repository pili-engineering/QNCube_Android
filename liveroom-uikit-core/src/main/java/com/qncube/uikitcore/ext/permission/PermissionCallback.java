package com.qncube.uikitcore.ext.permission;

import java.util.List;


public interface PermissionCallback {
    void onComplete(List<String> grantedPermissions, List<String> deniedPermissions, List<String> alwaysDeniedPermissions);
}
