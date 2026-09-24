package com.example.medialibrary

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.repository.XmlRepository
import com.example.medialibrary.backend.repository.database.BaseRepository
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.backend.utils.XmlExportImport
import com.example.medialibrary.book.BookActivity
import com.example.medialibrary.home.MainActivity
import com.example.medialibrary.music.MusicActivity
import com.example.medialibrary.other.OtherActivity
import com.example.medialibrary.video.VideoActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

open class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected var binding: VB
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreate and onDestroy")
        set(value) {
            _binding = value
        }

    override fun onResume() {
        super.onResume()
        ViewModelProvider(this)[SharedRefreshViewModel::class.java].incrementVersion()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        val navView: NavigationView? = findViewById(R.id.nav_view)
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            menuInflater.inflate(R.menu.overflow, menu)
        }
        return result
    }

    protected fun setupDrawer(navView: NavigationView) {
        navView.setNavigationItemSelectedListener { item ->
            val handled = onOptionsItemSelected(item)
            if (handled) {
                val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)
                drawerLayout?.closeDrawers()
            }
            handled
        }
    }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/xml")) { uri ->
        val dbHelper = MediaLibraryDbHelper(this)
        uri?.let {
            exportData(it, contentResolver, binding, dbHelper)
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val dbHelper = MediaLibraryDbHelper(this)

        uri?.let {
            importData(it, contentResolver, binding, dbHelper)
            recreate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_import -> {
                importLauncher.launch("text/xml")
                return true
            }
            R.id.nav_export -> {
                exportLauncher.launch("library_export.xml")
                return true
            }
            R.id.nav_reset_app -> {
                showResetAppConfirmationDialog()
                return true
            }
            R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_book -> {
                val intent = Intent(this, BookActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_music -> {
                val intent = Intent(this, MusicActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_other -> {
                val intent = Intent(this, OtherActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_movie -> {
                val intent = Intent(this, VideoActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showResetAppConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_app_confirm_title)
            .setMessage(R.string.reset_app_confirm_message)
            .setPositiveButton(R.string.reset_app) { dialog, _ ->
                val dbHelper = MediaLibraryDbHelper(this)
                dbHelper.resetDatabase()
                BaseRepository.clearAllCaches()
                ViewModelProvider(this)[SharedRefreshViewModel::class.java].incrementVersion()
                Snackbar.make(binding.root, "App reset successfully", Snackbar.LENGTH_SHORT).show()
                recreate()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun <T : ViewBinding> exportData(uri: Uri, contentResolver: ContentResolver, binding: T, dbHelper: MediaLibraryDbHelper) {
        try {
            val xmlRepository = XmlRepository(dbHelper)
            val data = xmlRepository.GetAllData()
            val xmlString = XmlExportImport.exportToXml(data)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(xmlString.toByteArray())
            }
            Snackbar.make(binding.root, "Data exported successfully", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Export failed: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun <T : ViewBinding> importData(uri: Uri, contentResolver: ContentResolver, binding: T, dbHelper: MediaLibraryDbHelper) {
        try {
            val xmlString = contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }

            if (xmlString != null) {
                val data = XmlExportImport.importFromXml(xmlString)
                val xmlRepository = XmlRepository(dbHelper)
                xmlRepository.SaveAllData(data)
                Snackbar.make(binding.root, "Data imported successfully", Snackbar.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Import failed: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }
}
