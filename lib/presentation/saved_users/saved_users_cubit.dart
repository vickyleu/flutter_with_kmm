


import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_with_kmm/domain/interactor.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import 'package:flutter_with_kmm/presentation/saved_users/saved_users_state.dart';

class SavedUsersCubit extends Cubit<SavedUsersState> {

  Interactor interactor;

  SavedUsersCubit(this.interactor) : super(SavedUsersLoading()){
    subscribeToUsersStream();
  }

  subscribeToUsersStream(){
    interactor.usersStream.stream.listen((users) {
      emit(SavedUsersLoaded(users: users));
    });
  }

}