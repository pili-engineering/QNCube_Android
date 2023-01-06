package com.qiniu.bzuicomp.gift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.qiniu.compui.trackview.TrackView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class GiftShowView extends RelativeLayout implements TrackView<GiftMsg> {
    private static final long TIME_OUT = 3500;                  //持续时间
    private ConstraintLayout rlGiftRoot;
    private ImageView ivGiftSenderAvatar;
    private TextView tvGiftSenderName;
    private TextView mtvGiftName;
    private ImageView ivGiftImage;
    private StrokeTextView ivGiftMulti;
    private GiftLinearLayout gllGiftNumRoot;

    /**
     * 动画第一部 show
     */
    private static final int STEP_SHOW = 1;
    private AnimatorSet initInAnimatorSet; // 初始化礼物进场动画
    private ObjectAnimator initGiftTranslation; // 初始化礼物图片进入动画
    private AnimatorSet initOutAnimatorSet; // 初始化礼物退场动画

    public GiftShowView(Context context) {
        super(context);
        initView(context);
    }

    public GiftShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public GiftShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_gift_new, this, true);
        rlGiftRoot = (ConstraintLayout) findViewById(R.id.rlGiftRoot);
        ivGiftSenderAvatar = (ImageView) findViewById(R.id.ivGiftSenderAvatar);
        tvGiftSenderName = (TextView) findViewById(R.id.tvGiftSenderName);
        mtvGiftName = (TextView) findViewById(R.id.mtvGiftName);
        ivGiftImage = (ImageView) findViewById(R.id.ivGiftImage);
        ivGiftMulti = (StrokeTextView) findViewById(R.id.ivGiftMulti);
        gllGiftNumRoot = (GiftLinearLayout) findViewById(R.id.gllGiftNumRoot);

        initAnima();
    }

    private Function0<Unit> finishedCall = null;

    @Nullable
    @Override
    public Function0<Unit> getFinishedCall() {
        return finishedCall;
    }

    @Override
    public void setFinishedCall(@Nullable Function0<Unit> finishedCall) {
        this.finishedCall = finishedCall;
    }

    @Override
    public boolean showInSameTrack(GiftMsg trackMode) {
        if (currentModel == null) {
            return false;
        }
        if (trackMode.getSenderUid().equals(this.currentModel.getSenderUid()) &&
                trackMode.getSendGift().getGiftId().equals(this.currentModel.getSendGift().getGiftId())
        ) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isShow() {
        return isShowAnimaing;
    }

    private void initAnima() {
        final ObjectAnimator inTranslation = new ObjectAnimator();
        inTranslation.setPropertyName("translationX");
        inTranslation.setFloatValues(-200, 0);
        inTranslation.setInterpolator(new BounceInterpolator());
        final ObjectAnimator inAlpha = new ObjectAnimator();
        inAlpha.setPropertyName("alpha");
        inAlpha.setFloatValues(0, 1);
        initInAnimatorSet = new AnimatorSet();
        initInAnimatorSet.playTogether(inTranslation, inAlpha);
        initInAnimatorSet.setDuration(50);

        initInAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rlGiftRoot.setAlpha(1);
                rlGiftRoot.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }
            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        initGiftTranslation = new ObjectAnimator();
        initGiftTranslation.setPropertyName("translationX");
        initGiftTranslation.setFloatValues(-500, 0);
        initGiftTranslation.setDuration(300);
        initGiftTranslation.setStartDelay(30);
        initGiftTranslation.setInterpolator(new OvershootInterpolator());

        final ObjectAnimator outAlpha = new ObjectAnimator();
        outAlpha.setPropertyName("alpha");
        outAlpha.setFloatValues(1, 0);
        final ObjectAnimator outTranslation = new ObjectAnimator();
        outTranslation.setPropertyName("translationX");
        outTranslation.setFloatValues(0, 350);
        initOutAnimatorSet = new AnimatorSet();
        initOutAnimatorSet.playTogether(outAlpha, outTranslation);
        initOutAnimatorSet.setDuration(50);

        initOutAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                ivGiftMulti.setVisibility(View.INVISIBLE);
                gllGiftNumRoot.setVisibility(View.INVISIBLE);
                gllGiftNumRoot.reset();
                rlGiftRoot.setVisibility(View.INVISIBLE);
                isShowAnimaing = false;
                ivGiftImage.setImageResource(0);
                currentAnimaStep = 0;
                currentModel = null;
                showCount = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
    private void checkFinish() {

        if (gift500Queue.isEmpty() && isTimeOut) {
            initOutAnimatorSet.setTarget(rlGiftRoot);
            initOutAnimatorSet.start();
        }
    }

    private void animGiftImage(final ImageView giftImage, final int url) {
        giftImage.setVisibility(View.INVISIBLE);
        initGiftTranslation.setTarget(giftImage);
        initGiftTranslation.removeAllListeners();
        initGiftTranslation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                giftImage.setVisibility(View.VISIBLE);
                giftImage.setImageResource(url);
            }
        });
        initGiftTranslation.start();
    }

    public volatile boolean isShowAnimaing = false;
    private volatile GiftMsg currentModel;
    private int currentAnimaStep = 0;
    private final Queue<GiftMsg> gift500Queue = new ArrayDeque<GiftMsg>();

    private boolean isTimeOut = false;

    /**
     * 隐藏 结束送礼任务
     */
    private final Runnable alphaRunnable = new Runnable() {
        @Override
        public void run() {
            isTimeOut = true;
            checkFinish();
        }
    };

    private void onNewAnimaArrive(@NotNull GiftMsg giftaModel) {
        isTimeOut = false;
        isShowAnimaing = true;
        currentModel = giftaModel;
        removeCallbacks(alphaRunnable);
        postDelayed(alphaRunnable, TIME_OUT);
        // 显示ui
        tvGiftSenderName.setText(giftaModel.getSenderName());
//                liverName
        mtvGiftName.setText(giftaModel.getSendGift().getGiftName());
        String header = giftaModel.getSenderAvatar();
        rlGiftRoot.setVisibility(View.VISIBLE);
        int showImage =(DataInterfaceNew.INSTANCE.getGiftIcon(Integer.parseInt(giftaModel.getSendGift().getGiftId())));

        //还没有滑动
        if (currentAnimaStep == 0) {
            Glide.with(getContext()).load(header)
                    .override(80, 80)
                    .transform(new CircleCrop())
                    .thumbnail(0.1f).into(ivGiftSenderAvatar);

            animGiftImage(ivGiftImage, showImage);
            initInAnimatorSet.setTarget(rlGiftRoot);
            initInAnimatorSet.start();
            currentAnimaStep = STEP_SHOW;
        } else {
            ivGiftImage.setImageResource(showImage);
        }
        numberAndGiftBackAnim(giftaModel);
    }

    int showCount;

    /**
     * 礼物数字跳动动画和礼物弹幕背景变换
     */
    private void numberAndGiftBackAnim(GiftMsg bean) {
        //数字背景以及数字动画
        showCount = showCount + bean.getNumber();
        if (true) {
            // 如果送礼人/收礼人是自己，则显示礼物数字
            ivGiftMulti.setVisibility(View.VISIBLE);
            gllGiftNumRoot.setVisibility(VISIBLE);
            loadNumber(showCount);
        } else {
            ivGiftMulti.setVisibility(View.INVISIBLE);
            gllGiftNumRoot.setVisibility(INVISIBLE);
        }
    }

    // 新的数字跳动代码
    private synchronized void loadNumber(int count) {
        gllGiftNumRoot.addNumber(count);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
       clear(true);
    }

    @Override
    public void onNewModel(GiftMsg mode) {
        onNewAnimaArrive(mode);
    }

    @Override
    public void clear(boolean isRoomChange) {

        if (initInAnimatorSet.isRunning()) {
            initInAnimatorSet.cancel();
        }
        if (initGiftTranslation.isRunning()) {
            initGiftTranslation.cancel();
        }
        if (initOutAnimatorSet.isRunning()) {
            initOutAnimatorSet.cancel();
        }
        showCount = 0;
        ivGiftMulti.setVisibility(View.INVISIBLE);
        gllGiftNumRoot.setVisibility(View.INVISIBLE);
        gllGiftNumRoot.reset();
        rlGiftRoot.setVisibility(View.INVISIBLE);
        isShowAnimaing = false;
        currentAnimaStep = 0;
        gift500Queue.clear();
        ivGiftImage.setImageResource(0);
        currentModel = null;
        removeCallbacks(alphaRunnable);
    }
}