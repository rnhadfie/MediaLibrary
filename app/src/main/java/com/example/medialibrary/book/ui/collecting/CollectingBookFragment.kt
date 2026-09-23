package com.example.medialibrary.book.ui.collecting

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
import com.example.medialibrary.book.BookFormActivity
import com.example.medialibrary.R
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.BookFragmentCollectingBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CollectingBookFragment : Fragment() {

    private var _binding: BookFragmentCollectingBinding? = null
    private val binding get() = _binding!!

    private var currentFilter: BookFilter? = null


    private var bookController: BookController = BookController()
    private var viewModel: CollectingBookViewModel = CollectingBookViewModel()

    private var setup: BookSetup = BookSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BookFragmentCollectingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewBooks
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewBooks.visibility = View.GONE
                binding.emptyStateContainer.root.visibility = View.VISIBLE
            } else {
                binding.recyclerviewBooks.visibility = View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = BookFilter()
        }
        currentFilter?.Collecting = true
        val items = bookController.GetListOfBooks(currentFilter)
        setup = bookController.GetBookSetup()
        viewModel.setItems(items ?: emptyList())
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
                val context = holder.itemView.context
                val intent = Intent(context, BookFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, BookFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                val bookController = BookController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Book Series?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, _ ->
                        bookController.DeleteBook(item.Id)
                        (holder.itemView.context as? FragmentActivity)?.let { act ->
                            ViewModelProvider(act)[SharedRefreshViewModel::class.java].incrementVersion()
                            act.finish()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}
