package com.example.medialibrary.book

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.form.BookFormFragment

class BookFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.book_activity_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (savedInstanceState == null) {
            val bookId = intent.getIntExtra("EXTRA_ID", -1)
            val isEdit = intent.getBooleanExtra("EXTRA_IS_EDIT", false)

            supportFragmentManager.beginTransaction()
                .replace(R.id.main, BookFormFragment.newInstance(bookId, isEdit))
                .commitNow()
        }

    }
}