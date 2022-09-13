package com.niucube.player.video.contronller;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.niucube.player.PlayerStatusListener;
import com.niucube.player.utils.PalyerUtil;
import com.niucube.player.video.IVideoPlayer;
import com.qiniu.comp.playersdk.R;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.niucube.player.PlayerStatus.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DefaultController extends FrameLayout implements View.OnTouchListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, PlayerStatusListener, IController,
        ChangeClarityDialog.OnClarityChangedListener {


    private Context mContext;
    protected IVideoPlayer mPlayer;
    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;
    private float mDownX;
    private float mDownY;
    private boolean mNeedChangePosition;
    private boolean mNeedChangeVolume;
    private boolean mNeedChangeBrightness;
    private static final int THRESHOLD = 80;
    private long mGestureDownPosition;
    private float mGestureDownBrightness;
    private int mGestureDownVolume;
    private long mNewPosition;
    private ImageView mCenterStart;
    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;
    private LinearLayout mBatteryTime;
    private TextView mTime;
    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mPosition;
    private TextView mDuration;
    private SeekBar mSeek;
    private TextView mClarity;
    private ImageView mFullScreen;
    private LinearLayout mLoading;
    private TextView mLoadText;
    private LinearLayout mChangePositon;
    private TextView mChangePositionCurrent;
    private ProgressBar mChangePositionProgress;
    private LinearLayout mChangeBrightness;
    private ProgressBar mChangeBrightnessProgress;
    private LinearLayout mChangeVolume;
    private ProgressBar mChangeVolumeProgress;
    private LinearLayout mError;
    private TextView mRetry;
    private LinearLayout mCompleted;
    private TextView mReplay;
    private ImageView mIVReplay;
    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;
    private List<Clarity> clarities;
    private int defaultClarityIndex;
    private TextView tvTinyWindow;
    public Boolean tinyWindowAble = false;
    public View.OnClickListener onBackIconClickListener =null;

    public DefaultController(@NonNull Context context) {
        super(context);
        mContext = context;
        this.setOnTouchListener(this);
        init();
    }

    public DefaultController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.setOnTouchListener(this);
        init();
    }

    public DefaultController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        this.setOnTouchListener(this);
        init();
    }

    private void init() {

        LayoutInflater.from(mContext).inflate(R.layout.tx_video_palyer_controller, this, true);
        mCenterStart = (ImageView) findViewById(R.id.center_start);
        mTop = (LinearLayout) findViewById(R.id.top);
        mBack = (ImageView) findViewById(R.id.back);
        mTitle = (TextView) findViewById(R.id.title);
        mBatteryTime = (LinearLayout) findViewById(R.id.battery_time);
        mTime = (TextView) findViewById(R.id.time);
        tvTinyWindow = findViewById(R.id.tvTinyWindow);
        mBottom = (LinearLayout) findViewById(R.id.bottom);
        mRestartPause = (ImageView) findViewById(R.id.restart_or_pause);
        mPosition = (TextView) findViewById(R.id.position);
        mDuration = (TextView) findViewById(R.id.duration);
        mSeek = (SeekBar) findViewById(R.id.seek);
        mFullScreen = (ImageView) findViewById(R.id.full_screen);
        mClarity = (TextView) findViewById(R.id.clarity);
        mLoading = (LinearLayout) findViewById(R.id.loading);
        mLoadText = (TextView) findViewById(R.id.load_text);
        mChangePositon = (LinearLayout) findViewById(R.id.change_position);
        mChangePositionCurrent = (TextView) findViewById(R.id.change_position_current);
        mChangePositionProgress = (ProgressBar) findViewById(R.id.change_position_progress);
        mChangeBrightness = (LinearLayout) findViewById(R.id.change_brightness);
        mChangeBrightnessProgress = (ProgressBar) findViewById(R.id.change_brightness_progress);
        mChangeVolume = (LinearLayout) findViewById(R.id.change_volume);
        mChangeVolumeProgress = (ProgressBar) findViewById(R.id.change_volume_progress);
        mError = (LinearLayout) findViewById(R.id.error);
        mRetry = (TextView) findViewById(R.id.retry);
        mCompleted = (LinearLayout) findViewById(R.id.completed);
        mReplay = (TextView) findViewById(R.id.replay);
        mIVReplay = findViewById(R.id.ivReplay);
        mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRestartPause.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mClarity.setOnClickListener(this);
        mRetry.setOnClickListener(this);
        mReplay.setOnClickListener(this);
        mSeek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
        tvTinyWindow.setOnClickListener(this);
    }

    public void setSeekAble(boolean seekAble) {
        mSeek.setEnabled(seekAble);
    }
    public void setSeekVisibility(int visibility) {
        mSeek.setVisibility(visibility);
    }


    private boolean isCompletedRestartAble = true;

    public void setCompletedRestartAble(boolean isCompletedRestartAble) {
        this.isCompletedRestartAble = isCompletedRestartAble;
    }

    private boolean pauseAble = true;

    public boolean isPauseAble() {
        return pauseAble;
    }

    public void setPauseAble(boolean pauseAble) {
        this.pauseAble = pauseAble;
        mRestartPause.setClickable(pauseAble);
        mCenterStart.setClickable(pauseAble);
    }


    public void attach(@NotNull IVideoPlayer player) {
        mPlayer = player;
    }


    public void detach() {
        mPlayer = null;
    }

    /**
     * 开启更新进度的计时器。
     */
    protected void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 1000);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public ImageView playButton() {
        return mCenterStart;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 只有全屏的时候才能拖动位置、亮度、声音
        if (!mPlayer.isFullScreen()) {
            return false;
        }
        // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
        if (mPlayer.isIdle()
                || mPlayer.isError()
                || mPlayer.isPreparing()
                || mPlayer.isPrepared()
                || mPlayer.isCompleted()) {
            hideChangePosition();
            hideChangeBrightness();
            hideChangeVolume();
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mNeedChangePosition = false;
                mNeedChangeVolume = false;
                mNeedChangeBrightness = false;
                break;
            case MotionEvent.ACTION_MOVE:

                if (mPlayer.isTinyWindow()) {
                    return false;
                }

                float deltaX = x - mDownX;
                float deltaY = y - mDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!mNeedChangePosition && !mNeedChangeVolume && !mNeedChangeBrightness) {
                    // 只有在播放、暂停、缓冲的时候能够拖动改变位置、亮度和声音
                    if (absDeltaX >= THRESHOLD) {
                        cancelUpdateProgressTimer();
                        mNeedChangePosition = true;
                        mGestureDownPosition = mPlayer.getCurrentPosition();
                    } else if (absDeltaY >= THRESHOLD) {
                        if (mDownX < getWidth() * 0.5f) {
                            // 左侧改变亮度
                            mNeedChangeBrightness = true;
                            mGestureDownBrightness = PalyerUtil.scanForActivity(mContext)
                                    .getWindow().getAttributes().screenBrightness;
                        } else {
                            // 右侧改变声音
                            mNeedChangeVolume = true;
                            mGestureDownVolume = mPlayer.getVolume();
                        }
                    }
                }
                if (mNeedChangePosition) {
                    long duration = mPlayer.getDuration();
                    long toPosition = (long) (mGestureDownPosition + duration * deltaX / getWidth());
                    mNewPosition = Math.max(0, Math.min(duration, toPosition));
                    int newPositionProgress = (int) (100f * mNewPosition / duration);
                    showChangePosition(duration, newPositionProgress);
                }
                if (mNeedChangeBrightness) {
                    deltaY = -deltaY;
                    float deltaBrightness = deltaY * 3 / getHeight();
                    float newBrightness = mGestureDownBrightness + deltaBrightness;
                    newBrightness = Math.max(0, Math.min(newBrightness, 1));
                    float newBrightnessPercentage = newBrightness;
                    WindowManager.LayoutParams params = PalyerUtil.scanForActivity(mContext)
                            .getWindow().getAttributes();
                    params.screenBrightness = newBrightnessPercentage;
                    PalyerUtil.scanForActivity(mContext).getWindow().setAttributes(params);
                    int newBrightnessProgress = (int) (100f * newBrightnessPercentage);
                    showChangeBrightness(newBrightnessProgress);
                }
                if (mNeedChangeVolume) {
                    deltaY = -deltaY;
                    int maxVolume = mPlayer.getMaxVolume();
                    int deltaVolume = (int) (maxVolume * deltaY * 3 / getHeight());
                    int newVolume = mGestureDownVolume + deltaVolume;
                    newVolume = Math.max(0, Math.min(maxVolume, newVolume));
                    mPlayer.setVolume(newVolume);
                    int newVolumeProgress = (int) (100f * newVolume / maxVolume);
                    showChangeVolume(newVolumeProgress);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mNeedChangePosition) {
                    mPlayer.seekTo((int) mNewPosition);
                    hideChangePosition();
                    startUpdateProgressTimer();
                    return true;
                }
                if (mNeedChangeBrightness) {
                    hideChangeBrightness();
                    return true;
                }
                if (mNeedChangeVolume) {
                    hideChangeVolume();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * 取消更新进度的计时器。
     */
    protected void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }

    public void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);
        mCenterStart.setVisibility(View.GONE);
        mBottom.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.ic_player_enlarge);
        mTop.setVisibility(View.VISIBLE);
    //    mBack.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
    }

    @Override
    public void onPlayStateChanged(int playState) {
        Log.d("onPlayStateChanged", "onPlayStateChanged" + playState);
        switch (playState) {
            case STATE_IDLE:
                break;
            case STATE_PREPARING:

                mLoading.setVisibility(View.VISIBLE);
                mLoadText.setText(getContext().getString(R.string.video_buffering));
                mError.setVisibility(View.GONE);
                mCompleted.setVisibility(View.GONE);
                mTop.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mCenterStart.setVisibility(View.GONE);

                break;
            case STATE_PREPARED:

                break;
            case STATE_PLAYING:
                if (tinyWindowAble) {
                    if (mPlayer.isFullScreen()) {
                        tvTinyWindow.setVisibility(View.GONE);
                    } else {
                        tvTinyWindow.setVisibility((View.VISIBLE));
                    }
                }
                mCenterStart.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                setTopBottomVisible(true);
                startUpdateProgressTimer();
                startDismissTopBottomTimer();
                break;
            case STATE_STOP:
            case STATE_PAUSED:
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                cancelDismissTopBottomTimer();
                break;
            case STATE_BUFFERING_PLAYING:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_pause);
                mLoadText.setText(getContext().getString(R.string.video_buffering));
                startDismissTopBottomTimer();
                break;
            case STATE_BUFFERING_PAUSED:
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.ic_player_start);
                mLoadText.setText(getContext().getString(R.string.video_buffering));
                cancelDismissTopBottomTimer();
                break;
            case STATE_ERROR:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mTop.setVisibility(View.VISIBLE);
                mError.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETED:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                //  mImage.setVisibility(View.VISIBLE);
                mCompleted.setVisibility(View.VISIBLE);
                if (isCompletedRestartAble) {
                    mReplay.setText("点击重播");
                    mReplay.setClickable(true);
                    mIVReplay.setVisibility(VISIBLE);
                } else {
                    mReplay.setClickable(false);
                    mReplay.setText("播放完成");
                    mIVReplay.setVisibility(GONE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayModeChanged(int playMode) {
        setTopBottomVisible(true);
        startDismissTopBottomTimer();
        switch (playMode) {
            case MODE_NORMAL:
              //  mBack.setVisibility(View.GONE);
                mFullScreen.setImageResource(R.drawable.ic_player_enlarge);
                mFullScreen.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                mBatteryTime.setVisibility(View.GONE);
                if (tinyWindowAble) {
                    tvTinyWindow.setVisibility(View.VISIBLE);
                    tvTinyWindow.setText("小窗");
                }
                break;
            case MODE_FULL_SCREEN:
                mBack.setVisibility(View.VISIBLE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.ic_player_shrink);
                if (clarities != null && clarities.size() > 1) {
                    mClarity.setVisibility(View.VISIBLE);
                }
                mBatteryTime.setVisibility(View.VISIBLE);
                if (tinyWindowAble) {
                    tvTinyWindow.setVisibility(View.GONE);
                }
                break;
            case MODE_TINY_WINDOW:
                mBack.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                mFullScreen.setVisibility(View.GONE);
                if (tinyWindowAble) {
                    tvTinyWindow.setVisibility(View.VISIBLE);
                    tvTinyWindow.setText("退出小窗");
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {

        if (v == tvTinyWindow) {
            if (mPlayer.isNormal()) {
                mPlayer.enterTinyWindow();
            } else if (mPlayer.isTinyWindow()) {
                mPlayer.exitTinyWindow();
            }
        }

        if (v == mCenterStart) {
            mPlayer.startPlay();

        } else if (v == mBack) {
            if (mPlayer.isFullScreen()) {
                mPlayer.exitFullScreen();
            } else if (mPlayer.isTinyWindow()) {
                mPlayer.exitTinyWindow();
            }else {
                if(onBackIconClickListener!=null){
                    onBackIconClickListener.onClick(v);
                }
            }
        } else if (v == mRestartPause) {
            if (mPlayer.isPlaying() || mPlayer.isBufferingPlaying()) {
                mPlayer.pause();
            } else if (mPlayer.isPaused() || mPlayer.isBufferingPaused()) {
                mPlayer.resume();
            }
        } else if (v == mFullScreen) {
            if (mPlayer.isNormal() || mPlayer.isTinyWindow()) {
                mPlayer.enterFullScreen();
            } else if (mPlayer.isFullScreen()) {
                mPlayer.exitFullScreen();
            }
        } else if (v == mClarity) {
            setTopBottomVisible(false); // 隐藏top、bottom

        } else if (v == mRetry) {
            mPlayer.resume();
        } else if (v == mReplay) {
            mPlayer.resume();
        } else if (v == this) {
            if (mPlayer.isPlaying()
                    || mPlayer.isPaused()
                    || mPlayer.isBufferingPlaying()
                    || mPlayer.isBufferingPaused()) {
                setTopBottomVisible(!topBottomVisible);
            }
        }
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mBottom.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        topBottomVisible = visible;
        if (visible) {
            if (!mPlayer.isPaused() && !mPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPlayer.isBufferingPaused() || mPlayer.isPaused()) {
            mPlayer.startPlay();
        }
        long d = mPlayer.getDuration();
        long sp = seekBar.getProgress();
        long position = (long) (mPlayer.getDuration() * seekBar.getProgress() / 100f);
        mPlayer.seekTo((int) position);
        startDismissTopBottomTimer();
    }


    protected void updateProgress() {
        if (mPlayer == null) {
            return;
        }
        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        int bufferPercentage = mPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeek.setProgress(progress);
        mPosition.setText(PalyerUtil.formatTime(position));
        mDuration.setText(PalyerUtil.formatTime(duration));
        // 更新时间
        mTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
        if (mProgressUpdateCall != null && mPlayer.isPlaying()) {
            mProgressUpdateCall.onProgressUpdate(position);
        }
    }

    protected void showChangePosition(long duration, int newPositionProgress) {
        mChangePositon.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mChangePositionCurrent.setText(PalyerUtil.formatTime(newPosition));
        mChangePositionProgress.setProgress(newPositionProgress);
        mSeek.setProgress(newPositionProgress);
        mPosition.setText(PalyerUtil.formatTime(newPosition));
    }

    protected void hideChangePosition() {
        mChangePositon.setVisibility(View.GONE);
    }

    protected void showChangeVolume(int newVolumeProgress) {
        mChangeVolume.setVisibility(View.VISIBLE);
        mChangeVolumeProgress.setProgress(newVolumeProgress);
    }

    protected void hideChangeVolume() {
        mChangeVolume.setVisibility(View.GONE);
    }

    protected void showChangeBrightness(int newBrightnessProgress) {
        mChangeBrightness.setVisibility(View.VISIBLE);
        mChangeBrightnessProgress.setProgress(newBrightnessProgress);
    }

    protected void hideChangeBrightness() {
        mChangeBrightness.setVisibility(View.GONE);
    }


    @Override
    public void onClarityChanged(int clarityIndex) {

    }

    @Override
    public void onClarityNotChanged() {

    }

    public ProgressUpdateCall mProgressUpdateCall = null;

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    public interface ProgressUpdateCall {
        void onProgressUpdate(long position);
    }
}
