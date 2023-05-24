#import "AppDelegate.h"

#import <React/RCTBundleURLProvider.h>

@interface AppDelegate()

@property (nonatomic, strong) UIViewController *reactNativeViewController;
@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"ekyc";
  // You can add your custom initial props in the dictionary below.
  // They will be passed down to the ViewController used by React Native.
  self.initialProps = @{};

  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

/// This method controls whether the `concurrentRoot`feature of React18 is turned on or off.
///
/// @see: https://reactjs.org/blog/2022/03/29/react-v18.html
/// @note: This requires to be rendering on Fabric (i.e. on the New Architecture).
/// @return: `true` if the `concurrentRoot` feature is enabled. Otherwise, it returns `false`.
- (BOOL)concurrentRootEnabled
{
  return true;
}

- (void)goToReactNative {

  [self.window.rootViewController dismissViewControllerAnimated:TRUE completion:nil];

}

- (void)goNativeStoryboard {

  UIViewController *vc = [UIStoryboard storyboardWithName:@"EKYC" bundle:nil].instantiateInitialViewController;

  [self.window.rootViewController presentViewController:vc animated:true completion:nil];

}

@end
