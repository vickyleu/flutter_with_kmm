import UIKit
import Flutter
import shared

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    private var gateway:SDKGateway?
    private var lifecyle: EngineLifecycleListener?
    private var platform: BaseApplication?
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        IdiotFlutterBridge.register(with:  self)
        
        // 获取主窗口
        guard let window = UIApplication.shared.windows.first else {
            return false
        }
        platform = BaseApplication(app: application){ flutterMethodChannel in
            // 在这里处理 handleFlutterEngineChange
            // 你可以使用传递进来的 flutterMethodChannel 进行处理
            flutterMethodChannel.setMethodCallHandler {  call, result in
                if let gateway = self.gateway {
                    gateway.processCall(method: call.method, arguments: call.arguments, callHandler: CallHandlerImpl(callResult: result))
                }else{
                    result(FlutterError(code: "10086", message: "gateway is down", details: nil))
                }
            }
            self.gateway?.setCallbacks(callback: CallbackHandlerImpl(methodChannel: flutterMethodChannel))
        }
        gateway = SharedSDK(driverFactory: DatabaseDriverFactory(),platform: platform!).gateway
        // 获取主窗口的根视图控制器
        if let flutterViewController = window.rootViewController as? FlutterViewController {
            // 在这里你可以访问或操作主窗口的根视图控制器
            lifecyle = SwiftEngineLifecycleListener(
                onPreEngineRestart: { [weak self] in
                    self?.onFlutterResume()
                },
                onEngineWillDestroy: { [weak self] in
                    self?.onFlutterDestroy()
                }
            )
            onFlutterCreate(flutterViewController)
        }
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    func onFlutterCreate(_ flutterViewController:FlutterViewController){
        platform?.setupEngine(controller: flutterViewController, lifecycle: lifecyle!)
    }
    func onFlutterResume(){
        
    }
    func onFlutterDestroy(){
        
    }
}
class SwiftEngineLifecycleListener: EngineLifecycleListener {
    var _onPreEngineRestart: (() -> Void)?
    var _onEngineWillDestroy: (() -> Void)?
    
    init(onPreEngineRestart: @escaping () -> Void, onEngineWillDestroy: @escaping () -> Void) {
        self._onPreEngineRestart = onPreEngineRestart
        self._onEngineWillDestroy = onEngineWillDestroy
    }
    
    func onPreEngineRestart() {
        // 实现 onPreEngineRestart 方法的逻辑
        _onPreEngineRestart?()
    }
    
    func onEngineWillDestroy() {
        // 实现 onEngineWillDestroy 方法的逻辑
        _onEngineWillDestroy?()
    }
}
