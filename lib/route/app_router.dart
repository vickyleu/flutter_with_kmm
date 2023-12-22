import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../presentation/main/main_screen.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_screen.dart';
import 'package:flutter_with_kmm/entities/user.dart';


class AppRouter {
  // GoRouter configuration
  static GoRouter router(GlobalKey<NavigatorState> navigatorKey) {
    return GoRouter(
      initialLocation: '/main',
      navigatorKey: navigatorKey,
      routes: [
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

/*static Route onGenerateRoute(RouteSettings settings) {
    Widget screen;
    switch (settings.name) {
      case "/main":
        screen = MainScreen();
        break;
      case "/user_info":
        screen = UserInfoScreen(settings.arguments as User);
        break;
      default:
        screen = Container();
    }
    return MaterialPageRoute(builder: (_) => screen);
  }*/
}
