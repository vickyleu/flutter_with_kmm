import 'dart:convert';
import 'package:flutter_with_kmm/entities/user.dart';
import 'platform.dart';

class Gateway {

  static getUsers(int page, int results) async{
    return platform.invokeMethod<String>("users", [page, results]);
  }

  static saveUser(User user) async{
    return platform.invokeMethod("saveUser", jsonEncode(user));
  }

  static setPlatformCallsListeners(Function onUsersUpdate){
    platform.setMethodCallHandler((call) {
      if (call.method == 'users' ) {
        String result = call.arguments as String;
        onUsersUpdate(result);
      }
      return Future.value();
    });
  }

}