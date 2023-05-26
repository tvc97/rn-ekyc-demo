import { useState, useEffect } from 'react';
import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import { useNavigation } from '@react-navigation/native';

const ACTION_TYPES = {
  MRZ: {
    KEY: 'mrzResult',
    EVENT: 'onMRZScanned'
  },
  NFC: {
    KEY: 'nfcResult',
    EVENT: 'onNFCScanned'
  },
  LIVENESS: {
    KEY: 'livenessResult',
    EVENT: 'onLivenessChecked'
  }
};

export const useEKYC = () => {
  const navigation = useNavigation();
  const [state, setState] = useState({
    mrzResult: null,
    nfcResult: null,
    livenessResult: null,
    scanningNFC: false
  });
  const [images, setImages] = useState([]);

  const setStateWithPrevious = next => {
    setState(prev => ({
      ...prev,
      ...next
    }));
  };

  const onScanNFC = () => {
    setStateWithPrevious({ scanningNFC: true });
    NativeModules.NativeModuleManager.startScanNFC();
  };

  const getResultForPlatform = (data, platform) => {
    let platformResult = data;

    if (platform === 'android') {
      let parsedData = {};
      try {
        parsedData = JSON.parse(data.result);
      } catch {}
      const { image_action, ...data } = parsedData;
      platformResult = {
        images: image_action?.map?.(item => item.image) || [],
        data
      };
    }

    return platformResult;
  };

  const onResult = key => value => {
    switch (key) {
      case ACTION_TYPES.MRZ.KEY:
        setStateWithPrevious({ [key]: value });
        navigation.goBack();
        break;
      case ACTION_TYPES.NFC.KEY:
        setStateWithPrevious({ [key]: value });
        break;
      case ACTION_TYPES.LIVENESS.KEY:
        const { images, data } = getResultForPlatform(value, Platform.OS);
        setStateWithPrevious({ [key]: data });
        setImages(images);
        navigation.goBack();
        break;
    }

    if (key === 'livenessResult') {
      if (Platform.OS === 'ios') {
        setImages([...(value.images || [])]);
        delete value.images;
        setStateWithPrevious({ [key]: value, scanningNFC: false });
      } else {
        let result = {};
        try {
          result = JSON.parse(value.result);
        } catch {}
        if (result.image_action) {
          setImages(result.image_action.map(item => item.image));
          delete result.image_action;
        }
        setStateWithPrevious({ [key]: result, scanningNFC: false });
      }
    } else {
      setStateWithPrevious({ [key]: value, scanningNFC: false });
    }
  };

  useEffect(() => {
    const nativeModuleEventEmitter = new NativeEventEmitter(
      NativeModules.ReactNativeEventEmitter
    );

    Object.values(ACTION_TYPES).forEach(action => {
      nativeModuleEventEmitter.addListener(action.EVENT, onResult(action.KEY));
    });

    return () => {
      Object.values(ACTION_TYPES).forEach(action => {
        nativeModuleEventEmitter.removeAllListeners(action.EVENT);
      });
    };
  }, []);

  return {
    ...state,
    images,
    onScanNFC
  };
};
