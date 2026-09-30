package com.example.medialibrary.other

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medialibrary.BaseActivity
import com.example.medialibrary.R
import com.example.medialibrary.databinding.OtherActivityBinding

class OtherActivity : BaseActivity<OtherActivityBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OtherActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarOther.toolbar)

        binding.appBarOther.fab?.setOnClickListener { view ->
            val intent = Intent(this, OtherFormActivity::class.java)
            startActivity(intent)
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_other) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_list, R.id.nav_collecting
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView?.let {
            setupDrawer(it)
        }

        binding.appBarOther.contentOther.bottomNavView?.setupWithNavController(navController)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_other)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}