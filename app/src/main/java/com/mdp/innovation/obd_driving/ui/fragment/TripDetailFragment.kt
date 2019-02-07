package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_trip_detail.*
import android.support.annotation.Nullable
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.*
import com.mdp.innovation.obd_driving.interactor.TripDetailInteractor
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.presenter.TripDetailPresenter
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.TripDetailView
import com.mdp.innovation.obd_driving.util.Message
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.res.ResourcesCompat
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.android.gms.maps.model.*
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD


class TripDetailFragment : BaseFragment(), TripDetailView, OnMapReadyCallback {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(model : ItemMyTripsModel, modelDetail: TripDetailResponse): TripDetailFragment{
            val fragment = TripDetailFragment()
            fragment.model = model
            fragment.modelDetail = modelDetail

            return fragment
        }
    }

    private val sdfDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val sdfTime = SimpleDateFormat("HH:mm:ss")

    private var myMapView: MapView? = null
    private var myMap: GoogleMap? = null

    private var model : ItemMyTripsModel? = null
    private var modelDetail : TripDetailResponse? = null

    private var latLngBounds: LatLngBounds? = null
    private var cameraUpdate: CameraUpdate? = null

    private var polygonOptions: PolygonOptions? = null
    private var polyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_trip_detail, container, false)

        //val mapFragment = fragmentManager?.findFragmentById(R.id.fr_map) as? SupportMapFragment
        //mapFragment?.getMapAsync(this)

        myMapView =  view.findViewById(R.id.map_dashBoard) as MapView
        myMapView!!.onCreate(savedInstanceState)
        myMapView!!.onResume()

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }

        myMapView!!.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap!!.uiSettings.isMapToolbarEnabled = false

        fillTripDetailData()

        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        /*myMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        myMap.addMarker(
            MarkerOptions()
                .position(LatLng(37.4233438, -122.0728817))
                .title("LinkedIn")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        myMap.addMarker(
            MarkerOptions()
                .position(LatLng(37.4629101, -122.2449094))
                .title("Facebook")
                .snippet("Facebook HQ: Menlo Park")
        )

        myMap.addMarker(
            MarkerOptions()
                .position(LatLng(37.3092293, -122.1136845))
                .title("Apple")
        )

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.4233438, -122.0728817), 10f))*/
    }

    private fun decodePolygon(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            //Log.d(TAG, (lat.toDouble() / 1E5).toString() + ", " + (lng.toDouble() / 1E5).toString())
            poly.add(p)
        }

        return poly
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*if (activity != null) {
            val mapFragment = activity!!.supportFragmentManager.findFragmentById(R.id.fr_map) as SupportMapFragment
            mapFragment?.getMapAsync(this)
        }*/

        initUI()
    }

    private fun initUI(){

        val toolbar = includeToolbar as Toolbar
        toolbar.title = "Detalle de Viaje"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        toolbar.setNavigationOnClickListener {
            System.out.println("backkkk")
            fragmentManager?.popBackStack()
        }

        fillTripData()
        //presenter.getTripDetail(model.tripId!!)

        transparent_image.setOnTouchListener{v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Disallow ScrollView to intercept touch events.
                    sv_trip_detail.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    false
                }
                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    sv_trip_detail.requestDisallowInterceptTouchEvent(false)
                     true
                }
                MotionEvent.ACTION_MOVE -> {
                    sv_trip_detail.requestDisallowInterceptTouchEvent(true)
                    false
                }
                else -> {
                    true
                }
            }
        }

    }

    /*override fun onGetTripDetailSuccess(response: TripDetailResponse) {

        if(context == null) return

        var events = response.events
        var scores = response.scores
        if(events.acceleration.isEmpty() && events.braking.isEmpty() &&
            events.takingCurves.isEmpty() && events.speeding.isEmpty()){
            Message.toastLong("En este viaje no ocurrieron eventos.", context)
            tv_events_title.text = "En este viaje no ocurrieron eventos"
            tv_events_title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }else{
            if(!events.acceleration.isEmpty())
                addEvent("Aceleración", events.acceleration, R.drawable.ic_acceleration_96,
                    scores.acceleration, ll_trip_detail_bottom)
            if(!events.braking.isEmpty())
                addEvent("Frenado", events.braking, R.drawable.ic_braking_96,
                    scores.braking, ll_trip_detail_bottom)
            if(!events.takingCurves.isEmpty())
                addEvent("Toma de Curvas", events.takingCurves, R.drawable.ic_taking_curves_96,
                    scores.takingCurves, ll_trip_detail_bottom)
            if(!events.speeding.isEmpty())
                addEvent("Exceso de Velocidad", events.speeding, R.drawable.ic_speeding_96,
                    scores.speeding, ll_trip_detail_bottom)
        }

        val latLon = decodePolygon(response.polygon)

        val iconHeight = 65
        val iconWidth = 65
        val bitmapDrawStart = ResourcesCompat.getDrawable(resources, R.drawable.ic_start_point_96, null) as BitmapDrawable
        val bitmapDrawEnd = ResourcesCompat.getDrawable(resources, R.drawable.ic_end_point_96, null) as BitmapDrawable
        val bitmapStart = bitmapDrawStart.bitmap
        val bitmapEnd = bitmapDrawEnd.bitmap
        val smallMarkerStart = Bitmap.createScaledBitmap(bitmapStart, iconWidth, iconHeight, false)
        val smallMarkerEnd = Bitmap.createScaledBitmap(bitmapEnd, iconWidth, iconHeight, false)

        myMap.addMarker(
            MarkerOptions().position(latLon.first()).title("Inicio").anchor(0.5f, 0.85f)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_point_32))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarkerStart))
        )
        myMap.addMarker(
            MarkerOptions().position(latLon.last()).title("Fin").anchor(0.5f, 0.85f)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_point_32))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarkerEnd))
        )

        val polyline1 = myMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(latLon)
                .width(6f)
                .color(Color.BLACK)
        )

        val builder = LatLngBounds.Builder()
        for(item in latLon){
            builder.include(item)
        }
        val bounds = builder.build()
        myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))


    }

    override fun onGetTripDetailError(message: String) {
        if(context == null)  return

        Message.toastLong("Ocurrió un error: "+message+". \n Vuelva a intentarlo en unos segundos.", context)
    }*/

    private fun addEvent(eventType: String, events: List<TripDetailResponse.EventItem>, icon: Int,
                         score: Float, container: LinearLayout){

        var eventTypeContainer = LayoutInflater.from(context).inflate(R.layout.item_trip_detail_event_type, null)
        var tvEventType = eventTypeContainer.findViewById<TextView>(R.id.tv_event_type)
        var tvEventScore = eventTypeContainer.findViewById<TextView>(R.id.tv_event_score)
        var llHeader = eventTypeContainer.findViewById<LinearLayout>(R.id.ll_header)
        var llEventsContainer = eventTypeContainer.findViewById<LinearLayout>(R.id.ll_events_container)
        tvEventType.text = eventType
        tvEventScore.text = score.toString()

        val iconHeight = 55
        val iconWidth = 55
        val bitmapDraw = ResourcesCompat.getDrawable(resources, icon, null) as BitmapDrawable
        val bitmap = bitmapDraw.bitmap
        val smallMarker = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, false)

        for (item in events){

            var eventTypeItem = LayoutInflater.from(context).inflate(R.layout.item_trip_detail_event, null)
            var tvSpeed = eventTypeItem.findViewById<TextView>(R.id.tv_speed)
            var tvDuration = eventTypeItem.findViewById<TextView>(R.id.tv_duration)
            var tvTime = eventTypeItem.findViewById<TextView>(R.id.tv_time)
            tvSpeed.text = item.speed
            tvDuration.text = item.duration
            var timeStartStr = " - "
            try{
                var date = sdfDateTime.parse(item.start)
                timeStartStr = sdfTime.format(date)
            }catch (ex: Exception){
                Log.d(TAG, ex.message)
            }
            tvTime.text = timeStartStr
            //tvTime.text = item.start

            llEventsContainer.addView(eventTypeItem)

            myMap!!.addMarker(
                MarkerOptions().position(LatLng(item.lat.toDouble(), item.lon.toDouble())).zIndex(10f)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
            )

        }

        container.addView(eventTypeContainer)

        setEventAnimation(llHeader, llEventsContainer)

    }

    private fun fillTripData(){
        if(context == null || model == null) return

        var scoreStr = " - "
        var timeStartStr = " - "
        var timeEndStr = " - "
        try{
            scoreStr = model!!.score.toString()
        }catch (ex: Exception){
            Log.d(TAG, ex.message)
        }
        try{
            timeStartStr = sdfDateTime.format(model!!.timeStart)
        }catch (ex: Exception){
            Log.d(TAG, ex.message)
        }
        try{
            timeEndStr = sdfDateTime.format(model!!.timeEnd)
        }catch (ex: Exception){
            Log.d(TAG, ex.message)
        }

        try{
            tv_duration_value.text = model!!.duration
            tv_distance_value.text = model!!.distance
            tv_score_value.text = scoreStr
            tv_start_value.text = model!!.timeStart
            tv_end_value.text = model!!.timeEnd
        }catch (ex: Exception){
            Message.toastLong("Hubo un problema al cargar el viaje. Vuelva a intentarlo.", context)
            fragmentManager!!.popBackStack()
        }



    }

    private fun fillTripDetailData() {

        if(context == null || modelDetail == null) return

        var events = modelDetail!!.events
        var scores = modelDetail!!.scores
        if(events.acceleration.isEmpty() && events.braking.isEmpty() &&
            events.takingCurves.isEmpty() && events.speeding.isEmpty()){
            Message.toastLong("En este viaje no ocurrieron eventos.", context)
            tv_events_title.text = "En este viaje no ocurrieron eventos"
            tv_events_title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }else{
            if(!events.acceleration.isEmpty())
                addEvent("Aceleración", events.acceleration, R.drawable.ic_acceleration_96,
                    scores.acceleration, ll_trip_detail_bottom)
            if(!events.braking.isEmpty())
                addEvent("Frenado", events.braking, R.drawable.ic_braking_96,
                    scores.braking, ll_trip_detail_bottom)
            if(!events.takingCurves.isEmpty())
                addEvent("Toma de Curvas", events.takingCurves, R.drawable.ic_taking_curves_96,
                    scores.takingCurves, ll_trip_detail_bottom)
            if(!events.speeding.isEmpty())
                addEvent("Exceso de Velocidad", events.speeding, R.drawable.ic_speeding_96,
                    scores.speeding, ll_trip_detail_bottom)
        }

        val latLon = decodePolygon(modelDetail!!.polygon)

        val iconHeight = 65
        val iconWidth = 65
        val bitmapDrawStart = ResourcesCompat.getDrawable(resources, R.drawable.ic_start_point_96, null) as BitmapDrawable
        val bitmapDrawEnd = ResourcesCompat.getDrawable(resources, R.drawable.ic_end_point_96, null) as BitmapDrawable
        val bitmapStart = bitmapDrawStart.bitmap
        val bitmapEnd = bitmapDrawEnd.bitmap
        val smallMarkerStart = Bitmap.createScaledBitmap(bitmapStart, iconWidth, iconHeight, false)
        val smallMarkerEnd = Bitmap.createScaledBitmap(bitmapEnd, iconWidth, iconHeight, false)

        myMap!!.addMarker(
            MarkerOptions().position(latLon.first()).title("Inicio").anchor(0.5f, 0.85f)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_point_32))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarkerStart))
        )
        myMap!!.addMarker(
            MarkerOptions().position(latLon.last()).title("Fin").anchor(0.5f, 0.85f)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_point_32))
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarkerEnd))
        )

        /*polyline = myMap!!.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(latLon)
                .width(6f)
                .color(Color.BLACK)
        )*/

        val po = PolylineOptions()
            .clickable(true)
            .width(6f)
            .color(Color.BLACK)

        for(item in latLon){
            po.add(item)
        }

        myMap!!.addPolyline( po )



        var north: LatLng = latLon.first()
        var south: LatLng = latLon.first()
        var east: LatLng = latLon.first()
        var west: LatLng = latLon.first()

        var first = true
        for(item in latLon){
            if(first){
                first = false
                continue
            }
            if(item.latitude > north.latitude) north = item
            if(item.latitude < south.latitude) south = item
            if(item.longitude > east.longitude) east = item
            if(item.longitude < west.longitude) west = item
        }


        val builder = LatLngBounds.Builder()
        /*for(item in latLon){
            builder.include(item)
        }*/

        //builder.include(latLon.first())
        //builder.include(latLon.last())

        builder.include(north)
        builder.include(south)
        builder.include(east)
        builder.include(west)

        //val bounds = builder.build()
        //myMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))

        latLngBounds = builder.build()
        cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 20)
        myMap!!.animateCamera(cameraUpdate)

    }

    private fun setEventAnimation(header: View, body: View){

        val line = header.findViewById<TextView>(R.id.tv_line_2)
        val arrow = header.findViewById<ImageView>(R.id.img_arrow)

        val slideDown = AnimationUtils.loadAnimation(context, R.anim.slidedown_events)
        val slideUp = AnimationUtils.loadAnimation(context, R.anim.slideup_events)

        body.visibility = View.GONE
        //body.startAnimation(slideDown)
        line.visibility = View.INVISIBLE
        arrow.setImageResource(android.R.drawable.arrow_down_float)



        var close = true
        header.setOnClickListener{
            if(close){
                body.visibility = View.VISIBLE
                //body.startAnimation(slideUp)

                /*body.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setListener(null)*/

                line.visibility = View.VISIBLE
                arrow.setImageResource(android.R.drawable.arrow_up_float)
            }else{
                body.visibility = View.GONE
                //body.startAnimation(slideDown)

                /*body.animate()
                    .alpha(0f)
                    .setDuration(1000)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            body.visibility = View.GONE
                        }
                    })*/

                line.visibility = View.INVISIBLE
                arrow.setImageResource(android.R.drawable.arrow_down_float)
            }
            close = !close
        }

    }

    override fun showLoading() {
        if(loading != null) loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if(loading != null) loading.visibility = View.GONE
    }

    override fun onStop() {
        if (ConnectOBD.isServiceBoundLocation) {
            // Desconectarse del servicio.
            // Esto le indica al servicio que esta actividad ya no está en primer plano
            // y que el servicio puede responder promoviéndose a sí mismo a un servicio en primer plano.
            ConnectOBD.doUnbindServiceLocation()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        if(myMap != null) {
            myMap!!.clear()
            myMap = null
        }
        if(myMapView != null){
            myMapView!!.onDestroy()
            myMapView = null
        }
        latLngBounds = null
        cameraUpdate = null
        polyline = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        myMapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        myMapView!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        myMapView!!.onLowMemory()
    }

}
