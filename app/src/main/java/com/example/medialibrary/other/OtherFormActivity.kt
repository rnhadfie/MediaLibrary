package com.example.medialibrary.other

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medialibrary.R
import com.example.medialibrary.other.ui.otherform.OtherFormFragment

class OtherFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.other_activity_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (savedInstanceState == null) {
            val otherId = intent.getStringExtra("EXTRA_ID") ?: "-1"
            val isEdit = intent.getBooleanExtra("EXTRA_IS_EDIT", false)

            supportActionBar?.title =  if(isEdit)  "Edit Collection" else "Add Collection"

            supportFragmentManager.beginTransaction()
                .replace(R.id.main, OtherFormFragment.newInstance(otherId, isEdit))
                .commitNow()
        }
    }
}