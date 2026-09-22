package com.example.medialibrary.music

import android.content.Intent
import android.os.Bundle
import android.view.Menu

import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medialibrary.R
import com.google.android.material.navigation.NavigationView

import com.example.medialibrary.BaseActivity

import com.example.medialibrary.databinding.MusicActivityBinding

class MusicActivity : BaseActivity<MusicActivityBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MusicActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMusic.toolbar)

        binding.appBarMusic.fab?.setOnClickListener { view ->
            val intent = Intent(this, MusicFormActivity::class.java)
            startActivity(intent)
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_music) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.navView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_display, R.id.nav_list, R.id.nav_collecting
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }

        binding.appBarMusic.contentMusic.bottomNavView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_display, R.id.nav_list, R.id.nav_collecting
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_music)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}