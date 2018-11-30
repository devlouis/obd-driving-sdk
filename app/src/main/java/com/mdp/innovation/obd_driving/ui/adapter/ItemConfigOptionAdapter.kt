package com.mdp.innovation.obd_driving.ui.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.model.ItemConfigOptionModel

class ItemConfigOptionAdapter(private val mDataList: ArrayList<ItemConfigOptionModel>, val context: Context?)
    : RecyclerView.Adapter<ItemConfigOptionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_config_option, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.iv_img.setImageResource(mDataList[position].img!!)
        holder.tv_text.text = mDataList[position].text
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var iv_img:  ImageView
        internal var tv_text: TextView

        init {
            iv_img = itemView.findViewById<View>(R.id.iv_img) as ImageView
            tv_text = itemView.findViewById<View>(R.id.tv_text) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            var item = mDataList[adapterPosition]
            var intent = Intent(context, item.screen)
            context?.startActivity(intent)

        }

    }

}