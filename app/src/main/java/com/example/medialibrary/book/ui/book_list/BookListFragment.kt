package com.example.medialibrary.book.ui.book_list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.example.medialibrary.Utils.FilterOption
import com.example.medialibrary.Utils.MultiSelectFilterHelper
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.models.book.Enums
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentListBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BookListFragment : Fragment() {

    private var _binding: BookFragmentListBinding? = null
    private val binding get() = _binding!!

    private var currentFilter: BookFilter? = null


    private var bookController: BookController = BookController()
    private var viewModel: BookListViewModel = BookListViewModel()

    private var setup: BookSetup = BookSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[BookListViewModel::class.java]
        _binding = BookFragmentListBinding.inflate(inflater, container, false)

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

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (currentFilter == null) currentFilter = BookFilter()
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = BookFilter()
                currentFilter?.Search = newText
                if (newText.isNullOrEmpty()) {
                    loadData()
                }
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.bookItemList?.setOnClickListener {
            val books = viewModel.items.value

            val sortedBooks = books?.sortedBy { it.Title }
            val bookList = buildString {
                sortedBooks?.forEach { book ->
                    appendLine(book.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Book List", bookList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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

        return binding.root
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = BookFilter()
        }
        val items = bookController.GetListOfBooks(currentFilter)
        setup = bookController.GetBookSetup()
        viewModel.setItems(items ?: emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showFilterSheet(setup: BookSetup, filter: BookFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BookBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: BookFilter()

        val publishers = setup.Publishers
        val tags = setup.Tag

        // Multi-select / Tri-state Setup
        val publisherOptions = publishers.map { FilterOption(it.Id, it.Name) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetPublisher,
            "Publishers",
            publisherOptions,
            f.IncludedPublishers,
            f.ExcludedPublishers
        )

        val typeOptions = setup.Type.filter { it.key != 0 }.map { FilterOption(Enums.BookType.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTypeBook,
            "Book Types",
            typeOptions,
            f.IncludedTypes,
            f.ExcludedTypes
        )

        val formatOptions = setup.Format.filter { it.key != 0 }.map { FilterOption(Enums.BookFormat.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetFormat,
            "Formats",
            formatOptions,
            f.IncludedFormats,
            f.ExcludedFormats
        )

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTagBook,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        val genreOptions = setup.Genre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetGenreBook,
            "Genres",
            genreOptions,
            f.IncludedGenres,
            f.ExcludedGenres
        )

        sheetBinding.switchSheetCompletedBook.isChecked = f.CompletedSeries ?: false
        sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
        sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            f.CompletedSeries = sheetBinding.switchSheetCompletedBook.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked

            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = BookFilter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
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
