package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_my_trips.*
import android.support.annotation.Nullable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.mdp.innovation.obd_driving.interactor.MyTripsInteractor
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.presenter.MyTripsPresenter
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.MyTripsView
import com.mdp.innovation.obd_driving.ui.adapter.ItemMyTripsAdapter
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Connection
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving.util.Preferences
import org.koin.android.ext.android.inject
import java.util.*


class MyTripsFragment : BaseFragment(), MyTripsView {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyTripsFragment{
            return MyTripsFragment()
        }
    }

    private val navigator by inject<Navigator>()

    private val presenter = MyTripsPresenter(this, MyTripsInteractor())
    private val preferences by inject<Preferences>()

    private val elementsByPage = 15
    private var currentPage = 1
    private var elementsFetched = 0
    private var isFirstLoad = true

    lateinit var adapter : ItemMyTripsAdapter


    private var mRecyclerView: RecyclerView? = null
    var itemMyTripsModelList: ArrayList<ItemMyTripsModel?> = ArrayList()
    //var handler = Handler()

    private val TRIP_LIST = 1
    private val TRIP_DETAIL = 2
    private var itemSelected :ItemMyTripsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_my_trips, container, false)

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Mis Viajes"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        drawerConfig(activity, toolbar)

        itemMyTripsModelList.clear()

        mRecyclerView = view.findViewById(R.id.rv_my_trips)
        var mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = mLayoutManager
        //var adapter = ItemMyTripsAdapter(itemMyTripsModelList, context)
        adapter = ItemMyTripsAdapter(context, itemMyTripsModelList, mRecyclerView!!)
        mRecyclerView!!.adapter = adapter
        /*adapter.setOnLoadMoreListener(object : ItemMyTripsAdapter.OnLoadMoreListener{
            override fun onLoadMore() {
                itemMyTripsModelList.add(null)

                mRecyclerView!!.postDelayed({
                    adapter.notifyItemInserted(itemMyTripsModelList.size - 1)

                    val vin = preferences.getDataUser(context)!!.vin*/

                    //presenter.getMyTrips(vin!!, currentPage, elementsByPage, false)

                    /*handler.postDelayed({

                        itemMyTripsModelList.removeAt(itemMyTripsModelList.size - 1)
                        adapter.notifyItemRemoved(itemMyTripsModelList.size)
                        for (i in 0..9) {
                            itemMyTripsModelList.add(option1)
                            adapter.notifyItemInserted(itemMyTripsModelList.size)
                        }
                        adapter.setLoaded()
                        adapter.stopScroll()
                    }, 2000)*/

                /*},100)

                println("load")
            }
        })*/

        adapter.setOnClickItemListener(object : ItemMyTripsAdapter.OnClickItemListener{
            override fun onClick(item: ItemMyTripsModel) {
                Log.d(TAG, "Mi score es: " + item.score)
                itemSelected = item
                showLoading()
                validateInternet(TRIP_DETAIL)
                //navigator.navigateToTripDetail(fragmentManager!!, R.id.content, item)
            }
        })

        return view
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        val vin = preferences.getDataUser(context)!!.vin


        validateInternet(TRIP_LIST)
        //val userId = preferences.getDataUser(context)!!.userId
        //presenter.getMyTrips(userId!!, currentPage, elementsByPage, true)

    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {
        if (context == null) return

        elementsFetched += response.trips.size
        currentPage += 1

        if(isFirstLoad){
            if(response.trips.isEmpty()){
                ll_my_trips_empty.visibility = View.VISIBLE
                return
            }else{
                if(response.trips.size == response.total){
                    adapter.stopScroll()
                }
            }
            isFirstLoad = false
        }else{
            //itemMyTripsModelList.removeAt(itemMyTripsModelList.size - 1)
            //adapter.notifyItemRemoved(itemMyTripsModelList.size)

            if(response.total == elementsFetched){
                adapter.stopScroll()
            }else{

            }
        }

        addToAdapter(response.trips)
        adapter.setLoaded()

    }

    private fun addToAdapter(list: List<MyTripsResponse.Trip>){
        for (item in list) {
            val model = ItemMyTripsModel()
            model.timeStart = item.timeStart
            model.duration = item.duration
            model.score = item.score
            model.distance =  item.distance
            model.timeEnd =  item.timeEnd
            model.tripId = item.tripId

            itemMyTripsModelList.add(model)
            adapter.notifyItemInserted(itemMyTripsModelList.size)
        }
    }

    override fun onGetMyTripsError(message: String) {
        if (context == null) return
        Message.toastLong("Ocurrió un error: "+message+". \n Vuelva a intentarlo en unos segundos.", context)

        if(!isFirstLoad){
            /*itemMyTripsModelList.removeAt(itemMyTripsModelList.size - 1)
            adapter.notifyItemRemoved(itemMyTripsModelList.size)
            adapter.stopScroll()*/
        }
    }

    override fun showLoading() {
        if(loading != null) loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        if(loading != null) loading.visibility = View.GONE
    }

    override fun onGetTripDetailSuccess(response: TripDetailResponse) {
        if (context == null) return
        navigator.navigateToTripDetail(fragmentManager!!, R.id.content, itemSelected!!, response)
        //navigator.navigateToTripDetail2(activity, itemSelected!!, response)
    }

    override fun onGetTripDetailError(message: String) {
        if (context == null) return
        Message.toastLong("Ocurrió un error: "+message+". \n Vuelva a intentarlo en unos segundos.", context)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        if(mRecyclerView != null) mRecyclerView!!.adapter = null
        mRecyclerView = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView!!.adapter = null
        mRecyclerView = null
    }

    private fun validateInternet(action: Int){
        Connection.validate(activity!!, object : Connection.OnConnectionFinishedListener{
            override fun onConnectionFinished(code: Int) {
                when(code){
                    Connection.OK ->{
                        when(action){
                            TRIP_LIST->{
                                val userId = preferences.getDataUser(context)!!.userId
                                presenter.getMyTrips(userId!!, currentPage, elementsByPage, true)
                            }
                            TRIP_DETAIL->{
                                presenter.getTripDetail(itemSelected!!.tripId!!)
                            }
                        }
                    }
                    Connection.NO_NETWORK ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.no_network), context)
                    }
                    Connection.NO_CONNECTION ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.no_connection), context)
                    }
                    Connection.EXCEPTION ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.connection_exception), context)
                    }
                }
            }
        })
    }


}
