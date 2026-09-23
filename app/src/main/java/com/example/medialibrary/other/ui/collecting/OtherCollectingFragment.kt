package com.example.medialibrary.other.ui.collecting

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.*
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherFragmentCollectingBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.other.OtherFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class OtherCollectingFragment : Fragment() {

    private var _binding: OtherFragmentCollectingBinding? = null
    private val binding get() = _binding!!

    private var currentFilter: Filter? = null

    private var otherController: OtherController = OtherController()
    private var viewModel: OtherCollectingViewModel = OtherCollectingViewModel()

    private var setup: MainSetup = MainSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OtherFragmentCollectingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewBooks
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        otherController = OtherController(dbHelper)

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewBooks.visibility = View.GONE
                binding.emptyStateContainer.root.visibility = View.VISIBLE
            } else {
                binding.recyclerviewBooks.visibility = View.VISIBLE
                binding.emptyStateContainer.root.visibility = View.GONE
                adapter.submitList(itemList)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = Filter()
        }
        currentFilter?.Collecting = true
        val items = otherController.GetListOfOtherCollections(currentFilter)
        setup = otherController.GetSetup()
        viewModel.setItems(items ?: emptyList())
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