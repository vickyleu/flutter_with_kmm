import 'package:flutter/material.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_cubit.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'route/app_router.dart';
import 'domain/interactor.dart';
import 'presentation/saved_users/saved_users_cubit.dart';
import 'presentation/users/users_cubit.dart';


void main() {
  runApp(FlutterWithKmmApp());
}

class FlutterWithKmmApp extends StatelessWidget {

  final Interactor interactor = Interactor();

  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey();

  final GlobalKey<ScaffoldMessengerState> _scaffoldKey = GlobalKey();



  @override
  Widget build(BuildContext context) {
    if(!interactor.isInitialized){
      interactor.init();
      interactor.errorStream.stream.listen((message) { showError(message); });
      interactor.progressStream.stream.listen((state) { updateProgress(state); });
    }
    return MultiBlocProvider(
      providers: [
        BlocProvider<UsersCubit>(create: (_) => UsersCubit(interactor),),
        BlocProvider<SavedUsersCubit>(create: (_) => SavedUsersCubit(interactor),),
        BlocProvider<UserInfoCubit>(create: (_) => UserInfoCubit(interactor),),
      ],
      child: MaterialApp.router(
        theme: ThemeData(
            primaryColor:Color(0xFF8B7ADF)
        ),
        debugShowCheckedModeBanner: false,
        title: 'FlutterWithKmm',
        routerConfig: AppRouter.router(_navigatorKey),
        scaffoldMessengerKey: _scaffoldKey,
      ),
      /*child: MaterialApp(
        theme: ThemeData(
            primaryColor:Color(0xFF8B7ADF)
        ),
        debugShowCheckedModeBanner: false,
        title: 'FlutterWithKmm',
        initialRoute:  '/main' ,
        onGenerateRoute: AppRouter.onGenerateRoute,
        navigatorKey: _navigatorKey,
        scaffoldMessengerKey: _scaffoldKey,
      ),*/
    );
  }

  showError(String errorText){
    _scaffoldKey.currentState?.showSnackBar(
        SnackBar(
          content: Text(errorText),
          backgroundColor: Colors.red,
        ),
    );
  }

  updateProgress(bool state) {
    if (state) {
      showDialog(
        barrierDismissible: false,
        barrierColor: Colors.transparent,
        context:_navigatorKey.currentContext!,
        builder:(BuildContext context){
          return Center(child: CircularProgressIndicator(),);
        },
      );
    } else {
      try {
        _navigatorKey.currentContext?.pop();
        // _navigatorKey.currentState?.pop();
      } catch (e) {
        print(e);
      }
    }
  }

}
