package com.example.medialibrary.video

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medialibrary.R
import com.example.medialibrary.databinding.VideoActivityBinding
import com.google.android.material.navigation.NavigationView
import com.example.medialibrary.BaseActivity


class VideoActivity : BaseActivity<VideoActivityBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VideoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarVideo.toolbar)

        binding.appBarVideo.fab?.setOnClickListener { view ->
            val intent = Intent(this, VideoFormActivity::class.java)
            startActivity(intent)
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_video) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_display, R.id.nav_list, R.id.nav_collecting
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView?.let {
            setupDrawer(it)
        }

        binding.appBarVideo.contentVideo.bottomNavView?.setupWithNavController(navController)
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_video)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}