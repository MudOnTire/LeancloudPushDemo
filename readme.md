# 前言

React Native现在是越来越火，一套代码同时构建iOS、Android两种应用真的是太爽了，而且有了ES6和React的加成开发效率出奇的高。 虽然坑奇多无比但是还是阻挡不了市场对它的热爱。但是使用React Native也并非总是那么舒服，尤其涉及到需要用objective-c或者java实现某些原生功能的时候，让广大前端出生没有原生App开发经验的同学们苦不堪言，但是没有办法，硬着头皮写下去总比丢工作强。所以React Native开发者们真的是痛并快乐着，爽并纠结着。然而能力就是在这个过程中快速提高的，所以大家加油，现在只不过是黎明前的黑暗！

今天要分享的是我在React Native开发过程中征服的一个小小领域：消息推送。

其实做手机App就绕不开消息推送，没有消息推送的App就像一个没有漂亮前台的公司（就像我们公司，嘿嘿），让人没有进去看看的欲望。。怎么可能火呢。

说正经的，虽然我做完React Native版的消息推送之后发现其实也并不难，但是一路上踩过大大小小的坑、尝试过无数的解决方案，到最终大功告成还是有点小成就感的（笨人获得满足感相对比较容易）。实现React Native App的消息推送可预见的难点在于：

1. 在众多的第三方推送服务提供商中选择哪一个最合适
1. 需要同时实现Android和IOS两个系统的推送，需要对两个系统的推送机制都非常熟悉
1. 哪些部分需要用原生实现，哪些需要用js实现，如何实现桥接
1. 如何实现App在后台或者关闭（inactive）状态下的推送，又如何实现App打开（active）状态下的消息推送

结下来我就针对以上的难点，并结合实际的项目来详细分析如何实现消息推送。

# 选择服务提供商

市场上的推送服务提供商有很多，比如友盟、极光推送、Leancloud、个推、环信、融云等等。这么多的选择我们不可能都用过，所以应该从哪些方面去考量呢？

1. 首先，必须支持React Native。为了验证这些第三方服务是否支持React Native，没有特别好的办法，我只能一个个的看他们的官网文档。如果文档里面都没有提到React Native那么果断放弃（有些厂商都不提供文档搜索功能，那也不建议选择，以后出问题都不好找）。
2. 推送服务要稳定、可靠、快速，这一点不太好判断，因为大家都说自己可靠快速，所以需要实际使用后才能判断。
3. 文档完善、清晰、准确、更新及时，能提供官方的React Native推送Demo。
4. 价格合理（我们还是要想办法为公司省点钱滴）。

下面是我整理的一些主流的推送服务提供商的对比，可能不是那么完善和客观，欢迎纠错，没有提到的厂商不好意思啦。。


