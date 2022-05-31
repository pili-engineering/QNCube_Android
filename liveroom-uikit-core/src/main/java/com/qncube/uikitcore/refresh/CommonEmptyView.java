package com.qncube.uikitcore.refresh;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.qncube.uikitcore.R;


/**
 * 通用empty 待替换ui设计
 */
public class CommonEmptyView extends FrameLayout implements View.OnClickListener, IEmptyView {

    private boolean clickEnable = true;

    private OnClickListener listener;
    private int mErrorState;
    private String strNoDataContent = "";

    ImageView img;
    TextView emptyText;

    public CommonEmptyView(Context context) {
        super(context);
        init(context);
    }

    public CommonEmptyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommonEmptyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.kit_view_custom_empty, this);
        setVisibility(GONE);

       // setBackgroundColor(-1);
        setOnClickListener(this);
        img = findViewById(R.id.img);
        emptyText = findViewById(R.id.empty_text);
        img.setOnClickListener(this);
    }

    /**
     * 設置背景
     */
    public void setEmptyIcon(int imgResource) {
        img.setImageResource(imgResource);
    }

    /**
     * 設置内容
     */
    public void setEmptyTips(String noDataContent) {
        strNoDataContent = noDataContent;
        if (emptyText != null) {
            emptyText.setText(strNoDataContent);
        }
    }

    /**
     * 設置内容
     */
    public void setEmptyTips(@StringRes int tipsRes) {
        setEmptyTips(getContext().getString(tipsRes));
    }

    @Override
    public View getContentView() {
        return this;
    }

    /**
     * 根据状态設置当前view
     *
     * @param i
     */
    @Override
    public void setStatus(int i) {

        final boolean disconnected = !NetUtil.isNetworkAvailable(getContext());
        if(refreshingView!=null){
            refreshingView.setVisibility(GONE);
        }
        switch (i) {
            case NETWORK_ERROR:
                setVisibility(View.VISIBLE);
                mErrorState = NETWORK_ERROR;
                emptyText.setText("网络错误");
                img.setImageResource(R.drawable.kit_pic_empty_network);
                img.setVisibility(View.VISIBLE);
                clickEnable = true;
                break;
            case NODATA:
                setVisibility(View.VISIBLE);
                mErrorState = NODATA;
                img.setImageResource(R.drawable.kit_pic_empty);
                img.setVisibility(View.VISIBLE);
                refreshEmptyView();
                clickEnable = true;
                break;
            case HIDE_LAYOUT:
                setVisibility(View.GONE);
                break;
            case START_REFREASH_WHEN_EMPTY:

                if (refreshingView != null) {
                    setVisibility(View.VISIBLE);
                    refreshingView.setVisibility(VISIBLE);
                }
                break;

            default:
                break;
        }
    }

    private View refreshingView = null;

    public void setRefreshingView(View view) {
        refreshingView = view;
        refreshingView.setVisibility(View.GONE);
        addView(refreshingView);

    }

    private void refreshEmptyView() {
        emptyText.setText(TextUtils.isEmpty(strNoDataContent) ?
                "牛儿有泪不轻弹，这就哭一个给你看" : strNoDataContent);

    }

    /**
     * 获取当前错误状态
     *
     * @return
     */
    public int getErrorState() {
        return mErrorState;
    }

    public void setOnLayoutClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mErrorState = HIDE_LAYOUT;
        }
        super.setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {
        if (clickEnable) {
            if (mErrorState == NETWORK_ERROR) {
             //   Toast.makeText(AppCache.getContext(), "TODO, Network Settings", Toast.LENGTH_LONG);
//                SystemSettingsUtils.launchWirelessSettings(getContext(), true);
                return;
            }
            if (listener != null) {
                listener.onClick(v);
            }
        }
    }
}
