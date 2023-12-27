import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_guide_cubit.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_cubit.dart';
import 'package:fps_monitor/widget/custom_widget_inspector.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'domain/interactor.dart';
import 'presentation/saved_users/saved_users_cubit.dart';
import 'presentation/users/users_cubit.dart';
import 'route/app_router.dart';

void main() {
  runApp(FlutterWithKmmApp());
}

class FlutterWithKmmApp extends StatelessWidget {
  final Interactor interactor = Interactor();

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
    SchedulerBinding.instance.addPostFrameCallback((t) =>
      overlayState =_navigatorKey.currentState!.overlay!
    );
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
        builder: (context,child){
          return MaterialApp.router(
            theme: ThemeData(primaryColor: Color(0xFF8B7ADF)),
            debugShowCheckedModeBanner: false,
            checkerboardOffscreenLayers: true, // 开启渲染层检测
            checkerboardRasterCacheImages: true, // 开启图片缓存检测
            themeAnimationDuration: Duration(milliseconds: 15), // 主题切换时间
            showPerformanceOverlay: true, // 开启FPS监控
            title: '',
            builder: (context, child) {
              return CustomWidgetInspector(child: child??Container());
            },
            routerConfig: AppRouter.router(_navigatorKey),
            scaffoldMessengerKey: _scaffoldKey,
          );
        },
      ),
    );
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
