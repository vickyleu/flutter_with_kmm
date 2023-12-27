import 'package:flutter/material.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_screen.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_screen.dart';
import 'package:go_router/go_router.dart';

import '../presentation/main/main_screen.dart';

class AppRouter {
  // GoRouter configuration
  static GoRouter router(GlobalKey<NavigatorState> navigatorKey) {
    return GoRouter(
      // initialLocation: '/main',
      initialLocation: '/splash',
      navigatorKey: navigatorKey,
      routes: [
        GoRoute(
          path: '/splash',
          builder: (context, state) {
            return SplashScreen();
          },
        ),
        GoRoute(
          path: '/main',
          builder: (context, state) => MainScreen(),
        ),
        GoRoute(
          path: '/user_info',
          builder: (context, state) {
            return UserInfoScreen(state.extra as User);
          },
        ),

      ],
    );
  }
}
