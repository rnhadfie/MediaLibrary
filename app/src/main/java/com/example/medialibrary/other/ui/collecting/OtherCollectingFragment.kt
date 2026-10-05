package com.example.medialibrary.other.ui.collecting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.databinding.OtherFragmentCollectingBinding
import com.example.medialibrary.other.ui.utils.SharedUtils
import com.example.medialibrary.other.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.OtherController
import models.other.OtherFilter
import models.shared.MainSetup
import repository.database.MediaLibraryDbHelper

class OtherCollectingFragment : BaseFragment<OtherFragmentCollectingBinding, OtherCollectingViewModel>(
    OtherFragmentCollectingBinding::inflate
) {

    private var otherController: OtherController = OtherController()
    private var currentFilter: OtherFilter? = null
    private var setup: MainSetup = MainSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[OtherCollectingViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(this)[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Collecting)

        val root: View = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewOther
        val adapter = BaseTransformAdapter()
        recyclerView?.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        otherController = OtherController(dbHelper)

        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_other),
            binding.emptyStateContainer,
            adapter
        )

        binding.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = OtherFilter()
                currentFilter?.Search = newText
                loadData()
                return true
            }
        })

        binding.buttonFilter?.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.sortBtn?.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateItemFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateItemFilter(updatedFilter as OtherFilter)
                loadData()
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

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateItemFilter()
        }
        currentFilter?.Collecting = true
        val items = otherController.GetListOfOtherCollections(currentFilter)
        setup = otherController.GetSetup()
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.Collecting
        ) {
            val emptyFilter = OtherFilter().apply { Collecting = true }
            currentFilter = emptyFilter
            sortFilterViewModel.updateItemFilter(emptyFilter)
            binding.searchView?.setQuery("", false)
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: OtherFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = OtherBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: OtherFilter()
        f.Collecting = true

        sheetBinding.collecting.root.visibility = View.GONE
        SharedUtils.filterSheetSetup(f, setup, sheetBinding)

        sheetBinding.buttonSheetFitlerOther.setOnClickListener {
            currentFilter = f
            f.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as? Boolean
            f.Collected = sheetBinding.collected.triStateButton.tag as? Boolean
            f.Collecting = sheetBinding.collecting.triStateButton.tag as? Boolean
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
