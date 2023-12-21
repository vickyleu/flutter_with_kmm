import 'package:bloc/bloc.dart';
import 'package:flutter_with_kmm/domain/interactor.dart';
import 'package:flutter_with_kmm/presentation/users/users_state.dart';
import 'package:flutter_with_kmm/entities/user.dart';

class UsersCubit extends Cubit<UsersState> {

  Interactor interactor;
  var _currentPage = 0;
  List<User> users = [];

  UsersCubit(this.interactor) : super(UsersLoading()){
    getUsers();
  }

  getUsers() async{
    interactor.updateProgress(true);
    if(await interactor.isInternetGranted()) {
      List<User>? newUsers = await interactor.getUsers(++_currentPage, 20);
      if (newUsers != null) {
        users.addAll(newUsers);
        emit(UsersLoaded(users: users));
        interactor.updateProgress(false);
      }
    } else {
      interactor.retry(() {
        print("retry getUsers");
        interactor.updateProgress(false);
        getUsers();
      });
      print("No internet connection\n");
      interactor.errorStream.add('No internet connection');
      // emit(UsersError(errorText: 'No internet connection'));
    }
  }

}
