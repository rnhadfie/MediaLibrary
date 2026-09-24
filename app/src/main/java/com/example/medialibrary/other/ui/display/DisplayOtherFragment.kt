package com.example.medialibrary.other.ui.display

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.Utils.FragmentType
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.Other
import com.example.medialibrary.backend.models.other.OtherFilter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherFragmentDisplayBinding

class DisplayOtherFragment : BaseFragment<OtherFragmentDisplayBinding, DisplayOtherViewModel>(
    OtherFragmentDisplayBinding::inflate
) {

    private var currentFilter = OtherFilter()
    private var otherController: OtherController? = null
    private var setup: MainSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[DisplayOtherViewModel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        viewModel.text.observe(viewLifecycleOwner) {
            binding.textDisplayTitle?.text = it
        }

        val dbHelper = MediaLibraryDbHelper(requireContext())
        otherController = OtherController(dbHelper)

        loadData()

        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.scrollViewOtherDisplay,
            binding.emptyStateContainer.root
        )

        binding.otherItemList.setOnClickListener {
            val collection = viewModel.MediaItems.value
            val sortedCollections = collection?.sortedBy { it.Title }
            val collectionList = buildString {
                sortedCollections?.forEach { collection ->
                    appendLine(collection.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Collection List", collectionList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()

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
        }

        return root
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = otherController?.GetOtherCollections(currentFilter) ?: emptyList()
        setup = otherController?.GetSetup()
        viewModel.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }

    private fun setupCharts(items: List<Other>, setup: MainSetup) {
    }
}
