package com.example.medialibrary.home.ui.collecting

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.R
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.BookFormActivity
import com.example.medialibrary.databinding.MainFragmentCollectingBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.music.MusicFormActivity
import com.example.medialibrary.other.OtherFormActivity
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CollectingFragment : Fragment() {

    private var _binding: MainFragmentCollectingBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = Filter()

    private lateinit var controller: MainController
    private lateinit var viewModel: CollectingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[CollectingViewModel::class.java]
        _binding = MainFragmentCollectingBinding.inflate(inflater, container, false)
        val root = binding.root

        val recyclerView = binding.recyclerviewMainCollecting
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewMainCollecting.visibility = View.GONE
                binding.emptyStateContainer.root.visibility = View.VISIBLE
            } else {
                binding.recyclerviewMainCollecting.visibility = View.VISIBLE
                binding.emptyStateContainer.root.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act)[SharedRefreshViewModel::class.java]
            var lastVersion = refreshViewModel.refreshVersion
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (refreshViewModel.refreshVersion != lastVersion) {
                        lastVersion = refreshViewModel.refreshVersion
                        loadData()
                    }
                }
            })
        }

        return root
    }

    private fun loadData() {
        currentFilter.Collecting = true
        val items = controller.GetAllItems(currentFilter)
        viewModel.setItems(items ?: emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TransformAdapter :
        ListAdapter<DisplayMediaItem, TransformViewHolder>(object : DiffUtil.ItemCallback<DisplayMediaItem>() {
            override fun areItemsTheSame(oldItem: DisplayMediaItem, newItem: DisplayMediaItem): Boolean = oldItem.Id == newItem.Id
            override fun areContentsTheSame(oldItem: DisplayMediaItem, newItem: DisplayMediaItem): Boolean =
                oldItem.Title == newItem.Title && (oldItem.Cover?.contentEquals(newItem.Cover ?: byteArrayOf()) ?: (newItem.Cover == null))
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {

            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TransformViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.item = item
            holder.binding.executePendingBindings()

            if (item.Cover != null && item.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(item.Cover, 0, item.Cover.size)
                holder.binding.mediaItemImageCover.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaItemImageCover.setImageResource(R.drawable.ic_gallery_black_24dp)
            }

            holder.itemView.setOnClickListener {
                editItem(item, holder)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                editItem(item, holder)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
                deleteItem(item, holder)
            }


        }

        private fun editItem(item: DisplayMediaItem, holder: TransformViewHolder)
        {
            val context = holder.itemView.context
            val intent = when (item.MediaType) {
                Enums.MediaType.Book -> Intent(context, BookFormActivity::class.java)
                Enums.MediaType.Video -> Intent(context, VideoFormActivity::class.java)
                Enums.MediaType.Music -> Intent(context, MusicFormActivity::class.java)
                Enums.MediaType.Other -> Intent(context, OtherFormActivity::class.java)
                else -> null
            }
            intent?.let {
                it.putExtra("EXTRA_ID", item.Id)
                it.putExtra("EXTRA_IS_EDIT", true)
                context.startActivity(it)
            }
        }

        private fun deleteItem(item: DisplayMediaItem, holder: TransformViewHolder)
        {
            val dbHelper = MediaLibraryDbHelper(holder.itemView.context)

            when (item.MediaType) {
                Enums.MediaType.Book -> {
                    val bookController = BookController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Book Series")
                        .setMessage("Are you sure you want to delete this Book Series?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            bookController.DeleteBook(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Video -> {
                    val videoController = VideoController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Movie or TV Series")
                        .setMessage("Are you sure you want to delete this Movie or TV Series?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            videoController.DeleteVideo(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Music -> {
                    val musicController = MusicController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove CD")
                        .setMessage("Are you sure you want to delete this Cd")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            musicController.DeleteMusic(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Other -> {
                    val otherController = OtherController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Other Collection")
                        .setMessage("Are you sure you want to delete this collection?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            otherController.DeleteOther(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }

                else -> {}
            }

        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}