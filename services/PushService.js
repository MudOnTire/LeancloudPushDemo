import AV from 'leancloud-storage';
import { AppState, Alert } from 'react-native';
const PushNotificationIOS = require('react-native').PushNotificationIOS;

const appId = 'ppdriT1clcnRoda0okCPaB48-gzGzoHsz';
const appKey = 'Qzarq5cMdWzAMjwDW4umWpBL';

AV.init({
    appId: appId,
    appKey: appKey
});

const Installation = require('leancloud-installation')(AV);

class PushService {

    //用于记录通知的临时变量
    backgroundNotification = null;

    _iOS_initPush = () => {
        PushNotificationIOS.addEventListener('register', this._iOS_onRegister);
        PushNotificationIOS.addEventListener('notification', this._iOS_onNotification);
        PushNotificationIOS.requestPermissions();
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
        //app关闭时，是否通过点击系统通知打开
        PushNotificationIOS.getInitialNotification()
            .then((notification) => {
                if (notification) {
                    this._iOS_onNotificationTapped();
                }
            });
    }

    _iOS_onRegister = (deviceToken) => {
        if (deviceToken) {
            this._iOS_saveInstallation(deviceToken);
        }
    }

    _iOS_onNotification = (notification) => {
        //如果app在前台则显示alert
        if (AppState.currentState === 'active') {
            this._showAlert(notification._alert);
        } else if (AppState.currentState === 'background') {
            //app在后台运行时点击通知
            this.backgroundNotification = notification;
        }
    }

    _iOS_saveInstallation = (deviceToken) => {
        const info = {
            apnsTopic: 'com.example.LeancloudPushDemo',
            deviceType: 'ios',
            deviceToken: deviceToken
        };
        Installation.getCurrent()
            .then(installation => installation.save(info))
            .then(result => console.log(result))
            .catch(error => console.error(error))
    }

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

    _iOS_onNotificationTapped = () => {
        Alert.alert('Notification Tapped');
    }

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
            viewTopInset: 20,
            onTapped: this._iOS_onNotificationTapped
        });
    }
}

export default new PushService();