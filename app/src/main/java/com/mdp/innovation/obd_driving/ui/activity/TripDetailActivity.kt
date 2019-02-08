package com.mdp.innovation.obd_driving.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import android.support.v4.app.FragmentManager
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import kotlinx.android.synthetic.main.fragment_trip_detail.*
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat

class TripDetailActivity : BaseAppCompat(), OnMapReadyCallback {

    val TAG =  javaClass.simpleName

    private val sdfDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val sdfTime = SimpleDateFormat("HH:mm:ss")

    private var myMapView: MapView? = null
    private var myMap: GoogleMap? = null

    private var model : ItemMyTripsModel? = null
    private var modelDetail : TripDetailResponse? = null

    private val navigator by inject<Navigator>()

    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_trip_detail)

        val modelStr = intent.getStringExtra("TRIP")
        val modelDetailStr = intent.getStringExtra("DETAIL")
        model = gson.fromJson(modelStr, ItemMyTripsModel::class.java)
        modelDetail = gson.fromJson(modelDetailStr, TripDetailResponse::class.java)

        initUI()

        myMapView =  findViewById(R.id.map_dashBoard) as MapView
        myMapView!!.onCreate(savedInstanceState)
        myMapView!!.onResume()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }

        myMapView!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap!!.uiSettings.isMapToolbarEnabled = false

        fillTripDetailData()
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

        transparent_image.setOnTouchListener{ v: View, event: MotionEvent ->
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

    private fun addEvent(eventType: String, events: List<TripDetailResponse.EventItem>, icon: Int,
                         score: Float, container: LinearLayout
    ){

        var eventTypeContainer = LayoutInflater.from(applicationContext).inflate(R.layout.item_trip_detail_event_type, null)
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

            var eventTypeItem = LayoutInflater.from(applicationContext).inflate(R.layout.item_trip_detail_event, null)
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
        if(applicationContext == null || model == null) return

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
            Message.toastLong("Hubo un problema al cargar el viaje. Vuelva a intentarlo.", applicationContext)
            fragmentManager!!.popBackStack()
        }



    }

    private fun fillTripDetailData() {

        if(applicationContext == null || modelDetail == null) return

        var events = modelDetail!!.events
        var scores = modelDetail!!.scores
        if(events.acceleration.isEmpty() && events.braking.isEmpty() &&
            events.takingCurves.isEmpty() && events.speeding.isEmpty()){
            Message.toastLong("En este viaje no ocurrieron eventos.", applicationContext)
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

        /*myMap!!.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(latLon)
                .width(6f)
                .color(Color.BLACK)
        )*/

        val builder = LatLngBounds.Builder()
        for(item in latLon){
            builder.include(item)
        }
        val bounds = builder.build()
        //myMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))


    }

    private fun setEventAnimation(header: View, body: View){

        val line = header.findViewById<TextView>(R.id.tv_line_2)
        val arrow = header.findViewById<ImageView>(R.id.img_arrow)

        val slideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slidedown_events)
        val slideUp = AnimationUtils.loadAnimation(applicationContext, R.anim.slideup_events)

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

    override fun onDestroy() {
        if(myMap != null) {
            myMap!!.clear()
            myMap = null
        }
        if(myMapView != null){
            myMapView!!.onDestroy()
            myMapView = null
        }
        super.onDestroy()
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
