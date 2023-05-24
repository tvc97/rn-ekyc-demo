import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';

const Stack = createStackNavigator();

import Menu from './Menu';
import MRZ from './MRZ';
import Liveness from './Liveness';

const App = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Menu" component={Menu} />
        <Stack.Screen name="MRZ" component={MRZ} />
        <Stack.Screen name="Liveness" component={Liveness} />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
