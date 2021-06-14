import 'package:flutter_with_kmm/entities/user.dart';

abstract class UsersState {}

class UsersLoaded extends UsersState {
  final List<User> users;
  UsersLoaded({required this.users});
}

class UsersLoading extends UsersState {

}
