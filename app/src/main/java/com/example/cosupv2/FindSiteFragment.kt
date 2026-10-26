package com.example.cosupv2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Map with all COSUP sites, search, nearest-site sorting and Drive / Walk / Transit directions. */
class FindSiteFragment : Fragment(R.layout.fragment_find_site) {

    private var mapView: MapView? = null
    private var sites: List<SiteItem> = emptyList()
    private var me: GeoPoint? = null
    private var meMarker: Marker? = null
    private var query = ""
    private val markers = HashMap<Int, Marker>()
    private val handler = Handler(Looper.getMainLooper())
    private var locListener: LocationListener? = null
    private var locManager: LocationManager? = null

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) locate()
            else setMsg("Location permission was denied. Allow it in your phone settings to see the nearest sites.")
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctx = requireContext()
        val cfg = Configuration.getInstance()
        cfg.load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        cfg.userAgentValue = ctx.packageName
        val base = File(ctx.cacheDir, "osmdroid")
        cfg.osmdroidBasePath = base
        cfg.osmdroidTileCache = File(base, "tiles")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mv = view.findViewById<MapView>(R.id.map)
        mapView = mv
        mv.setTileSource(TileSourceFactory.MAPNIK)
        mv.setMultiTouchControls(true)
        mv.controller.setZoom(10.0)
        mv.controller.setCenter(GeoPoint(-25.72, 28.2))

        // let the map move without the page scrolling
        val scroll = view.findViewById<ScrollView>(R.id.findScroll)
        mv.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> scroll.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scroll.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        view.findViewById<EditText>(R.id.siteSearch).doAfterTextChanged {
            query = it?.toString()?.trim()?.lowercase() ?: ""
            render()
        }
        view.findViewById<View>(R.id.btnLocate).setOnClickListener { onLocateClicked() }

        Repo.loadSites { list ->
            if (!isAdded) return@loadSites
            sites = list
            render()
        }
    }

    // ---------- list + map ----------

    private fun render() {
        val v = view ?: return
        val mv = mapView ?: return
        val list = v.findViewById<LinearLayout>(R.id.siteList)
        list.removeAllViews()

        var shown = sites.filter { s ->
            query.isEmpty() ||
                (s.name + " " + s.address + " " + s.services.joinToString(" ")).lowercase().contains(query)
        }
        val here = me
        if (here != null) shown = shown.sortedBy { km(here, it) }

        mv.overlays.clear()
        markers.clear()
        shown.forEach { s ->
            val m = Marker(mv)
            m.position = GeoPoint(s.lat, s.lng)
            m.title = s.name
            m.snippet = s.address
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mv.overlays.add(m)
            markers[s.id] = m
        }
        meMarker?.let { mv.overlays.add(it) }
        mv.invalidate()

        val inflater = LayoutInflater.from(requireContext())
        shown.forEachIndexed { i, s -> addCard(inflater, list, s, here != null && i == 0) }
    }

    private fun addCard(inflater: LayoutInflater, list: LinearLayout, s: SiteItem, nearest: Boolean) {
        val ctx = requireContext()
        val card = inflater.inflate(R.layout.item_site, list, false)
        card.findViewById<TextView>(R.id.siteName).text = s.name
        card.findViewById<View>(R.id.badgeMain).visibility = if (s.isMain) View.VISIBLE else View.GONE
        card.findViewById<View>(R.id.badgeNearest).visibility = if (nearest) View.VISIBLE else View.GONE
        card.findViewById<TextView>(R.id.siteAddress).text = s.address
        card.findViewById<TextView>(R.id.siteHours).text = s.hours
        card.findViewById<TextView>(R.id.siteServices).text = s.services.joinToString(" \u00B7 ")

        val here = me
        val d = if (here != null) km(here, s) else null
        val dist = card.findViewById<TextView>(R.id.siteDistance)
        if (d != null) dist.text = "About ${fmtDist(d)} away" else dist.visibility = View.GONE

        val modes = listOf(
            Triple(R.id.dirDrive, "driving", "Drive"),
            Triple(R.id.dirWalk, "walking", "Walk"),
            Triple(R.id.dirTransit, "transit", "Transit")
        )
        modes.forEach { (id, mode, label) ->
            val tv = card.findViewById<TextView>(id)
            tv.text = if (d != null) "$label\n${eta(d, mode)}" else label
            tv.setOnClickListener { ctx.openUrl(gmaps(s, mode)) }
        }

        val call = card.findViewById<TextView>(R.id.siteCall)
        if (s.phone.isBlank()) {
            call.visibility = View.GONE
        } else {
            call.text = "Call ${s.phone}"
            call.setOnClickListener { ctx.dial(s.phone.replace(" ", "")) }
        }

        card.setOnClickListener { focusOn(s) }
        list.addView(card)
    }

    private fun focusOn(s: SiteItem) {
        val mv = mapView ?: return
        mv.controller.animateTo(GeoPoint(s.lat, s.lng))
        mv.controller.setZoom(14.0)
        markers[s.id]?.showInfoWindow()
        view?.findViewById<ScrollView>(R.id.findScroll)?.smoothScrollTo(0, 0)
    }

    private fun gmaps(s: SiteItem, mode: String): String {
        val here = me
        val origin = if (here != null) "&origin=${here.latitude},${here.longitude}" else ""
        return "https://www.google.com/maps/dir/?api=1$origin&destination=${s.lat},${s.lng}&travelmode=$mode"
    }

    // ---------- my location ----------

    private fun setMsg(text: String) {
        view?.findViewById<TextView>(R.id.locateMsg)?.text = text
    }

    private fun onLocateClicked() {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            locate()
        } else {
            permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        if (!isAdded) return
        val ctx = requireContext()
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locManager = lm
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
            try { lm.isProviderEnabled(it) } catch (e: Exception) { false }
        }
        if (providers.isEmpty()) {
            setMsg("Turn on location in your phone settings, then try again.")
            return
        }
        setMsg("Finding your location...")

        var best: Location? = null
        for (p in providers) {
            try {
                val l = lm.getLastKnownLocation(p)
                if (l != null && (best == null || l.time > best.time)) best = l
            } catch (e: SecurityException) {
                // ignore
            }
        }
        val lastKnown = best
        if (lastKnown != null && System.currentTimeMillis() - lastKnown.time < 120_000) {
            onLocated(lastKnown)
            return
        }

        stopLocating()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                stopLocating()
                onLocated(location)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locListener = listener
        try {
            providers.forEach { lm.requestLocationUpdates(it, 0L, 0f, listener, Looper.getMainLooper()) }
        } catch (e: SecurityException) {
            setMsg("Location permission was denied. Allow it in your phone settings.")
            return
        }
        handler.postDelayed({
            stopLocating()
            if (lastKnown != null) onLocated(lastKnown)
            else setMsg("Could not get your location. Try again near a window or outside.")
        }, 15000)
    }

    private fun stopLocating() {
        locListener?.let {
            try { locManager?.removeUpdates(it) } catch (e: Exception) { /* ignore */ }
        }
        locListener = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun onLocated(location: Location) {
        if (!isAdded) return
        val mv = mapView ?: return
        val p = GeoPoint(location.latitude, location.longitude)
        me = p
        val marker = Marker(mv)
        marker.position = p
        marker.title = "You are here"
        marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_me)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        meMarker = marker
        render()

        val nearest = sites.minByOrNull { km(p, it) }
        if (nearest != null) {
            setMsg("Showing the nearest sites first. Nearest: ${nearest.name} (about ${fmtDist(km(p, nearest))}).")
            try {
                mv.zoomToBoundingBox(
                    BoundingBox.fromGeoPoints(listOf(p, GeoPoint(nearest.lat, nearest.lng))), true, 90
                )
            } catch (e: Exception) {
                // keep the current view
            }
        }
    }

    // ---------- lifecycle ----------

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        stopLocating()
        mapView?.onDetach()
        mapView = null
        super.onDestroyView()
    }

    // ---------- distance helpers ----------

    private fun km(a: GeoPoint, s: SiteItem): Double {
        val r = 6371.0
        val dLat = Math.toRadians(s.lat - a.latitude)
        val dLng = Math.toRadians(s.lng - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(s.lat)) * sin(dLng / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }

    private fun fmtDist(k: Double): String =
        if (k < 1) "${(k * 1000).roundToInt()} m" else String.format(Locale.US, "%.1f km", k)

    private fun eta(km: Double, mode: String): String {
        val speed = when (mode) {
            "driving" -> 40.0
            "walking" -> 5.0
            else -> 22.0
        }
        val mins = max(1, (km * 1.3 / speed * 60).roundToInt())
        return if (mins < 60) "$mins min" else {
            val h = mins / 60
            val m = mins % 60
            if (m == 0) "$h h" else "$h h $m min"
        }
    }
}
