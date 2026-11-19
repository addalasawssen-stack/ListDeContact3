package com.example.listcontact3

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import coil.load
import com.example.listcontact3.databinding.FragmentDetailContactBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DetailContactFragment : Fragment() {

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()

    private var contactId: String? = null
    private var currentContact: Contact? = null
    private var currentPhotoUri: String? = null

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentPhotoUri = it.toString()
                binding.ivDetailPhoto.load(it) {
                    placeholder(android.R.drawable.ic_menu_camera)
                    error(android.R.drawable.ic_menu_camera)
                }
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                val savedUri = saveBitmapToInternalStorage(it)
                if (savedUri != null) {
                    currentPhotoUri = savedUri.toString()
                    binding.ivDetailPhoto.setImageBitmap(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactId = it.getString(ARG_CONTACT_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadContactData()
        setupListeners()
    }

    private fun loadContactData() {
        val id = contactId
        if (id != null) {
            currentContact = viewModel.getContactById(id)
            currentContact?.let { contact ->
                binding.tvDetailTitle.text = getString(R.string.title_contact_detail)
                binding.etNom.setText(contact.nom)
                binding.etPrenom.setText(contact.prenom)
                binding.etTelephone.setText(contact.telephone)
                binding.etEmail.setText(contact.email)
                binding.etLatitude.setText(contact.latitude?.toString() ?: "")
                binding.etLongitude.setText(contact.longitude?.toString() ?: "")
                currentPhotoUri = contact.photoUri

                if (!contact.photoUri.isNullOrBlank()) {
                    binding.ivDetailPhoto.load(Uri.parse(contact.photoUri)) {
                        placeholder(android.R.drawable.ic_menu_camera)
                        error(android.R.drawable.ic_menu_camera)
                    }
                } else {
                    binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_camera)
                }
            }
        } else {
            binding.tvDetailTitle.text = getString(R.string.title_contact_new)
            binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    private fun setupListeners() {
        binding.btnPickPhoto.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        binding.btnTakePhoto.setOnClickListener {
            takePhotoLauncher.launch(null)
        }

        binding.btnCall.setOnClickListener {
            makePhoneCall()
        }

        binding.btnEmail.setOnClickListener {
            sendEmail()
        }

        binding.btnViewMap.setOnClickListener {
            openMap()
        }

        binding.btnSave.setOnClickListener {
            saveContact()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun makePhoneCall() {
        val phone = binding.etTelephone.text.toString().trim()
        if (phone.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                "Veuillez remplir le numéro de téléphone",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendEmail() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                "Veuillez remplir l'adresse e-mail",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openMap() {
        val latStr = binding.etLatitude.text.toString().trim()
        val lonStr = binding.etLongitude.text.toString().trim()

        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            val nom = binding.etNom.text.toString().trim()
            val prenom = binding.etPrenom.text.toString().trim()
            val title = "$prenom $nom".trim().ifEmpty { "Contact" }

            parentFragmentManager.commit {
                replace(
                    R.id.fragment_container,
                    CarteContactFragment.newInstance(lat, lon, title)
                )
                addToBackStack(null)
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_invalid_coordinates),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveContact() {
        val nom = binding.etNom.text.toString().trim()
        val prenom = binding.etPrenom.text.toString().trim()
        val telephone = binding.etTelephone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty() || email.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_required_fields),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val latStr = binding.etLatitude.text.toString().trim()
        val lonStr = binding.etLongitude.text.toString().trim()

        val lat = if (latStr.isNotEmpty()) latStr.toDoubleOrNull() else null
        val lon = if (lonStr.isNotEmpty()) lonStr.toDoubleOrNull() else null

        if (latStr.isNotEmpty() && (lat == null || lat !in -90.0..90.0)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_invalid_coordinates),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (lonStr.isNotEmpty() && (lon == null || lon !in -180.0..180.0)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_invalid_coordinates),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val id = contactId ?: UUID.randomUUID().toString()
        val updatedContact = Contact(
            id = id,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = currentPhotoUri,
            latitude = lat,
            longitude = lon
        )

        viewModel.saveContact(updatedContact)
        parentFragmentManager.popBackStack()
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri? {
        return try {
            val file = File(
                requireContext().filesDir,
                "contact_photo_${UUID.randomUUID()}.jpg"
            )
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTACT_ID = "arg_contact_id"

        fun newInstance(contactId: String?): DetailContactFragment {
            val fragment = DetailContactFragment()
            val args = Bundle().apply {
                putString(ARG_CONTACT_ID, contactId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
