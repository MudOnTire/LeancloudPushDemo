/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Component } from 'react';
import { Text, View } from 'react-native';
import PushService from './services/PushService';
const MessageBarAlert = require('react-native-message-bar').MessageBar;
const MessageBarManager = require('react-native-message-bar').MessageBarManager;

type Props = {};
export default class App extends Component<Props> {

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
}