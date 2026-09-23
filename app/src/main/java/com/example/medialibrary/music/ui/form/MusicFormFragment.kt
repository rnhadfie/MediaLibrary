package com.example.medialibrary.music.ui.form

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.music.Enums.MusicGenre
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicFragmentFormBinding


import java.io.ByteArrayOutputStream
import kotlin.collections.forEach

class MusicFormFragment : Fragment() {

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(id: Int = -1, isEdit: Boolean = false) = MusicFormFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private val viewModel: MusicFormViewModel by viewModels()
    private var _binding: MusicFragmentFormBinding? = null
    private val binding get() = _binding!!

    private var pendingImageTarget: String? = null


    private var controller: MusicController? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                val byteArray = outputStream.toByteArray()

                if (pendingImageTarget == "music") {
                    viewModel.updateCover(byteArray)
                    binding.imageMusicCover.setImageBitmap(bitmap)
                    binding.imageMusicCover.imageTintList = null
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MusicController(dbHelper)

        _binding = MusicFragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getInt(ARG_ID) ?: -1
        val isEdit = arguments?.getBoolean(ARG_IS_EDIT) ?: false

        if (isEdit && id != -1) {
            viewModel.loadCd(id, controller)
        }

        var setup = controller?.GetMusicSetup()

        if(setup == null)
            setup = MusicSetup()

        setupMusicGenreRadioGroup()
        setupTagSelection(setup)
        setupInputListeners()

        binding.buttonChangeCover.setOnClickListener {
            pendingImageTarget = "book"
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.buttonSaveMusic.setOnClickListener {
            val error = viewModel.validate()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
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

            // Update RadioGroup
            for (i in 0 until binding.radioGroupMusicGenre.childCount) {
                val rb = binding.radioGroupMusicGenre.getChildAt(i) as RadioButton
                if (rb.tag == music.MusicGenre) {
                    rb.isChecked = true
                    break
                }
            }

            if (music.Cover != null) {
                val bitmap = BitmapFactory.decodeByteArray(music.Cover, 0, music.Cover.size)
                binding.imageMusicCover.setImageBitmap(bitmap)
                binding.imageMusicCover.imageTintList = null
            }
        }

    }

    private fun setupMusicGenreRadioGroup() {
        MusicGenre.entries.forEach { type ->
            if (type == MusicGenre.NoneSelected) return@forEach
            val rb = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = type.name
                tag = type
            }
            binding.radioGroupMusicGenre.addView(rb)
        }

        binding.radioGroupMusicGenre.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            viewModel.updateMusicGenre(rb.tag as MusicGenre)
        }
    }

    private fun setupTagSelection(setup: MusicSetup) {
        val tags = setup.Tags
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        binding.musicTagAutocomplete.setAdapter(adapter)

        binding.musicTagAutocomplete.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        binding.musicTagAutocomplete.addTextChangedListener(object : TextWatcher {
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
            override fun afterTextChanged(s: Editable?) { viewModel.updateTitle(s.toString()) }
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
        _binding = null
    }


}