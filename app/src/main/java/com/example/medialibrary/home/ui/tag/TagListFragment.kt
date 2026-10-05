package com.example.medialibrary.home.ui.tag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.databinding.TagFragmentListBinding
import com.example.medialibrary.databinding.TagItemBinding
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import controllers.MainController
import models.shared.Tag
import repository.database.MediaLibraryDbHelper

class TagListFragment : BaseFragment<TagFragmentListBinding, TagViewModel>(
    TagFragmentListBinding::inflate
) {

    private lateinit var controller: MainController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[TagViewModel::class.java]
        setFragmentType(FragmentType.Tag)

        val root = super.onCreateView(inflater, container, savedInstanceState)
        controller = MainController(MediaLibraryDbHelper(requireContext()))

        val adapter = TagAdapter(
            onEdit = { showTagDialog(it) },
            onDelete = { deleteTag(it) }
        )
        binding.recyclerviewTag.adapter = adapter

        viewModel.tags.observe(viewLifecycleOwner) { tags ->
            if (tags.isNullOrEmpty()) {
                binding.recyclerviewTag.visibility = View.GONE
                binding.emptyStateContainer.root.visibility = View.VISIBLE
            } else {
                binding.recyclerviewTag.visibility = View.VISIBLE
                binding.emptyStateContainer.root.visibility = View.GONE
                adapter.submitList(tags) {
                    binding.recyclerviewTag.scrollToPosition(0)
                }
            }
        }

        binding.fabAddTag.setOnClickListener {
            showTagDialog()
        }

        loadTags()

        return root
    }

    override fun onRefreshData() {
        loadTags()
    }

    private fun loadTags() {
        viewModel.setTags(controller.GetTags() ?: emptyList())
    }

    //region Dialogs

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
                        controller.AddTag(Tag("", name))
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
                val result = controller.DeleteTag(tag.Id, false)
                if (result.ConflictDetected) {
                    showConflictDialog(requireContext(), tag)
                } else {
                    if (result.DeleteSuccessful) {
                        ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                        loadTags()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete Tag", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showConflictDialog(context: Context, tag: Tag) {
        AlertDialog.Builder(context)
            .setTitle("Conflict Detected")
            .setMessage("Tag ${tag.Name} is being used. Would you like to still delete it?")
            .setPositiveButton("Proceed") { dialog, _ ->
                val result = controller.DeleteTag(tag.Id, true)
                if (result.DeleteSuccessful) {
                    ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                    loadTags()
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Failed to delete Tag", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //endregion

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
