package com.example.medialibrary.book.ui.list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
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
import com.example.medialibrary.databinding.ViewEmptyStateBinding
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

    //region Components
    private var emptyStateContainer: ViewEmptyStateBinding? = null
    private var filterButton: ImageButton? = null
    private var sortButton: ImageButton? = null
    private var copyListButton: ImageButton? = null
    private var searchView: SearchView? = null
    //endregion

    //region sheet Components
    private var applyFilterBtn: Button? = null
    private var clearActiveFilter: Button? = null
    private var readToggle: Button? = null
    private var readingToggle: Button? = null
    private var ownedToggle: Button? = null
    private var ongoingToggle: Button? = null
    private var collectedToggle: Button? = null
    private var collectingRoot: Button? = null
    //endregion



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

        setComponentBindings()

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_book),
            emptyStateContainer!!,

            adapter
        )

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        filterButton?.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        sortButton?.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateBookFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateBookFilter(updatedFilter as BookFilter)
                loadData(true)
            }
        }

        copyListButton?.setOnClickListener {
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

    //region Setup

    private fun setComponentBindings()
    {
        emptyStateContainer = binding.emptyStateContainer
        filterButton = binding.filterBtn
        sortButton = binding.sortBtn
        copyListButton = binding.copyListBtn
        searchView = binding.searchView
    }

    private fun setComponentSheetBindings(sb: BookBottomSheetBinding)
    {
        readToggle = sb.read.triStateButton
        readingToggle = sb.reading.triStateButton
        ownedToggle = sb.anyItemsOwned.triStateButton
        ongoingToggle = sb.standaloneOrSeriesComplete.triStateButton
        collectedToggle = sb.collected.triStateButton
        collectingRoot = sb.collecting.triStateButton
        applyFilterBtn = sb.applyFilterBtn
        clearActiveFilter = sb.clearActiveFilter
    }

    //endregion

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
        setComponentSheetBindings(sheetBinding)

        val f = filter ?: sortFilterViewModel.getOrCreateBookFilter()

        sheetBinding = filterSheetSetup(f, setup, sheetBinding)

        sheetBinding.applyFilterBtn.setOnClickListener {
            f.Read = readToggle?.tag as Boolean?
            f.Reading = readingToggle?.tag as Boolean?
            f.AnyOwned = ownedToggle?.tag as Boolean?
            f.Ongoing = ongoingToggle?.tag as Boolean?
            f.Collecting = collectingRoot?.tag as Boolean?
            f.Collected = collectedToggle?.tag as Boolean?
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
