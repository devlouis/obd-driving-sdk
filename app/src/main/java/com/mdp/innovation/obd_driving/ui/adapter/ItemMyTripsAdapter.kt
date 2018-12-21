package com.mdp.innovation.obd_driving.ui.adapter

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.util.Message
import java.text.SimpleDateFormat
import android.view.MotionEvent



class ItemMyTripsAdapter(private var mDataList: java.util.ArrayList<ItemMyTripsModel?>, var context: Context?)
    : RecyclerView.Adapter<ItemMyTripsAdapter.MyViewHolder>() {

    private val TAG = javaClass.simpleName
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

    private var visibleThreshold = 1
    private var loading: Boolean = false
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var onClickItemListener: OnClickItemListener? = null
    private var stopScroll = false

    constructor(context: Context?, mDataList: java.util.ArrayList<ItemMyTripsModel?>, recyclerView: RecyclerView)
        : this(mDataList, context){

        this.mDataList = mDataList
        this.context = context

        if(recyclerView.layoutManager is LinearLayoutManager){
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        totalItemCount = linearLayoutManager.itemCount
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                        if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold && !stopScroll) {
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener?.onLoadMore()
                            }
                            loading = true
                        }
                    }
                }
            )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewHolder = if (viewType == 1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_trips, parent, false)
            MyViewHolderItem(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            MyViewHolderProgress(view)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(holder is MyViewHolderItem){
            var dateStr = " - "
            var scoreStr = " - "
            try{
                dateStr = sdf.format(mDataList[position]?.timeStart)
                scoreStr = mDataList[position]?.score.toString()
            }catch (ex: Exception){
                Log.d(TAG, ex.message)
            }

            holder.tv_date_value.text = dateStr
            holder.tv_duration_value.text = mDataList[position]?.duration
            holder.tv_score_value.text = scoreStr
        }else if(holder is MyViewHolderProgress){
            holder.pb_loading.isIndeterminate = true
        }

    }

    fun setLoaded() {
        loading = false
    }

    fun stopScroll() {
        stopScroll = true
    }

    override fun getItemViewType(position: Int) : Int{
        return if (mDataList[position] != null) 1 else 0
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    open class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class MyViewHolderItem(itemView: View) : MyViewHolder(itemView), View.OnClickListener {
        internal var tv_date_value:  TextView
        internal var tv_duration_value: TextView
        internal var tv_score_value: TextView
        /*internal var ll_score: LinearLayout
        internal var ll_score_initial_x: Float
        internal var ll_score_initial_y: Float*/

        init {

            tv_date_value = itemView.findViewById<View>(R.id.tv_date_value) as TextView
            tv_duration_value = itemView.findViewById<View>(R.id.tv_duration_value) as TextView
            tv_score_value = itemView.findViewById<View>(R.id.tv_score_value) as TextView
            //ll_score = itemView.findViewById<View>(R.id.ll_score) as LinearLayout

            itemView.setOnClickListener(this)

            /*ll_score_initial_x = ll_score.x
            ll_score_initial_y = ll_score.y

            ll_score.setOnTouchListener(object : View.OnTouchListener {
                var prevX: Int = 0
                var prevY: Int = 0

                var dX: Float = 0F
                //var dY: Float = 0F

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    val par = v.layoutParams as LinearLayout.LayoutParams
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            /*par.topMargin += event.rawY.toInt() - prevY
                            prevY = event.rawY.toInt()

                            prevX = event.rawX.toInt()
                            v.layoutParams = par*/
                            Log.d(TAG, "ACTION_MOVE")

                            v.animate()
                                .x(event.rawX + dX)
                                //.y(event.rawY + dY)
                                .setDuration(0)
                                .start()

                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            /*par.topMargin += event.rawY.toInt() - prevY

                            v.layoutParams = par*/
                            Log.d(TAG, "ACTION_UP")

                            v.animate()
                                .x(ll_score_initial_x)
                                //.y(event.rawY + dY)
                                .setDuration(0)
                                .start()

                            return true
                        }
                        MotionEvent.ACTION_DOWN -> {
                            /*prevX = event.rawX.toInt()
                            prevY = event.rawY.toInt()
                            par.bottomMargin = -2 * v.height

                            v.layoutParams = par*/

                            dX = v.x - event.rawX
                            //dY = v.y - event.rawY



                            Log.d(TAG, "ACTION_DOWN")
                            return true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            Log.d(TAG, "ACTION_CANCEL")

                            v.animate()
                                .x(ll_score_initial_x)
                                //.y(event.rawY + dY)
                                .setDuration(0)
                                .start()

                            true
                        }
                        MotionEvent.ACTION_OUTSIDE -> {
                            Log.d(TAG, "ACTION_OUTSIDE")
                            true
                        }
                    }
                    return false
                }
            })*/
        }

        override fun onClick(v: View) {

            var item = mDataList[adapterPosition]
            onClickItemListener!!.onClick(item!!)

            Message.toastShort("Aloja!", v.context)
        }

    }

    inner class MyViewHolderProgress(itemView: View) : MyViewHolder(itemView){
        internal var pb_loading:  ProgressBar

        init {
            pb_loading = itemView.findViewById<View>(R.id.pb_loading) as ProgressBar
        }

    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }
    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    interface OnClickItemListener {
        fun onClick(item: ItemMyTripsModel)
    }
    fun setOnClickItemListener(onClickItemListener: OnClickItemListener) {
        this.onClickItemListener = onClickItemListener
    }

}