package com.example.medialibrary.book

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medialibrary.BaseActivity
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookActivityBinding

class BookActivity : BaseActivity<BookActivityBinding>() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BookActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarBook.toolbar)

        binding.appBarBook.fab?.setOnClickListener { _ ->



            val intent = Intent(this, BookFormActivity::class.java)
            formLauncher.launch(intent)
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_book) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_publisher, R.id.nav_tag -> {
                    binding.appBarBook.fab?.visibility = View.GONE
                }
                else -> {
                    binding.appBarBook.fab?.visibility = View.VISIBLE
                }
            }
        }


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_display, R.id.nav_list, R.id.nav_collecting, R.id.nav_publisher, R.id.nav_tag
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView?.let {
            setupDrawer(it)
        }

        binding.appBarBook.contentBook.bottomNavView?.setupWithNavController(navController)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_book)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}