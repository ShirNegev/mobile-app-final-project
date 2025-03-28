package com.example.where_am_i_app

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ch.hsr.geohash.GeoHash
import com.example.where_am_i_app.model.UserAlertReport
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AlertsMapFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private val viewModel: UserAlertReportsViewModel by viewModels()
    private var currentPopup: UserAlertReportPopup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alerts_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.map)
        setupMap()
        checkAndShowLocation()

        viewModel.userAlertReports.observe(viewLifecycleOwner) { reports ->
            addMarkersForReports(reports)
        }
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkAndShowLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showUserLocation()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showUserLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        myLocationOverlay?.enableMyLocation()
        mapView.overlays.add(myLocationOverlay)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLocation = GeoPoint(it.latitude, it.longitude)
                mapView.controller.setCenter(userLocation)
            }
        }

        mapView.invalidate()
    }

    private fun addMarkersForReports(reports: List<UserAlertReport>?) {
        reports?.forEach { report ->
            report.geohashLocation.let { geoHash ->
                if (geoHash.isNotEmpty()) {
                    try {
                        val geoHashObject = GeoHash.fromGeohashString(geoHash)
                        val point = geoHashObject.originatingPoint
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(point.latitude, point.longitude)
                        marker.setOnMarkerClickListener { clickedMarker, _ ->
                            currentPopup?.dismiss()
                            currentPopup = UserAlertReportPopup(requireContext(), mapView, report, clickedMarker)
                            true
                        }
                        mapView.overlays.add(marker)
                    } catch (e: Exception) {
                        // Skip invalid GeoHash
                    }
                }
            }
        }
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        viewModel.refreshAllUserAlertReports()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        currentPopup?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myLocationOverlay?.disableMyLocation()
        mapView.overlays.clear()
        currentPopup?.dismiss()
    }
}