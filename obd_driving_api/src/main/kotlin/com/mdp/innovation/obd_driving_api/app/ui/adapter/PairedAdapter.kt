package com.mdp.innovation.obd_driving_api.app.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import kotlinx.android.synthetic.main.layout_devices_paired.view.*
import java.util.ArrayList

class PairedAdapter(val mContext: Context, val listParied: ArrayList<BluetoothDevice>, var paired: Boolean, var pos: Int, val listener:(BluetoothDevice, Int) -> Unit) : RecyclerView.Adapter<PairedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(
        R.layout.layout_devices_paired, parent, false))

    override fun getItemCount(): Int = listParied.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(mContext, listParied[position], paired, pos, listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mContext: Context, devicesMac: BluetoothDevice,  paired: Boolean, pos: Int, listener: (BluetoothDevice, Int) -> Unit) = with(itemView){
            LogUtils().v("PairedAdapter", "" + paired)
            LogUtils().v("PairedAdapter", "" + (itemId.toInt() + 1))
            val position = (itemId.toInt() + 1)


            if (paired){
                tviStatus.visibility = View.VISIBLE
                tviStatus.setTextColor(resources.getColor(R.color.error_color_material))
            }else{
                tviStatus.visibility = View.GONE
            }
            tviStatus.isChecked = position == pos

            var name = if(devicesMac.name == null) "" else "${devicesMac.name} "
            rbuDevice.text = "$name\n${devicesMac.address}"
            setOnClickListener { listener(devicesMac, itemId.toInt()) }
        }
    }

}