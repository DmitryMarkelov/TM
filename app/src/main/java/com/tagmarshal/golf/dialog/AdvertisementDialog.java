package com.tagmarshal.golf.dialog;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.AdvertisementModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMTextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdvertisementDialog extends DialogFragment {

    private static final int LAYOUT = R.layout.dialog_advertisement;
    private static final String ADVERTISEMENT = "advertisement";
    private final String TAG = getClass().getName();

    @BindView(R.id.advertisement_title)
    TMTextView advertisementTitle;
    @BindView(R.id.advertisement_text)
    TMTextView advertisementText;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.circularTimerView)
    CircularTimerView circularTimerView;
    @BindView(R.id.skipButton)
    Button skipButton;
    @BindView(R.id.countDownTimerContainer)
    LinearLayout countDownTimerContainer;
    @BindView(R.id.advertisement_image)
    AppCompatImageView advertisementImage;
    @BindView(R.id.advertisement_video)
    VideoView advertisementVideo;

    private AdvertisementModel advertisementModel;
    private CountDownTimer countdownTimer;

    public static AdvertisementDialog newInstance(AdvertisementModel advertisementModel) {
        AdvertisementDialog dialog = new AdvertisementDialog();

        Bundle args = new Bundle();
        args.putSerializable(ADVERTISEMENT, advertisementModel);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, view);
        setCancelable(true);
        advertisementModel = (AdvertisementModel) getArguments().getSerializable(ADVERTISEMENT);
        return view;
    }

    @OnClick(R.id.skipButton)
    void dismissAdvert() {
        this.dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setGravity(Gravity.START);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        getDialog().getWindow().setAttributes(p);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.GONE);
        if (advertisementModel.isSound() && !advertisementModel.getType().equalsIgnoreCase("video")) {
            TMUtil.vibrateAndMakeNoiseDevice(requireContext());
        }
        if (advertisementModel.getType().equalsIgnoreCase("text")) {
            advertisementTitle.setText(advertisementModel.getTitle());
            advertisementText.setText(advertisementModel.getText());
            advertisementTitle.setVisibility(View.VISIBLE);
            advertisementText.setVisibility(View.VISIBLE);
            advertisementImage.setVisibility(View.GONE);
            advertisementVideo.setVisibility(View.GONE);

        } else if (advertisementModel.getType().equalsIgnoreCase("video")) {
            try {
                File videoFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), advertisementModel.getDownloadsUrls().get(0));
                advertisementVideo.setVideoPath(videoFile.getAbsolutePath());
                advertisementVideo.setVisibility(View.VISIBLE);
                advertisementImage.setVisibility(View.GONE);
                advertisementTitle.setVisibility(View.GONE);
                advertisementText.setVisibility(View.GONE);
                advertisementVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        if (advertisementModel.isSound()) {
                            mediaPlayer.setVolume(1.0f, 1.0f);
                        } else {
                            mediaPlayer.setVolume(0.0f, 0.0f);
                        }
                    }
                });
                advertisementVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // Handle video completion
                        dismissAllowingStateLoss();
                    }
                });
                advertisementVideo.start();
            } catch (Exception e) {
                Log.d(TAG, "Exception: ", e);
            }

        } else if (advertisementModel.getType().equalsIgnoreCase("image")) {
            try {
                File imageFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), advertisementModel.getDownloadsUrls().get(0));
                Glide.with(requireContext())
                        .load(imageFile)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(advertisementImage);
                advertisementImage.setVisibility(View.VISIBLE);
                advertisementTitle.setVisibility(View.GONE);
                advertisementText.setVisibility(View.GONE);
                advertisementVideo.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d(TAG, "Exception: ", e);
            }
        }

        if (advertisementModel.getDisplayTime() != 0) {
            countDownTimerContainer.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.GONE);
            long totalTimeMillis = advertisementModel.getDisplayTime() * 1000L;

            countdownTimer = new CountDownTimer(totalTimeMillis, 100L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int progress = (int) ((millisUntilFinished / (float) totalTimeMillis) * 100);
                    String secondsLeft = String.valueOf(millisUntilFinished / 1000);
                    circularTimerView.setProgress(progress, secondsLeft);
                }

                @Override
                public void onFinish() {
                    circularTimerView.setProgress(0, "0");
                    skipButton.setVisibility(View.VISIBLE);
                    countDownTimerContainer.setVisibility(View.GONE);
                }
            };
            countdownTimer.start();
        } else {
            countDownTimerContainer.setVisibility(View.GONE);
            if (advertisementModel.isCanExit()) {
                skipButton.setVisibility(View.VISIBLE);
            } else {
                skipButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.d(TAG, "Exception: ", e);
        }
    }
}
