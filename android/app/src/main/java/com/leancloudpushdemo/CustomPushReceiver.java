package com.leancloudpushdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class CustomPushReceiver extends BroadcastReceiver {
    private static final String TAG = "CustomPushReceiver";
    private HandleMessage handleMessage;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
            //获取消息内容
            String data = intent.getExtras().getString("com.avos.avoscloud.Data");
            JSONObject jsonObject = JSON.parseObject(data);
            if (jsonObject != null) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("action", action);
                map.put("channel", channel);
                map.put("data", data);
                PushModule.onCustomReceive(map);
                if (handleMessage!=null){
                    handleMessage.receiveMessage(jsonObject);
                }
            }
        } catch (JSONException e) {
            PushModule.onError(e);
        }
    }

    interface HandleMessage{
        public void receiveMessage(JSONObject jsonObject);
    }

    public void setHandleMessage(HandleMessage handleMessage) {
        this.handleMessage = handleMessage;
    }
}