import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_constraintlayout/flutter_constraintlayout.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_guide_cubit.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_state.dart';

class SplashScreen extends StatelessWidget {
  static const routeName = '/splash';

  final ConstraintId box0 = ConstraintId('box0');
  final ConstraintId box1 = ConstraintId('box1');

  @override
  Widget build(BuildContext context) {
    var splashGuideCubit = context.read<SplashGuideCubit>();
    return Scaffold(
      body: BlocConsumer<SplashGuideCubit, SplashState>(
          listenWhen: (_, state) => state is SplashLoaded,
          listener: (context, state) {
            if (state is SplashLoaded) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text("Complete"),
                  backgroundColor: Colors.grey,
                ),
              );
            }
          },
          buildWhen: (_, state) => true,
          builder: (context, state) {
            return ConstraintLayout(
              children: [
                PageView.builder(
                  controller: PageController(
                    viewportFraction: 1,
                  ),
                  itemBuilder: (context,index){
                    return ConstraintLayout(
                      children: [
                        Container(
                          color: Colors.yellow,
                        ).apply(constraint: Constraint(
                            id: box1,
                            width: 50,
                            left: ConstraintAlign(parent, ConstraintAlignType.left),
                            top: ConstraintAlign(parent, ConstraintAlignType.top),
                            bottom: ConstraintAlign(parent, ConstraintAlignType.bottom)
                        ))
                      ],
                    );
                  },).apply(constraint: Constraint(
                  id: box0,
                  top: ConstraintAlign(parent, ConstraintAlignType.top),
                  left: ConstraintAlign(parent, ConstraintAlignType.left),
                  right: ConstraintAlign(parent, ConstraintAlignType.right),
                  bottom: ConstraintAlign(parent, ConstraintAlignType.bottom),
                ))
              ],
            );
          }),
    );
  }
}
