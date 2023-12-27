import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_constraintlayout/flutter_constraintlayout.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_guide_cubit.dart';
import 'package:flutter_with_kmm/presentation/splash/splash_state.dart';

class SplashScreen extends StatelessWidget {
  static const routeName = '/splash';

  final ConstraintId box0 = ConstraintId('box0');
  final ConstraintId box1 = ConstraintId('box1');
  final ConstraintId boxJump = ConstraintId('boxJump');

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
            // flutter获取状态栏高度
            double statusBarHeight = MediaQuery.of(context).padding.top;
            return Container(
              padding: EdgeInsets.only(top: statusBarHeight),
              child: ConstraintLayout(
                children: [
                  PageView.builder(
                    physics: BouncingScrollPhysics(),
                    onPageChanged: (index) {
                      splashGuideCubit.changeIndex(index);
                    },
                    controller: PageController(
                      viewportFraction: 1,
                    ),
                    itemCount: 3,
                    itemBuilder: (context, index) {
                      return ConstraintLayout(
                        children: [
                          Container(
                            color: Colors.yellow,
                          ).apply(
                              constraint: Constraint(
                                  id: box1,
                                  width: 50,
                                  left: ConstraintAlign(
                                      parent, ConstraintAlignType.left),
                                  top: ConstraintAlign(
                                      parent, ConstraintAlignType.top),
                                  bottom: ConstraintAlign(
                                      parent, ConstraintAlignType.bottom)))
                        ],
                      );
                    },
                  ).apply(
                      constraint: Constraint(
                        id: box0,
                        top: ConstraintAlign(parent, ConstraintAlignType.top),
                        left: ConstraintAlign(parent, ConstraintAlignType.left),
                        right: ConstraintAlign(parent, ConstraintAlignType.right),
                        bottom: ConstraintAlign(parent, ConstraintAlignType.bottom),
                      )),
                  Container(
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: Color(0xFFD8D8D8), width: 1.w),
                    ),
                    child: Center(
                      child: Text(
                        "跳过",
                        style: TextStyle(
                          fontSize: 12.sp,
                          fontWeight: FontWeight.w500,
                          color: Color(0xFF9A9A9A),
                        ),
                      ),
                    )
                  ).apply(
                      constraint: Constraint(
                        id: boxJump,
                        width: 40.w,
                        height: 21.h,
                        zIndex: 100,
                        margin: EdgeInsets.only(top: 6.h,right: 18.w),
                        topRightTo:parent,
                      ))
                ],
              ),
            );
          }),
    );
  }
}
