package com.example.medialibrary.book

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookActivityBinding

import com.example.medialibrary.BaseActivity

class BookActivity : BaseActivity<BookActivityBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            //reloadData()


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BookActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarBook.toolbar)

        binding.appBarBook.fab?.setOnClickListener { view ->



            val intent = Intent(this, BookFormActivity::class.java)
            formLauncher.launch(intent)
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_book) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.navView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_display, R.id.nav_list,  R.id.nav_collecting, R.id.nav_publisher, R.id.nav_tag
                ),
                binding.drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }

        binding.appBarBook.contentBook.bottomNavView?.let {
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_display, R.id.nav_list,  R.id.nav_collecting, R.id.nav_publisher, R.id.nav_tag
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            it.setupWithNavController(navController)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_book)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}