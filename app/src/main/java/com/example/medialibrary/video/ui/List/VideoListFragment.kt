package com.example.medialibrary.video.ui.List

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.medialibrary.Utils.FilterOption
import com.example.medialibrary.Utils.MultiSelectFilterHelper
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
                binding.emptyStateContainer.root.visibility = View.VISIBLE
            } else {
                binding.recyclerviewTransform.visibility = View.VISIBLE
                binding.emptyStateContainer.root.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilter?.Search = newText
                if (newText.isNullOrEmpty()) {
                    loadData()
                }
                return true
            }
        })
        if(currentFilter == null) {currentFilter= VideoFilter()}
        binding.buttonFilter.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter?: VideoFilter() ) }
        }

        binding.videoItemList?.setOnClickListener {
            val videos = viewModel.items?.value

            val sortedVideos = videos?.sortedBy { it.Title }
            val videoList = buildString {
                sortedVideos?.forEach { video ->
                    appendLine(video.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Movie & TV Show List", videoList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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
        val items = videoController.GetListOfVideos(currentFilter) ?: emptyList()
        setup = videoController.GetVideoSetup()
        viewModel.setItems(items)
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter
        val tags = setup.Tag

        val videoTagOptions = setup.VideoTags.filter { it.key != 0 }.map { FilterOption(Enums.VideoTag.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetVideoTag,
            "Video Tags",
            videoTagOptions,
            f.IncludedVideoTags,
            f.ExcludedVideoTags
        )

        val typeOptions = setup.Types.filter { it.key != 0 }.map { FilterOption(Enums.VideoType.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTypeVideo,
            "Video Types",
            typeOptions,
            f.IncludedTypes,
            f.ExcludedTypes
        )

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTagVideo,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        val genreOptions = setup.Genre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetGenreVideo,
            "Genres",
            genreOptions,
            f.IncludedGenres,
            f.ExcludedGenres
        )

        sheetBinding.switchSheetCompletedVideo.isChecked = f.CompletedSeries ?: false
        sheetBinding.switchSheetCollectedVideo.isChecked = f.Collecting ?: false
        sheetBinding.switchSheetStartedVideo.isChecked = f.AnyOwned ?: false

        sheetBinding.buttonSheetFitlerVideo.setOnClickListener {
            f.CompletedSeries = sheetBinding.switchSheetCompletedVideo.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedVideo.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedVideo.isChecked

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
                holder.binding.mediaItemImageCover.setImageBitmap(bitmap)
            } else {
                holder.binding.mediaItemImageCover.setImageResource(R.drawable.ic_gallery_black_24dp)
            }

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
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