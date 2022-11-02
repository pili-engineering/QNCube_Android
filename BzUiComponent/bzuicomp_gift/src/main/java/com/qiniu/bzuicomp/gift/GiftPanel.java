package com.qiniu.bzuicomp.gift;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.niucube.comproom.RoomManager;
import com.niucube.rtm.RtmManager;
import com.qiniusdk.userinfoprovide.UserInfoProvider;

import java.util.ArrayList;
import java.util.Objects;

public class GiftPanel extends LinearLayout {

    private GridView gvGift;
    private GiftAdapter adapter;
    private TextView tvGiftSend;
    private EditText editNum;

    private ArrayList<Gift> gifts;

    private int currentPosition = -1;
    private GiftSendListener giftSendListener;

    public GiftPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public interface GiftSendListener {
        void onGiftSent(GiftMsg msg);
    }

    public void setGiftSendListener(GiftSendListener giftSendListener) {
        this.giftSendListener = giftSendListener;
    }

    private void initView(final Context context) {
        final View layout = LayoutInflater.from(getContext()).inflate(R.layout.widget_gift_panel, this);

        gvGift = (GridView) findViewById(R.id.gv_gift);
        tvGiftSend = (TextView) findViewById(R.id.tv_gift_send);
        editNum = (EditText) findViewById(R.id.edit_gift_num);
        gifts = DataInterfaceNew.INSTANCE.getGifts();
        adapter = new GiftAdapter(context, gifts);
        gvGift.setAdapter(adapter);
        tvGiftSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editNum.getText()) || Integer.parseInt(editNum.getText().toString()) == 0) {
                    Toast.makeText(context, "礼物数不能为0", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(editNum.getText().toString()) > 99) {
                    Toast.makeText(context, "礼物数不能超过99", Toast.LENGTH_SHORT).show();
                } else {
                    Gift gift = gifts.get(currentPosition);

                    GiftMsg msg = new GiftMsg();
                    msg.setNumber(Integer.parseInt(editNum.getText().toString()));
                    msg.setSendGift(gift);
                    msg.setSenderRoomId(Objects.requireNonNull(RoomManager.INSTANCE.getMCurrentRoom()).provideRoomId());
                    msg.setSenderUid(UserInfoProvider.INSTANCE.getGetLoginUserIdCall().invoke());
                    msg.setSenderName(UserInfoProvider.INSTANCE.getGetLoginUserNameCall().invoke());
                    msg.setSenderAvatar(UserInfoProvider.INSTANCE.getGetLoginUserAvatarCall().invoke());
                    if (giftSendListener != null) {
                        giftSendListener.onGiftSent(msg);
                    }
                }
            }
        });

        gvGift.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                for (int i = 0; i < adapterView.getCount(); i++) {
                    View v = adapterView.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        view.setSelected(true);
                        currentPosition = position;
                    } else {
                        v.setSelected(false);
                    }
                }
                tvGiftSend.setEnabled(true);
            }
        });
    }
}
