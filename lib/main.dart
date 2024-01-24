import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_guide_cubit.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_cubit.dart';
import 'package:fps_monitor/widget/custom_widget_inspector.dart';
import 'package:go_router/go_router.dart';

import 'domain/interactor.dart';
import 'domain/platform.dart';
import 'presentation/saved_users/saved_users_cubit.dart';
import 'presentation/users/users_cubit.dart';
import 'route/app_router.dart';

/// 启动入口,args是从原生传递过来的参数,但是此方法不适用热重启,flutter引擎获得的参数不会再改变了
Future<void> main(List<String> args) async {
  WidgetsFlutterBinding.ensureInitialized();
  var platformReady = false;
  bool isHotRestart = true;
  while (!platformReady) {
    try {
      // 必须当flutter引擎加载完成,原生已经获取到flutter引擎的实例才开始绘制flutter的视图
      isHotRestart = bool.parse(await platform.invokeMethod("isHotRestart") as String);
      platformReady = true;
    } catch (e) {
      print("isHotRestart::::${e}");
      platformReady = false;
    }
  }
  // 绘制布局边界
  debugPaintSizeEnabled = true;
  runApp(FlutterWithKmmApp(isHotRestart: isHotRestart));
}

class FlutterWithKmmApp extends StatelessWidget {
  final Interactor interactor = Interactor();
  final bool isHotRestart;

  FlutterWithKmmApp({this.isHotRestart = false});

  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey();

  final GlobalKey<ScaffoldMessengerState> _scaffoldKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    if (!interactor.isInitialized) {
      interactor.init();
      interactor.errorStream.stream.listen((message) {
        showError(message);
      });
      interactor.progressStream.stream.listen((state) {
        updateProgress(state);
      });
    }
    getOverlay();
    // 添加一个全局动画,当视图开始显示时,一个从上往下的fadein动画展示出来,并且由上扩散的粒子效果,持续时间为1秒,
    // 全部使用flutter自带的动画,不需要引入其他库
    return MultiBlocProvider(
      providers: [
        BlocProvider<UsersCubit>(
          create: (_) => UsersCubit(interactor),
        ),
        BlocProvider<SavedUsersCubit>(
          create: (_) => SavedUsersCubit(interactor),
        ),
        BlocProvider<UserInfoCubit>(
          create: (_) => UserInfoCubit(interactor),
        ),
        BlocProvider<SplashGuideCubit>(
          create: (_) => SplashGuideCubit(interactor),
        ),
      ],
      child: ScreenUtilInit(
        designSize: const Size(375, 812),
        minTextAdapt: true,
        splitScreenMode: true,
        builder: (context, child) {
          return MaterialApp.router(
            theme: ThemeData(primaryColor: Color(0xFF8B7ADF)),
            debugShowCheckedModeBanner: false,
            checkerboardOffscreenLayers: true,
            // 开启渲染层检测
            checkerboardRasterCacheImages: true,
            // 开启图片缓存检测
            themeAnimationDuration: Duration(milliseconds: 15),
            // 主题切换时间
            showPerformanceOverlay: true,
            // 开启FPS监控
            title: '',
            builder: (context, child) {
              final screenSize = MediaQuery.of(context).size;
              if (screenSize.isEmpty) {
                return Container();
              }
              if (child is Router<Object>) {
                final config = child.routerDelegate.currentConfiguration;
                if (config is RouteMatchList && config.isEmpty) {
                  return AppStateWidget(child,
                      isHotRestart: isHotRestart, screenSize: screenSize);
                }
              }
              return CustomWidgetInspector(child: child ?? Container());
            },
            routerConfig: AppRouter.router(_navigatorKey),
            scaffoldMessengerKey: _scaffoldKey,
          );
        },
      ),
    );
  }

  void getOverlay() {
    SchedulerBinding.instance.addPostFrameCallback((t) {
      final overlay = _navigatorKey.currentState?.overlay;
      if (overlay != null) {
        overlayState = overlay;
      } else {
        getOverlay();
      }
    });
  }

  showError(String errorText) {
    _scaffoldKey.currentState?.showSnackBar(
      SnackBar(
        content: Text(errorText),
        backgroundColor: Colors.red,
      ),
    );
  }

  updateProgress(bool state) {
    if (state) {
      showDialog(
        barrierDismissible: false,
        barrierColor: Colors.transparent,
        context: _navigatorKey.currentContext!,
        builder: (BuildContext context) {
          return Center(
            child: CircularProgressIndicator(),
          );
        },
      );
    } else {
      try {
        _navigatorKey.currentContext?.pop();
        // _navigatorKey.currentState?.pop();
      } catch (e) {
        print(e);
      }
    }
  }
}

class AppStateWidget extends StatefulWidget {
  final Widget? child;
  final bool isHotRestart;
  final Size screenSize;

  AppStateWidget(this.child,
      {required this.isHotRestart, super.key, required this.screenSize});

  @override
  State<StatefulWidget> createState() {
    return _AppState(child: this.child);
  }
}

class _AppState extends State<AppStateWidget>
    with SingleTickerProviderStateMixin {
  final Widget? child;

  _AppState({this.child});

  AnimationController? animationController;
  Animation<Offset>? animation1;
  Animation<double>? animation2;

  @override
  void reassemble() {
    super.reassemble();
  }

  @override
  void initState() {
    animationController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 300),
    );
    super.initState();

    SchedulerBinding.instance.addPostFrameCallback((t) {
      final curved = CurvedAnimation(
        parent: animationController!,
        curve: Curves.fastEaseInToSlowEaseOut, // 选择合适的插值器
      );
      animation1 =
          Tween(begin: Offset(0, -widget.screenSize.height), end: Offset(0, 0))
              .animate(curved);
      // animation2缩放动画
      animation2 = Tween(begin: 0.0, end: 1.0).animate(curved);
      if (widget.isHotRestart) {
        animationController!.forward();
        return;
      }
      waitForLoading(() {
        animationController!.forward();
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Stack(
        children: [
          AnimatedBuilder(
            animation: animationController!,
            builder: (context, child) {
              final dy = animation1?.value.dy ?? -widget.screenSize.height;
              return Positioned(
                  top: dy,
                  left: 0,
                  right: 0,
                  child: Container(
                    width: widget.screenSize.width * (animation2?.value ?? 0),
                    height: widget.screenSize.height,
                    child: Center(
                      child: CustomWidgetInspector(child: child ?? Container()),
                    ),
                  ));
            },
            child: widget.child,
          ),
        ],
      ),
      color: Colors.blue,
    );
  }

  Future<void> waitForLoading(VoidCallback callback) async {
    await Future.delayed(Duration(milliseconds: 100)).then((value) async {
      bool isHotRestart = true;
      try {
        isHotRestart = bool.parse(await platform.invokeMethod("isHotRestart") as String);
        if (!isHotRestart) {
          await waitForLoading(callback);
          return;
        }
      } catch (e) {
        await waitForLoading(callback);
        return;
      }
      callback.call();
    });
  }
}
