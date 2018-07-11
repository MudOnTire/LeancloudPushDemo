import AV from 'leancloud-storage';
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
        PushNotificationIOS.requestPermissions();
    }

    _iOS_onRegister = (deviceToken) => {
        if (deviceToken) {
            this._saveInstallation(deviceToken);
        }
    }

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

export default PushService();