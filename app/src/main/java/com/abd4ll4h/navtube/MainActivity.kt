package com.abd4ll4h.navtube

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.abd4ll4h.navtube.bubbleWidget.BubbleService
import com.abd4ll4h.navtube.bubbleWidget.Content
import com.abd4ll4h.navtube.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {
    var contentHasLoaded = false
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        startLoadingContent()
        setupSplashScreen(splashScreen)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigationBar()
        navBottomBehavior()
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        Log.i("check@bubble","satrt")

        if (!Settings.canDrawOverlays(this)) {
            Log.i("check@bubble","check premisson")

            startActivityForResult(intent, 101)
        }else login()
    }
    fun login() {
        Log.i("check@bubble","login")

        if ( Settings.canDrawOverlays(this)) {
            Log.i("check@bubble","start login")

            val service = Intent(this, BubbleService::class.java)
            ContextCompat.startForegroundService(this,service)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && Settings.canDrawOverlays(this)) {
            login()
        }
    }
    private fun startLoadingContent() {
        // For this example, the Timer delay represents awaiting a response from a network call
        Timer().schedule(3000){
            contentHasLoaded = true
        }
    }


    private fun setupSplashScreen(splashScreen: SplashScreen) {
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (contentHasLoaded) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else false
                }
            }
        )

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideBack = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_X,
                0f,
                -splashScreenView.view.width.toFloat()
            ).apply {
                interpolator = DecelerateInterpolator()
                duration = 800L
                doOnEnd { splashScreenView.remove() }
            }

            slideBack.start()
        }
    }




    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }

    private fun setupBottomNavigationBar() {
        binding.bottomNavigationView.setupWithNavController(navController)
    }
    fun navBottomBehavior(){
        binding.bottomNavigationView.setOnItemReselectedListener {
            if (it.itemId == R.id.mainMenu) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                with(navHostFragment!!.childFragmentManager.fragments[0] as MainFragment){
                    smoothScrollToTop()
                }
            }
        }

    }
}