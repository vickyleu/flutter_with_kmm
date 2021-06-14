import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import 'gateway.dart';
import 'package:rxdart/rxdart.dart';

class Interactor {

  StreamController<String> errorStream = StreamController<String>.broadcast();
  StreamController<bool> progressStream = StreamController<bool>.broadcast();
  final usersStream = BehaviorSubject<List<User>>();

  init() async{
    setPlatformCallsListeners();
  }

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
    Gateway.setPlatformCallsListeners(onUsersUpdate);
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
  }

}
