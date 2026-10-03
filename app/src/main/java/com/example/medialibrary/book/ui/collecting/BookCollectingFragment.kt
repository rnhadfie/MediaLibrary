package com.example.medialibrary.book.ui.collecting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.*
import com.example.medialibrary.book.ui.utils.SharedUtils.Companion.filterSheetSetup
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentCollectingBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.ViewEmptyStateBinding
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.BookController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.*
import repository.database.MediaLibraryDbHelper

class BookCollectingFragment : BaseFragment<BookFragmentCollectingBinding, BookCollectingViewModel>(
    BookFragmentCollectingBinding::inflate
) {

    private var currentFilter: BookFilter? = null
    private var bookController: BookController = BookController()
    private var setup: BookSetup = BookSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    //region Components
    private var root: View? = null
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
    private var collectingRoot: ConstraintLayout? = null
    //endregion


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[BookCollectingViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Collecting)

         root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewBooks
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))

        //setups binding for components
        setComponentBindings()
        setupBindings(recyclerView, adapter)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)
        observeSortFilterViewModel()

        return root!!
    }

    private fun setupBindings(recyclerView: RecyclerView, adapter: BaseTransformAdapter)
    {
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
                loadData()
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
    }

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
        collectingRoot = sb.collecting.root
        applyFilterBtn = sb.applyFilterBtn
        clearActiveFilter = sb.clearActiveFilter

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
        //set up filter
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateBookFilter()
        }
        val effectiveFilter = BookFilter().apply {
            Search = currentFilter?.Search
            Tag = currentFilter?.Tag ?: 0
            Genre = currentFilter?.Genre ?: 0
            MediaType = currentFilter?.MediaType
            Collecting = true
            Ongoing = currentFilter?.Ongoing
            AnyOwned = currentFilter?.AnyOwned
            Collected = currentFilter?.Collected
            Read = currentFilter?.Read
            Reading = currentFilter?.Reading
            IncludedTags = currentFilter?.IncludedTags ?: ArrayList()
            ExcludedTags = currentFilter?.ExcludedTags ?: ArrayList()
            IncludedGenres = currentFilter?.IncludedGenres ?: ArrayList()
            ExcludedGenres = currentFilter?.ExcludedGenres ?: ArrayList()
            IncludedMediaTypes = currentFilter?.IncludedMediaTypes ?: ArrayList()
            ExcludedMediaTypes = currentFilter?.ExcludedMediaTypes ?: ArrayList()
            IncludedTypes = currentFilter?.IncludedTypes ?: ArrayList()
            ExcludedTypes = currentFilter?.ExcludedTypes ?: ArrayList()
            IncludedPublishers = currentFilter?.IncludedPublishers ?: ArrayList()
            ExcludedPublishers = currentFilter?.ExcludedPublishers ?: ArrayList()
            IncludedFormats = currentFilter?.IncludedFormats ?: ArrayList()
            ExcludedFormats = currentFilter?.ExcludedFormats ?: ArrayList()
        }

        //load list of items
        val items = bookController.GetListOfBooks(effectiveFilter)
        if(!reload) {
            if (setup.Publishers.isEmpty() && setup.Tag.isEmpty()) {
                setup = bookController.GetBookSetup()
            }
        }
        viewModel.setItems(items ?: emptyList())


        //setup filter summary
        FilterSummaryHelper.bindFilterSummary(
            root?.findViewById(R.id.active_filter_card),
            effectiveFilter,
            setup,
            FragmentType.Collecting
        ) {
            val emptyFilter = BookFilter().apply { Collecting = true }
            currentFilter = emptyFilter
            sortFilterViewModel.updateBookFilter(emptyFilter)
            searchView?.setQuery("", false)
        }
    }

    private fun showFilterSheet(setup: BookSetup, filter: BookFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = BookBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        setComponentSheetBindings(sheetBinding)

        val f = filter ?: sortFilterViewModel.getOrCreateBookFilter()
        f.Collecting = true

        sheetBinding = filterSheetSetup(f, setup, sheetBinding)
       collectingRoot?.visibility = View.GONE

        sheetBinding.applyFilterBtn.setOnClickListener {
            f.Read = readToggle?.tag as Boolean?
            f.Reading = readingToggle?.tag as Boolean?
            f.AnyOwned = ownedToggle?.tag as Boolean?
            f.Ongoing = ongoingToggle?.tag as Boolean?
            f.Collecting = true
            f.Collected = collectedToggle?.tag as Boolean?
            sortFilterViewModel.updateBookFilter(f)
            loadData(true)
            dialog.dismiss()
        }

        clearActiveFilter?.setOnClickListener {
            sortFilterViewModel.updateBookFilter(BookFilter().apply { Collecting = true })
            dialog.dismiss()
        }

        dialog.show()
    }
}