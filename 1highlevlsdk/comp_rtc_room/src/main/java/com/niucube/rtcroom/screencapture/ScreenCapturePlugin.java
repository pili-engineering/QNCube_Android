package com.niucube.rtcroom.screencapture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.niucube.rtcroom.screencapture.utils.RequestFragment;
import com.niucube.rtcroom.screencapture.utils.RequestFragmentHelper;
import com.qiniu.droid.rtc.QNScreenVideoTrack;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

public class ScreenCapturePlugin {

    public interface ScreenCaptureListener {
        void onSuccess();

        public void onError(int code, String msg);
    }

    public static final int REQUEST_CODE = QNScreenVideoTrack.SCREEN_CAPTURE_PERMISSION_REQUEST_CODE;

    private static class InstanceHolder {
        private static final ScreenCapturePlugin instance = new ScreenCapturePlugin();
    }

    public static ScreenCapturePlugin getInstance() {
        return InstanceHolder.instance;
    }

    private ScreenCapturePlugin() {
        super();
    }

    public void startMediaRecorder(final FragmentActivity activity, final ScreenCaptureListener callback) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        RequestFragment fragment = RequestFragmentHelper.INSTANCE.getPermissionReqFragment(activity);
        fragment.setCall(new Function3<Integer, Integer, Intent, Unit>() {
            @Override
            public Unit invoke(Integer integer, final Integer integer2, final Intent intent) {
                if (createVirtualDisplay(integer, integer2,intent)) {
                    ServiceConnection  serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            callback.onSuccess();
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                        }
                    };
                    RecordService.Companion.bindService(activity, serviceConnection);
                }
                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            private boolean createVirtualDisplay(int requestCode, int resultCode, Intent intent) {

                if (resultCode != Activity.RESULT_OK) {
                    callback.onError(Constants.ERROR_CODE_NO_PERMISSION, "no permission");
                    return false;
                }
                return (requestCode == QNScreenVideoTrack.SCREEN_CAPTURE_PERMISSION_REQUEST_CODE && QNScreenVideoTrack.checkActivityResult(
                        requestCode,
                        resultCode,
                        intent));
            }
        });
        fragment.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }
}
