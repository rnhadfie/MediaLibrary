package com.example.medialibrary.home.ui.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.TagFragmentListBinding
import com.example.medialibrary.databinding.TagItemBinding

class TagListFragment: Fragment() {
    private var _binding: TagFragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TagViewModel
    private lateinit var controller: MainController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[TagViewModel::class.java]
        _binding = TagFragmentListBinding.inflate(inflater, container, false)

        controller = MainController(MediaLibraryDbHelper(requireContext()))

        val adapter = TagAdapter(
            onEdit = { showTagDialog(it) },
            onDelete = { deleteTag(it) }
        )
        binding.recyclerviewTag.adapter = adapter

        viewModel.tags.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.fabAddTag.setOnClickListener {
            showTagDialog()
        }

        loadTags()

        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act)[SharedRefreshViewModel::class.java]
            var lastVersion = refreshViewModel.refreshVersion
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (refreshViewModel.refreshVersion != lastVersion) {
                        lastVersion = refreshViewModel.refreshVersion
                        loadTags()
                    }
                }
            })
        }

        return binding.root
    }

    private fun loadTags() {
        viewModel.setTags(controller.GetTags() ?: emptyList())
    }

    private fun showTagDialog(tag: Tag? = null) {
        val editText = EditText(requireContext()).apply {
            setText(tag?.Name)
            hint = "Tag Name"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (tag == null) "Add Tag" else "Edit Tag")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    if (tag == null) {
                        controller.AddTag(Tag(0, name))
                    } else {
                        controller.UpdateTag(Tag(tag.Id, name))
                    }
                    ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                    loadTags()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTag(tag: Tag) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Tag")
            .setMessage("Are you sure you want to delete ${tag.Name}?")
            .setPositiveButton("Delete") { _, _ ->
                controller.DeleteTag(tag.Id)
                ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                loadTags()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TagAdapter(
        private val onEdit: (Tag) -> Unit,
        private val onDelete: (Tag) -> Unit
    ) : ListAdapter<Tag, TagViewHolder>(TagDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val binding = TagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TagViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            val tag = getItem(position)
            holder.binding.textTagName.text = tag.Name
            holder.binding.buttonEditTag.setOnClickListener { onEdit(tag) }
            holder.binding.buttonDeleteTag.setOnClickListener { onDelete(tag) }
        }
    }

    class TagViewHolder(val binding: TagItemBinding) : RecyclerView.ViewHolder(binding.root)

    class TagDiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem.Id == newItem.Id
        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem.Name == newItem.Name
    }
}