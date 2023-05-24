import React from 'react';
import { View, ActivityIndicator, StyleSheet, Text } from 'react-native';

const FWLoading = ({ message }) => {
  return (
    <View
      style={[
        StyleSheet.absoluteFill,
        {
          alignItems: 'center',
          justifyContent: 'center',
          backgroundColor: 'rgba(255, 255, 255, 0.5)'
        }
      ]}>
      <ActivityIndicator />
      <Text>{message}</Text>
    </View>
  );
};

export default FWLoading;
