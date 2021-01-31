/*
 * @ProjectName VideoGo
 * @Copyright null
 *
 * @FileName RealPlayActivity.java
 * @Description 这里对文件进行描述
 *
 * @author chenxingyf1
 * @data 2014-6-11
 *
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 *
 * @warning 这里写本文件的相关警告
 */
package ai.apdigital.ipcamera.ui.realplay;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import ai.apdigital.ipcamera.R;
import ai.apdigital.ipcamera.RootActivity;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZPTZAction;
import com.videogo.openapi.EZConstants.EZPTZCommand;
import com.videogo.openapi.EZConstants.EZRealPlayConstants;
import com.videogo.openapi.EZConstants.EZVideoLevel;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;

import ai.apdigital.ipcamera.common.ScreenOrientationHelper;
import ai.apdigital.ipcamera.ui.util.AudioPlayUtil;
import ai.apdigital.ipcamera.ui.util.DataManager;
import ai.apdigital.ipcamera.ui.util.EZUtils;
import ai.apdigital.ipcamera.ui.util.VerifyCodeInput;

import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.TitleBar;

import ai.apdigital.ipcamera.widget.WaitDialog;
import ai.apdigital.ipcamera.widget.loading.LoadingTextView;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.videogo.openapi.EZConstants.MSG_GOT_STREAM_TYPE;
import static com.videogo.openapi.EZConstants.MSG_VIDEO_SIZE_CHANGED;

public class EZRealPlayActivity extends RootActivity implements OnClickListener, SurfaceHolder.Callback, Handler.Callback, OnTouchListener, VerifyCodeInput.VerifyCodeInputListener {
    private static final String TAG = EZRealPlayActivity.class.getSimpleName();

    private static final int ANIMATION_DURING_TIME = 500;

    // UI消息
    public static final int MSG_PLAY_UI_UPDATE = 200;

    public static final int MSG_AUTO_START_PLAY = 202;

    public static final int MSG_CLOSE_PTZ_PROMPT = 203;

    public static final int MSG_HIDE_PTZ_DIRECTION = 204;

    public static final int MSG_HIDE_PAGE_ANIM = 205;

    public static final int MSG_PLAY_UI_REFRESH = 206;

    public static final int MSG_PREVIEW_START_PLAY = 207;

    public static final int MSG_SET_VEDIOMODE_SUCCESS = 105;

    public static final int MSG_SET_VEDIOMODE_FAIL = 106;

    private String mRtspUrl = null;
    private RealPlaySquareInfo mRealPlaySquareInfo = null;

    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;

    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    private Rect mRealPlayRect = null;

    private LinearLayout mRealPlayPageLy = null;
    private RelativeLayout mPortraitTitleBar = null;
    private Button mTiletRightBtn = null;
    private RelativeLayout mRealPlayPlayRl = null;

    private SurfaceView mRealPlaySv = null;
    private SurfaceHolder mRealPlaySh = null;
    private CustomTouchListener mRealPlayTouchListener = null;


    private RelativeLayout mRealPlayLoadingRl;
    private TextView mRealPlayTipTv;
    private ImageView mRealPlayPlayIv;
    private LoadingTextView mRealPlayPlayLoading;
    private LinearLayout mRealPlayPlayPrivacyLy;
    private ImageView mPageAnimIv = null;
    private AnimationDrawable mPageAnimDrawable = null;

    private RelativeLayout mRealPlayControlRl = null;
    private ImageButton mRealPlayBtn = null;
    private ImageButton mRealPlaySoundBtn = null;
    private TextView mRealPlayFlowTv = null;
    private int mControlDisplaySec = 0;

    private int mCaptureDisplaySec = 0;
    private ImageView mRealPlayRecordIv = null;
    private TextView mRealPlayRecordTv = null;

    private boolean isRecording = false;
    private String mRecordTime = null;
    private int mRecordSecond = 0;

    private HorizontalScrollView mRealPlayOperateBar = null;

    private RelativeLayout mRealPlayTalkBtnLy = null;
    private RelativeLayout mRealPlayCaptureBtnLy = null;

    private AppCompatImageView mRealPlayTalkBtn = null;
    private AppCompatImageView mRealPlayCaptureBtn = null;
    private ImageButton mRealPlayQualityBtn = null;

    private ImageView mRealPlayPtzDirectionIv = null;
    private ImageButton mRealPlayFullAnimBtn = null;
    private int[] mStartXy = new int[2];
    private int[] mEndXy = new int[2];

    private WaitDialog mWaitDialog = null;

    private RealPlayBroadcastReceiver mBroadcastReceiver = null;
    private Timer mUpdateTimer = null;
    private TimerTask mUpdateTimerTask = null;

    private ScreenOrientationHelper mScreenOrientationHelper;
    private float mZoomScale = 0;

    private boolean mIsOnTalk = false;

    private EZPlayer mEZPlayer = null;
    //    private StubPlayer mStub = new StubPlayer();
    private EZVideoLevel mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_HD;
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private long mStreamFlow = 0;

