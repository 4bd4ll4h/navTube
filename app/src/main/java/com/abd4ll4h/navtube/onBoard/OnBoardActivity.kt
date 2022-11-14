package com.abd4ll4h.navtube.onBoard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.MainActivity
import com.abd4ll4h.navtube.R
import com.abd4ll4h.navtube.databinding.ActivityMainBinding
import com.abd4ll4h.navtube.databinding.ActivityOnBoardBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnBoardActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityOnBoardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val adapter = ViewPagerAdapter(
            this.supportFragmentManager,
            lifecycle
        )

        binding.onBoardPager.adapter = adapter
        binding.tabLayout.attachTo(binding.onBoardPager)
        binding.next.setOnClickListener(this)
        binding.onBoardPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {

                    2 -> {
                        binding.root.background =
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.screen2_shape,
                                this@OnBoardActivity.theme
                            )
                        Log.i("asffgh","here")
                        binding.next.text = getString(R.string.finish)
                        if (!Settings.canDrawOverlays(applicationContext)) {
                            binding.next.isClickable = false
                            binding.next.isFocusable = false
                            binding.next.alpha = 0.5f
                        }
                    }
                    0 -> {
                        binding.root.background =
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.screen1_shape,
                                this@OnBoardActivity.theme
                            )
                        binding.next.text = getString(R.string.next)
                        binding.next.isClickable = true
                        binding.next.isFocusable = true
                        binding.next.alpha = 1f
                    }
                    1 -> {
                        binding.root.background =
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.screen2_shape,
                                this@OnBoardActivity.theme
                            )
                        binding.next.text = getString(R.string.next)
                        binding.next.isClickable = true
                        binding.next.isFocusable = true
                        binding.next.alpha = 1f
                    }


                }
            }
        })
    }



    override fun onResume() {
        super.onResume()
        if (!Settings.canDrawOverlays(applicationContext)&& binding.onBoardPager.currentItem==2) {
            binding.next.isClickable = false
            binding.next.isFocusable = false
            binding.next.alpha = 0.5f
        } else {
            binding.next.isClickable = true
            binding.next.isFocusable = true
            binding.next.alpha = 1f
        }
    }

    override fun onClick(v: View?) {

        if (binding.onBoardPager.currentItem == 2) {
            val sharedPreferences = applicationContext.getSharedPreferences(
                application.packageName,
                MODE_PRIVATE
            )
            sharedPreferences.edit().putBoolean(KeyText.firstTime, false).apply()
            finish()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        } else binding.onBoardPager.setCurrentItem((binding.onBoardPager.currentItem + 1) % 3, true)
    }

}