服务商    | React Native支持否  | 文档质量  | 官方Demo                                                          | 价格
----------|---------------------|-----------|-------------------------------------------------------------------|---------
友盟      | 是                  |差，放弃   |未调查                                                             |未调查
极光      | 是                  |缺少       |[Demo](https://github.com/jpush/jpush-react-native)                |[价格](https://www.jiguang.cn/push-price)
Leancloud | 是                  |高         |[iOS](https://github.com/leancloud/react-native-installation-demo) |[价格](https://leancloud.cn/pricing/)
腾讯信鸽  | 否（未找到相关文档）|/          |/                                                                  |/
阿里云推送| 否（未找到相关文档）|/          |/                                                                  |/
百度云推送| 否（未找到相关文档）|/          |/                                                                  |/
网易云信  | 是                  |缺少       |[Demo](https://github.com/netease-im/NIM_ReactNative_Demo)         |[价格](http://netease.im/price)
云巴      | 否（未找到相关文档）|/          |/                                                                  |/
个推      | 是                  |缺少       |[Demo](https://github.com/GetuiLaboratory/react-native-getui)      |[价格](http://www.getui.com/cn/getui.html)
环信      | 否（只支持IM）      |/          |/                                                                  |/
融云      | 否（未找到相关文档）|/          |/                                                                  |/

以上所有的厂商里面只有4家是支持React Native消息推送的，BAT跟商量好了似的都不支持，难道集体看衰React Native吗？在仅有的4家厂商中，文档方面只有Leancloud一家是我觉得文档质量比较好的，其他厂商都缺少接入React Native的相关文档。个推只有一个小Demo，而且Demo的文档也很简陋，所以先排除。极光的Demo应该是做的最好的，star数最多，Demo文档看起来也写的挺好的，虽然没有明码标价，但是免费版貌似就够用了，推送次数没有上限，二十万条/秒的推送速度也够用了，做一般应用应该足够了。网易的Demo看起来也挺完善，文档也说的过去，关键是价格太贵啦，商用版1800/月。。为了只做一个推送不值得，放弃！最后Leancloud是我个人比较喜欢的，因为之前有项目用到过，不管是文档、SDK的易用性、服务的可靠性和速度还是网站的审美都可以算得上同行中的佼佼者，而且商用版30/天的价格也可以接收（30/天包含了除推送外其他更多的功能和服务）。所以总结下来，只有极光推送和Leancloud值得一用（欢迎极光和Leancloud联系我打赏，嘿嘿），由于我的个人偏好，最终选择了使用Leancloud。

# 消息推送概念普及

我们在实现具体项目之前还是有必要了解一下消息推送的相应机制和基本概念的，这里我就不赘述了，欢迎阅读Leancloud的 [消息推送服务总览](https://leancloud.cn/docs/push_guide.html)。

# 接入Leancloud

首先我们创建一个React Native项目（[本文Demo地址](https://github.com/MudOnTire/LeancloudPushDemo)）：

```
react-native init LeancloudPushDemo
```

并在Leancloud创建一个同名应用，开发版就好：

![image](https://note.youdao.com/yws/api/personal/file/WEBc04400f8a72ca735a7d91ececbc04e8b?method=download&shareKey=907d1990ff866d7133be86065a497efc)


安装完成后，我们需要安装Leancloud推送相关的js sdk：

```
$ npm install leancloud-storage --save

$ npm install leancloud-installation --save
```

我们在项目根目录下创建services文件夹，并在其中添加PushService.js文件，用于管理消息推送的主要逻辑，初始内容如下：

```
import AV from 'leancloud-storage';

const appId = 'ppdriT1clcnRoda0okCPaB48-gzGzoHsz';
const appKey = 'Qzarq5cMdWzAMjwDW4umWpBL';

AV.init({
    appId: appId,
    appKey: appKey
});

const Installation = require('leancloud-installation')(AV);

class PushService {

}

export default new PushService();
```

目前PushService还是一个空的class，稍后我们会逐渐丰富它的功能。
Leancloud的AppId，AppKey可以在如下页面获取：

![image](https://note.youdao.com/yws/api/personal/file/WEB3527a7163629a39ddd98ba260e60adf9?method=download&shareKey=7113d3949ff5c63cf599708ad162ccc1)

由于iOS、Android推送方式的差异，接下来我们将分别进行实现。

# iOS消息推送

在React Native中实现iOS的消息推送相对Android简单一些，因为官方已经给出了`PushNotificationIOS`这样现成的组件。

## 配置

首先，根据，在iOS 项目中引入 RCTPushNotification，可参考：[Linking Libraries - React Native docs](https://facebook.github.io/react-native/docs/linking-libraries-ios.html#content) 

### 步骤1：将PushNotification项目拖到当前iOS主项目

![image](https://note.youdao.com/yws/api/personal/file/WEB953f2a842aa1f303862ac05c3cc1b335?method=download&shareKey=95567475b967c3f20c6d01ad06aa4fdc)

### 步骤2：添加libRCTPushNotification静态库

![image](https://note.youdao.com/yws/api/personal/file/WEBd0ef5ecadd7fd5bfe1bef60cb76917de?method=download&shareKey=2f31b03cda2c289919b3af01b8c9cab6)

### 步骤3: 开启Push Notification功能

![image](https://note.youdao.com/yws/api/personal/file/WEB664b56718df3df438b49e1cc1913d275?method=download&shareKey=bb9001be3c8ca728bf24af1bee803873)


然后，修改AppDelegate.m，增加推送相关事件代理，可参考：[PushNotificationIOS - React Native docs](https://facebook.github.io/react-native/docs/pushnotificationios.html#content)，。

## 获取devideToken，更新_Installation表

Leancloud需要根据iOS设备的deviceToken来决定推送到哪台设备，所以需要把deviceToken保存到_Installation表。而保存的最佳时机就在App刚刚启动的时候，在PushService下添加如下代码：

```
//引用PushNotificationIOS
const PushNotificationIOS = require('react-native').PushNotificationIOS;

...

class PushService {
    //获取iOS消息通知权限
    _iOS_initPush = () => {
        PushNotificationIOS.addEventListener('register', this._iOS_onRegister);
        PushNotificationIOS.requestPermissions();
    }

    //权限获取成功回调
    _iOS_onRegister = (deviceToken) => {
        if (deviceToken) {
            this._iOS_saveInstallation(deviceToken);
        }
    }

    //保存deviceToken到Installation
    _iOS_saveInstallation = (deviceToken) => {
        const info = {
            apnsTopic: 'com.example',
            deviceType: 'ios',
            deviceToken: deviceToken
        };
        Installation.getCurrent()
            .then(installation => installation.save(info))
            .then(result => console.log(result))
            .catch(error => console.error(error))
    }
}

...
```

修改App.js，在`componentDidMount`时执行初始化：

```
import React, { Component } from 'react';
import { Text, View } from 'react-native';
import PushService from './services/PushService';

type Props = {};
export default class App extends Component<Props> {

  componentDidMount() {
    PushService._iOS_initPush();
  }

  render() {
    return (
      <View>
        <Text>Leancloud Push Demo</Text>
      </View>
    );
  }
}
```

现在我们来运行一下项目（须使用真机，模拟器获取不到deviceToken），看是否能获取到deviceToken并保存。

保存成功后发现_Installation表中多了一条记录：

![image](https://note.youdao.com/yws/api/personal/file/WEB751606a0e65c8298d4eea9aac4ffe068?method=download&shareKey=825f8104edcf01589c0041d0903c0c9c)


## 推送证书设置

成功保存deviceToken意味着我们已经成功了一半了，但如果要让iOS设备能收到通知，还需要配置推送证书，详细步骤请参考 [iOS推送证书设置指南](https://leancloud.cn/docs/ios_push_cert.html)。推荐使用Token Authentication。

推送证书设置完成之后，就可以测试手机是否能收到消息通知了。Leancloud提供在线发送消息的功能：
![image](https://note.youdao.com/yws/api/personal/file/WEB157187c759fd8ed0263273ffd8ce2ff6?method=download&shareKey=3478cc7981b2d875a9be7409b0c868d6)

在线发送之后，手机就可以收到通知了（不小心暴露我的起床时间了。。）：

![image](https://note.youdao.com/yws/api/personal/file/WEB931d90f18c7b0e1e21e215c13802dd9f?method=download&shareKey=c4afe0a161d830b0fa5dc7aebeda22a8)


## 通知的接收和处理

到目前为止我们已经成功了一大半了，但是我们还想做得更多一点，一款成熟的应用还应该包括以下功能：

- App在前台、后台运行或者关闭状态都能看到通知消息
- App在后台或者关闭状态收到通知，App图标能显示通知个数的badge
- 点击通知能够进行自定义的操作，比如跳转到具体页面

### App打开时通知的显示

当App在前台运行时收到通知iOS默认是不会提醒的（iOS 10开始支持在前台显示，请参考 [stackoverflow](https://stackoverflow.com/questions/14872088/get-push-notification-while-app-in-foreground-ios)），因此我们需要自己实现接收通知并显示的逻辑。

我们选择用 [react-native-message-bar](https://github.com/KBLNY/react-native-message-bar)来展示通知，先安装react-native-message-bar：

```
npm install react-native-message-bar --save
```

然后，在App.js中引入并注册MessageBar：


```
...

const MessageBarAlert = require('react-native-message-bar').MessageBar;
const MessageBarManager = require('react-native-message-bar').MessageBarManager;

...

componentDidMount() {
    PushService._iOS_initPush();
    MessageBarManager.registerMessageBar(this.refs.alert);
}

componentWillUnmount() {
    PushNotificationIOS.removeEventListener('register');
    MessageBarManager.unregisterMessageBar();
}

render() {
return (
  <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
    <Text style={{ fontSize: 24 }}>Leancloud Push Demo</Text>
    <MessageBarAlert ref="alert" />
  </View>
);
}

...
```

接着，修改PushService，增加对`notification`事件的监听，和显示自定义Alert的方法：

```
...

_iOS_initPush = () => {
    PushNotificationIOS.addEventListener('register', this._iOS_onRegister);
    PushNotificationIOS.addEventListener('notification', this._iOS_onNotification);
    PushNotificationIOS.requestPermissions();
}

_iOS_onNotification = (notification) => {
    //如果app在前台则显示alert
    if (AppState.currentState === 'active') {
        this._showAlert(notification._alert);
    }
}

...

 _showAlert = (message) => {
    const MessageBarManager = require('react-native-message-bar').MessageBarManager;
    MessageBarManager.showAlert({
        title: '您有一条新的消息',
        message: message,
        alertType: 'success',
        stylesheetSuccess: {
            backgroundColor: '#7851B3', 
            titleColor: '#fff', 
            messageColor: '#fff'
        },
        viewTopInset : 20
    });
}

...
```

最后重新运行App，然后在线发送一条通知，App打开状态下也能显示通知了：

![image](https://note.youdao.com/yws/api/personal/file/WEBe09bfd994eafb30506cca0898a3e9d3b?method=download&shareKey=1ac3ccbd49f90fd86978aff1a7e3eda2)

### 收到通知显示Badge

要实现badge显示并能随着通知个数递增非常简单，只需要在Leancloud控制台中勾选`Increment iOS badges`，然后发送通知后App图标上就会出现红色的badge了：

![image](https://note.youdao.com/yws/api/personal/file/WEBf066f7b09c47c349cadca1f8260cbab3?method=download&shareKey=2b2a7d80bc0a3740393b205bf19a326c)

![image](https://note.youdao.com/yws/api/personal/file/WEB347c20410aa959a838119a23c3947b70?method=download&shareKey=2c69fddac309d02ae924173bbf90741b)

#### 清除badge

添加badge之后，我们需要在合适的时间点再将其清除，Leancloud将每个设备上badge的数量也保存在_Installation表中，所以清除设备的badge同时需要修改Installation表：

```
_iOS_cleanBadge = () => {
    Installation.getCurrent()
        .then((installation) => {
            installation.set('badge', 0);
            return installation.save();
        })
        .then((result) => {
            PushNotificationIOS.setApplicationIconBadgeNumber(0);
        })
        .catch(error => console.log(error));
}
```

### 自定义点击通知的行为

点击通知又分为点击iOS系统弹出的通知提醒和点击我们自定义的MessageBar。而点击iOS系统的通知又可分为App在后台运行和App处于关闭状态。接下来我们就分别讨论这三种状态下如何处理：

#### 1. App打开，点击MessageBar

react-native-message-bar提供了`onTapped`的callback，所以我们只需要传入我们想要执行的方法就行了，我们将PushService进行如下修改：

```
...

_iOS_onNotificationTapped = () => {
    Alert.alert('Notification Tapped');
}

_showAlert = (message) => {
    const MessageBarManager = require('react-native-message-bar').MessageBarManager;
    MessageBarManager.showAlert({
        ...
        onTapped: this._iOS_onNotificationTapped
    });
}

...
```

#### 2. App在后台运行，点击系统通知

实现思路是，当app在后台运行时收到通知，点击通知会触发`notification`事件，我们用一个临时变量记录下当前的通知，再通过监听app状态的变化，当app从后台切换到前台后临时变量是否有值判断是否是点击通知打开的app，如果是通过点击通知打开app，执行我们想要的逻辑。说的有点绕，让我们来看代码：

```
...

class PushService {

    //用于记录通知的临时变量
    backgroundNotification = null;
    
    _iOS_initPush = () => {
        ...
        
        //监听app状态的改变
        AppState.addEventListener('change', (newState) => {
            if (newState === 'active') {
                if (this.backgroundNotification != null) {
                    this._iOS_onNotificationTapped();
                    this.backgroundNotification = null;
                    this._iOS_cleanBadge();
                }
            }
        });
    }
    
    ...
    
    _iOS_onNotification = (notification) => {
        ...
        
        } else if (AppState.currentState === 'background') { 
            //app在后台运行时点击通知
            this.backgroundNotification = notification;
        }
    }
    
    ...
    
```

#### 3. App关闭状态下，点击系统通知

直接调用`PushNotificationIOS.getInitialNotification`判断app关闭时，是否通过点击系统消息打开：

```
...

_iOS_initPush = () => {
    ...
    
    //app关闭时，是否通过点击系统通知打开
    PushNotificationIOS.getInitialNotification()
        .then((notification) => {
            if (notification) {
                this._iOS_onNotificationTapped();
            }
        });
}

...
```


至此，使用Leancloud实现iOS的消息推送已实现完成，并涵盖了主要的应用场景。

---

# Android
接下来，咱们来实现Android的消息推送，主要思路和iOS的实现类似。

# 接入Leancloud

在接入Leancloud之前，还是推荐先阅读Leancloud官方的 [Android消息推送开发指南](https://leancloud.cn/docs/android_push_guide.html)。

## 安装Leancloud SDK

SDK有多种安装方式，详情请参考[Android SDK安装指南](https://leancloud.cn/docs/sdk_setup-android.html)。我选择用Gradle安装，先在根目录下的`build.gradle`中添加Leancloud的maven仓库地址：


```
buildscript {
    repositories {
        jcenter()
        maven {
            url 'https://maven.google.com/'
            name 'Google'
        }

        maven {
            url "http://mvn.leancloud.cn/nexus/content/repositories/public"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'
    }
}

allprojects {
    repositories {
        mavenLocal()
        jcenter()
        maven {
            url "$rootDir/../node_modules/react-native/android"
        }
        maven {
            url 'https://maven.google.com/'
            name 'Google'
        }
        maven {
            url "http://mvn.leancloud.cn/nexus/content/repositories/public"
        }
    }
}

```

然后打开 app 目录下的 `build.gradle` 进行如下配置：


```
android {
    //为了解决部分第三方库重复打包了META-INF的问题
    packagingOptions{
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
    }
    lintOptions {
        abortOnError false
    }
    ...
}

...

dependencies {
    ...
    
    // LeanCloud 基础包
    compile ('cn.leancloud.android:avoscloud-sdk:v4.6.4')
    // 推送与实时聊天需要的包
    compile ('cn.leancloud.android:avoscloud-push:v4.6.4@aar'){transitive = true}
}

```

## 初始化Leancloud

我们需要在App创建后用Leancloud的AppId，AppKey进行初始化，修改`MainApplication`如下：

```
 @Override
  public void onCreate() {
    super.onCreate();
    ...
    //初始化leancloud
    AVOSCloud.initialize(this,"ppdriT1clcnRoda0okCPaB48-gzGzoHsz","Qzarq5cMdWzAMjwDW4umWpBL");
  }
```

接下来，在`AndroidManifest.xml`中配置Leancloud SDK所需的权限以及消息推送所需的service和receiver：


```
...

<!-- 基础模块（必须加入以下声明）START -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<!-- 基础模块 END -->

<application
  ...
  android:name=".MainApplication" >
  ...

  <!-- 实时通信模块、推送（均需要加入以下声明） START -->
  <!-- 实时通信模块、推送都要使用 PushService -->
  <service android:name="com.avos.avoscloud.PushService"/>
  <receiver android:name="com.avos.avoscloud.AVBroadcastReceiver">
    <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED"/>
      <action android:name="android.intent.action.USER_PRESENT"/>
      <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
  </receiver>
  <!-- 实时通信模块、推送 END -->
</application>
```

到此，Leancloud SDK的接入完成，我们需要测试一下SDK能不能正常使用。我们在`MainActivity.java`的`onCreate`方法中添加代码看能不能保存数据到Leancloud数据库：

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
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

    ...
}
```

启动App，前往Leancloud控制台，查看数据库中是否多了一条TestObject的记录，如果有说明Leancloud SDK接入成功：

![image](https://note.youdao.com/yws/api/personal/file/WEB9da414d2cdcbaa974c56b017a679499d?method=download&shareKey=1b306b7d8c923126ec75fae17a685e6f)

## 保存Installation

和iOS一样，Android也需要保存installation才能让Leancloud确定推送到哪些设备。**但是比较坑的是：Leancloud官方提供的 [leancloud-installation](https://www.npmjs.com/package/leancloud-installation)只能正确保存iOS设备的installation。** 因此我们只能使用Android的SDK保存installation，而且我们最好把这个方法封装成一个native模块暴露给js调用，以方便在保存成功或失败后执行相应操作。

在`com.leancloudpushdemo`文件夹中创建`PushModule.java`，`PushDemo`继承于`ReactContextBaseJavaModule`并实现`ActivityEventListener`接口，添加如下代码：

```
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
        return "androidPushModule";
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

```

接着在同一目录下面添加`PushPackage.java`用于注册`PushModule`模块，代码如下：


```
package com.leancloudpushdemo;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PushPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new PushModule(reactContext));
        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}

```

然后，在`MainApplication.java`中的`getPackages`方法中增加`PushPackage`：

```
@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
            ...
            new PushPackage()
    );
}
```

接着，在我们的`PushService.js`中引入`PushModule`并保存installation：

```
...
import { NativeModules } from 'react-native';
const AndroidPush = NativeModules.androidPushModule;

...
class PushService {
    
    ...
    //Android
    _an_initPush = () => {
        this._an_saveInstallation();
    }

    _an_saveInstallation = () => {
        AndroidPush.saveInstaillation((installationId) => {
            if (installationId) {
                console.log('Android installation 保存成功！');
            }
        })
    }
    ...
}
```

最后，在`App.js`中执行Android的初始化：

```
componentDidMount() {
    if (Platform.OS === 'ios') {
        PushService._iOS_initPush();
    } else {
        PushService._an_initPush();
    }
    MessageBarManager.registerMessageBar(this.refs.alert);
}
```

重启App，前往Leancloud控制台中查看数据库中是否多了一条installation记录，如果有说明保存成功：

![image](https://note.youdao.com/yws/api/personal/file/WEBd1f33ad33731db081f188a4b0d128751?method=download&shareKey=2e7189afcfa12f13fb4fd23726754b35)

如果确认代码没问题，但是还是保存不成功，我建议：
1. 重启Android Studio
2. 重启React Native Packager
3. 重启电脑、手机。。
4. 如果还有问题，欢迎咨询我

## 实现系统推送

### 启动推送服务

首先调用Leancloud SDK启动推送服务：

```
PushService.setDefaultPushCallback(getReactApplicationContext(), PushHandlerActivity.class);
```

`PushHandlerActivity`为收到通知默认打开的activity，我们接下来实现。

### PushHandlerActivity实现

该activity的定位为接收并初步解析通知数据。我们在`com.leancloudpushdemo`文件夹下添加`PushHandlerActivity.java`，内容如下：

```
package com.leancloudpushdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;


public class PushHandlerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processPush();
        finish();
        if (!PushModule.isActive()) {  //todo：判断PushModule是否实例化
            relaunchActivity();
        }
    }

    private void processPush() {
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
            String data = intent.getExtras().getString("com.avos.avoscloud.Data");
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", action);
            map.put("channel", channel);
            map.put("data", data);
            PushModule.onReceive(map); //todo：处理通知
        } catch (Exception e) {
            PushModule.onError(e); // todo：处理错误
        }
    }

    private void relaunchActivity() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }
}

```
别忘了在`AndroidManifest.xml`中加上该activity：

```
<activity android:name=".PushHandlerActivity"></activity>
```

### 主要处理逻辑实现

`PushHandlerActivity`代码中有三处`todo`是我们接下来要在`PushModule`中实现的逻辑。关于接收到通知后如何处理，我的思路是当native module收到通知时，通过`RCTDeviceEventEmitter`触发相应的Event，在js中监听这些Event并响应，修改`PushModule`如下：

```
public class PushModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static PushModule singleton;
    private static String ON_RECEIVE = "leancloudPushOnReceive";
    private static String ON_ERROR = "leancloudPushOnError";
    
    public PushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        singleton = this;
    }
    
    ...
    
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
        constants.put("ON_ERROR", ON_ERROR);
        return constants;
    }
    
    ...
```

最后，我们在`PushService.js`增加对消息通知相关事件的监听和处理的逻辑，我选择在保存installation成功后增加监听：

```
...

import { DeviceEventEmitter } from 'react-native';

...
class PushService {
    ...
    
    _an_saveInstallation = () => {
        AndroidPush.saveInstaillation((installationId, error) => {
            if (installationId) {
                DeviceEventEmitter.addListener(AndroidPush.ON_RECEIVE, (notification) => {
                    console.log('receive android notification');
                    this._an_onNotificationTapped(notification);
                });
                DeviceEventEmitter.addListener(AndroidPush.ON_ERROR, (res) => {
                    console.log('android notification error');
                    console.log(res);
                });
            } else {
                console.log(error);
            }
        })
    }

    _an_onNotificationTapped = (notification) => {
        Alert.alert('Android Notification Tapped');
    }
}
...
```
现在我们在Leancloud控制台发送一条通知，手机应该能收到消息：

![image](https://note.youdao.com/yws/api/personal/file/WEB107a2c5f3146d3fb6b4f5a04866bf486?method=download&shareKey=9aed91a60a031951ad00188c852e30b3)

当点击通知的时候，App打开并执行我们自定义的逻辑：

![image](https://note.youdao.com/yws/api/personal/file/WEB325089d4f812e2ed4fb749faf622ed06?method=download&shareKey=ac329887769f278d0cc70428b07812dc)

## 实现App打开状态下的推送
到目前为止，我们已经实现了系统级的推送，和iOS一样，我们希望Android App打开状态下也能弹出通知提醒。Leancloud提供了这样的可能，我们可以通过 [自定义Receiver](https://leancloud.cn/docs/android_push_guide.html#hash1393576931) 来实现。

### 自定义Receiver

我们在`com.leancloudpushdemo`路径下添加`CustomPushReceiver.java`，代码如下：

```
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
                PushModule.onCustomReceive(map); //todo: 处理通知
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
```
`todo`的方法待会儿在`PushModule`中实现。接着，在`AndroidManifest.xml`中添加custom receiver:

```
<receiver android:name="com.leancloudpushdemo.CustomPushReceiver">
    <intent-filter>
        <action android:name="com.cnuip.INNER_NOTI" />
    </intent-filter>
</receiver>
```
### 通知处理

然后修改`PushModule`如下：

```
public class PushModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    ...
    private static String ON_CUSTOM_RECEIVE = "leancloudPushOnCustomReceive";
    
    ...
    
    protected static void onCustomReceive(Map<String, String> map) {
        if (singleton != null) {
            WritableMap pushNotification = getWritableMap(map);
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = singleton.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(ON_CUSTOM_RECEIVE, pushNotification);
        }
    }

    ...
    
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("ON_RECEIVE", ON_RECEIVE);
        constants.put("ON_CUSTOM_RECEIVE", ON_CUSTOM_RECEIVE);
        constants.put("ON_ERROR", ON_ERROR);
        return constants;
    }
}
```

最后，修改`PushService.js`，增加对`ON_CUSTOM_RECEIVE`事件的监听：

```
...
_an_saveInstallation = () => {
    AndroidPush.saveInstaillation((installationId, error) => {
        if (installationId) {
            ...
            DeviceEventEmitter.addListener(AndroidPush.ON_CUSTOM_RECEIVE, (notification) => {
                console.log('receive custom android notification');
                this._showAlert(JSON.parse(notification.data).alert);
            });
            ...
        } else {
            ...
        }
    })
}
...
```

同时通知的消息提也需要做相应修改，才能让custom receiver接收到，我们可以用Postman来发送消息：

![image](https://note.youdao.com/yws/api/personal/file/WEBd967a28c240e086617cd6ff980bba89d?method=download&shareKey=f6510f4ce7a4705ad0ada9db92b79cd6)

消息发出后，App中成功弹出消息提醒，完美。

![image](https://note.youdao.com/yws/api/personal/file/WEB68e524077cbc04eb395488ce3b44c969?method=download&shareKey=ea6d4980038afb366536a38e92f67649)


# 结语

经过不懈的努力，我们已经成功使用Leancloud实现了iOS和Android上的消息通知，第一次写这么长的文章还是有点累的。。如果对你有帮助欢迎点赞！还有虽然功能都实现了，但是我想可能还会有更好的实现方式，欢迎找到的同学分享，谢谢！



