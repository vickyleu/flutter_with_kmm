
import 'package:flutter_with_kmm/entities/user.dart';

abstract class UserInfoState {}

class UserInfoLoading extends UserInfoState {}

class UserInfoInitial extends UserInfoState {
  final User user;
  UserInfoInitial(this.user);
}

class UserInfoSaved extends UserInfoState {}

