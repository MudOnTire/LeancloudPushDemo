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


# 预告

至此，使用Leancloud实现iOS的消息推送已实现完成，并涵盖了主要的应用场景。出于控制篇幅的原因，Android的实现将会另起一章分享给大家，敬请期待！


本文Demo Github地址：[https://github.com/MudOnTire/LeancloudPushDemo](https://github.com/MudOnTire/LeancloudPushDemo)，如果对你有帮助，star一下吧。

