import UIKit
import Flutter
import shared

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
  private  var gateway:SDKGateway?
    
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    IdiotFlutterBridge.register(with:  self)
    gateway = SharedSDK(driverFactory: DatabaseDriverFactory(),platform: BaseApplication(app: application)).gateway
      
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
