package com.example.medialibrary.other.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.databinding.OtherFragmentListBinding
import com.example.medialibrary.other.ui.utils.SharedUtils
import com.example.medialibrary.other.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.OtherController
import models.other.OtherFilter
import models.shared.MainSetup
import repository.database.MediaLibraryDbHelper

class OtherListFragment : BaseFragment<OtherFragmentListBinding, OtherListViewModel>(
    OtherFragmentListBinding::inflate
) {

    private var currentFilter = OtherFilter()
    private var controller: OtherController = OtherController()
    private var setup: MainSetup = MainSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[OtherListViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewOther
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = OtherController(dbHelper)

        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_other),
            binding.emptyStateContainer,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentFilter.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilter.Search = newText
                loadData()
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.sortBtn.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateItemFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateItemFilter(updatedFilter as OtherFilter)
                loadData()
            }
        }

        return root
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = controller.GetListOfOtherCollections(currentFilter)
        setup = controller.GetSetup()
        viewModel.setItems(items ?: emptyList())
        currentFilter = sortFilterViewModel.getOrCreateItemFilter()

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            currentFilter = OtherFilter()
            binding.searchView.setQuery("", false)
            loadData()
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: OtherFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = OtherBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: OtherFilter()
        SharedUtils.filterSheetSetup(f, setup, sheetBinding)

        sheetBinding.buttonSheetFitlerOther.setOnClickListener {
            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearOther.setOnClickListener {
            currentFilter = OtherFilter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
