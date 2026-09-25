package com.example.medialibrary.other.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.OtherFilter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.databinding.OtherFragmentListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class OtherListFragment : BaseFragment<OtherFragmentListBinding, OtherListViewModel>(
    OtherFragmentListBinding::inflate
) {

    private var currentFilter = OtherFilter()
    private var controller: OtherController = OtherController()
    private var setup: MainSetup = MainSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[OtherListViewModel::class.java]
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
            binding.emptyStateContainer.root,
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
                if (newText.isNullOrEmpty()) {
                    loadData()
                }
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
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

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
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
        val tags = setup.Tag

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        sheetBinding.tagAutocomplete.autoCompleteLabel.setText(R.string.tag)
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.tagAutocomplete.autocomplete,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.standaloneOrSeriesComplete.root,
            R.string.completed_series,
            f.StandaloneOrSeriesIsComplete
        ) { f.StandaloneOrSeriesIsComplete = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collected.root,
            R.string.completely_collected,
            f.Collected
        ) { f.Collected = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collecting.root,
            R.string.collecting,
            f.Collecting
        ) { f.Collecting = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.anyItemsOwned.root,
            R.string.started_collecting,
            f.AnyOwned
        ) { f.AnyOwned = it }

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
