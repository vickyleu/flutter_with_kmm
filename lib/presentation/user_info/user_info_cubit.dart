import 'package:bloc/bloc.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_state.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import '../../domain/interactor.dart';

class UserInfoCubit extends Cubit<UserInfoState> {

  Interactor interactor;
  late User _user;

  UserInfoCubit(this.interactor) : super(UserInfoLoading());

  saveUser() async{
    interactor.updateProgress(true);
    var result = await interactor.saveUser(_user);
    if (result != null) {
      emit(UserInfoSaved());
      interactor.updateProgress(false);
    }
  }

  set user(User value) {
    _user = value;
    emit(UserInfoInitial(_user));
  }

}
