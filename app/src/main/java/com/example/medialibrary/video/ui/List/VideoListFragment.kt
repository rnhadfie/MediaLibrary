package com.example.medialibrary.video.ui.List

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
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.video.Enums
import com.example.medialibrary.backend.models.video.VideoFilter
import com.example.medialibrary.backend.models.video.VideoSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentListBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class VideoListFragment : Fragment() {

    private var _binding: VideoFragmentListBinding? = null
    private val binding get() = _binding!!

    private var currentFilter: VideoFilter? = null;


    private var videoController: VideoController = VideoController()
    private var viewModel: VideoListModel = VideoListModel()

    private var setup: VideoSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(VideoListModel::class.java)
        _binding = VideoFragmentListBinding.inflate(inflater, container, false)
        val root = binding.root

        val recyclerView = binding.recyclerviewTransform
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        val controller = VideoController(dbHelper)

        fun loadData() {
            val items = controller.GetListOfVideos(currentFilter);
            viewModel.setItems(items ?: emptyList())
        }

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewTransform.visibility = View.GONE
                binding.emptyStateContainer?.visibility = View.VISIBLE
            } else {
                binding.recyclerviewTransform.visibility = View.VISIBLE
                binding.emptyStateContainer?.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

        binding.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilter?.Search = newText
                return true
            }
        })
        if(currentFilter == null) {currentFilter= VideoFilter()}
        binding.buttonFilter?.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter?: VideoFilter() ) }
        }

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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        val items = videoController?.GetListOfVideos(currentFilter) ?: emptyList()
        setup = videoController?.GetVideoSetup()
        viewModel?.setItems(items)
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        // Setup adapters
        val videoTags = setup.VideoTags.filter { it.key != 0 }
        sheetBinding.dropdownSheetVideoTag.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, videoTags.values.toList())
        )

        val types = setup.Types.filter { it.key != 0 }
        sheetBinding.dropdownSheetTypeVideo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types.values.toList())
        )

        val genres = setup.Genre.filter { it.key != 0 }
        sheetBinding.dropdownSheetGenreVideo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genres.values.toList())
        )

        val tags = setup.Tag
        sheetBinding.dropdownSheetGenreVideo.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags.map { it.Name })
        )

        // Populate existing filter
        sheetBinding.switchSheetCompletedVideo.isChecked = filter.CompletedSeries ?: false
        sheetBinding.switchSheetCollectedVideo.isChecked = filter.Collecting ?: false
        sheetBinding.switchSheetStartedVideo.isChecked = filter.AnyOwned ?: false

        filter.Type?.let { if (it != Enums.VideoType.NoneSelected) sheetBinding.dropdownSheetTypeVideo.setText(setup.Types[it.ordinal], false) }


        tags.find { it.Id == filter.Tag }?.let { sheetBinding.dropdownSheetTagVideo.setText(it.Name, false) }
        setup.Genre[filter.Genre]?.let { sheetBinding.dropdownSheetGenreVideo.setText(it, false) }

        sheetBinding.buttonSheetFitlerVideo.setOnClickListener {
            filter.CompletedSeries = sheetBinding.switchSheetCompletedVideo.isChecked
            filter.Collecting = sheetBinding.switchSheetCollectedVideo.isChecked
            filter.AnyOwned = sheetBinding.switchSheetStartedVideo.isChecked

            val typeStr = sheetBinding.dropdownSheetTypeVideo.text.toString()
            filter.Type = setup.Types.entries.find { it.value == typeStr }?.key?.let { Enums.VideoType.values()[it] }


            val tagStr = sheetBinding.dropdownSheetTagVideo.text.toString()
            filter.Tag = tags.find { it.Name == tagStr }?.Id ?: 0

            val genreStr = sheetBinding.dropdownSheetGenreVideo.text.toString()
            filter.Genre = setup.Genre.entries.find { it.value == genreStr }?.key ?: 0

            val videoTagStr = sheetBinding.dropdownSheetVideoTag.text.toString()
            filter.VideoTag = Enums.VideoTag.entries[tags.find { it.Name == videoTagStr }?.Id ?: 0]


            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearVideo.setOnClickListener {
            currentFilter = VideoFilter()
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
                holder.binding.mediaItemImageCover?.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaItemImageCover?.setImageResource(R.drawable.ic_gallery_black_24dp)
            }

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem?.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem?.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                var videoController = VideoController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Movie/TV Series?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, which ->
                        videoController.DeleteVideo(item.Id)
                        (holder.itemView.context as? FragmentActivity)?.let { act ->
                            ViewModelProvider(act).get(SharedRefreshViewModel::class.java).incrementVersion()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}