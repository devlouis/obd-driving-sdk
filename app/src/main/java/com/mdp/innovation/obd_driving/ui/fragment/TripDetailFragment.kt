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
import android.util.Log
import com.mdp.innovation.obd_driving.interactor.TripInteractor
import com.mdp.innovation.obd_driving.model.ItemConfigOptionModel
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.presenter.TripPresenter
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.ui.TripView
import com.mdp.innovation.obd_driving.ui.adapter.ItemConfigOptionAdapter
import com.mdp.innovation.obd_driving.ui.adapter.ItemMyTripsAdapter
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import java.util.*


class TripDetailFragment : BaseFragment(), TripView {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(model : ItemMyTripsModel): TripDetailFragment{
            val fragment = TripDetailFragment()
            fragment.model = model
            return fragment
        }
    }

    private lateinit var model : ItemMyTripsModel

    private val presenter = TripPresenter(this, TripInteractor())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_trip_detail, container, false)

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        val toolbar = includeToolbar as Toolbar
        toolbar.title = "Procesando..."
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        toolbar.setNavigationOnClickListener {
            System.out.println("backkkk")
            fragmentManager?.popBackStack()
        }

    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {



    }

    override fun onGetMyTripsError(message: String) {



    }

    override fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loading.visibility = View.GONE
    }


}
