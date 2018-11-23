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

class SearchBlutoothAdapter(val mContext: Context, val listParied: ArrayList<BluetoothDevice>, val listener:(BluetoothDevice) -> Unit) : RecyclerView.Adapter<SearchBlutoothAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(
        R.layout.layout_devices_paired, parent, false))

    override fun getItemCount(): Int = listParied.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position, mContext, listParied[position], listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val TAG = javaClass.simpleName

        fun bind(position: Int, mContext: Context, devicesMac: BluetoothDevice, listener: (BluetoothDevice) -> Unit) = with(itemView){
            tviStatus.visibility = View.GONE
            var name = if(devicesMac.name == null) "" else "${devicesMac.name} "

            if(name.isEmpty()){
                tviDevice.text = "${devicesMac.address}"
            }else {
                tviDevice.text = "$name\n${devicesMac.address}"
            }

            setOnClickListener { listener(devicesMac) }
        }
    }

}