package com.example.medialibrary.book.ui.list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.book.ui.utils.SharedUtils.Companion.filterSheetSetup
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentListBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.BookController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.BookFilter
import models.book.BookSetup
import repository.database.MediaLibraryDbHelper

class BookListFragment : BaseFragment<BookFragmentListBinding, BookListViewModel>(
    BookFragmentListBinding::inflate
) {

    private var currentFilter: BookFilter? = null
    private var bookController: BookController = BookController()
    private var setup: BookSetup = BookSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[BookListViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewBooks
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter
        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_book),
            binding.emptyStateContainer,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateBookFilter()
                filter.Search = query
                sortFilterViewModel.updateBookFilter(filter)
                loadData(true)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateBookFilter()
                filter.Search = newText
                sortFilterViewModel.updateBookFilter(filter)
                loadData(true)
                return true
            }
        })

        binding.filterBtn.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.sortBtn.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateBookFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateBookFilter(updatedFilter as BookFilter)
                loadData(true)
            }
        }

        binding.copyListBtn.setOnClickListener {
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

        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentBookFilter.collectLatest { filter ->
                currentFilter = filter ?: BookFilter()
                loadData(true)
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData(reload: Boolean = false) {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateBookFilter()
        }
        val items = bookController.GetListOfBooks(currentFilter)
        if (!reload) {
            setup = bookController.GetBookSetup()
            if (setup.Publishers.isEmpty() && setup.Tag.isEmpty()) {
                setup = bookController.GetBookSetup()
            }
        }

        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.active_filter_card),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            val emptyFilter = BookFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateBookFilter(emptyFilter)
            binding.searchView.setQuery("", false)
        }
    }

    private fun showFilterSheet(setup: BookSetup, filter: BookFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = BookBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: sortFilterViewModel.getOrCreateBookFilter()
        sheetBinding = filterSheetSetup(f, setup, sheetBinding)

        sheetBinding.applyFilterBtn.setOnClickListener {
            f.Read = sheetBinding.read.triStateButton.tag as? Boolean
            f.Reading = sheetBinding.reading.triStateButton.tag as? Boolean
            f.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as? Boolean
            f.Ongoing = sheetBinding.standaloneOrSeriesComplete.triStateButton.tag as? Boolean
            f.Collecting = sheetBinding.collecting.triStateButton.tag as? Boolean
            f.Collected = sheetBinding.collected.triStateButton.tag as? Boolean
            sortFilterViewModel.updateBookFilter(f)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            sortFilterViewModel.updateBookFilter(BookFilter())
            dialog.dismiss()
        }

        dialog.show()
    }
}
