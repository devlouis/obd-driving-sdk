package com.mdp.innovation.obd_driving_api.app.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mdp.innovation.obd_driving_api.R
import java.util.ArrayList
import android.widget.Toast
import android.widget.RadioButton
import android.widget.TextView
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference


class PairedAdapter(val mContext: Context, val listParied: ArrayList<BluetoothDevice>, var mac: String) : RecyclerView.Adapter<PairedAdapter.ViewHolder>() {
    var mMacDevice = ""
    var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_devices_paired, parent, false)

        return ViewHolder(view)
    }


    override fun getItemCount(): Int = listParied.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        LogUtils().v( "PairedAdapter" , " :: ${position}")
        var devicesMac = listParied[position]
        mMacDevice = devicesMac.address

        holder.tviStatus.visibility = View.VISIBLE
        holder.tviStatus.setTextColor(mContext.resources.getColor(R.color.error_color_material))

        if (lastSelectedPosition >= 0) {
            holder.tviStatus.isChecked = lastSelectedPosition == position
        }else{
            holder.tviStatus.isChecked = SharedPreference(mContext).getMacBluetooth()[SharedPreference(mContext).MAC_DEVICE]!! == mMacDevice
        }

        var name = if(devicesMac.name == null) "" else "${devicesMac.name} "
        holder.tviDevice.text = "$name\n${devicesMac.address}"
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tviStatus = view.findViewById<RadioButton>(R.id.tviStatus)
        var tviDevice = view.findViewById<TextView>(R.id.tviDevice)

        private var clickListener: View.OnClickListener = View.OnClickListener {
            lastSelectedPosition = adapterPosition
            SharedPreference(mContext).saveMacBluetooth(listParied[lastSelectedPosition].address)
            notifyItemRangeChanged(0, listParied.size)
        }

        init {
            tviStatus.setOnClickListener(clickListener)
            itemView.setOnClickListener(clickListener)

        }
    }



}