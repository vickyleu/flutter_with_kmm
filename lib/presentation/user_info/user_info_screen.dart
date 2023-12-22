import 'dart:ui';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:provider/provider.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import 'user_info_cubit.dart';

class UserInfoScreen extends StatelessWidget {

  final User _user;

  UserInfoScreen(this._user);

  static const routeName = '/user_info';

  @override
  Widget build(BuildContext context) {
    var userInfoCubit = context.read<UserInfoCubit>();
    userInfoCubit.user = _user;
    return Scaffold(
      appBar: AppBar(
        title: Text("UserInfo"),
    ),
      body: Container(
          child: BlocConsumer<UserInfoCubit, UserInfoState>(
              listenWhen: (_,state) => state is UserInfoSaved ,
              listener: (context, state) {
                if(state is UserInfoSaved) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text("Сохранено"),
                      backgroundColor: Colors.grey,
                    ),
                  );
                }
              },
              buildWhen: (_,state) => state is UserInfoInitial || state is UserInfoLoading,
              builder: (context, state) {
                if (state is UserInfoInitial) {
                  return Stack(
                    children: [
                      Container(
                        padding: EdgeInsets.only(bottom: 28),
                        child: AspectRatio(
                          aspectRatio: 1,
                          child: Stack(
                            children: [
                              Container(
                                width: double.infinity,
                                height: double.infinity,
                                child: Image.network(
                                  _user.picture,
                                  fit: BoxFit.fitHeight,
                                ),
                              ),
                              Align(
                                alignment: Alignment.bottomLeft,
                                child: Padding(
                                  padding: EdgeInsets.all(16),
                                  child: Text(_user.firstName,
                                    style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      Positioned(
                        bottom: 0,
                        right: 24,
                        width: 56,
                        height: 56,
                        child: FloatingActionButton(
                            onPressed: () => userInfoCubit.saveUser(),
                            backgroundColor: Color(0xFFF2638E),
                            child: Icon(Icons.save,size: 24,)
                        ),
                      )
                    ],
                  );
                } else {
                  return Center(child: CircularProgressIndicator(),);
                }
          })
      ),
    );
  }

}


