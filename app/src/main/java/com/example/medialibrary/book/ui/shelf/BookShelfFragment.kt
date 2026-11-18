package com.example.medialibrary.book.ui.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.SharedUtils.Companion.filterSheetSetup
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.FragmentBookShelfBinding
import com.example.medialibrary.shared.FilterSummaryHelper
import com.example.medialibrary.shared.FragmentType
import com.example.medialibrary.shared.ShelfItemDecoration
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.BookController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.BookFilter
import models.book.BookSetup
import repository.database.MediaLibraryDbHelper

//class MusicShelfFragment : Fragment(R.layout.fragment_book_shelf) {
class BookShelfFragment : BaseFragment<FragmentBookShelfBinding, BookShelfViewModel>(
    FragmentBookShelfBinding::inflate
) {
    private lateinit var adapter: BookShelfAdapter

    private var currentFilter: BookFilter? = null
    private var bookController: BookController = BookController()
    private var setup: BookSetup = BookSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(inflater: LayoutInflater,
                               container: ViewGroup?,
                               savedInstanceState: Bundle?
    ): View {
        //super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BookShelfViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.rvBookshelf

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        adapter = BookShelfAdapter { book ->
            // Handle book click (e.g., open book details)
        }

        // Calculate how many books fit comfortably across the screen (~48dp per spine)
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val spanCount = (screenWidthDp / 48).toInt().coerceAtLeast(4)

        recyclerView.layoutManager = flexboxLayoutManager
        recyclerView.addItemDecoration(ShelfItemDecoration(shelfHeightPx = 28))
        recyclerView.adapter = adapter

        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

        observeSortFilterViewModel()
        /*
        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            -1,
            binding.emptyStateContainer,
            adapter
        )*/
/*
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
            val sortedBooks = books?.sortedBy { it.BookTitle }
            val bookList = buildString {
                sortedBooks?.forEach { book ->
                    appendLine(book.BookTitle)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Book List", bookList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }*/

        // Load and sort books by sortOrder
        adapter.submitList(viewModel.items.value)
        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentBookFilter.collectLatest { filter ->
                currentFilter = filter ?: BookFilter()
                loadData(true)
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { books ->
            val sortedBooks = books.orEmpty()// Replace with your sort property
            adapter.submitList(sortedBooks) {
                // Force Flexbox to recalculate row wrapping after DiffUtil commits
                binding.rvBookshelf.requestLayout()
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
        val items = bookController.GetBookSpines(currentFilter)
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
            //binding.searchView.setQuery("", false)
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