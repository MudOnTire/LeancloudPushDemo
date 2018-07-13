package com.leancloudpushdemo;

import android.app.Activity;
import android.content.Intent;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.SaveCallback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class PushModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public PushModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "AndroidPushModule";
    }

    @Override
    public void onNewIntent(Intent intent) {}

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {}

    /**
     * 保存installation
     */
    @ReactMethod
    public void saveInstaillation(final Callback resultCallback) {
        AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            public void done(AVException e) {
                if (e == null) {
                    // 保存成功
                    String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                    resultCallback.invoke(installationId);
                } else {
                    resultCallback.invoke();
                }
            }
        });
    }
}
