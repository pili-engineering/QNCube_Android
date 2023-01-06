package  com.hapi.baseframe.adapter

import android.os.Build
import android.util.SparseArray
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.AdapterView
import android.widget.Checkable
import android.widget.CompoundButton
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

open class QRecyclerViewHolder(view: View) : ViewHolder(view) {
    /**
     * Views indexed with their IDs
     */
    private val views = SparseArray<View?>()
    private val childClickViewIds = LinkedHashSet<Int>()
    private val itemChildLongClickViewIds = LinkedHashSet<Int>()
    private var adapter: QRecyclerAdapter<*>? = null
    fun getItemChildLongClickViewIds(): HashSet<Int> {
        return itemChildLongClickViewIds
    }

    fun getChildClickViewIds(): HashSet<Int> {
        return childClickViewIds
    }

    /**
     * Sets the adapter of a adapter view.
     *
     * @param adapter The adapter;
     * @return The BaseViewHolder for chaining.
     */
    fun setAdapter(adapter: QRecyclerAdapter<*>): QRecyclerViewHolder {
        this.adapter = adapter
        return this
    }

    fun <T : View?> getView(@IdRes viewId: Int): T? {
        var view = views[viewId]
        if (view == null) {
            view = itemView.findViewById(viewId)
            views.put(viewId, view)
        }
        return view as T?
    }

