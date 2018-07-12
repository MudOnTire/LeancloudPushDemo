import AV from 'leancloud-storage';
import { AppState } from 'react-native';
const PushNotificationIOS = require('react-native').PushNotificationIOS;

const appId = 'ppdriT1clcnRoda0okCPaB48-gzGzoHsz';
const appKey = 'Qzarq5cMdWzAMjwDW4umWpBL';

AV.init({
    appId: appId,
    appKey: appKey
});

const Installation = require('leancloud-installation')(AV);

class PushService {

    _iOS_initPush = () => {
        PushNotificationIOS.addEventListener('register', this._iOS_onRegister);
        PushNotificationIOS.addEventListener('notification', this._iOS_onNotification);
        PushNotificationIOS.requestPermissions();
    }

    _iOS_onRegister = (deviceToken) => {
        if (deviceToken) {
            this._iOS_saveInstallation(deviceToken);
        }
    }

    _iOS_onNotification = (notification) => {
        if (AppState.currentState === 'active') {
            this._showAlert(notification._alert);
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
}

export default new PushService();