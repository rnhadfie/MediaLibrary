package com.example.medialibrary.Utils

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.BookFormActivity
import com.example.medialibrary.music.MusicFormActivity
import com.example.medialibrary.other.OtherFormActivity
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SaveEditDeleteUtils(private val item: DisplayMediaItem, private val holder: RecyclerView.ViewHolder) {

    fun showDeleteDialog() {
        val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
        val activity = holder.itemView.context as? FragmentActivity

        val (title, message, deleteAction) = when (item.MediaType) {
            Enums.MediaType.Book -> Triple(
                "Remove Book Series",
                "Are you sure you want to delete this Book Series?",
                { BookController(dbHelper).DeleteBook(item.Id) }
            )
            Enums.MediaType.Video -> Triple(
                "Remove Movie or TV Series",
                "Are you sure you want to delete this Movie or TV Series?",
                { VideoController(dbHelper).DeleteVideo(item.Id) }
            )
            Enums.MediaType.Music -> Triple(
                "Remove CD",
                "Are you sure you want to delete this CD?",
                { MusicController(dbHelper).DeleteMusic(item.Id) }
            )
            Enums.MediaType.Other -> Triple(
                "Remove Other Collection",
                "Are you sure you want to delete this collection?",
                { OtherController(dbHelper).DeleteOther(item.Id)}
            )
            else -> return
        }

        MaterialAlertDialogBuilder(holder.itemView.context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Confirm") { dialog, _ ->
                deleteAction()
                activity?.let { act ->
                    ViewModelProvider(act)[SharedRefreshViewModel::class.java].incrementVersion()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun addEditItem(isEdit: Boolean, id: Int?) {
        val context = holder.itemView.context
        val intent = when (item.MediaType) {
            Enums.MediaType.Book -> Intent(context, BookFormActivity::class.java)
            Enums.MediaType.Video -> Intent(context, VideoFormActivity::class.java)
            Enums.MediaType.Music -> Intent(context, MusicFormActivity::class.java)
            Enums.MediaType.Other -> Intent(context, OtherFormActivity::class.java)
            else -> null
        }

        intent?.let {
            it.putExtra("EXTRA_ID", id)
            it.putExtra("EXTRA_IS_EDIT", isEdit)
            context.startActivity(it)
        }
    }
}