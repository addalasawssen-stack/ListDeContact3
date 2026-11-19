package com.example.listcontact3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.listcontact3.databinding.FragmentCarteContactBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarteContactBinding? = null
    private val binding get() = _binding!!

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var contactTitle: String = "Contact"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getDouble(ARG_LAT, 0.0)
            longitude = it.getDouble(ARG_LON, 0.0)
            contactTitle = it.getString(ARG_TITLE, "Contact")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarteContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMapTitle.text = contactTitle
        binding.btnMapBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.map_fragment, it)
                    .commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(contactTitle)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LAT = "arg_lat"
        private const val ARG_LON = "arg_lon"
        private const val ARG_TITLE = "arg_title"

        fun newInstance(lat: Double, lon: Double, title: String): CarteContactFragment {
            val fragment = CarteContactFragment()
            val args = Bundle().apply {
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LON, lon)
                putString(ARG_TITLE, title)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
