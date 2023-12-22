import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_with_kmm/presentation/saved_users/saved_users_screen.dart';
import 'package:flutter_with_kmm/presentation/users/users_screen.dart';

class MainScreen extends StatefulWidget {

  @override
  State<StatefulWidget> createState() => MainPresentationState();

}

class MainPresentationState extends State<MainScreen> {

  var _currentIndex = 0;

  final _presentation = [
    UsersScreen(),
    SavedUsersScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: _presentation[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        type: BottomNavigationBarType.fixed,
        onTap: (index) => setState(() { _currentIndex = index; }),
        items: [
          BottomNavigationBarItem(icon: Icon(Icons.group), label: "Users"),
          BottomNavigationBarItem(icon: Icon(Icons.save), label: "Saved"),
        ],
      ),
    );
  }

}




