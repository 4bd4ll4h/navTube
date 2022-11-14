package com.abd4ll4h.navtube

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.os.HandlerCompat.postDelayed
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.abd4ll4h.navtube.DataFetch.Repository
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.databinding.ActivityMainBinding
import com.abd4ll4h.navtube.onBoard.OnBoardActivity
import com.abd4ll4h.navtube.utils.ConnectionLiveData
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    var contentHasLoaded = false
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lunchOnboardOrContinue()
    }

    private fun lunchOnboardOrContinue() {
        val sharedPreferences =
            applicationContext.getSharedPreferences(application.packageName, MODE_PRIVATE)
        val firstLunch = sharedPreferences.getBoolean(KeyText.firstTime, true)
        if (firstLunch) {
            startActivity(Intent(applicationContext, OnBoardActivity::class.java))
            finish()
        } else {
            val splashScreen = installSplashScreen()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            startLoadingContent()
            setupSplashScreen(splashScreen)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupBottomNavigationBar()
            navBottomBehavior()

            ConnectionLiveData(this).observe(this) { connectivityStateChange(it) }
        }
    }

    private fun connectivityStateChange(isConnected: Boolean) {
        when (binding.bottomNavigationView.selectedItemId) {
            R.id.mainMenu -> {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                with(navHostFragment!!.childFragmentManager.fragments[0] as MainFragment) {

                    connectivityChanged(isConnected)
                }
            }
        }


    }


    private fun startLoadingContent() {
        // For this example, the Timer delay represents awaiting a response from a network call
        lifecycleScope.launch {
            if (!Repository.DataRepository.getInstance(applicationContext)
                    .setUpConnection(applicationContext)
            ) {
                Toast.makeText(this@MainActivity, getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
            }
            contentHasLoaded = true

        }
        postDelayed(Handler(this.mainLooper),{contentHasLoaded=true},null,4000)
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
                duration = 400L
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

    private fun navBottomBehavior() {
        binding.bottomNavigationView.setOnItemReselectedListener {
            if (it.itemId == R.id.mainMenu) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                with(navHostFragment!!.childFragmentManager.fragments[0] as MainFragment) {
                    smoothScrollToTop()
                }
            }
        }

    }
}