    private int mVideoWidth;
    private int mVideoHeight;
    private EZVideoLevel qualityMode = EZVideoLevel.VIDEO_LEVEL_BALANCED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        new Handler().postDelayed(() -> {
            if (mRealPlaySv != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mRealPlaySv.getWindowToken(), 0);
            }
        }, 200);

        initUI();
        //mRealPlaySv.setVisibility(View.VISIBLE);

        LogUtil.i(TAG, "onResume real play status:" + mStatus);
        if (mCameraInfo != null && mDeviceInfo != null && mDeviceInfo.getStatus() != 1) {
            if (mStatus != RealPlayStatus.STATUS_STOP) {
                stopRealPlay();
            }
            setRealPlayFailUI(getString(R.string.realplay_fail_device_not_exist));
        } else {
            if (mStatus == RealPlayStatus.STATUS_PAUSE || mStatus == RealPlayStatus.STATUS_DECRYPT) {
                // 开始播放
                startRealPlay();
            }
        }
        updateQualityBtnVisibility();
    }

    /**
     * 更新清晰切换按钮可见性
     */
    private void updateQualityBtnVisibility() {
        // 获取不到清晰度数据时，不展示清晰度
        if (mCameraInfo != null && mCameraInfo.getVideoQualityInfos() != null && mCameraInfo.getVideoQualityInfos().size() > 0) {
            mRealPlayQualityBtn.setVisibility(View.VISIBLE);
        } else {
            mRealPlayQualityBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mScreenOrientationHelper != null) {
            mScreenOrientationHelper.postOnStop();
        }

        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        hidePageAnim();

        if (mCameraInfo == null && mRtspUrl == null) {
            return;
        }
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            stopRealPlay();
            mStatus = RealPlayStatus.STATUS_PAUSE;
            setRealPlayStopUI();
        } else {
            setStopLoading();
        }
        //mRealPlaySv.setVisibility(View.INVISIBLE);

    }


    private void initData() {
        Application application = (Application) getApplication();
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        mLocalInfo = LocalInfo.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));

        mHandler = new Handler(this);

        mBroadcastReceiver = new RealPlayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver, filter);

        mRealPlaySquareInfo = new RealPlaySquareInfo();
        Intent intent = getIntent();
        if (intent != null) {
            mCameraInfo = intent.getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = intent.getParcelableExtra(IntentConsts.EXTRA_DEVICE_INFO);
            mRtspUrl = intent.getStringExtra(IntentConsts.EXTRA_RTSP_URL);
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
            }
            LogUtil.d(TAG, "rtspUrl:" + mRtspUrl);

            getRealPlaySquareInfo();
        }
    }

    private void getRealPlaySquareInfo() {
        if (TextUtils.isEmpty(mRtspUrl)) {
            return;
        }
        Uri uri = Uri.parse(mRtspUrl.replaceFirst("&", "?"));
        try {
            mRealPlaySquareInfo.mSquareId = Integer.parseInt(uri.getQueryParameter("squareid"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            mRealPlaySquareInfo.mChannelNo = Integer.parseInt(Utils.getUrlValue(mRtspUrl, "channelno=", "&"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mRealPlaySquareInfo.mCameraName = getIntent().getStringExtra("CameraName");
        try {
            mRealPlaySquareInfo.mSoundType = Integer.parseInt(uri.getQueryParameter("soundtype"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mRealPlaySquareInfo.mCoverUrl = uri.getQueryParameter("md5Serial");
        if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCoverUrl)) {
            mRealPlaySquareInfo.mCoverUrl = mLocalInfo.getServAddr() + mRealPlaySquareInfo.mCoverUrl + "_mobile.jpeg";
        }
    }

    private boolean isHandset = false;

    private class RealPlayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    mStatus = RealPlayStatus.STATUS_PAUSE;
                    setRealPlayStopUI();
                }
            }
        }
    }

    private AppCompatImageView btnBack;
    AppCompatTextView tvTitleName;

    private void initTitleBar() {

        mPortraitTitleBar = findViewById(R.id.title_bar_portrait);
        tvTitleName = findViewById(R.id.tvTitleName);
        btnBack = findViewById(R.id.btnBack);
        //mPortraitTitleBar.setBackground(getResources().getDrawable(R.drawable.common_title_back_selector));
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    setRealPlayStopUI();
                }
                finish();
            }
        });

    }

    private void initRealPlayPageLy() {
        mRealPlayPageLy = findViewById(R.id.realplay_page_ly);
        ViewTreeObserver viewTreeObserver = mRealPlayPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(() -> {
            if (mRealPlayRect == null) {
                // 获取状况栏高度
                mRealPlayRect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(mRealPlayRect);
            }
        });
    }

    private void initView() {
        setContentView(R.layout.ap_ez_realplay_page);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Typeface tf = Typeface.createFromAsset(getAssets(), getString(R.string.font));
        ((TextView)findViewById(R.id.tvTitleName)).setTypeface(tf);
        initTitleBar();
        initRealPlayPageLy();
        initLoadingUI();
        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        mRealPlaySv = (SurfaceView) findViewById(R.id.realplay_sv);
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);
        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                return mStatus == RealPlayStatus.STATUS_PLAY;
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // 出界判断 Out of bounds
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // 左移/右移出界判断 Left / right out of bounds
                        return mDeviceInfo.isSupportPTZ();
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // 上移/下移出界判断  Move up / down to judge
                        return mDeviceInfo.isSupportPTZ();
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(View v, MotionEvent e) {
                LogUtil.d(TAG, "onDoubleClick:");
                changeZoomStatus(v, e);
            }

            @Override
            public void onZoom(float scale) {
                LogUtil.d(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    startZoom(scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    stopZoom();
                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:");
            }

            /**
             * 未放大情况下，以双击点位置为坐标原点将画面放大2倍
             * 已放大情况下，取消画面放大效果
             */
            @SuppressWarnings("PointlessArithmeticExpression")
            private void changeZoomStatus(View v, MotionEvent e) {
                if (hasZoomIn) {
                    int invalid = -1;
                    mEZPlayer.setDisplayRegion(invalid, invalid, invalid, invalid);
                } else {
                    // x轴方向
                    double xOffsetRateOfAnchor = (e.getX() / (double) v.getWidth()) - 0.5;
                    int left = (int) (mVideoWidth / 4 * 1 + xOffsetRateOfAnchor * mVideoWidth);
                    int right = (int) (mVideoWidth / 4 * 3 + +xOffsetRateOfAnchor * mVideoWidth);
                    if (left < 0) { // left超出边界，需要修正
                        left = 0;
                        right = mVideoWidth / 2;
                    }
                    if (right > mVideoWidth) { // right超出边界，需要修正
                        right = mVideoWidth;
                        left = mVideoWidth / 2;
                    }
                    // y轴方向
                    double yOffsetRateOfAnchor = (e.getY() / (double) v.getHeight()) - 0.5;
                    int top = (int) (mVideoHeight / 4 * 1 + yOffsetRateOfAnchor * mVideoHeight);
                    int bottom = (int) (mVideoHeight / 4 * 3 + +yOffsetRateOfAnchor * mVideoHeight);
                    if (top < 0) { // top超出边界，需要修正
                        top = 0;
                        bottom = mVideoHeight / 2;
                    }
                    if (bottom > mVideoHeight) { // bottom超出边界，需要修正
                        bottom = mVideoHeight;
                        top = mVideoHeight / 2;
                    }
                    // 设置坐标
                    mEZPlayer.setDisplayRegion(left, top, right, bottom);
                }
                hasZoomIn = !hasZoomIn;
            }

            private boolean hasZoomIn;
        };

        findViewById(R.id.ptz_top_btn).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.ptz_bottom_btn).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.ptz_left_btn).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.ptz_right_btn).setOnTouchListener(mOnTouchListener);

        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);

        mRealPlayPtzDirectionIv = (ImageView) findViewById(R.id.realplay_ptz_direction_iv);

        mRealPlayControlRl = (RelativeLayout) findViewById(R.id.realplay_control_rl);
        mRealPlayBtn = (ImageButton) findViewById(R.id.realplay_play_btn);
        mRealPlaySoundBtn = (ImageButton) findViewById(R.id.realplay_sound_btn);
        mRealPlayFlowTv = (TextView) findViewById(R.id.realplay_flow_tv);
        mRealPlayFlowTv.setText("0k/s");

        mRealPlayQualityBtn = (ImageButton) findViewById(R.id.realplay_quality_btn);

        // 全屏按钮 Full screen button
        ImageButton mFullscreenButton = (ImageButton) findViewById(R.id.fullscreen_button);

        if (mRtspUrl == null) {
            initOperateBarUI(false);

            initFullOperateBarUI();
            mRealPlayOperateBar.setVisibility(View.VISIBLE);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //mj 2015/11/01 realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
            mRealPlayPlayRl.setBackgroundColor(getResources().getColor(R.color.common_bg));
        }

        setRealPlaySvLayout();
        initCaptureUI();
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, /*//mFullscreenFullButton*/null);

        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDialog.setCancelable(false);
    }

    public void startDrag(int direction, float distance, float rate) {
    }

    public void stopDrag(boolean control) {
    }

    private void startZoom(float scale) {
        if (mEZPlayer == null) {
            return;
        }

        hideControlRlAndFullOperateBar(false);
        boolean preZoomIn = mZoomScale > 1.01;
        boolean zoomIn = scale > 1.01;
        if (mZoomScale != 0 && preZoomIn != zoomIn) {
            LogUtil.d(TAG, "startZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
        if (scale != 0 && mZoomScale == 0) {
            mZoomScale = scale;
            LogUtil.d(TAG, "startZoom start:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_START);
        }
    }

    private void stopZoom() {
        if (mEZPlayer == null) {
            return;
        }
        if (mZoomScale != 0) {
            LogUtil.d(TAG, "stopZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
    }

    private void setPtzDirectionIv(int command) {
        setPtzDirectionIv(command, 0);
    }

    private void setPtzDirectionIv(int command, int errorCode) {
        if (command != -1 && errorCode == 0) {
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            switch (command) {
                case RealPlayStatus.PTZ_LEFT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.left_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_RIGHT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.right_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_UP:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.up_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_DOWN:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.down_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else if (errorCode != 0) {
            LayoutParams svParams = (LayoutParams) mRealPlaySv.getLayoutParams();
            LayoutParams params;
            switch (errorCode) {
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_left_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_right_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:
                    params = new LayoutParams(svParams.width, LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_top_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:
                    params = new LayoutParams(svParams.width, LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_bottom_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        }
    }

    private void initUI() {
        mPageAnimDrawable = null;
        mRealPlaySoundBtn.setVisibility(View.VISIBLE);

        if (mCameraInfo != null) {
            tvTitleName.setText(mCameraInfo.getCameraName());
            setCameraInfoTiletRightBtn();
            if (mLocalInfo.isSoundOpen()) {
                mRealPlaySoundBtn.setImageResource(R.drawable.ap_ic_sound);
            } else {
                mRealPlaySoundBtn.setImageResource(R.drawable.ap_ic_sound_disable);
            }
            mRealPlayCaptureBtnLy.setVisibility(View.VISIBLE);
            updateUI();
        } else if (mRtspUrl != null) {
            if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCameraName)) {
                tvTitleName.setText(mRealPlaySquareInfo.mCameraName);
            }
            mRealPlaySoundBtn.setVisibility(View.GONE);
        }
        updateQualityBtnVisibility();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateOperatorUI();
        }
    }

    private void setCameraInfoTiletRightBtn() {
        if (mTiletRightBtn != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() == 1) {
                mTiletRightBtn.setVisibility(View.VISIBLE);
            } else {
                mTiletRightBtn.setVisibility(View.GONE);
            }
        }
    }

    private void initOperateBarUI(boolean bigScreen) {
        bigScreen = false;
        if (mRealPlayOperateBar != null) {
            mRealPlayOperateBar.setVisibility(View.GONE);
            mRealPlayOperateBar = null;
        }
        /*if (bigScreen) {

        } else {*/
        mRealPlayOperateBar = (HorizontalScrollView) findViewById(R.id.ezopen_realplay_operate_bar);
        mRealPlayTalkBtnLy = (RelativeLayout) findViewById(R.id.realplay_talk_btn_ly);
        mRealPlayCaptureBtnLy = (RelativeLayout) findViewById(R.id.realplay_previously_btn_ly);
        mRealPlayTalkBtn =  findViewById(R.id.realplay_talk_btn);
        mRealPlayCaptureBtn =  findViewById(R.id.realplay_previously_btn);
        /*}*/
        mRealPlayOperateBar.setVisibility(View.VISIBLE);
    }

    private void setBigScreenOperateBtnLayout() {
    }

    private void initFullOperateBarUI() {

        mRealPlayFullAnimBtn = (ImageButton) findViewById(R.id.realplay_full_anim_btn);
    }

    private void startFullBtnAnim(final View animView, final int[] startXy, final int[] endXy, final AnimationListener animationListener) {
        animView.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(startXy[0], endXy[0], startXy[1], endXy[1]);
        anim.setAnimationListener(animationListener);
        anim.setDuration(ANIMATION_DURING_TIME);
        animView.startAnimation(anim);
    }

    private void setVideoLevel() {
        if (mCameraInfo == null || mEZPlayer == null || mDeviceInfo == null) {
            return;
        }
        mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);
        /**************
         * 本地数据保存 需要更新之前获取到的设备列表信息，开发者自己设置
         *
         * Local data saved need to be updated before the obtained device list information, the developer's own settings
         * *********************/
        mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;

        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    //TODO Orientation
    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreen(false);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.VISIBLE);
            if (mRtspUrl == null) {
                mRealPlayOperateBar.setVisibility(View.VISIBLE);
            }
        } else {
            fullScreen(true);
            mPortraitTitleBar.setVisibility(View.GONE);
            // hide the
            if (mRtspUrl == null) {
                mRealPlayOperateBar.setVisibility(View.GONE);
            }
        }

        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void onOrientationChanged() {
        setRealPlaySvLayout();
        updateOperatorUI();
        updateCaptureUI();
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder , int,
     * int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder )
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
        if (mStatus == RealPlayStatus.STATUS_INIT) {
            // 开始播放
            startRealPlay();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view. SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }


    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.realplay_play_btn || id == R.id.realplay_play_iv) {
            if (mStatus != RealPlayStatus.STATUS_STOP) {
                stopRealPlay();
                setRealPlayStopUI();
            } else {
                startRealPlay();
            }
        } else if (id == R.id.realplay_previously_btn) {
            onCapturePicBtnClick();
        } else if (id == R.id.realplay_talk_btn) {
            onVoiceTalkManagement();
        } else if (id == R.id.realplay_quality_btn) {
            updateQuality();
        } else if (id == R.id.realplay_sound_btn) {
            onSoundBtnClick();
        }
    }

    private void updateQuality() {
        if (qualityMode == EZVideoLevel.VIDEO_LEVEL_BALANCED) {
            mRealPlayQualityBtn.setImageResource(R.drawable.ap_ic_quality);
            qualityMode = EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR;
        } else {
            mRealPlayQualityBtn.setImageResource(R.drawable.ap_ic_quality_disable);
            qualityMode = EZVideoLevel.VIDEO_LEVEL_BALANCED;
        }
        setQualityMode(qualityMode);
    }

    private void setFullPtzStopUI(boolean startAnim) {
        if (startAnim) {
            mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.yuntai_pressed);
            startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                }
            });
        }
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
    }

    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);
            mRealPlaySoundBtn.setImageResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
        } else {
            mLocalInfo.setSoundOpen(true);
            mRealPlaySoundBtn.setImageResource(R.drawable.btn_ap_sound_selector);
        }

        setRealPlaySound();
    }

    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mRtspUrl == null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            } else {
                if (mRealPlaySquareInfo.mSoundType == 0) {
                    mEZPlayer.closeSound();
                } else {
                    mEZPlayer.openSound();
                }
            }
        }
    }

    private void startVoiceTalk() {
        LogUtil.d(TAG, "startVoiceTalk");
        if (mEZPlayer == null) {
            LogUtil.d(TAG, "EZPlaer is null");
            return;
        }
        if (mCameraInfo == null) {
            return;
        }
        mIsOnTalk = true;

        updateOrientation();
        Utils.showToast(this, R.string.start_voice_talk);
        if (mEZPlayer != null) {
            mEZPlayer.closeSound();
        }
        mEZPlayer.startVoiceTalk();
        onStartTalk();
    }

    private void stopVoiceTalk() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "stopVoiceTalk");
        mEZPlayer.stopVoiceTalk();
        onStopTalk();
    }

    private void ptzOption(final EZPTZCommand command, final EZPTZAction action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ptz_result = false;
                try {
                    ptz_result = getOpenSDK().controlPTZ(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), command, action, EZConstants.PTZ_SPEED_DEFAULT);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                LogUtil.i(TAG, "controlPTZ ptzCtrl result: " + ptz_result);
            }
        }).start();
    }

    private final OnTouchListener mOnTouchListener = (view, motionevent) -> {
        int action = motionevent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int id = view.getId();
                if (id == R.id.ptz_top_btn) {//mPtzControlLy.setBackgroundResource(R.drawable.ptz_up_sel);
                    setPtzDirectionIv(RealPlayStatus.PTZ_UP);
                    ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTART);
                } else if (id == R.id.ptz_bottom_btn) {//mPtzControlLy.setBackgroundResource(R.drawable.ptz_bottom_sel);
                    setPtzDirectionIv(RealPlayStatus.PTZ_DOWN);
                    ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTART);
                } else if (id == R.id.ptz_left_btn) {//mPtzControlLy.setBackgroundResource(R.drawable.ptz_left_sel);
                    setPtzDirectionIv(RealPlayStatus.PTZ_LEFT);
                    ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTART);
                } else if (id == R.id.ptz_right_btn) {//mPtzControlLy.setBackgroundResource(R.drawable.ptz_right_sel);
                    setPtzDirectionIv(RealPlayStatus.PTZ_RIGHT);
                    ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTART);
                }
                break;
            case MotionEvent.ACTION_UP:
                int viewId = view.getId();
                if (viewId == R.id.ptz_top_btn) {
                    ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTOP);
                } else if (viewId == R.id.ptz_bottom_btn) {
                    ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTOP);
                } else if (viewId == R.id.ptz_left_btn) {
                    ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTOP);
                } else if (viewId == R.id.ptz_right_btn) {
                    ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTOP);
                }
                break;
            default:
                break;
        }
        return false;
    };


    boolean isVoiceTalk = false;

    private void onVoiceTalkManagement() {
        isVoiceTalk = !isVoiceTalk;
        if (isVoiceTalk) {
            startVoiceTalk();
        } else {
            stopVoiceTalk();
        }
    }

    public void onStopTalk() {
        mRealPlayTalkBtn.setImageResource(R.drawable.ap_ic_voice_disable);
        mEZPlayer.setVoiceTalkStatus(false);
        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            if (mEZPlayer != null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            }
        }
    }

    public void onStartTalk() {
        mRealPlayTalkBtn.setImageResource(R.drawable.ap_ic_voice_selected);
        mEZPlayer.setVoiceTalkStatus(true);
    }

    private void setQualityMode(final EZVideoLevel mode) {
        if (!ConnectionDetector.isNetworkAvailable(EZRealPlayActivity.this)) {
            Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {
            mWaitDialog.setWaitText(this.getString(R.string.setting_video_level));
            mWaitDialog.show();

            Thread thr = new Thread(() -> {
                try {
                    // need to modify by yudan at 08-11
                    getOpenSDK().setVideoLevel(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), mode.getVideoLevel());
                    mCurrentQulityMode = mode;
                    Message msg = Message.obtain();
                    msg.what = MSG_SET_VEDIOMODE_SUCCESS;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode success");
                } catch (BaseException e) {
                    mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_FLUNET;
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = MSG_SET_VEDIOMODE_FAIL;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode fail");
                }

            }) {
            };
            thr.start();
        }
    }

    private void onCapturePicBtnClick() {

        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            //Prompt SD card is not available
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            //Prompt for insufficient memory
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_capture_fail_for_memory);
            return;
        }

        if (mEZPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();

            Thread thr = new Thread() {
                @Override
                public void run() {
                    Bitmap bmp = mEZPlayer.capturePicture();
                    if (bmp != null) {
                        try {
                            mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

                            // final String strCaptureFile = getExternalCacheDir().getAbsolutePath() + "/Captures/" + System.currentTimeMillis() + ".jpg";
                            final String strCaptureFile = "/storage/emulated/0/Pictures/Captures/" + System.currentTimeMillis() + ".jpg";
                            LogUtil.e(TAG, "captured picture file path is " + strCaptureFile);

                            if (TextUtils.isEmpty(strCaptureFile)) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                            EZUtils.saveCapturePicture(strCaptureFile, bmp);
                            MediaScanner mMediaScanner = new MediaScanner(EZRealPlayActivity.this);
                            mMediaScanner.scanFile(strCaptureFile, "jpg");
                            runOnUiThread(() -> Toast.makeText(EZRealPlayActivity.this, getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show());
                        } catch (InnerException e) {
                            e.printStackTrace();
                        } finally {
                            if (bmp != null) {
                                bmp.recycle();
                            }
                        }
                    } else {
                        showToast("抓图失败, 检查是否开启了硬件解码");
                    }
                    super.run();
                }
            };
            thr.start();
        }
    }

    private void onRealPlaySvClick() {
        if (mCameraInfo != null && mEZPlayer != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() != 1) {
                return;
            }
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRealPlayControlRlVisibility();
            } else {
                setRealPlayFullOperateBarVisibility();
            }
        } else if (mRtspUrl != null) {
            setRealPlayControlRlVisibility();
        }
    }

    private void setRealPlayControlRlVisibility() {
        mControlDisplaySec = 0;

    }

    private void setRealPlayFullOperateBarVisibility() {

        mControlDisplaySec = 0;
    }

    private void startRealPlay() {
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        //Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
            //Prompt not to connect to the network
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        if (mCameraInfo != null) {
            mEZPlayer = getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
            if (mEZPlayer == null) return;
            if (mDeviceInfo == null) {
                return;
            }

            mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);

            mEZPlayer.startRealPlay();
        } else if (mRtspUrl != null) {
            mEZPlayer = getOpenSDK().createPlayerWithUrl(mRtspUrl);
            if (mEZPlayer == null) return;
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);

            // 不建议使用，会导致抓图功能失效
