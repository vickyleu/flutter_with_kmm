import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import '../common/constants.dart';
import 'gateway.dart';
import 'package:rxdart/rxdart.dart';

class Interactor {

  StreamController<String> errorStream = StreamController<String>.broadcast();
  StreamController<bool> progressStream = StreamController<bool>.broadcast();
  StreamController<Map<String,dynamic>> nativeStream = StreamController<Map<String,dynamic>>.broadcast();
  final usersStream = BehaviorSubject<List<User>>();

  bool isInitialized = false;

  init() async{
    setPlatformCallsListeners();
    isInitialized = true;
  }

  Future<bool> isInternetGranted() async {
    return await doOnKMM(() async {
      bool isGranted = (await Gateway.isInternetGranted() as bool?) ?? false;
      return isGranted;
    });
  }



  List get nativeMethod  => [InternetGranted];
  Future<List<User>?> getUsers(int page, int results) async {
    return doOnKMM(() async {
      String? users = await Gateway.getUsers(page, results);
      List<User> res = (json.decode(users!) as List)
          .map((model) => User.fromJson(model)).toList();
      return res;
    });
  }

  saveUser(User user) async {
    return doOnKMM(() async {
      return await Gateway.saveUser(user);
    });
  }

  void setPlatformCallsListeners(){
    var onUsersUpdate = (String result) {
      var users = (json.decode(result) as List).map((model) =>
          User.fromJson(model)).toList();
      usersStream.add(users);
    };

    var onNativeCall = (Map<String,dynamic> result) {
      if(nativeMethod.contains(result['method'])){
        nativeStream.add(result);
      }
    };
    Gateway.setPlatformCallsListeners(onUsersUpdate,onNativeCall);
  }

  updateProgress(bool state) {
    progressStream.add(state);
  }

  Future<T?> doOnKMM<T> (Function toDo) async{
    try {
      return await toDo();
    } on PlatformException catch (e) {
      errorStream.add(e.message ?? 'Unknown error');
      updateProgress(false);
    }
    return null;
  }

  void retry(VoidCallback callback) {
    // 只监听一次
    StreamSubscription<Map<String, dynamic>>? sub;
    sub= nativeStream.stream.listen((Map<String,dynamic> result) {
      final String method = result['method'];
      if(method == InternetGranted){
        final Map<String,dynamic> args = result['args'] as Map<String,dynamic>;
        final SDKNetworkGrantedType type =SDKNetworkGrantedType.fromString(args['Granted']);
        switch(type){
          case SDKNetworkGrantedType.Accessible:{
            callback();
            sub?.cancel();
            sub=null;
          }
          case SDKNetworkGrantedType.Restricted:{

          }
          case SDKNetworkGrantedType.Unknown:{

          }
        }
      }
    });
  }

}
