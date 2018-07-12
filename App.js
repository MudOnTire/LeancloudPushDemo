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

type Props = {};
export default class App extends Component<Props> {

  componentDidMount() {
    PushService._iOS_initPush();
  }

  componentWillUnmount() {
    PushNotificationIOS.removeEventListener('register');
  }

  render() {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <Text style={{ fontSize: 24 }}>Leancloud Push Demo</Text>
      </View>
    );
  }
}