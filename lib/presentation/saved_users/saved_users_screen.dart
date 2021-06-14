import 'package:flutter/material.dart';
import 'package:flutter_with_kmm/presentation/saved_users/saved_users_cubit.dart';
import 'package:flutter_with_kmm/presentation/saved_users/saved_users_state.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_screen.dart';
import 'package:flutter_with_kmm/presentation/users/users_screen.dart';
import 'package:flutter_with_kmm/presentation/users/users_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_with_kmm/entities/user.dart';

class SavedUsersScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    Function(User) onItemClick = (user) {
      Navigator.pushNamed(context, UserInfoScreen.routeName, arguments: user);
    };
    return Scaffold(
      appBar: AppBar(
        title: Text("Users"),
      ),
      body: Container(
          color: Colors.black12,
          child: BlocBuilder<SavedUsersCubit, SavedUsersState>(
              builder: (context, state) {
                if (state is SavedUsersLoaded) {
                  return ListView(
                    children: state.users
                        .map((e) => UserListItem(e, onItemClick))
                        .toList(),
                  );
                } else if (state is UsersLoading) {
                  return Center(child: CircularProgressIndicator(),);
                } else {
                  return Center(child: CircularProgressIndicator(),);
                }
          })
      ),
    );
  }

}



