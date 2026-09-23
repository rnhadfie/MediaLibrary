package com.example.medialibrary.home.ui.all_list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.medialibrary.Utils.FilterOption
import com.example.medialibrary.Utils.MultiSelectFilterHelper
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.BookFormActivity

import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentListBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.music.MusicFormActivity
import com.example.medialibrary.other.OtherFormActivity
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AllItemsViewFragment : Fragment() {

    private var _binding: MainFragmentListBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = Filter()

    private var controller: MainController = MainController()
    private var viewModel: AllItemsViewModelViewModel = AllItemsViewModelViewModel()

    private var setup: MainSetup = MainSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AllItemsViewModelViewModel::class.java]
        _binding = MainFragmentListBinding.inflate(inflater, container, false)
        val root = binding.root

        val recyclerView = binding.recyclerviewTransform
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        fun loadData() {
            val items = controller.GetAllItems(currentFilter)
            setup = controller.GetSetup()
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

        binding.allItemList?.setOnClickListener {
            val books = viewModel.items.value

            val sortedBooks = books?.sortedBy { it.Title }
            val bookList = buildString {
                sortedBooks?.forEach { book ->
                    appendLine(book.Title)
                }
            }

            //TO DO ORGANISE BY TYPE
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Book List", bookList)
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
        val items = controller.GetAllItems(currentFilter)
        setup = controller.GetSetup()
        viewModel.setItems(items ?: emptyList())
    }

    private fun showFilterSheet(setup: MainSetup, filter: Filter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MainBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: Filter()
        val tags = setup.Tag

        val typeOptions = setup.MediaType.filter { it.key != 0 }.map { FilterOption(Enums.MediaType.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTypeBook,
            "Media Types",
            typeOptions,
            f.IncludedMediaTypes,
            f.ExcludedMediaTypes
        )

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTagBook,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        val genreOptions = setup.Genre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetGenreBook,
            "Genres",
            genreOptions,
            f.IncludedGenres,
            f.ExcludedGenres
        )

        sheetBinding.switchSheetCompletedBook.isChecked = f.CompletedSeries ?: false
        sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
        sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            f.CompletedSeries = sheetBinding.switchSheetCompletedBook.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked

            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = Filter()
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

            holder.itemView.setOnClickListener {
                editItem(item, holder)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                editItem(item, holder)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
                deleteItem(item, holder)
            }


        }

        private fun editItem(item: DisplayMediaItem, holder: TransformViewHolder)
        {
            val context = holder.itemView.context
            val intent = when (item.MediaType) {
                Enums.MediaType.Book -> Intent(context, BookFormActivity::class.java)
                Enums.MediaType.Video -> Intent(context, VideoFormActivity::class.java)
                Enums.MediaType.Music -> Intent(context, MusicFormActivity::class.java)
                Enums.MediaType.Other -> Intent(context, OtherFormActivity::class.java)
                else -> null
            }
            intent?.let {
                it.putExtra("EXTRA_ID", item.Id)
                it.putExtra("EXTRA_IS_EDIT", true)
                context.startActivity(it)
            }
        }

        private fun deleteItem(item: DisplayMediaItem, holder: TransformViewHolder)
        {
            val dbHelper = MediaLibraryDbHelper(holder.itemView.context)

            when (item.MediaType) {
                Enums.MediaType.Book -> {
                    val bookController = BookController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Book Series")
                        .setMessage("Are you sure you want to delete this Book Series?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            bookController.DeleteBook(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Video -> {
                    val videoController = VideoController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Movie or TV Series")
                        .setMessage("Are you sure you want to delete this Movie or TV Series?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            videoController.DeleteVideo(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Music -> {
                    val musicController = MusicController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove CD")
                        .setMessage("Are you sure you want to delete this Cd")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            musicController.DeleteMusic(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }
                Enums.MediaType.Other -> {
                    val otherController = OtherController(dbHelper)
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Remove Other Collection")
                        .setMessage("Are you sure you want to delete this collection?")
                        .setCancelable(false) // Prevents closing by tapping outside
                        .setPositiveButton("Confirm") { dialog, _ ->
                            otherController.DeleteOther(item.Id)
                            (holder.itemView.context as? FragmentActivity)?.let { act ->
                                ViewModelProvider(act)[SharedRefreshViewModel::class.java]
                                    .incrementVersion()
                                act.finish()
                            }
                            dialog.dismiss()
                        }.show()
                }

                else -> {}
            }

        }
    }



    class TransformViewHolder(val binding: ItemTransformBinding) : RecyclerView.ViewHolder(binding.root)
}
