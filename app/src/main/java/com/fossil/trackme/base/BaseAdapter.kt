package com.fossil.trackme.base

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

/**
 * This is a Base recyclerView adapter class that support DataBinding
 * The BaseRVAdapter suit for data contains a list of enity only, and can do multi types of views like normal adapter
 * Since we use DataBinding, we just point the listData to the MutableList of ViewModel
 * so the setData method work fine as we change the data in ViewModel
 * This can use with multi viewType if using the RecyclerView.ViewHolder
 * Ca
 * +Another use of this is we just have to keep the BindableAdapter in any case and use setData method
 * if we not using DataBinding+
 * @param layoutId the resourcen make another Base easy-to-use adapter base on this with a BaseViewHolder to extendsId of itemConfigBox layout
 * @param vhClass the class of itemConfigBox ViewHolder, must extend from [BaseViewHolder]
 * @param clickListener handle itemConfigBox callback listener, can be empty
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseAdapter<TYPE>(
    private var layoutId: Int,
    private var vhClass: KClass<out BaseViewHolder<TYPE>>,
    private var clickListener: RVClickListener<TYPE>? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    BindableAdapter<TYPE> {

    override var listData: ArrayList<TYPE> = ArrayList()
    /**
     * Override this if want to make infinite list
     * @return the number of itemConfigBox in RecyclerView
     */
    override fun getItemCount(): Int {
        return listData.size
    }

    /**
     * Override this if want to make infinite list
     * @param position the position of itemConfigBox in RecyclerView
     * @return the object of itemConfigBox
     */
    protected fun getItem(position: Int): TYPE {
        return listData[position]
    }

    /**
     * Auto transfer data to [BaseViewHolder.bind]
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IViewHolder<*>) {
            (holder as IViewHolder<TYPE>).apply {
                set(listData[position])
                bind()
            }
        }
    }

    override fun setData(data: List<TYPE>) {
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }
}

/**
 * Interface class contains setData method with form of data to use in DataBinding or normal way
 */
interface BindableAdapter<TYPE> {
    /**
     * Data of Adapter, always a list
     */
    var listData: ArrayList<TYPE>

    /**
     * Set data for [BindableAdapter]
     * Have to handle the data manually
     */
    fun setData(data: List<TYPE>)

    fun isEmpty(): Boolean {
        return listData.size == 0
    }

    /**
     * Get data base on position (for a list)
     */
    fun getData(pos: Int): TYPE {
        return listData[pos]
    }
}

/**
 * RecyclerView click listener
 * Use with [BaseRVAdapter] and [BaseViewHolder] for best suit
 * Can use in custom Adapter too
 */
interface RVClickListener<TYPE> {
    /**
     * Callback when click to an itemConfigBox in RecyclerView
     * @param pos the itemConfigBox position in List
     * @param data the Object of data (base on position and listData of BindableAdapter if using)
     */
    fun onItemClick(pos: Int, data: TYPE?) {}

    /**
     * Callback when click to a sub itemConfigBox in RecyclerView
     * pos and data is the same with [onItemClick]
     * @param viewId the subView id when clicked, handle by subView.OnClickListener
     */
    fun onSubItemClick(pos: Int, data: TYPE?, viewId: Int = 0) {}

    /**
     * Callback when click to a sub itemConfigBox that return different data (Rv in rv)
     * @param data come as Bundle instead of ViewHolder object data
     */
    fun onSubItemClick(pos: Int, data: Bundle?, viewId: Int = 0) {}
}
