package com.rino.visualdestortion.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.rino.visualdestortion.R
import com.rino.visualdestortion.ui.services.ServicesFragment

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigation: MeowBottomNavigation
    lateinit var handler: Handler
    lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigation = findViewById(R.id.meowBottomNavigation)
        navigationSetup(navController, bottomNavigation)
        handler = Handler()
        runnable = Runnable {
            Toast.makeText(this, "the session ended please login again", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            navController.navigate(R.id.fingerPrintFragment)
        }
        startHandler()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        stopHandler()
        startHandler()
    }

    override fun onStop() {
        super.onStop()
        stopHandler()
    }

    private fun stopHandler() {
        handler.removeCallbacks(runnable)
    }

    private fun startHandler() {
        handler.postDelayed(runnable, 240000.toLong())
    }

    private fun navigationSetup(
        navController: NavController,
        bottomNavigation: MeowBottomNavigation
    ) {
        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.ic_baseline_home_24))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.ic_baseline_history_24))
        bottomNavigation.add(
            MeowBottomNavigation.Model(
                3,
                R.drawable.ic_baseline_qr_code_scanner_24
            )
        )
        bottomNavigation.add(MeowBottomNavigation.Model(4, R.drawable.ic_baseline_settings_24))
        bottomNavigation.show(1)
        bottomNavigation.setOnClickMenuListener { model: MeowBottomNavigation.Model? ->
            when (model?.id) {
                1 -> {
                    navController.popBackStack()
                    navController.navigate(R.id.servicesFragment)
                }
                2 -> {
                    navController.popBackStack()
                    navController.navigate(R.id.historyFragment)
                }
                3 -> {
                    navController.popBackStack()
                    navController.navigate(R.id.qrCodeScannerFragment)

                }
                4 -> {
                    navController.popBackStack()
                    navController.navigate(R.id.settingFragment)
                }
                else -> {

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val navHostFragment = supportFragmentManager.fragments.first()
                as? NavHostFragment

        navHostFragment?.let {
            val childFragments =
                navHostFragment.childFragmentManager.fragments

            childFragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)

                if (fragment is ServicesFragment) {
                    val page = supportFragmentManager
                        .findFragmentByTag(
                            "android:switcher:${R.id.servicesFragment}:${fragment.arguments}"
                        )

                    page?.let {
                        page.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }


}