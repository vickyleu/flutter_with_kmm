import 'dart:convert';
import 'package:flutter_with_kmm/entities/user.dart';
import 'platform.dart';

class Gateway {

  static isInternetGranted() async{
    return platform.invokeMethod("isInternetGranted").then((value) => bool.parse(value.toString()));
  }
  static getUsers(int page, int results) async{
    return platform.invokeMethod<String>("users", [page, results]);
  }

  static saveUser(User user) async{
    return platform.invokeMethod("saveUser", jsonEncode(user));
  }

  static setPlatformCallsListeners(Function onUsersUpdate,Function(Map<String,dynamic> map) onNativeCall){
    platform.setMethodCallHandler((call) async{
      if (call.method == 'users' ) {
        String result = call.arguments as String;
        onUsersUpdate(result);
      }
      else if (call.method == 'nativeCallback' ) {
        final Map<dynamic,dynamic> arguments = call.arguments as Map<dynamic,dynamic>;
        final Map<String,dynamic> map =arguments.map((key, value){
          if(value is Map<dynamic,dynamic>){
            return MapEntry(key.toString(), value.map((key, value) => MapEntry(key.toString(), value)));
          }else{
            return MapEntry(key.toString(), value);
          }
        });
        onNativeCall(map);
      }
      return Future.value();
    });
  }

}