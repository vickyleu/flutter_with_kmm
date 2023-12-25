import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_with_kmm/entities/user.dart';
import 'package:flutter_with_kmm/presentation/user_info/user_info_screen.dart';
import 'package:flutter_with_kmm/presentation/users/users_cubit.dart';
import 'package:flutter_with_kmm/presentation/users/users_state.dart';
import 'package:go_router/go_router.dart';
import 'package:keframe/keframe.dart';

class UsersScreen extends StatelessWidget {
  final ScrollController _scrollController = new ScrollController();

  @override
  Widget build(BuildContext context) {
    Function(User) onItemClick = (user) {
      context.push(UserInfoScreen.routeName, extra: user);
      // Navigator.pushNamed(context, UserInfoScreen.routeName, arguments: user);
    };
    _scrollController.addListener(() {
      if (_scrollController.position.pixels ==
          _scrollController.position.maxScrollExtent) {
        context.read<UsersCubit>().getUsers();
      }
    });
    return Scaffold(
      appBar: AppBar(
        title: Text("Users"),
      ),
      body: Container(
          color: Colors.black12,
          child: BlocBuilder<UsersCubit, UsersState>(builder: (context, state) {
            if (state is UsersLoaded) {
              return SizeCacheWidget(
                  child: ListView.builder(
                      controller: _scrollController,
                      itemCount: state.users.length,
                      itemBuilder: (BuildContext context, int index) {
                        return FrameSeparateWidget(
                          child: UserListItem(state.users[index], onItemClick),
                          placeHolder: Container(
                            height: 60,
                          ),
                        );
                      }));
            } else if (state is UsersLoading) {
              return Center(
                child: CircularProgressIndicator(),
              );
            } else {
              return Center(
                child: CircularProgressIndicator(),
              );
            }
          })),
    );
  }
}

class UserListItem extends StatelessWidget {
  final User user;
  final Function(User) onItemClick;

  UserListItem(this.user, this.onItemClick);

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () => onItemClick.call(user),
      child: Column(
        children: [
          Row(
            children: [
              Padding(
                padding: EdgeInsets.all(16),
                child: Image.network(
                  user.thumbnail,
                  width: 50,
                  height: 50,
                ),
              ),
              Column(
                children: [Text("${user.firstName} ${user.lastName}")],
              )
            ],
          ),
          Divider()
        ],
      ),
    );
  }
}
