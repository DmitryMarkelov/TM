package com.tagmarshal.golf.fragment.contact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.Unbinder;


public class FragmentContactUs extends BaseFragment implements FragmentContactUsContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.call_number)
    TMButton mCallNumberBtn;

    @BindView(R.id.cancel)
    TMButton mCancelBtn;

    @BindView(R.id.input_message)
    EditText mInput;

    private Unregistrar unregistrar;

    private FragmentContactUsPresenter presenter;

    private Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new FragmentContactUsPresenter(this);

        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();

        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unregistrar = KeyboardVisibilityEvent.registerEventListener(getBaseActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen) {
                    mCallNumberBtn.setVisibility(View.GONE);
                    mCancelBtn.setVisibility(View.GONE);
                } else {
                    mCallNumberBtn.setVisibility(View.VISIBLE);
                    mCancelBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mInput != null && mInput.isFocused() && mInput.getText().toString().trim().length() > 299) {
                    mInput.setText(s.toString().substring(0, 299));
                    mInput.setSelection(s.length() - 1);
                    Toast.makeText(getActivity(), "Maximum number of characters reached.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        presenter.getContactNumber();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        presenter = null;

        unregistrar.unregister();
        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();

        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        InputMethodManager inputMethodManager = (InputMethodManager) getBaseActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            Activity activity = getBaseActivity();
            if (activity == null)
                return;
            if (activity.getCurrentFocus() == null)
                return;
            if (activity.getCurrentFocus().getWindowToken() == null)
                return;
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @OnClick(R.id.input_message)
    void onInputFieldClick() {
        mCallNumberBtn.setVisibility(View.GONE);
        mCancelBtn.setVisibility(View.GONE);
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.send_message)
    void onSendMessageClick() {
        if (mInput.getText().toString().length() == 0) {
            Toast.makeText(getContext(), getString(R.string.please_enter_text), Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, String> body = new HashMap<>();
        body.put("message", mInput.getText().toString());

        presenter.sendMessage(body);
    }

    @OnFocusChange({R.id.input_message})
    void onFocusChanged(View view) {
        if (view.hasFocus()) {
            mCallNumberBtn.setVisibility(View.GONE);
            mCancelBtn.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.cancel)
    void onCancelClick() {
        getActivity().onBackPressed();
    }

    @Override
    public void shoWaitDialog(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void showRequestFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetContactNumber(String number) {
        mCallNumberBtn.setText(number);
    }

    @Override
    public void onMessageSent() {
        Toast.makeText(GolfApplication.context,
                GolfApplication.context.getString(R.string.message_sent),
                Toast.LENGTH_LONG).show();

        getBaseActivity().onBackPressed();
    }
}
