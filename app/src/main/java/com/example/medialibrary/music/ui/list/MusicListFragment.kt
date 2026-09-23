package com.example.medialibrary.music.ui.list

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
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentListBinding
import com.example.medialibrary.databinding.ItemTransformBinding
import com.example.medialibrary.music.MusicFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MusicListFragment : Fragment() {

    private var _binding: MusicFragmentListBinding? = null
    private val binding get() = _binding!!

    private var currentFilter: MusicFilter? = null


    private var musicController: MusicController = MusicController()
    private var viewModel: MusicListViewModel = MusicListViewModel()

    private var setup: MusicSetup = MusicSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]
        _binding = MusicFragmentListBinding.inflate(inflater, container, false)

        val recyclerView = binding.recyclerviewCds
        val adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        musicController = MusicController(dbHelper)

        loadData()

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                binding.recyclerviewCds.visibility = View.GONE
                binding.emptyStateContainer.visibility = View.VISIBLE
            } else {
                binding.recyclerviewCds.visibility = View.VISIBLE
                binding.emptyStateContainer.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (currentFilter == null) currentFilter = MusicFilter()
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = MusicFilter()
                currentFilter?.Search = newText
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

        return binding.root
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = MusicFilter()
        }
        val items = musicController.GetListOfBooks(currentFilter)
        setup = musicController.GetMusicSetup()
        viewModel.setItems(items ?: emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showFilterSheet(setup: MusicSetup, filter: MusicFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        // Setup adapters

        val genres = setup.MusicGenre.filter { it.key != 0 }
        sheetBinding.dropdownSheetGenreMusic.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genres.values.toList())
        )



        val tags = setup.Tags
        sheetBinding.dropdownSheetTagMusic.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags.map { it.Name })
        )

        // Populate existing filter
        filter?.let { f ->
            sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
            sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false



            val currentTag = tags.find { it.Id == f.Tag }
            currentTag?.let { sheetBinding.dropdownSheetTagMusic.setText(it.Name, false) }

            val currentGenre = setup.MusicGenre[f.Genre]
            currentGenre?.let { sheetBinding.dropdownSheetGenreMusic.setText(it, false) }
        }

        sheetBinding.buttonSheetFitlerMusic.setOnClickListener {
            val f = currentFilter ?: MusicFilter()
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked


            val tagStr = sheetBinding.dropdownSheetTagMusic.text.toString()
            f.Tag = tags.find { it.Name == tagStr }?.Id ?: 0

            val genreStr = sheetBinding.dropdownSheetGenreMusic.text.toString()
            f.Genre = setup.MusicGenre.entries.find { it.value == genreStr }?.key ?: 0

            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = MusicFilter()
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
                val intent = Intent(context, MusicFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemEditItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MusicFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.mediaItemDeleteItem.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                val bookController = MusicController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Book Series?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, _ ->
                        bookController.DeleteMusic(item.Id)
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