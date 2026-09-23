package com.example.medialibrary.other.ui.list

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
import com.example.medialibrary.R
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.OtherFilter
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherFragmentListBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.medialibrary.databinding.OtherBottomSheetBinding
import com.example.medialibrary.other.OtherFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class OtherListFragment : Fragment() {

    private var _binding: OtherFragmentListBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = OtherFilter()

    private var controller: OtherController = OtherController()
    private var viewModel: OtherListViewModel = OtherListViewModel()

    private var setup: MainSetup = MainSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[OtherListViewModel::class.java]
        _binding = OtherFragmentListBinding.inflate(inflater, container, false)
        val root = binding.root

        val recyclerView = binding.recyclerviewOther
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = OtherController(dbHelper)

        fun loadData() {
            val items = controller.GetListOfOtherCollections(currentFilter)
            viewModel.setItems(items ?: emptyList())
        }

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewOther.visibility = View.GONE
                binding.emptyStateContainer.visibility = View.VISIBLE
            } else {
                binding.recyclerviewOther.visibility = View.VISIBLE
                binding.emptyStateContainer.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        val items = controller.GetListOfOtherCollections(currentFilter)
        setup = controller.GetSetup()
        viewModel.setItems(items ?: emptyList())
    }

    private fun showFilterSheet(setup: MainSetup, filter: OtherFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = OtherBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        // Setup adapters

        val tags = setup.Tag
        sheetBinding.dropdownSheetTagOther.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags.map { it.Name })
        )

        // Populate existing filter
        filter?.let { f ->
            sheetBinding.switchSheetCompletedOther.isChecked = f.CompletedSeries ?: false
            sheetBinding.switchSheetCollectedOther.isChecked = f.Collecting ?: false
            sheetBinding.switchSheetStartedOther.isChecked = f.AnyOwned ?: false

            val currentTag = tags.find { it.Id == f.Tag }
            currentTag?.let { sheetBinding.dropdownSheetTagOther.setText(it.Name, false) }


        }

        sheetBinding.buttonSheetFitlerOther.setOnClickListener {
            val f = currentFilter
            f.CompletedSeries = sheetBinding.switchSheetCompletedOther.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedOther.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedOther.isChecked


            val tagStr = sheetBinding.dropdownSheetTagOther.text.toString()
            f.Tag = tags.find { it.Name == tagStr }?.Id ?: 0


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
                holder.binding.mediaItemImageCover.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaItemImageCover.setImageResource(R.drawable.ic_gallery_black_24dp)
            }

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, OtherFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, OtherFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                val controller = OtherController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Book Series?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, _ ->
                        controller.DeleteOther(item.Id)
                        (holder.itemView.context as? FragmentActivity)?.let { act ->
                            ViewModelProvider(act)[SharedRefreshViewModel::class.java].incrementVersion()
                            act.finish()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}