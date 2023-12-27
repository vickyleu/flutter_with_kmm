package com.example.flutter_with_kmm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.core.view.children
import com.airbnb.lottie.LottieAnimationView
import com.example.flutter_with_kmm.utils.HarmonyCheck
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.max


class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottie)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(HarmonyCheck.isHarmonyOs()){
                this.runOnUiThread {
                    launchLottieAnimation()
                }
            }else{
                splashScreen.setOnExitAnimationListener { screenViewProvider ->
                    launchLottieAnimation(screenViewProvider = screenViewProvider)
                }
            }
        } else {
            this.runOnUiThread {
                launchLottieAnimation()
            }
        }
    }

    private fun launchLottieAnimation(screenViewProvider: SplashScreenViewProvider? = null) {
        val lottieView = findViewById<LottieAnimationView>(R.id.animationView)
        lottieView.enableMergePathsForKitKatAndAbove(true)
        val contentView = findViewById<FrameLayout>(android.R.id.content)
        postFlutterView?.measure(
            View.MeasureSpec.makeMeasureSpec(contentView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(contentView.height, View.MeasureSpec.EXACTLY)
        )
        postFlutterView?.layout(0, 0, contentView.width, contentView.height)
        // We compute the delay to wait for the end of the splash screen icon
        // animation.
        val delayTime = if (screenViewProvider != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val splashScreenAnimationEndTime =
                    Instant.ofEpochMilli(screenViewProvider.iconAnimationStartMillis + screenViewProvider.iconAnimationDurationMillis)
                val delay = Instant.now(Clock.systemUTC()).until(
                    splashScreenAnimationEndTime,
                    ChronoUnit.MILLIS
                )
                delay
            } else {
                null
            }
        } else {
            null
        }
        if (screenViewProvider != null && delayTime != null) {
            // Once the delay expires, we start the lottie animation
            lottieView.postDelayed({
                screenViewProvider.view.alpha = 0f
                screenViewProvider.iconView.alpha = 0f
                lottieView.playAnimation()
            }, delayTime)
        } else {
            lottieView.playAnimation()
        }

        // Finally we dismiss display our app content using a
        // nice circular reveal
        val animationTime = 600L
        lottieView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val imageView = findViewById<ImageView>(R.id.imageView)
                val flutterView: View = postFlutterView ?: FrameLayout(this@MainActivity)
                flutterView.alpha = 0f
                // 透明度
                val animatorFlutter = ObjectAnimator.ofFloat(
                    flutterView,
                    "alpha",
                    0f,
                    1f
                )
                val animationLayer = contentView.children.last()

                val animator = ViewAnimationUtils.createCircularReveal(
                    imageView,
                    contentView.width / 2,
                    contentView.height / 2,
                    0f,
                    max(contentView.width, contentView.height).toFloat()
                ).setDuration(animationTime * 2)
                imageView.alpha = 0f
                val animator1 = ObjectAnimator.ofFloat(
                    imageView,
                    "alpha",
                    0f,
                    1f
                ).setDuration(animationTime)

                animatorFlutter.startDelay = animator1.duration



                animator.interpolator = AnticipateInterpolator()
                // 添加一个imageView放大的动画,类似与推特的小鸟动画
                val animator2 = ObjectAnimator.ofFloat(
                    imageView,
                    "scaleX",
                    1f,
                    1.5f
                )
                val animator3 = ObjectAnimator.ofFloat(
                    imageView,
                    "scaleY",
                    1f,
                    1.5f
                )
                //合并animator2和animator
                val animatorSet = AnimatorSet()
                val animatorScaleSet = AnimatorSet()
                animatorScaleSet.playTogether(animator2, animator3)
                animatorScaleSet.setDuration(animationTime * 2)
                animatorScaleSet.interpolator = AccelerateDecelerateInterpolator()

                animatorFlutter.setDuration(animatorScaleSet.duration - animator.duration)

                animatorSet.playTogether(
                    animator, animator1,
                    animatorScaleSet,
                    animatorFlutter
                )
                animatorSet.interpolator = AccelerateDecelerateInterpolator()

                animatorSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        imageView.clearAnimation()
                        contentView.removeViewAt(1)
                        flutterView.alpha = 1f
                        flutterView.clearAnimation()
                    }
                })
                imageView.visibility = View.VISIBLE
                contentView.addView(flutterView, 0)
                animatorSet.start()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // We display our landing activity edge to edge just like the splash screen
        // to have a seamless transition from the system splash screen.
        // This is done in onResume() so we are sure that our Activity is attached
        // to its window.
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private var postFlutterView: View? = null
    override fun setContentView(view: View?) {
        postFlutterView = view
    }

    /**
     * 由于鸿蒙系统缺失了onBackPressedCallback类，所以需要自己实现一个导出变量,使用反射获取不了,读取function表就会抛异常
     */
    val flutterEngineImpl: FlutterEngine?
        get() =
            this.flutterEngine
}
