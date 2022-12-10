package com.example.capstone.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.FragmentMapsBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.event_model.Greevents
import com.example.capstone.util.Constanta
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: ViewModelFactory
    private val mapsViewModel: MapsViewModel by viewModels { viewModelFactory }
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var mMap: GoogleMap


    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true


        mMap = googleMap

        val dummyLocation = LatLng(Constanta.DICODING_LATITUDE, Constanta.DICODING_LONGITUDE)
        googleMap.addMarker(
            MarkerOptions()
                .position(dummyLocation)
                .title("Marker in dummyLocation")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dummyLocation, 15f))

        getMyLocation()

        setViewModel()
        getStoryWithLocation(googleMap)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun setViewModel(){
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }
    private fun getStoryWithLocation(googleMap: GoogleMap) {
        mapsViewModel.getStoriesMap().observe(this) { result ->
            if (result != null) {
                when(result) {
                    is Result.Error -> {
                        Toast.makeText(activity, "Gagal Memuat Map", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        showMarker(result.data.data, googleMap)
                    }
                }
            }
        }
    }

    private fun showMarker(listStory: List<Greevents>, googleMap: GoogleMap) {
        listStory.forEach { event ->
            val latLng = LatLng(event.latitude, event.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(event.name)
                    .snippet(StringBuilder("created: ")
                        .append(event.createdAt.subSequence(11, 16).toString())
                        .toString()
                    )
            )
            boundsBuilder.include(latLng)
        }
    }
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            getMyLocation()
        }
    }
    private fun getMyLocation(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
        } else {
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }





}