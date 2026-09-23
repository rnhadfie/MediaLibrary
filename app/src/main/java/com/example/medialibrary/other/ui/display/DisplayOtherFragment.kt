package com.example.medialibrary.other.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.Other
import com.example.medialibrary.backend.models.other.OtherFilter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherFragmentDisplayBinding
import com.github.mikephil.charting.utils.ColorTemplate

class DisplayOtherFragment : Fragment() {

    private var _binding: OtherFragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = OtherFilter()
    private var otherController: OtherController? = null
    private var viewModel: DisplayOtherViewModel? = null
    private var setup: MainSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DisplayOtherViewModel::class.java)
        _binding = OtherFragmentDisplayBinding.inflate(inflater, container, false)

        viewModel!!.text.observe(viewLifecycleOwner) {
            binding.textDisplayTitle?.text = it
        }

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        otherController = OtherController(dbHelper)

        loadData()

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
        val items = otherController?.GetOtherCollections(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.scrollViewOtherDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.scrollViewOtherDisplay.visibility = View.VISIBLE
        }

        setup = otherController?.GetSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }



    private fun setupCharts(items: List<Other>, setup: MainSetup) {
        val colors = ColorTemplate.MATERIAL_COLORS.toList()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}