package com.example.medialibrary.music.ui.form

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.music.Enums.MusicGenre
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.models.shared.Enums as SharedEnums
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicFragmentFormBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.example.medialibrary.utils.SharedRefreshViewModel

class MusicFormFragment : BaseFormFragment<MusicFragmentFormBinding, MusicFormViewModel>(
    MusicFragmentFormBinding::inflate
) {

    companion object {
        fun newInstance(id: String = "-1", isEdit: Boolean = false) = MusicFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private var controller: MusicController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MusicFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MusicController(dbHelper)

        if (isEdit && itemId != "-1") {
            viewModel.loadCd(itemId, controller)
        }

        var setup = controller?.GetMusicSetup()
        if (setup == null)
            setup = MusicSetup()

        setupMusicGenreRadioGroup(setup)
        setupTagSelection(setup)
        setupCollectingPriorityDropdown(binding.collectingPriorityAutocomplete) { priority ->
            viewModel.updateCollectingPriority(priority)
        }
        setupInputListeners()

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("music")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("music")
        }

        binding.buttonSaveMusic.setOnClickListener {
            val error = viewModel.validate()
            if (error.isNotEmpty()) {
                if (error.containsKey("general")) {
                    Toast.makeText(requireContext(), error["general"], Toast.LENGTH_SHORT).show()
                }
                if (error.containsKey("title")) {
                    binding.editMusicTitleLabel.error = error["title"]
                    Toast.makeText(requireContext(), error["title"], Toast.LENGTH_SHORT).show()
                }
            } else {
                val saveObj = viewModel.getSaveObject()
                if(controller != null) {

                    val result = if (isEdit) {
                        controller!!.UpdateMusic(saveObj)
                    } else {
                        controller!!.AddMusic(saveObj)
                    }

                    if(result) {
                        ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                        Toast.makeText(
                            requireContext(),
                            if (isEdit) "Cd Updated" else "Cd Saved",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        activity?.finish()
                    }
                    else {
                        Toast.makeText(
                            requireContext(),
                            "Cd Failed to Save",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

        // Observe ViewModel
        viewModel.music.observe(viewLifecycleOwner) { music ->
            binding.editMusicTitle.setText(music.Title)
            binding.editMusicArtist.setText(music.Artist)
            binding.musicCollecting.isChecked = music.Collecting ?: false

            val currentPriority = music.CollectingPriority ?: SharedEnums.CollectingPriority.NoPriority
            binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

            // Update RadioGroup
            val musicGenreId = music.MusicGenre?.ordinal ?: 0
            if(musicGenreId != 0) {
                RadioGridUtils.setSelection(
                    binding.musicGenreRadio.dynamicTableLayout,
                    musicGenreId
                )
            }

            if (music.Cover != null && music.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(music.Cover, 0, music.Cover.size)
                binding.changeImage.imageBookCover.setImageBitmap(bitmap)
                binding.changeImage.imageBookCover.imageTintList = null
                binding.changeImage.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
                binding.changeImage.buttonClearCover.visibility = View.GONE
            }
        }

    }

    private fun setupMusicGenreRadioGroup(setup: MusicSetup) {

        binding.musicGenreRadio.radioButtonLabel.setText(R.string.music_genre)
        val tableLayout = binding.musicGenreRadio.dynamicTableLayout
        val musicGenre = setup.MusicGenre.filter { it.key != MusicGenre.NoneSelected.ordinal };
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = musicGenre,
            columnCount = 2
        ) { selectedId ->
            handleRadioSelectionChange(selectedId)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        // You can update a ViewModel, save state, or trigger network calls here
        val genre = MusicGenre.entries.find { it.ordinal == id } ?: return
        viewModel.updateMusicGenre(genre)
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        viewModel.updateCover(byteArray)
    }

    private fun setupTagSelection(setup: MusicSetup) {
        val tags = setup.Tags
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        binding.tagAutocomplete.autocomplete.setAdapter(adapter)
        binding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        binding.tagAutocomplete.autocomplete.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        binding.tagAutocomplete.autocomplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val existing = tags.find { it.Name == text }
                if (existing == null) {
                    viewModel.updateTagName(text)
                } else {
                    viewModel.updateTag(existing)
                }
            }
        })
    }

    private fun setupInputListeners() {
        binding.editMusicTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateTitle(s.toString())
                binding.editMusicTitleLabel.error = null
            }
        })

        binding.editMusicArtist.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateArtist(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.musicCollecting.setOnCheckedChangeListener {
                _, isChecked -> viewModel.toggleCollecting(isChecked)
        }
        binding.musicHasEnded.setOnCheckedChangeListener {
                _, isChecked -> viewModel.toggleCollectionComplete(isChecked)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


}