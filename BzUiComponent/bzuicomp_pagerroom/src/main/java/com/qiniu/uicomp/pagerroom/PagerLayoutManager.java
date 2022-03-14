package com.qiniu.uicomp.pagerroom;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

public class PagerLayoutManager extends LinearLayoutManager {
    private FixPagerSnapHelper mPagerSnapHelper;
    private OnViewPagerListener mOnViewPagerListener;
    private RecyclerView mRecyclerView;
    private int mDrift;//位移，用来判断移动方向

    public PagerLayoutManager(Context context, int orientation) {
        super(context, orientation, false);
        init();
    }

    public PagerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    private void init() {
        mPagerSnapHelper = new FixPagerSnapHelper();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mPagerSnapHelper.attachToRecyclerView(view);
        this.mRecyclerView = view;
        mRecyclerView.addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
    }

    private int currentIndex = 0;

    /**
     * 滑动状态的改变
     * 缓慢拖拽-> SCROLL_STATE_DRAGGING
     * 快速滚动-> SCROLL_STATE_SETTLING
     * 空闲状态-> SCROLL_STATE_IDLE
     *
     * @param state
     */
    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                View viewIdle = mPagerSnapHelper.findSnapView(this);
                if (viewIdle != null) {
                    int positionIdle = getPosition(viewIdle);
                    if (mOnViewPagerListener != null && getChildCount() == 1) {
                        if (positionIdle != currentIndex) {
                            currentIndex = positionIdle;
                            mOnViewPagerListener.onPageSelected(positionIdle, positionIdle == getItemCount() - 1, viewIdle);
                        }
                    }
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
            case RecyclerView.SCROLL_STATE_DRAGGING:
                View viewDrag = mPagerSnapHelper.findSnapView(this);
                if (viewDrag != null) {
                    int positionDrag = getPosition(viewDrag);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 监听竖直方向的相对偏移量
     *
     * @param dy
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    /**
     * 监听水平方向的相对偏移量
     *
     * @param dx
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dx;
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setOnViewPagerListener(OnViewPagerListener listener) {
        this.mOnViewPagerListener = listener;
    }

    private RecyclerView.OnChildAttachStateChangeListener mChildAttachStateChangeListener = new RecyclerView.OnChildAttachStateChangeListener() {
        /**
         * itemView依赖Window
         */
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnViewPagerListener != null && getChildCount() == 1) {
                int positionIdle = getPosition(view);
                currentIndex = positionIdle;
                mOnViewPagerListener.onInitComplete(view);
            }
        }

        /**
         *itemView脱离Window
         */
        @Override
        public void onChildViewDetachedFromWindow(View view) {
            if (mDrift >= 0) {
                if (mOnViewPagerListener != null)
                    mOnViewPagerListener.onPageRelease(true, getPosition(view), view);
            } else {
                if (mOnViewPagerListener != null)
                    mOnViewPagerListener.onPageRelease(false, getPosition(view), view);
            }
        }
    };

    /**
     * 设置一个内容承载界面
     *
     * @param rootView view
     * @param listener 判断是否需要加载
     */
    public void setViewGroup(final ViewGroup rootView, final IreloadInterface listener) {
        mOnViewPagerListener = new OnViewPagerListener() {

            private boolean isInit = false;
            @Override
            public void onInitComplete(View view) {
                ViewGroup viewGroup = (ViewGroup) view;
                ViewGroup pa = (ViewGroup) rootView.getParent();
                if(pa!=null){
                    pa.removeView(rootView);
                }
                viewGroup.addView(rootView);
                if (listener != null && !isInit) {

                    listener.onReloadPage(-1000, false, viewGroup);
                }
                isInit = true;
            }

            @Override
            public void onPageRelease(boolean isNext, int position, View view) {
                if (currentIndex != position) {
                    //做直播间切换的时候防止释放错误
                    return;
                }
                if (listener != null) {
                    listener.onDestroyPage(isNext, realPosition(position), view);
                }
                ViewGroup viewGroup = (ViewGroup) view;
                viewGroup.removeView(rootView);
                //双重判断防止删除的时候没有移除掉
                if (rootView.getParent() != null && rootView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) (viewGroup.getParent())).removeView(rootView);
                }
            }

            @Override
            public void onPageSelected(int position, boolean isBottom, View view) {
                ViewGroup viewGroup = (ViewGroup) view;
                viewGroup.addView(rootView);
                if (listener != null) {
                    listener.onReloadPage(realPosition(position), isBottom, viewGroup);
                }
            }
        };
    }

    private int realPosition(int position) {
        BaseQuickAdapter baseQuickAdapter = (BaseQuickAdapter) mRecyclerView.getAdapter();
        int size = baseQuickAdapter.getData().size();
        return position >= size ? position % size : position;
    }

    static public interface IreloadInterface {
        /**
         * 重载界面
         *
         * @param position 当页面的游标
         * @param isBottom 是否到底
         * @param view     ViewGroup
         */
        public void onReloadPage(int position, boolean isBottom, View view);

        /**
         * 销毁界面
         *
         * @param isNext   是否有下一个
         * @param position 页面的游标
         * @param view     ViewGroup
         */
        public void onDestroyPage(boolean isNext, int position, View view);
    }
}
