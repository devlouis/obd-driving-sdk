package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_configuration.*
import android.support.annotation.Nullable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.model.ItemConfigOptionModel
import com.mdp.innovation.obd_driving.ui.adapter.ItemConfigOptionAdapter
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity


class ConfigurationFragment : BaseFragment() {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): ConfigurationFragment{
            return ConfigurationFragment()
        }
    }

    private var mRecyclerView: RecyclerView? = null
    var itemConfigOptionModelList: ArrayList<ItemConfigOptionModel> = ArrayList()

    var myActivity = HomeActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_configuration, container, false)

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Configuración"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        myActivity = activity as HomeActivity
        myActivity.drawerConfig(toolbar)

        setOptions(view)

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myActivity = activity as HomeActivity
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

    fun setOptions(view: View){

        val option1 = ItemConfigOptionModel()
        option1.img = R.drawable.ic_bluetooth
        option1.text = resources.getString(R.string.configOption1)
        option1.screen = PairObdActivity::class.java
        itemConfigOptionModelList.add(option1)

        mRecyclerView = view.findViewById(R.id.rv_config_option)
        var mLayoutManager = GridLayoutManager(activity, 2)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.adapter = ItemConfigOptionAdapter(itemConfigOptionModelList, context)

    }


}
