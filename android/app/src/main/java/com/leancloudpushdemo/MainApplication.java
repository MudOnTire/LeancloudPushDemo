package com.leancloudpushdemo;

import android.app.Application;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    //初始化leancloud
    AVOSCloud.initialize(this,"ppdriT1clcnRoda0okCPaB48-gzGzoHsz","Qzarq5cMdWzAMjwDW4umWpBL");

    // 测试 SDK 是否正常工作的代码
    AVObject testObject = new AVObject("TestObject");
    testObject.put("words","Hello World!");
    testObject.saveInBackground(new SaveCallback() {
      @Override
      public void done(AVException e) {
        if(e == null){
          Log.d("saved","success!");
        }
      }
    });
  }
}
