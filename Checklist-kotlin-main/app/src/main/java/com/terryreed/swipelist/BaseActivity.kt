package com.terryreed.swipelist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

/**
 * Activity that hosts a persistent bottom navigation bar.
 *
 * Future screens should extend this class to automatically gain the bottom
 * navigation and inflate their own content into the provided frame layout.
 */
abstract class BaseActivity : AppCompatActivity() {

    /** Layout resource that will be inflated inside the base container. */
    protected abstract fun getLayoutResId(): Int

    private var backPressCount = 0
    private var lastBackPressTime = 0L
    private val backPressTimeout = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statusBarColor = ContextCompat.getColor(this, R.color.brand_light_blue)
        val navigationBarColor = ContextCompat.getColor(this, R.color.surface)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(statusBarColor, statusBarColor),
            navigationBarStyle = SystemBarStyle.auto(navigationBarColor, navigationBarColor)
        )
        setContentView(R.layout.activity_base)

        SavedListsRepository.load(applicationContext)
        CategoryOrderRepository.load(applicationContext)
        GroceryCategorizer.loadCustomKeywords(applicationContext)

        val container: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(getLayoutResId(), container, true)

        val adView: AdView = findViewById(R.id.adView)
        adView.visibility = View.GONE
        val playServicesAvailable =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        if (playServicesAvailable) {
            try {
                MobileAds.initialize(this)
                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        adView.visibility = View.VISIBLE
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        adView.visibility = View.GONE
                    }
                }
                adView.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                Log.w("BaseActivity", "AdMob initialization failed", e)
            }
        }

        val bottom: BottomNavigationView = findViewById(R.id.bottomNav)
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                        finish()
                    }
                    true
                }
                R.id.nav_saved_lists -> {
                    if (this !is SavedListsActivity) {
                        startActivity(Intent(this, SavedListsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                        finish()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                        finish()
                    }
                    true
                }
                R.id.nav_categories -> {
                    if (this !is CategoriesActivity) {
                        startActivity(Intent(this, CategoriesActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                        finish()
                    }
                    true
                }
                else -> {
                    Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
        bottom.setOnItemReselectedListener { }
    }

    override fun onBackPressed() {
        if (!isTaskRoot) {
            super.onBackPressed()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime > backPressTimeout) {
            backPressCount = 0
        }
        backPressCount++
        lastBackPressTime = currentTime
        when (backPressCount) {
            1 -> {
                // wait for subsequent presses
            }
            2 -> {
                Snackbar.make(
                    findViewById(R.id.content_frame),
                    R.string.press_back_once_more_to_exit,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            3 -> finish()
        }
    }
}

