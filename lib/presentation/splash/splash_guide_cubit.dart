import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_with_kmm/domain/interactor.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_state.dart';

class SplashGuideCubit extends Cubit<SplashState> {
  Interactor interactor;

  SplashGuideCubit(this.interactor) : super(SplashLoading());
}
