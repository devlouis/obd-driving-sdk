package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_my_trips.*
import android.support.annotation.Nullable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mdp.innovation.obd_driving.model.ItemConfigOptionModel
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.ui.TripView
import com.mdp.innovation.obd_driving.ui.adapter.ItemConfigOptionAdapter
import com.mdp.innovation.obd_driving.ui.adapter.ItemMyTripsAdapter
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import java.util.*


class MyTripsFragment : BaseFragment(), TripView {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyTripsFragment{
            return MyTripsFragment()
        }
    }

    private var mRecyclerView: RecyclerView? = null
    var itemMyTripsModelList: ArrayList<ItemMyTripsModel?> = ArrayList()
    var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_my_trips, container, false)

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Mis Viajes"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        drawerConfig(activity, toolbar)

        val option1 = ItemMyTripsModel()
        option1.timeStart = Date()
        option1.duration = "00:35:51"
        option1.score = 9.5f
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)
        itemMyTripsModelList.add(option1)

        mRecyclerView = view.findViewById(R.id.rv_my_trips)
        var mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = mLayoutManager
        //var adapter = ItemMyTripsAdapter(itemMyTripsModelList, context)
        var adapter = ItemMyTripsAdapter(context, itemMyTripsModelList, mRecyclerView!!)
        mRecyclerView!!.adapter = adapter
        adapter.setOnLoadMoreListener(object : ItemMyTripsAdapter.OnLoadMoreListener{
            override fun onLoadMore() {
                itemMyTripsModelList.add(null)

                mRecyclerView!!.postDelayed({
                    adapter.notifyItemInserted(itemMyTripsModelList.size - 1)


                    handler.postDelayed({

                        itemMyTripsModelList.removeAt(itemMyTripsModelList.size - 1)
                        adapter.notifyItemRemoved(itemMyTripsModelList.size)
                        for (i in 0..9) {
                            itemMyTripsModelList.add(option1)
                            adapter.notifyItemInserted(itemMyTripsModelList.size)
                        }
                        adapter.setLoaded()
                        adapter.stopScroll()
                    }, 2000)

                },100)

                println("load")
            }
        })

        adapter.setOnClickItemListener(object : ItemMyTripsAdapter.OnClickItemListener{
            override fun onClick(item: ItemMyTripsModel) {



            }
        })

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {

    }

    override fun onGetMyTripsError(message: String) {

    }

    override fun showProgress() {

    }

    override fun hideProgress() {

    }


}
