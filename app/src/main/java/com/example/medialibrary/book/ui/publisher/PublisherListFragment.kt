package com.example.medialibrary.book.ui.publisher

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
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.models.book.Publisher
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.PublisherFragmentListBinding
import com.example.medialibrary.databinding.PublisherItemBinding

class PublisherListFragment : BaseFragment<PublisherFragmentListBinding, PublisherViewModel>(
    PublisherFragmentListBinding::inflate
) {

    private lateinit var controller: BookController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        setFragmentType(FragmentType.Publisher)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        controller = BookController(MediaLibraryDbHelper(requireContext()))

        val adapter = PublisherAdapter(
            onEdit = { showPublisherDialog(it) },
            onDelete = { deletePublisher(it) }
        )
        binding.recyclerviewPublishers.adapter = adapter

        viewModel.publishers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.fabAddPublisher.setOnClickListener {
            showPublisherDialog()
        }

        loadPublishers()

        return root
    }

    override fun onRefreshData() {
        loadPublishers()
    }

    private fun loadPublishers() {
        viewModel.setPublishers(controller.GetPublishers() ?: emptyList())
    }

    private fun showPublisherDialog(publisher: Publisher? = null) {
        val editText = EditText(requireContext()).apply {
            setText(publisher?.Name)
            hint = "Publisher Name"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (publisher == null) "Add Publisher" else "Edit Publisher")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    if (publisher == null) {
                        controller.AddPublisher(Publisher("", name))
                    } else {
                        controller.UpdatePublisher(Publisher(publisher.Id, name))
                    }
                    ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                    loadPublishers()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePublisher(publisher: Publisher) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Publisher")
            .setMessage("Are you sure you want to delete ${publisher.Name}?")
            .setPositiveButton("Delete") { _, _ ->
                controller.DeletePublisher(publisher.Id)
                ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                loadPublishers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class PublisherAdapter(
        private val onEdit: (Publisher) -> Unit,
        private val onDelete: (Publisher) -> Unit
    ) : ListAdapter<Publisher, PublisherViewHolder>(PublisherDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublisherViewHolder {
            val binding = PublisherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PublisherViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PublisherViewHolder, position: Int) {
            val publisher = getItem(position)
            holder.binding.textPublisherName.text = publisher.Name
            holder.binding.buttonEditPublisher.setOnClickListener { onEdit(publisher) }
            holder.binding.buttonDeletePublisher.setOnClickListener { onDelete(publisher) }
        }
    }

    class PublisherViewHolder(val binding: PublisherItemBinding) : RecyclerView.ViewHolder(binding.root)

    class PublisherDiffCallback : DiffUtil.ItemCallback<Publisher>() {
        override fun areItemsTheSame(oldItem: Publisher, newItem: Publisher): Boolean = oldItem.Id == newItem.Id
        override fun areContentsTheSame(oldItem: Publisher, newItem: Publisher): Boolean = oldItem.Name == newItem.Name
    }
}
