package  com.hapi.baseframe.smartrecycler

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hapi.baseframe.adapter.QMultipleItemRvAdapter
import com.hapi.baseframe.adapter.QRecyclerAdapter
import com.hapi.baseframe.adapter.QRecyclerViewBindAdapter

abstract class QSmartAdapter<T> : QRecyclerAdapter<T>, IAdapter<T> {
    constructor(@androidx.annotation.LayoutRes resID: Int) : super(resID)
    constructor(@androidx.annotation.LayoutRes resID: Int, data: List<T>) : super(resID, data)

    override fun bindRecycler(recyclerView: RecyclerView) {
        this.bindToRecyclerView(recyclerView)
    }

    override fun addDataList(mutableList: MutableList<T>) {
        addData(mutableList)
    }

    override fun setNewDataList(mutableList: MutableList<T>) {
        setNewData(mutableList)
    }

    override fun isCanShowEmptyView(): Boolean {
        return this.data.isEmpty() && (this.headerLayoutCount + this.footerLayoutCount == 0)
    }

    override fun setItemClick(listener: (View, T, index: Int) -> Unit) {
        onItemClickListener = OnItemClickListener { _, view, position ->
            listener.invoke(view, data[position],position)
        }
    }
}

abstract class QSmartViewBindAdapter<T, R : ViewBinding>() :
    QRecyclerViewBindAdapter<T, R>(), IAdapter<T> {
    override fun bindRecycler(recyclerView: RecyclerView) {
        this.bindToRecyclerView(recyclerView)
    }

    override fun addDataList(mutableList: MutableList<T>) {
        addData(mutableList)
    }

    override fun setNewDataList(mutableList: MutableList<T>) {
        setNewData(mutableList)
    }

    override fun isCanShowEmptyView(): Boolean {
        return this.data.isEmpty() && (this.headerLayoutCount + this.footerLayoutCount == 0)
    }
    override fun setItemClick(listener: (View, T, index: Int) -> Unit) {
        onItemClickListener = OnItemClickListener { _, view, position ->
            listener.invoke(view, data[position],position)
        }
    }
}

abstract class QSmartMultipleAdapter<T>(data: List<T>) : QMultipleItemRvAdapter<T>(data),
    IAdapter<T> {
    override fun bindRecycler(recyclerView: RecyclerView) {
        this.bindToRecyclerView(recyclerView)
    }

    override fun addDataList(mutableList: MutableList<T>) {
        addData(mutableList)
    }

    override fun setNewDataList(mutableList: MutableList<T>) {
        setNewData(mutableList)
    }

    override fun isCanShowEmptyView(): Boolean {
        return this.data.isEmpty() && (this.headerLayoutCount + this.footerLayoutCount == 0)
    }
    override fun setItemClick(listener: (View, T, index: Int) -> Unit) {
        onItemClickListener = OnItemClickListener { _, view, position ->
            listener.invoke(view, data[position],position)
        }
    }
}