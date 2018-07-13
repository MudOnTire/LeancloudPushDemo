package com.leancloudpushdemo;

import android.app.Activity;
import android.content.Intent;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

public class PushModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static PushModule singleton;
    private static String ON_RECEIVE = "leancloudPushOnReceive";
    private static String ON_CUSTOM_RECEIVE = "leancloudPushOnCustomReceive";
    private static String ON_ERROR = "leancloudPushOnError";

    public PushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        singleton = this;
        PushService.setDefaultPushCallback(getReactApplicationContext(), PushHandlerActivity.class);

    }

    @Override
    public String getName() {
        return "androidPushModule";
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    }

    protected static boolean isActive() {
        return singleton != null;
    }

    private static WritableMap getWritableMap(Map<String, String> map) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.putString("action", map.get("action"));
        writableMap.putString("channel", map.get("channel"));
        writableMap.putString("data", map.get("data"));
        return writableMap;
    }

    protected static void onReceive(Map<String, String> map) {
        if (singleton != null) {
            WritableMap pushNotification = getWritableMap(map);
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = singleton.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(ON_RECEIVE, pushNotification);
        }
    }

    protected static void onCustomReceive(Map<String, String> map) {
        if (singleton != null) {
            WritableMap pushNotification = getWritableMap(map);
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = singleton.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(ON_CUSTOM_RECEIVE, pushNotification);
        }
    }

    protected static void onError(Exception e) {
        if (singleton != null) {
            WritableMap error = Arguments.createMap();
            error.putString("message", e.getLocalizedMessage());
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = singleton.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(ON_ERROR, error);
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("ON_RECEIVE", ON_RECEIVE);
        constants.put("ON_CUSTOM_RECEIVE", ON_CUSTOM_RECEIVE);
        constants.put("ON_ERROR", ON_ERROR);
        return constants;
    }


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
                    resultCallback.invoke(null, e);
                }
            }
        });
    }
}