//            mEZPlayer.setHardDecode(true);

            mEZPlayer.startRealPlay();
        }
        updateLoadingProgress(0);
    }

    private void stopRealPlay() {
        LogUtil.d(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;

        stopUpdateTimer();
        if (mEZPlayer != null) {
            mEZPlayer.stopRealPlay();
        }
        mStreamFlow = 0;
    }

    private void setRealPlayLoadingUI() {
        setStartLoading();
        mRealPlayBtn.setImageResource(R.drawable.ap_ic_play_selected);

        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);
        }

        showControlRlAndFullOperateBar();
    }

    private void showControlRlAndFullOperateBar() {
        mControlDisplaySec = 0;
    }

    private void setRealPlayStopUI() {
        stopUpdateTimer();
        updateOrientation();
        setRealPlaySvLayout();
        setStopLoading();
        hideControlRlAndFullOperateBar(true);
        mRealPlayBtn.setImageResource(R.drawable.btn_ap_play_selector);
        if (mCameraInfo != null && mDeviceInfo != null) {
            setFullPtzStopUI(false);
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);
        }
    }

    private void setRealPlayFailUI(String errorStr) {
        showType();

        stopUpdateTimer();
        updateOrientation();

        {
            setLoadingFail(errorStr);
        }
        mRealPlayBtn.setImageResource(R.drawable.btn_ap_play_selector);
        hideControlRlAndFullOperateBar(true);

        if (mCameraInfo != null && mDeviceInfo != null) {
            setFullPtzStopUI(false);

            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1 && (mEZPlayer == null));
        }
    }

    private void setRealPlaySuccessUI() {
        showType();

        updateOrientation();
        setLoadingSuccess();
        //mRealPlayFlowTv.setVisibility(View.VISIBLE);
        mRealPlayBtn.setImageResource(R.drawable.ap_ic_stop);

        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(true);
            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);
        }
        startUpdateTimer();
    }

    private void checkRealPlayFlow() {
        if ((mEZPlayer != null && mRealPlayFlowTv.getVisibility() == View.VISIBLE)) {
            // 更新流量数据
            //Update traffic data
            long streamFlow = mEZPlayer.getStreamFlow();
            updateRealPlayFlowTv(streamFlow);
        }
    }

    private void updateRealPlayFlowTv(long streamFlow) {
        long streamFlowUnit = streamFlow - mStreamFlow;
        if (streamFlowUnit < 0) streamFlowUnit = 0;
        float fKBUnit = (float) streamFlowUnit / (float) Constant.KB;
        String descUnit = String.format("%.2f k/s ", fKBUnit);
        //mRealPlayFlowTv.setText(descUnit);
        mStreamFlow = streamFlow;
    }


    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            LogUtil.d(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }
        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else mScreenOrientationHelper.disableSensorOrientation();
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            LogUtil.d(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
        if (this.isFinishing()) {
            return false;
        }
        LogUtil.i(TAG, "handleMessage:" + msg.what);
        switch (msg.what) {
            case MSG_VIDEO_SIZE_CHANGED:
                LogUtil.d(TAG, "MSG_VIDEO_SIZE_CHANGED");
                try {
                    String temp = (String) msg.obj;
                    String[] strings = temp.split(":");
                    mVideoWidth = Integer.parseInt(strings[0]);
                    mVideoHeight = Integer.parseInt(strings[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                updateLoadingProgress(20);
                handleGetCameraInfoSuccess();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                updateLoadingProgress(40);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                updateLoadingProgress(60);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                showDecodeType();
                handlePlaySuccess(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                handlePlayFail(msg.obj);
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVideoModeSuccess();
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVideoModeFail(msg.arg1);
                break;
            case EZRealPlayConstants.MSG_PTZ_SET_FAIL:
                handlePtzControlFail(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:
                //TODO VoiceTalk Success
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:
                stopVoiceTalk();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:
                ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                handleVoiceTalkFailed(errorInfo);
                break;
            case MSG_PLAY_UI_UPDATE:
                updateRealPlayUI();
                break;
            case MSG_AUTO_START_PLAY:
                startRealPlay();
                break;
            case MSG_HIDE_PTZ_DIRECTION:
                handleHidePtzDirection(msg);
                break;
            case MSG_HIDE_PAGE_ANIM:
                hidePageAnim();
                break;
            case MSG_PLAY_UI_REFRESH:
                initUI();
                break;
            case MSG_PREVIEW_START_PLAY:
                mPageAnimIv.setVisibility(View.GONE);
                mStatus = RealPlayStatus.STATUS_INIT;
                startRealPlay();
                break;
            case MSG_GOT_STREAM_TYPE:
                showStreamType(msg.arg1);
                break;
            default:
                // do nothing
                break;
        }
        return false;
    }

    private void showDecodeType() {
        /*  if (mEZPlayer != null && mEZPlayer.getPlayPort() >= 0) {
          int intDecodeType = Player.getInstance().getDecoderType(mEZPlayer.getPlayPort());
            String strDecodeType;
            if (intDecodeType == 1) {
                strDecodeType = "hard";
            } else {
                strDecodeType = "soft";
            }
            String streamTypeMsg = "decode type: " + strDecodeType;
            TextView streamTypeTv = (TextView) findViewById(R.id.tv_decode_type);
            if (streamTypeTv != null) {
                streamTypeTv.setText(streamTypeMsg);
                streamTypeTv.setVisibility(View.VISIBLE);
            }
        }*/
    }

    private void showStreamType(int streamType) {
       /* String streamTypeMsg = getApplicationContext().getString(R.string.stream_type) + changeIntTypeToStringType(streamType);
        TextView streamTypeTv = (TextView) findViewById(R.id.tv_stream_type);
        if (streamTypeTv != null) {
            streamTypeTv.setText(streamTypeMsg);
            streamTypeTv.setVisibility(View.VISIBLE);
        }*/
    }

    private String changeIntTypeToStringType(int streamType) {
        String strStreamType;
        switch (streamType) {
            /*
              取流方式切换到私有流媒体转发模式
             */
            case 0:
                strStreamType = "private_stream";
                break;
            /*
              取流方式切换到P2P模式
             */
            case 1:
                strStreamType = "p2p";
                break;
            /*
              取流方式切换到内网直连模式
             */
            case 2:
                strStreamType = "direct_inner";
                break;
            /*
              取流方式切换到外网直连模式
             */
            case 3:
                strStreamType = "direct_outer";
                break;
            /*
              取流方式切换到云存储回放
             */
            case 4:
                strStreamType = "cloud_playback";
                break;
            /*
              取流方式切换到云存储留言
             */
            case 5:
                strStreamType = "cloud_leave_msg";
                break;
            /*
              取流方式切换到反向直连模式
             */
            case 6:
                strStreamType = "direct_reverse";
                break;
            /*
              取流方式切换到HCNETSDK
             */
            case 7:
                strStreamType = "hcnetsdk";
                break;
            default:
                strStreamType = "unknown(" + streamType + ")";
                break;
        }
        return strStreamType;
    }

    private void handleHidePtzDirection(Message msg) {
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        if (msg.arg1 > 2) {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(msg.arg1 == 1 ? View.GONE : View.VISIBLE);
            Message message = new Message();
            message.what = MSG_HIDE_PTZ_DIRECTION;
            message.arg1 = msg.arg1 + 1;
            mHandler.sendMessageDelayed(message, 500);
        }
    }

    private void handlePtzControlFail(Message msg) {
        LogUtil.d(TAG, "handlePtzControlFail:" + msg.arg1);
        switch (msg.arg1) {
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_CALLING_PRESET_FAILED:
                // 正在调用预置点，键控动作无效
                //Calling preset point, name action is invalid
                Utils.showToast(EZRealPlayActivity.this, R.string.camera_lens_too_busy, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_PRESETING_FAILE:// 当前正在调用预置点
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_is_preseting, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_SOUND_LACALIZATION_FAILED:
                // 当前正在声源定位
                //Is currently locating at sound source
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_CRUISE_TRACK_FAILED:
                // 键控动作超时(当前正在轨迹巡航)
                //Key action timeout (currently tracing)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_control_timeout_cruise_track_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_INVALID_POSITION_FAILED:
                // 当前预置点信息无效
                //The current preset information is invalid
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_invalid_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_CURRENT_POSITION_FAILED:
                // 该预置点已是当前位置
                //The preset point is the current position
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_current_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_SOUND_LOCALIZATION_FAILED:
                // 设备正在响应本次声源定位
                //The device is responding to this sound source location
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_sound_localization_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_OPENING_PRIVACY_FAILED:// 当前正在开启隐私遮蔽 Is currently opening privacy masking
            case ErrorCode.ERROR_CAS_PTZ_CLOSING_PRIVACY_FAILED:// 当前正在关闭隐私遮蔽   The privacy mask is currently being turned off
            case ErrorCode.ERROR_CAS_PTZ_MIRRORING_FAILED:// 设备正在镜像操作（设备镜像要几秒钟，防止频繁镜像操作）The device is mirroring (the device mirroring takes a few seconds to prevent frequent mirroring)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_too_frequently, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROLING_FAILED:// 设备正在键控动作（上下左右）(一个客户端在上下左右控制，另外一个在开其它东西) The device is keying action (up and down left and right) (a client in the upper and lower left and right control, the other one in the open other things)
                break;
            case ErrorCode.ERROR_CAS_PTZ_FAILED:// 云台当前操作失败 PTZ current operation failed
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_EXCEED_MAXNUM_FAILED:// 当前预置点超过最大个数 The current preset exceeds the maximum number
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_exceed_maxnum_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRIVACYING_FAILED:// 设备处于隐私遮蔽状态（关闭了镜头，再去操作云台相关）The device is in a privacy state (close the lens, and then operate the PTZ related)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_privacying_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_TTSING_FAILED:// 设备处于语音对讲状态(区别以前的语音对讲错误码，云台单独列一个）Equipment in the voice intercom state (the difference between the previous voice intercom error code, PTZ separate one)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_mirroring_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:// 设备云台旋转到达上限位 The PTZ rotation reaches the upper limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:// 设备云台旋转到达下限位 The PTZ rotation reaches the lower limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:// 设备云台旋转到达左限位  The PTZ rotation reaches the left limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:// 设备云台旋转到达右限位 The PTZ rotation reaches the right limit
                setPtzDirectionIv(-1, msg.arg1);
                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_failed, msg.arg1);
                break;
        }
    }

    private void hidePageAnim() {
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        if (mPageAnimDrawable != null) {
            if (mPageAnimDrawable.isRunning()) {
                mPageAnimDrawable.stop();
            }
            mPageAnimDrawable = null;
            mPageAnimIv.setBackground(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
        if (mPageAnimIv != null) {
            mPageAnimIv.setBackground(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
    }

    private void setRealPlayTalkUI() {
        if (mEZPlayer != null && mDeviceInfo != null && (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)) {
            mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
            mRealPlayTalkBtn.setEnabled(mCameraInfo != null && mDeviceInfo.getStatus() == 1);
        } else {
            mRealPlayTalkBtnLy.setVisibility(View.GONE);
        }
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
    }

    private void updatePermissionUI() {
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        setRealPlayTalkUI();
        setVideoLevel();
        updatePermissionUI();
    }

    private void handleGetCameraInfoSuccess() {
        LogUtil.i(TAG, "handleGetCameraInfoSuccess");
        updateUI();

    }

    private void handleVoiceTalkFailed(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "Talk Back failed. " + errorInfo.toString());
        stopVoiceTalk();
        switch (errorInfo.errorCode) {
            case ErrorCode.ERROR_TRANSF_DEVICE_TALKING:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_ison);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_PRIVACYON:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_privacy);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_TTS_MSG_REQ_TIMEOUT:
            case ErrorCode.ERROR_TTS_MSG_SVR_HANDLE_TIMEOUT:
            case ErrorCode.ERROR_TTS_WAIT_TIMEOUT:
            case ErrorCode.ERROR_TTS_HNADLE_TIMEOUT:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_request_timeout, errorInfo.errorCode);
                break;
            case ErrorCode.ERROR_CAS_AUDIO_SOCKET_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_RECV_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_SEND_ERROR:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_network_exception, errorInfo.errorCode);
                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail, errorInfo.errorCode);
                break;
        }
    }

    private void handleSetVideoModeSuccess() {
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            // 停止对讲
            // 停止播放 Stop play
            stopRealPlay();
            SystemClock.sleep(500);
            // 开始播放 start play
            startRealPlay();
        }
    }

    private void handleSetVideoModeFail(int errorCode) {
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_vediomode_fail, errorCode);
    }

    private void hideControlRlAndFullOperateBar(boolean excludeLandscapeTitle) {
    }

    private void updateRealPlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            hideControlRlAndFullOperateBar(false);
        }
        checkRealPlayFlow();
        updateCaptureUI();

        if (isRecording) {
            updateRecordTime();
        }
    }

    private void initCaptureUI() {
        mCaptureDisplaySec = 0;
    }

    // 更新抓图/录像显示UI
    //Update the capture / video display UI
    private void updateCaptureUI() {
        if (mCaptureDisplaySec >= 4) {
            initCaptureUI();
        }
    }

    private void updateRecordTime() {
        if (mRealPlayRecordIv.getVisibility() == View.VISIBLE) {
            mRealPlayRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRealPlayRecordIv.setVisibility(View.VISIBLE);
        }

        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;

        String recordTime = String.format("%02d:%02d", minitue, second);
        mRealPlayRecordTv.setText(recordTime);
    }

    private void handlePlaySuccess(Message msg) {
        LogUtil.d(TAG, "handlePlaySuccess");
        mStatus = RealPlayStatus.STATUS_PLAY;

        // 声音处理  Sound processing
        setRealPlaySound();

        // temp solution for OPENSDK-92
        // Android 预览3Q10的时候切到流畅之后 视频播放窗口变大了
        //        if (description.arg1 != 0) {
        //            mRealRatio = (float) description.arg2 / description.arg1;
        //        } else {
        //            mRealRatio = Constant.LIVE_VIEW_RATIO;
        //        }
        mRealRatio = Constant.LIVE_VIEW_RATIO;

        boolean bSupport = true;//(float) mLocalInfo.getScreenHeight() / mLocalInfo.getScreenWidth() >= BIG_SCREEN_RATIO;
        if (bSupport) {
            initOperateBarUI(mRealRatio <= Constant.LIVE_VIEW_RATIO);
            initUI();
            if (mRealRatio <= Constant.LIVE_VIEW_RATIO) {
                setBigScreenOperateBtnLayout();
            }
        }
        setRealPlaySvLayout();
        setRealPlaySuccessUI();
        mRealPlayTalkBtn.setEnabled(mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport);
        if (mEZPlayer != null) {
            mStreamFlow = mEZPlayer.getStreamFlow();
        }
    }

    private void setRealPlaySvLayout() {

        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo.getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation, mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO), screenWidth, screenHeight);
        LayoutParams svLp = new LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);
        playWindowVg.setLayoutParams(svLp);

        if (mRtspUrl != null) {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);

    }

    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            LogUtil.d(TAG, "handlePlayFail:" + errorInfo.errorCode);
        }


        hidePageAnim();

        stopRealPlay();

        updateRealPlayFailUI(errorCode);
    }

    private void updateRealPlayFailUI(int errorCode) {
        String txt = null;
        LogUtil.i(TAG, "updateRealPlayFailUI: errorCode:" + errorCode);
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                //
                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                if (mCameraInfo != null) {
                    mCameraInfo.setIsShared(0);
                }
                txt = getString(R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_INNER_STREAM_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_LOGIN, this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_HARDWARE, this, null);
//                SecureValidate.secureValidateDialog(this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_TRANSF_TERMINAL_BINDING:
                txt = "请在萤石客户端关闭终端绑定 " + "Please close the terminal binding on the fluorite client";
                break;
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), "CHVNIB");
                VerifyCodeInput.VerifyCodeInputDialog(this, this).show();
            }
            break;
            case ErrorCode.ERROR_EXTRA_SQUARE_NO_SHARING:
            default:
                txt = Utils.getErrorTip(this, R.string.realplay_play_fail, errorCode);
                break;
        }

        if (!TextUtils.isEmpty(txt)) {
            setRealPlayFailUI(txt);
        } else {
            setRealPlayStopUI();
        }
    }


    private void startUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mRealPlayControlRl.getVisibility() == View.VISIBLE && mControlDisplaySec < 5) {
                    mControlDisplaySec++;
                }
                if (mEZPlayer != null && isRecording) {

                    Calendar OSDTime = mEZPlayer.getOSDTime();
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!TextUtils.equals(playtime, mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_PLAY_UI_UPDATE);
                }
            }
        };
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    private void stopUpdateTimer() {
        mCaptureDisplaySec = 4;
        updateCaptureUI();
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    private void showType() {
        /*if (Config.LOGGING && mEZPlayer != null) {
            Utils.showLog(EZRealPlayActivity.this, "cost: " + (mStopTime - mStartTime) + " ms");
        }*/
    }

    private void initLoadingUI() {
        mRealPlayLoadingRl = (RelativeLayout) findViewById(R.id.realplay_loading_rl);
        mRealPlayTipTv = (TextView) findViewById(R.id.realplay_tip_tv);
        mRealPlayPlayIv = (ImageView) findViewById(R.id.realplay_play_iv);
        mRealPlayPlayLoading = (LoadingTextView) findViewById(R.id.realplay_loading);
        mRealPlayPlayPrivacyLy = (LinearLayout) findViewById(R.id.realplay_privacy_ly);
        mRealPlayPlayIv.setOnClickListener(this);
        mPageAnimIv = (ImageView) findViewById(R.id.realplay_page_anim_iv);
    }

    private void updateLoadingProgress(final int progress) {
        mRealPlayPlayLoading.setTag(Integer.valueOf(progress));
        mRealPlayPlayLoading.setText(progress + "%");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlayPlayLoading != null) {
                    Integer tag = (Integer) mRealPlayPlayLoading.getTag();
                    if (tag != null && tag.intValue() == progress) {
                        Random r = new Random();
                        mRealPlayPlayLoading.setText((progress + r.nextInt(20)) + "%");
                    }
                }
            }

        }, 500);
    }

    private void setStartLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.VISIBLE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setStopLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.VISIBLE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setLoadingFail(String errorStr) {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setText(errorStr);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    private void setLoadingSuccess() {
        mRealPlayLoadingRl.setVisibility(View.INVISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.d(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), verifyCode);
        if (mEZPlayer != null) {
            startRealPlay();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        mHandler = null;

        if (mBroadcastReceiver != null) {
            // 取消锁屏广播的注册 Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        mScreenOrientationHelper = null;
    }

    private void exit() {
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            stopRealPlay();
            setRealPlayStopUI();
        }
        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        if (mBroadcastReceiver != null) {
            // Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        exit();
    }

}