    /**
     * Add an action to set the alpha of a view. Can be called multiple times.
     * Alpha between 0-1.
     */
    fun setAlpha(@IdRes viewId: Int, value: Float): QRecyclerViewHolder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView<View>(viewId)!!.alpha = value
        } else {
            // Pre-honeycomb hack to set Alpha value
            val alpha = AlphaAnimation(value, value)
            alpha.duration = 0
            alpha.fillAfter = true
            getView<View>(viewId)!!.startAnimation(alpha)
        }
        return this
    }

    /**
     * Set a view visibility to VISIBLE (true) or GONE (false).
     *
     * @param viewId  The view id.
     * @param visible True for VISIBLE, false for GONE.
     * @return The BaseViewHolder for chaining.
     */
    fun setGone(@IdRes viewId: Int, visible: Boolean): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    /**
     * Set a view visibility to VISIBLE (true) or INVISIBLE (false).
     *
     * @param viewId  The view id.
     * @param visible True for VISIBLE, false for INVISIBLE.
     * @return The BaseViewHolder for chaining.
     */
    fun setVisible(@IdRes viewId: Int, visible: Boolean): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        return this
    }

    /**
     * Sets the on click listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on click listener;
     * @return The BaseViewHolder for chaining.
     */
    @Deprecated("")
    fun setOnClickListener(
        @IdRes viewId: Int,
        listener: View.OnClickListener?
    ): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnClickListener(listener)
        return this
    }

    /**
     * add childView id
     *
     * @param viewIds add the child views id can support childview click
     * @return if you use adapter bind listener
     * @link {(adapter.setOnItemChildClickListener(listener))}
     *
     *
     * or if you can use  recyclerView.addOnItemTouch(listerer)  wo also support this menthod
     */
    fun addOnClickListener(@IdRes vararg viewIds: Int): QRecyclerViewHolder {
        for (viewId in viewIds) {
            childClickViewIds.add(viewId)
            val view = getView<View>(viewId)
            if (view != null) {
                if (!view.isClickable) {
                    view.isClickable = true
                }
                view.setOnClickListener(View.OnClickListener { v ->
                    if (adapter!!.onItemChildClickListener != null) {
                        var position = adapterPosition
                        if (position == RecyclerView.NO_POSITION) {
                            return@OnClickListener
                        }
                        position -= adapter!!.headerLayoutCount
                        adapter!!.onItemChildClickListener!!
                            .onItemChildClick(adapter, v, position)
                    }
                })
            }
        }
        return this
    }

    /**
     * add long click view id
     *
     * @param viewIds
     * @return if you use adapter bind listener
     * @link {(adapter.setOnItemChildLongClickListener(listener))}
     *
     *
     * or if you can use  recyclerView.addOnItemTouch(listerer)  wo also support this menthod
     */
    fun addOnLongClickListener(@IdRes vararg viewIds: Int): QRecyclerViewHolder {
        for (viewId in viewIds) {
            itemChildLongClickViewIds.add(viewId)
            val view = getView<View>(viewId)
            if (view != null) {
                if (!view.isLongClickable) {
                    view.isLongClickable = true
                }
                view.setOnLongClickListener(View.OnLongClickListener { v ->
                    if (adapter!!.onItemChildLongClickListener == null) {
                        return@OnLongClickListener false
                    }
                    var position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@OnLongClickListener false
                    }
                    position -= adapter!!.headerLayoutCount
                    adapter!!.onItemChildLongClickListener!!
                        .onItemChildLongClick(adapter, v, position)
                })
            }
        }
        return this
    }

    /**
     * Sets the on touch listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on touch listener;
     * @return The BaseViewHolder for chaining.
     */
    @Deprecated("")
    fun setOnTouchListener(
        @IdRes viewId: Int,
        listener: View.OnTouchListener?
    ): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnTouchListener(listener)
        return this
    }

    /**
     * Sets the on long click listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on long click listener;
     * @return The BaseViewHolder for chaining.
     * Please use [.addOnLongClickListener] (adapter.setOnItemChildLongClickListener(listener))}
     */
    @Deprecated("")
    fun setOnLongClickListener(
        @IdRes viewId: Int,
        listener: View.OnLongClickListener?
    ): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnLongClickListener(listener)
        return this
    }

    /**
     * Sets the listview or gridview's item click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item on click listener;
     * @return The BaseViewHolder for chaining.
     * Please use [.addOnClickListener] (int)} (adapter.setOnItemChildClickListener(listener))}
     */
    @Deprecated("")
    fun setOnItemClickListener(
        @IdRes viewId: Int,
        listener: AdapterView.OnItemClickListener?
    ): QRecyclerViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemClickListener = listener
        return this
    }

    /**
     * Sets the listview or gridview's item long click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item long click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnItemLongClickListener(
        @IdRes viewId: Int,
        listener: AdapterView.OnItemLongClickListener?
    ): QRecyclerViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemLongClickListener = listener
        return this
    }

    /**
     * Sets the listview or gridview's item selected click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item selected click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnItemSelectedClickListener(
        @IdRes viewId: Int,
        listener: AdapterView.OnItemSelectedListener?
    ): QRecyclerViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemSelectedListener = listener
        return this
    }

    /**
     * Sets the on checked change listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The checked change listener of compound button.
     * @return The BaseViewHolder for chaining.
     */
    fun setOnCheckedChangeListener(
        @IdRes viewId: Int,
        listener: CompoundButton.OnCheckedChangeListener?
    ): QRecyclerViewHolder {
        val view = getView<CompoundButton>(viewId)!!
        view.setOnCheckedChangeListener(listener)
        return this
    }

    /**
     * Sets the tag of the view.
     *
     * @param viewId The view id.
     * @param tag    The tag;
     * @return The BaseViewHolder for chaining.
     */
    fun setTag(@IdRes viewId: Int, tag: Any?): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.tag = tag
        return this
    }

    /**
     * Sets the tag of the view.
     *
     * @param viewId The view id.
     * @param key    The key of tag;
     * @param tag    The tag;
     * @return The BaseViewHolder for chaining.
     */
    fun setTag(@IdRes viewId: Int, key: Int, tag: Any?): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.setTag(key, tag)
        return this
    }

    /**
     * Sets the checked status of a checkable.
     *
     * @param viewId  The view id.
     * @param checked The checked status;
     * @return The BaseViewHolder for chaining.
     */
    fun setChecked(@IdRes viewId: Int, checked: Boolean): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        // View unable cast to Checkable
        if (view is Checkable) {
            (view as Checkable).isChecked = checked
        }
        return this
    }

    /**
     * Set the enabled state of this view.
     *
     * @param viewId The view id.
     * @param enable The checked status;
     * @return The BaseViewHolder for chaining.
     */
    fun setEnabled(@IdRes viewId: Int, enable: Boolean): QRecyclerViewHolder {
        val view = getView<View>(viewId)!!
        view.isEnabled = enable
        return this
    }
}