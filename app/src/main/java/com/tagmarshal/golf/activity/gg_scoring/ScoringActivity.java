package com.tagmarshal.golf.activity.gg_scoring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.activity.main.MainActivity;
import com.tagmarshal.golf.callback.GolfCallback;
import com.tagmarshal.golf.dialog.GolfAlertDialog;
import com.tagmarshal.golf.dialog.PleaseWaitDialog;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.GolfGeniusModel;
import com.tagmarshal.golf.rest.model.SaveScoreModel;
import com.tagmarshal.golf.view.TMButton;
import com.tagmarshal.golf.view.TMTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ScoringActivity extends AppCompatActivity implements View.OnClickListener, ScoringContract.View {

    private final ArrayList<Integer> scores = new ArrayList<>();
    private final ArrayList<Boolean> noScores = new ArrayList<>();
    @BindView(R.id.tvHole)
    TMTextView mHoleNumber;
    @BindView(R.id.tvNextHole)
    TextView mNextHole;
    @BindView(R.id.tvPrevHole)
    TextView mPrevHole;
    @BindView(R.id.tvDesc)
    TextView mHoleDesc;
    @BindView(R.id.ivLeft)
    ImageView mLeft;
    @BindView(R.id.ivRight)
    ImageView mRight;
    @BindView(R.id.btnPlayer1)
    TMButton mPlayerOne;
    @BindView(R.id.btnPlayer2)
    TMButton mPlayerTwo;
    @BindView(R.id.btnPlayer3)
    TMButton mPlayerThree;
    @BindView(R.id.btnPlayer4)
    TMButton mPlayerFour;
    @BindView(R.id.tvPlayer1)
    TextView mPlayerOneText;
    @BindView(R.id.tvPlayer2)
    TextView mPlayerTwoText;
    @BindView(R.id.tvPlayer3)
    TextView mPlayerThreeText;
    @BindView(R.id.tvPlayer4)
    TextView mPlayerFourText;
    @BindView(R.id.btn0)
    TMButton mZero;
    @BindView(R.id.btn1)
    TMButton mOne;
    @BindView(R.id.btn2)
    TMButton mTwo;
    @BindView(R.id.btn3)
    TMButton mThree;
    @BindView(R.id.btn4)
    TMButton mFour;
    @BindView(R.id.btn5)
    TMButton mFive;
    @BindView(R.id.btn6)
    TMButton mSix;
    @BindView(R.id.btn7)
    TMButton mSeven;
    @BindView(R.id.btn8)
    TMButton mEight;
    @BindView(R.id.btn9)
    TMButton mNine;
    @BindView(R.id.btnClear)
    TMButton mClear;
    @BindView(R.id.btnNoScore)
    TMButton mNoScore;
    @BindView(R.id.btnUndo)
    TMButton mUndo;
    @BindView(R.id.btnSave)
    TMButton mSaveScores;
    @BindView(R.id.tvAlert)
    TextView mAlert;
    @BindView(R.id.ivSignal)
    ImageView mSignalIcon;
    Unbinder unbinder;
    GolfAlertDialog dialog;
    private GolfGeniusModel golfGeniusModel;
    private int holeIndex = 0;
    private int playerIndex = -1;
    private TMButton selectedButton;
    private boolean scoreKeyboardActive = false;
    private GolfGeniusModel.Score score;
    private ScoringPresenter presenter;
    private PleaseWaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);

        waitDialog = new PleaseWaitDialog(this);

        unbinder = ButterKnife.bind(this);

        presenter = new ScoringPresenter(this);

        presenter.startInactiveStateTimer();

        golfGeniusModel = PreferenceManager.getInstance().getGolfGenius();

        if (golfGeniusModel == null) {
            finish();
            return;
        }

        Intent intent = getIntent();
        String passedHole = intent.getStringExtra("hole");
        if (passedHole != null) {
            holeIndex = Integer.parseInt(passedHole) - 1;
        } else {
            holeIndex = PreferenceManager.getInstance().getScoreLastHoleIndex();
        }

        score = golfGeniusModel.getScores().get(holeIndex);
        scores.addAll(score.getScores());
        noScores.addAll(score.getNoScores());

        if (scores.size() > 0) { // player 1 score
            if (!golfGeniusModel.getPlayers().get(0).getScoreRegime().get(holeIndex).equals("x")) {
                mPlayerOne.setText(String.valueOf(scores.get(0)));
                mPlayerOne.setVisibility(View.VISIBLE);
            } else {
                mPlayerOne.setVisibility(View.GONE);
            }
            mPlayerOneText.setText(golfGeniusModel.getPlayers().get(0).getName());
        } else {
            mPlayerOne.setVisibility(View.GONE);
            mPlayerOneText.setVisibility(View.GONE);
        }

        if (scores.size() > 1) { // player 2 score
            if (!golfGeniusModel.getPlayers().get(1).getScoreRegime().get(holeIndex).equals("x")) {
                mPlayerTwo.setText(String.valueOf(scores.get(1)));
                mPlayerTwo.setVisibility(View.VISIBLE);
            } else {
                mPlayerTwo.setVisibility(View.GONE);
            }
            mPlayerTwoText.setText(golfGeniusModel.getPlayers().get(1).getName());
        } else {
            mPlayerTwo.setVisibility(View.GONE);
            mPlayerTwoText.setVisibility(View.GONE);
        }

        if (scores.size() > 2) { // player 3 score
            mPlayerThree.setText(String.valueOf(scores.get(2)));
            if (!golfGeniusModel.getPlayers().get(2).getScoreRegime().get(holeIndex).equals("x")) {
                mPlayerThree.setText(String.valueOf(scores.get(2)));
                mPlayerThree.setVisibility(View.VISIBLE);
            } else {
                mPlayerThree.setVisibility(View.GONE);
            }

            mPlayerThreeText.setText(golfGeniusModel.getPlayers().get(2).getName());
        } else {
            mPlayerThree.setVisibility(View.GONE);
            mPlayerThreeText.setVisibility(View.GONE);
        }

        if (scores.size() > 3) { // player 4 score
            if (!golfGeniusModel.getPlayers().get(3).getScoreRegime().get(holeIndex).equals("x")) {
                mPlayerFour.setText(String.valueOf(scores.get(3)));
                mPlayerFour.setVisibility(View.VISIBLE);
            } else {
                mPlayerFour.setVisibility(View.GONE);
            }
            mPlayerFourText.setText(golfGeniusModel.getPlayers().get(3).getName());
        } else {
            mPlayerFour.setVisibility(View.GONE);
            mPlayerFourText.setVisibility(View.GONE);
        }

        mHoleDesc.setText(score.getDesc());
        mHoleNumber.setText("HOLE " + score.getHole());

        if (holeIndex == 0) {
            mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(golfGeniusModel.getScores().size() - 1).getHole());
            mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(1).getHole());
        } else if (holeIndex == golfGeniusModel.getScores().size() - 1) {
            mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(golfGeniusModel.getScores().size() - 2).getHole());
            mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(0).getHole());
        } else {
            mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex - 1).getHole());
            mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex + 1).getHole());
        }

        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
        mNextHole.setOnClickListener(this);
        mPrevHole.setOnClickListener(this);
        mPlayerOne.setOnClickListener(this);
        mPlayerTwo.setOnClickListener(this);
        mPlayerThree.setOnClickListener(this);
        mPlayerFour.setOnClickListener(this);

        mZero.setOnClickListener(this);
        mOne.setOnClickListener(this);
        mTwo.setOnClickListener(this);
        mThree.setOnClickListener(this);
        mFour.setOnClickListener(this);
        mFive.setOnClickListener(this);
        mSix.setOnClickListener(this);
        mSeven.setOnClickListener(this);
        mEight.setOnClickListener(this);
        mNine.setOnClickListener(this);

        mClear.setOnClickListener(this);
        mUndo.setOnClickListener(this);
        mNoScore.setOnClickListener(this);

        mSaveScores.setOnClickListener(this);

        findViewById(R.id.redLayout).setVisibility(View.GONE);
        toggle(false);
    }

    @OnClick(R.id.backToMap)
    void onBackToMapClick() {
        Intent intent = new Intent(ScoringActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btnCompleteScore)
    void onCompleteScoreClick() {
        StringBuilder string = new StringBuilder("Please complete holes: ");
        boolean pass = true;
        for (GolfGeniusModel.Score score : golfGeniusModel.getScores()) {
            for (int s : score.getScores()) {
                if (s == 0) {
                    pass = false;
                    string.append(score.getHole()).append(", ");
                    break;
                }
            }
        }

        String title = getString(R.string.complete_scores);
        StringBuilder message = new StringBuilder(getString(R.string.complete_and_end));
        String okText = !pass ? getString(R.string.enter_scores) : getString(R.string.yes);
        String cancelText = getString(R.string.no);
        if (!pass) {
            title = getString(R.string.not_saved_scores);
            message = new StringBuilder(string.substring(0, string.length() - 2));
        } else { // Signal loss
            List<SaveScoreModel> failedScores = PreferenceManager.getInstance().getGolfFailedScores();
            if (failedScores.size() > 0) {
                title = getString(R.string.signal_loss);
                message = new StringBuilder("Submit these hole scores to management: ");
                for (SaveScoreModel saveScoreModel : failedScores) {
                    message.append(saveScoreModel.getHole()).append(", ");
                }
                message = new StringBuilder(message.substring(0, message.length() - 2));
                okText = "View scorecard";
                cancelText = getString(R.string.cancel);
            }
        }

        GolfAlertDialog dialog = new GolfAlertDialog(this)
                .setCancelable(false)
                .setTitle(title)
                .setBottomBtnText(cancelText)
                .setMessage(message.toString())
                .setOkText(okText);

        if (!pass) {
            dialog.hideBottomButton();
        }

        final boolean finalPass = pass;
        dialog.setOnDialogListener(new GolfCallback.GolfDialogListener() {
            @Override
            public void onOkClick() {
                if (!finalPass) {
                    dialog.dissmiss();
                } else {
                    PreferenceManager.getInstance().setScoreLastHoleIndex(0);
                    PreferenceManager.getInstance().completeGolfGenius();

                    Intent intent = new Intent();
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onBottomBtnClick() {
                dialog.dissmiss();
            }
        });
        dialog.show();
    }

    private void toggle(boolean show) {
        View redLayout = findViewById(R.id.redLayout);
        ViewGroup parent = findViewById(R.id.parent);

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(500);
        transition.addTarget(R.id.redLayout);

        TransitionManager.beginDelayedTransition(parent, transition);
        redLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showPleaseWaitDialog(boolean show) {
        if (show) {
            waitDialog.show();
        } else {
            waitDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        presenter.stopBackToMapTimer();
        switch (v.getId()) {
            case R.id.ivLeft:
            case R.id.tvPrevHole:
                if (!areAllScoresSaved()) return;

                if (holeIndex == 0) {
                    holeIndex = golfGeniusModel.getScores().size() - 1;
                    score = golfGeniusModel.getScores().get(holeIndex);
                    mHoleDesc.setText(score.getDesc());
                    mHoleNumber.setText("HOLE " + score.getHole());
                    mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex - 1).getHole());
                    mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(0).getHole());
                } else {
                    holeIndex--;
                    score = golfGeniusModel.getScores().get(holeIndex);
                    mHoleDesc.setText(score.getDesc());
                    mHoleNumber.setText("HOLE " + score.getHole());
                    if (holeIndex == 0) {
                        mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(golfGeniusModel.getScores().size() - 1).getHole());
                    } else {
                        mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex - 1).getHole());
                    }
                    mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex + 1).getHole());
                }
                PreferenceManager.getInstance().setScoreLastHoleIndex(holeIndex);
                switchHole();
                break;
            case R.id.ivRight:
            case R.id.tvNextHole:
                if (!areAllScoresSaved()) return;
                if (holeIndex == golfGeniusModel.getScores().size() - 1) {
                    holeIndex = 0;
                    score = golfGeniusModel.getScores().get(holeIndex);
                    mHoleDesc.setText(score.getDesc());
                    mHoleNumber.setText("HOLE " + score.getHole());

                    mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(golfGeniusModel.getScores().size() - 1).getHole());
                    mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex + 1).getHole());
                } else {
                    holeIndex++;
                    score = golfGeniusModel.getScores().get(holeIndex);
                    mHoleDesc.setText(score.getDesc());
                    mHoleNumber.setText("HOLE " + score.getHole());

                    if (holeIndex == golfGeniusModel.getScores().size() - 1) {
                        mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(0).getHole());
                    } else {
                        mNextHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex + 1).getHole());
                    }
                    mPrevHole.setText("HOLE " + golfGeniusModel.getScores().get(holeIndex - 1).getHole());
                }
                PreferenceManager.getInstance().setScoreLastHoleIndex(holeIndex);
                switchHole();
                break;

            case R.id.btnPlayer1:
            case R.id.btnPlayer2:
            case R.id.btnPlayer3:
            case R.id.btnPlayer4:
                playerClick(v);
                break;

            // Keyboard
            case R.id.btn0:
            case R.id.btn1:
            case R.id.btn2:
            case R.id.btn3:
            case R.id.btn4:
            case R.id.btn5:
            case R.id.btn6:
            case R.id.btn7:
            case R.id.btn8:
            case R.id.btn9:
                keyboardClick(v);
                break;

            case R.id.btnClear:
                selectedButton.setText("0");
                scores.set(playerIndex, 0);
                break;

            case R.id.btnNoScore:
                mNoScore.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), noScores.get(playerIndex) ? R.color.white : R.color.green));
                noScores.set(playerIndex, !noScores.get(playerIndex));
                break;

            case R.id.btnUndo:
                for (int i = 0; i < scores.size(); i++) {
                    scores.set(i, score.getScores().get(i));
                    noScores.set(i, score.getNoScores().get(i));
                }

                if (scores.size() > 0) { // player 1 score
                    mPlayerOne.setText(String.valueOf(scores.get(0)));
                }

                if (scores.size() > 1) { // player 2 score
                    mPlayerTwo.setText(String.valueOf(scores.get(1)));
                }

                if (scores.size() > 2) { // player 3 score
                    mPlayerThree.setText(String.valueOf(scores.get(2)));
                }

                if (scores.size() > 3) { // player 4 score
                    mPlayerFour.setText(String.valueOf(scores.get(3)));
                }

                selectedButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                scoreKeyboardActive = false;
                toggle(false);
                break;

            case R.id.btnSave:
                ArrayList<GolfGeniusModel.PlayerScore> players = new ArrayList<>();

                for (int i = 0; i < golfGeniusModel.getPlayers().size(); i++) {
                    if (scores.get(i) == 0) {
                        // index 0
                        if (i == 0 && !golfGeniusModel.getPlayers().get(0).getScoreRegime().get(holeIndex).equals("x")) {
                            Toast.makeText(getApplicationContext(), golfGeniusModel.getPlayers().get(i).getName() + " have no score " + i, Toast.LENGTH_LONG).show();
                            return;
                        }
                        // index 1
                        if (i == 1 && !golfGeniusModel.getPlayers().get(1).getScoreRegime().get(holeIndex).equals("x")) {
                            Toast.makeText(getApplicationContext(), golfGeniusModel.getPlayers().get(i).getName() + " have no score " + i, Toast.LENGTH_LONG).show();
                            return;
                        }
                        // index 2
                        if (i == 2 && !golfGeniusModel.getPlayers().get(2).getScoreRegime().get(holeIndex).equals("x")) {
                            Toast.makeText(getApplicationContext(), golfGeniusModel.getPlayers().get(i).getName() + " have no score " + i, Toast.LENGTH_LONG).show();
                            return;
                        }
                        // index 3
                        if (i == 3 && !golfGeniusModel.getPlayers().get(3).getScoreRegime().get(holeIndex).equals("x")) {
                            Toast.makeText(getApplicationContext(), golfGeniusModel.getPlayers().get(i).getName() + " have no score " + i, Toast.LENGTH_LONG).show();
                            return;
                        }

                    }

                    players.add(new GolfGeniusModel.PlayerScore(
                            golfGeniusModel.getPlayers().get(i).getNumber(),
                            noScores.get(i) ? -1 * (100 + scores.get(i)) : scores.get(i)
                    ));
                }

                SaveScoreModel saveScoreModel = new SaveScoreModel(
                        golfGeniusModel.getRoundId(),
                        score.getHole(),
                        players
                );

                mAlert.setVisibility(View.GONE);
                mSignalIcon.setVisibility(View.GONE);
                selectedButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                scoreKeyboardActive = false;
                toggle(false);

                presenter.insertScore(saveScoreModel);
                break;
        }
    }

    private boolean areAllScoresSaved() {
        for (int i = 0; i < scores.size(); i++) {
            if (!scores.get(i).equals(score.getScores().get(i))) {
                Toast.makeText(getApplicationContext(), "Scores not saved", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void switchHole() {
        for (int i = 0; i < scores.size(); i++) {
            scores.set(i, score.getScores().get(i));
            noScores.set(i, score.getNoScores().get(i));
        }

        if (scores.size() > 0) { // player 1 score
            mPlayerOne.setText(String.valueOf(scores.get(0)));
        }

        if (scores.size() > 1) { // player 2 score
            mPlayerTwo.setText(String.valueOf(scores.get(1)));
        }

        if (scores.size() > 2) { // player 3 score
            mPlayerThree.setText(String.valueOf(scores.get(2)));
        }

        if (scores.size() > 3) { // player 4 score
            mPlayerFour.setText(String.valueOf(scores.get(3)));
        }

        if (scoreKeyboardActive) {
            if (selectedButton != null)
                selectedButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            scoreKeyboardActive = false;
            toggle(false);
        }

        mAlert.setVisibility(View.GONE);
        mSignalIcon.setVisibility(View.GONE);
    }

    private void keyboardClick(View v) {
        int num = scores.get(playerIndex);
        String strokes = v.getTag().toString();
        if (num != 0) {
            strokes = num + strokes;
        }

        if (Integer.parseInt(strokes) > 99)
            return;

        selectedButton.setText(strokes);
        scores.set(playerIndex, Integer.parseInt(strokes));
    }

    public void playerClick(View v) {
        int oldPlayerIndex = playerIndex;
        playerIndex = Integer.parseInt(v.getTag().toString());

        if (oldPlayerIndex == playerIndex && scoreKeyboardActive) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            scoreKeyboardActive = false;
            toggle(false);
            return;
        }

        mNoScore.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), !noScores.get(playerIndex) ? R.color.white : R.color.green));

        selectedButton = (TMButton) v;

        mPlayerOne.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mPlayerTwo.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mPlayerThree.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        mPlayerFour.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        if (!scoreKeyboardActive) {
            scoreKeyboardActive = true;
            toggle(true);
        }
        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
    }

    @Override
    public void onInsertScore(SaveScoreModel saveScoreModel) {
        // remove failed score if you re-saved and succeeded
        List<SaveScoreModel> failedScores = PreferenceManager.getInstance().getGolfFailedScores();
        List<SaveScoreModel> newFailedScores = new ArrayList<>();
        for (SaveScoreModel failedSaveScoreModel : failedScores) {
            if (failedSaveScoreModel.getHole() != saveScoreModel.getHole()
                    && !failedSaveScoreModel.getRoundId().equals(saveScoreModel.getRoundId())) {
                newFailedScores.add(failedSaveScoreModel);
            }
        }
        PreferenceManager.getInstance().saveFailedScores(newFailedScores);

        score.setScores(new ArrayList<>(scores));
        List<GolfGeniusModel.Score> _scores = golfGeniusModel.getScores();
        GolfGeniusModel.Score _score = _scores.get(holeIndex);
        _score.setScores(new ArrayList<>(scores));
        _score.setNoScores(new ArrayList<>(noScores));
        golfGeniusModel.setScores(_scores);
        PreferenceManager.getInstance().saveGolfGenius(golfGeniusModel);
        golfGeniusModel = PreferenceManager.getInstance().getGolfGenius();

        mAlert.setBackgroundResource(R.drawable.rectangle_shape_button);
        mSignalIcon.setVisibility(View.GONE);
        mAlert.setText(R.string.all_scores_saved);
        mAlert.setVisibility(View.VISIBLE);

        presenter.startBackToMapTimer();
    }

    @Override
    public void showWaitDialog(boolean show) {
        showPleaseWaitDialog(show);
    }

    @Override
    public void showSignalRestored() {
        mAlert.setBackgroundResource(R.drawable.rectangle_shape_button);
        mSignalIcon.setImageResource(R.drawable.ic_baseline_signal_wifi_4_bar_24);
        mAlert.setText(R.string.signal_restored);
        mAlert.setVisibility(View.VISIBLE);
        mSignalIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void backToMap() {
        finish();
    }

    @Override
    public void showFailureMessage(SaveScoreModel saveScoreModel) {
        score.setScores(new ArrayList<>(scores));
        List<GolfGeniusModel.Score> _scores = golfGeniusModel.getScores();
        GolfGeniusModel.Score _score = _scores.get(holeIndex);
        _score.setScores(new ArrayList<>(scores));
        _score.setNoScores(new ArrayList<>(noScores));
        golfGeniusModel.setScores(_scores);
        PreferenceManager.getInstance().saveGolfGenius(golfGeniusModel);
        golfGeniusModel = PreferenceManager.getInstance().getGolfGenius();

        PreferenceManager.getInstance().saveFailedScore(saveScoreModel);

        mAlert.setBackgroundResource(R.drawable.rectangle_shape_button_red);
        mSignalIcon.setImageResource(R.drawable.ic_baseline_signal_wifi_off_24);
        mAlert.setText(R.string.signal_lost);
        mAlert.setVisibility(View.VISIBLE);
        mSignalIcon.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dissmiss();
        }
        presenter.onDestroy();
        presenter = null;

        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (scoreKeyboardActive) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            scoreKeyboardActive = false;
            toggle(false);
        } else {
            finish();
        }
    }
}