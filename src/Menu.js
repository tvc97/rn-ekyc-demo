import React from 'react';
import { Button, ScrollView, Text, Image, StatusBar, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';

import FWLoading from './components/FWLoading';
import { useEKYC } from './hooks/useEKYC';

const Menu = () => {
  const { navigation } = useNavigation();
  const {
    mrzResult,
    nfcResult,
    livenessResult,
    scanningNFC,
    images,
    onScanNFC
  } = useEKYC();

  const goMRZ = () => navigation.navigate('MRZ');
  const goLiveness = () => navigation.navigate('Liveness');

  const renderImage = base64data => {
    return (
      <Image
        source={{ uri: `data:image/jpeg;base64,${base64data}` }}
        style={{ width: '100%', aspectRatio: 1 }}
      />
    );
  };

  return (
    <View style={{ flex: 1 }}>
      <ScrollView>
        <StatusBar barStyle={'dark-content'} />
        <Button title={'MRZ Scan'} onPress={goMRZ} />
        <Button
          title={'NFC Scan'}
          disabled={!mrzResult || scanningNFC}
          onPress={onScanNFC}
        />
        <Button
          title={'Liveness check'}
          disabled={!nfcResult}
          onPress={goLiveness}
        />
        <Text>{JSON.stringify(mrzResult, null, 2)}</Text>
        <Text>{JSON.stringify(nfcResult, null, 2)}</Text>
        <Text>{JSON.stringify(livenessResult, null, 2)}</Text>
        {images.map(renderImage)}
      </ScrollView>
      {scanningNFC && (
        <FWLoading
          message={
            'Please place ID card with NFC on the back of your phone to start scanning'
          }
        />
      )}
    </View>
  );
};

export default Menu;
