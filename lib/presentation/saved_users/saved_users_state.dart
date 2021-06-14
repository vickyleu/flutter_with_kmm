import 'package:flutter_with_kmm/entities/user.dart';

abstract class SavedUsersState {}

class SavedUsersLoaded extends SavedUsersState {
  final List<User> users;
  SavedUsersLoaded({required this.users});
}

class SavedUsersLoading extends SavedUsersState {}

