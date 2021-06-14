import 'package:flutter/material.dart';
import 'presentation/main/main_screen.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_screen.dart';
import 'package:flutter_with_kmm/entities/user.dart';

class AppRouter {
  static Route onGenerateRoute(RouteSettings settings) {
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
  }
}
