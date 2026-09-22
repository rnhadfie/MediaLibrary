package com.example.medialibrary.book.ui.book_list

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

    private var currentFilter: BookFilter? = null;


    private var bookController: BookController = BookController();
    private var viewModel: BookListViewModel = BookListViewModel();

    private var setup: BookSetup = BookSetup();

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(BookListViewModel::class.java)
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
                binding.emptyStateContainer.visibility = View.VISIBLE
            } else {
                binding.recyclerviewBooks.visibility = View.VISIBLE
                binding.emptyStateContainer.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

        binding.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (currentFilter == null) currentFilter = BookFilter()
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = BookFilter()
                currentFilter?.Search = newText
                return true
            }
        })

        binding.buttonFilter?.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act).get(SharedRefreshViewModel::class.java)
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

        // Setup adapters
        val formats = setup.Format.filter { it.key != 0 }
        sheetBinding.dropdownSheetFormat.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, formats.values.toList())
        )

        val types = setup.Type.filter { it.key != 0 }
        sheetBinding.dropdownSheetTypeBook.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types.values.toList())
        )

        val genres = setup.Genre.filter { it.key != 0 }
        sheetBinding.dropdownSheetGenreBook.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genres.values.toList())
        )

        val publishers = setup.Publishers
        sheetBinding.dropdownSheetPublisher.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, publishers.map { it.Name })
        )

        val tags = setup.Tag
        sheetBinding.dropdownSheetTagBook.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags.map { it.Name })
        )

        // Populate existing filter
        filter?.let { f ->
            sheetBinding.switchSheetCompletedBook.isChecked = f.CompletedSeries ?: false
            sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
            sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false

            f.PrimaryFormat?.let { if (it != Enums.BookFormat.NoneSelected) sheetBinding.dropdownSheetFormat.setText(setup.Format[it.ordinal], false) }
            f.Type?.let { if (it != Enums.BookType.NoneSelected) sheetBinding.dropdownSheetTypeBook.setText(setup.Type[it.ordinal], false) }

            val currentPub = publishers.find { it.Id == f.Publisher }
            currentPub?.let { sheetBinding.dropdownSheetPublisher.setText(it.Name, false) }

            val currentTag = tags.find { it.Id == f.Tag }
            currentTag?.let { sheetBinding.dropdownSheetTagBook.setText(it.Name, false) }

            val currentGenre = setup.Genre[f.Genre]
            currentGenre?.let { sheetBinding.dropdownSheetGenreBook.setText(it, false) }
        }

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            val f = currentFilter ?: BookFilter()
            f.CompletedSeries = sheetBinding.switchSheetCompletedBook.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked

            val formatStr = sheetBinding.dropdownSheetFormat.text.toString()
            f.PrimaryFormat = setup.Format.entries.find { it.value == formatStr }?.key?.let { Enums.BookFormat.values()[it] }

            val typeStr = sheetBinding.dropdownSheetTypeBook.text.toString()
            f.Type = setup.Type.entries.find { it.value == typeStr }?.key?.let { Enums.BookType.values()[it] }

            val pubStr = sheetBinding.dropdownSheetPublisher.text.toString()
            f.Publisher = publishers.find { it.Name == pubStr }?.Id ?: 0

            val tagStr = sheetBinding.dropdownSheetTagBook.text.toString()
            f.Tag = tags.find { it.Name == tagStr }?.Id ?: 0

            val genreStr = sheetBinding.dropdownSheetGenreBook.text.toString()
            f.Genre = setup.Genre.entries.find { it.value == genreStr }?.key ?: 0

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
                holder.binding.mediaItemImageCover?.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaItemImageCover?.setImageResource(R.drawable.ic_gallery_black_24dp)
            }


            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, BookFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem?.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, BookFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem?.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                var bookController = BookController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Book Series?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, which ->
                        bookController.DeleteBook(item.Id)
                        (holder.itemView.context as? FragmentActivity)?.let { act ->
                            ViewModelProvider(act).get(SharedRefreshViewModel::class.java).incrementVersion()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}
