import UIKit
import Flutter
import shared
@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
   private lazy var gateway:SDKGateway
    
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    gateway = SharedSDK(driverFactory = DatabaseDriverFactory(application)).gateway
      
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
