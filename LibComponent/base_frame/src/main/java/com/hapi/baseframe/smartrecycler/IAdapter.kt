package  com.hapi.baseframe.smartrecycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface IAdapter<T> {
    fun bindRecycler(recyclerView: RecyclerView)
    fun addDataList(mutableList: MutableList<T>)
    fun setNewDataList(mutableList: MutableList<T>)
    fun isCanShowEmptyView(): Boolean
    fun setItemClick(listener: (View, T, index: Int) -> Unit)
}