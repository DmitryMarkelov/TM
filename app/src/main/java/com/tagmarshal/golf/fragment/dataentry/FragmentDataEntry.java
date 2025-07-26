package com.tagmarshal.golf.fragment.dataentry;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.CompletedFields;
import com.tagmarshal.golf.rest.model.DataEntryStatus;
import com.tagmarshal.golf.rest.model.Disclaimer;
import com.tagmarshal.golf.rest.model.Fields;
import com.tagmarshal.golf.util.TMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentDataEntry extends BaseFragment implements FragmentDataEntryContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.player1FormFields)
    LinearLayout player1FormFields;

    @BindView(R.id.player2FormFields)
    LinearLayout player2FormFields;

    @BindView(R.id.continueButton)
    Button continueButton;

    @BindView(R.id.backButton)
    Button backButton;

    @BindView(R.id.loadingSpinner)
    ProgressBar loadingProgressBar;

    Disclaimer mDisclaimer;
    private final List<Fields> fields = new ArrayList<>();
    private FragmentDataEntryPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull @io.reactivex.annotations.NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_entry, container, false);
        ButterKnife.bind(this, view);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
        presenter = new FragmentDataEntryPresenter(this);
        mDisclaimer = PreferenceManager.getInstance().getDisclaimer();

        // Check if fields are null or empty
        if (mDisclaimer.getFields() == null || mDisclaimer.getFields().isEmpty()) {
            //Toast.makeText(getContext(), "No fields available", Toast.LENGTH_SHORT).show();
            mDisclaimer.setDataEntryStatus(DataEntryStatus.SUBMITTED);
            mDisclaimer.setLastShownTime(System.currentTimeMillis());
            updateDisclaimer(mDisclaimer);
            popFragment(); // Close the fragment
            return null; // Return null to prevent further initialization
        }

        presenter.getFields();
        return view;
    }

    @OnClick(R.id.continueButton)
    void onContinue() {
        boolean isValid = true;
        List<CompletedFields> completedFields = new ArrayList<>();

        for (int i = 0; i < player1FormFields.getChildCount(); i++) {
            View view = player1FormFields.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView label = (TextView) ((LinearLayout) view).getChildAt(0);
                View secondChildView = ((LinearLayout) view).getChildAt(1);
                if (secondChildView instanceof EditText input) {
                    String value = input.getText().toString();
                    String key = (String) input.getTag();
                    String labelText = label.getText().toString();

                    if (value.trim().isEmpty()) {
                        isValid = false;
                        input.setError(getResources().getString(R.string.required_field_missing));
                    } else {
                        completedFields.add(new CompletedFields(1, labelText, key, value));
                    }
                } else {
                    LinearLayout countryCodeInputContainer = (LinearLayout) secondChildView;
                    EditText input = countryCodeInputContainer.findViewById(R.id.country_code_edittext);
                    String value = input.getText().toString();
                    String key = (String) input.getTag();
                    String labelText = label.getText().toString();

                    if (value.trim().isEmpty()) {
                        isValid = false;
                        input.setError(getResources().getString(R.string.required_field_missing));
                    } else {
                        completedFields.add(new CompletedFields(1, labelText, key, value));
                    }
                }
            }
        }

        for (int i = 0; i < player2FormFields.getChildCount(); i++) {
            View view = player2FormFields.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView label = (TextView) ((LinearLayout) view).getChildAt(0);
                View secondChildView = ((LinearLayout) view).getChildAt(1);
                if (secondChildView instanceof EditText input) {
                    String value = input.getText().toString();
                    String key = (String) input.getTag();
                    String labelText = label.getText().toString();

                    if (value.trim().isEmpty()) {
                        isValid = false;
                        input.setError(getResources().getString(R.string.required_field_missing));
                    } else {
                        completedFields.add(new CompletedFields(2, labelText, key, value));
                    }
                } else {
                    LinearLayout countryCodeInputContainer = (LinearLayout) secondChildView;
                    EditText input = countryCodeInputContainer.findViewById(R.id.country_code_edittext);
                    String value = input.getText().toString();
                    String key = (String) input.getTag();
                    String labelText = label.getText().toString();

                    if (value.trim().isEmpty()) {
                        isValid = false;
                        input.setError(getResources().getString(R.string.required_field_missing));
                    } else {
                        completedFields.add(new CompletedFields(2, labelText, key, value));
                    }
                }
            }
        }

        if (isValid) {
            continueButton.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.VISIBLE);
            presenter.onContinue(completedFields);
        }
    }

    public void onDataCaptureResponse(boolean isSuccessful) {
        if (isSuccessful) {
            Toast.makeText(getContext(), getResources().getString(R.string.data_capture_successful), Toast.LENGTH_LONG).show();
            mDisclaimer.setDataEntryStatus(DataEntryStatus.SUBMITTED);
            mDisclaimer.setLastShownTime(System.currentTimeMillis());
            updateDisclaimer(mDisclaimer);
            popFragment();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.data_capture_failed), Toast.LENGTH_LONG).show();
            loadingProgressBar.setVisibility(View.GONE);
            continueButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.backButton})
    void onBackButton() {
        mDisclaimer.setDataEntryStatus(DataEntryStatus.DISMISSED);
        mDisclaimer.setLastShownTime(System.currentTimeMillis());
        updateDisclaimer(mDisclaimer);
        popFragment();
    }

    private void updateDisclaimer(Disclaimer disclaimer) {
        PreferenceManager.getInstance().setDisclaimer(disclaimer);
    }

    @Override
    public void onViewCreated(@NonNull @io.reactivex.annotations.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Handle back press in this fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mDisclaimer.setDataEntryStatus(DataEntryStatus.DISMISSED);
                mDisclaimer.setLastShownTime(System.currentTimeMillis());
                updateDisclaimer(mDisclaimer);
                popFragment();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private LinearLayout createInputFieldLayout(Fields field) {
        LinearLayout itemLayout = new LinearLayout(getContext());
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(10, 10, 10, 10);

        TextView label = new TextView(getContext());
        LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f);
        label.setLayoutParams(labelLayoutParams);
        label.setText(field.getLabel());
        label.setTextColor(getResources().getColor(R.color.white));
        label.setTypeface(null, Typeface.BOLD);

        itemLayout.addView(label);
        itemLayout.addView(Objects.equals(field.getKey(), "phoneNumber") ? createCountryCodeInput(field) : createEditTextInput(field));
        return itemLayout;
    }

    private LinearLayout createCountryCodeInput(Fields field) {
        LinearLayout countryCodeInputContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.country_code_input, null);
        LinearLayout.LayoutParams countryCodeInputContainerLayoutParams = new LinearLayout.LayoutParams(
                0,
                (int) getResources().getDimension(R.dimen.input_height), 0.8f);
        countryCodeInputContainer.setLayoutParams(countryCodeInputContainerLayoutParams);
        EditText input = countryCodeInputContainer.findViewById(R.id.country_code_edittext);
        input.setHint(String.format("Enter %s", field.getLabel()));
        input.setPadding(dpToPx(10), 0, 0, dpToPx(10));
        input.setHintTextColor(getResources().getColor(R.color.light_grey));
        input.setTextColor(getResources().getColor(R.color.white));
        input.setBackgroundResource(R.drawable.gray_fill_button_background);
        input.setInputType(getInputTypeFromKey(field.getKeyboard()));
        input.setTag(field.getKey());

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllFieldsFilled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return countryCodeInputContainer;
    }

    private EditText createEditTextInput(Fields field) {
        EditText input = new EditText(getContext());
        LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
                0,
                (int) getResources().getDimension(R.dimen.input_height), 0.8f);
        input.setLayoutParams(inputLayoutParams);
        input.setHint(String.format("Enter %s", field.getLabel()));
        input.setPadding(dpToPx(10), 0, 0, 0);
        input.setHintTextColor(getResources().getColor(R.color.light_grey));
        input.setTextColor(getResources().getColor(R.color.white));
        input.setBackgroundResource(R.drawable.gray_fill_button_background);
        input.setInputType(getInputTypeFromKey(field.getKeyboard()));
        input.setTag(field.getKey());

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllFieldsFilled();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return input;
    }

    @Override
    public void showFields(List<Fields> fieldsList) {
        fields.addAll(fieldsList);
        for (int i = 0; i < fieldsList.size(); i++) {
            Fields field = fieldsList.get(i);
            LinearLayout player1InputLayout = createInputFieldLayout(field);
            LinearLayout player2InputLayout = createInputFieldLayout(field);

            player1FormFields.addView(player1InputLayout);
            player2FormFields.addView(player2InputLayout);
        }
    }

    private void checkAllFieldsFilled() {
        boolean player1Filled = true;
        boolean player2Filled = true;

        for (int i = 0; i < player1FormFields.getChildCount(); i++) {
            View view = player1FormFields.getChildAt(i);
            if (view instanceof LinearLayout) {
                View secondChildView = ((LinearLayout) view).getChildAt(1);
                if (secondChildView instanceof EditText input) {
                    if (input.getText().toString().trim().isEmpty()) {
                        player1Filled = false;
                        break;
                    }
                } else {
                    LinearLayout countryCodeInputContainer = (LinearLayout) secondChildView;
                    EditText input = countryCodeInputContainer.findViewById(R.id.country_code_edittext);
                    if (input.getText().toString().trim().isEmpty()) {
                        player1Filled = false;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < player2FormFields.getChildCount(); i++) {
            View view = player2FormFields.getChildAt(i);
            if (view instanceof LinearLayout) {
                View secondChildView = ((LinearLayout) view).getChildAt(1);
                if (secondChildView instanceof EditText input) {
                    if (input.getText().toString().trim().isEmpty()) {
                        player2Filled = false;
                        break;
                    }
                } else {
                    LinearLayout countryCodeInputContainer = (LinearLayout) secondChildView;
                    EditText input = countryCodeInputContainer.findViewById(R.id.country_code_edittext);
                    if (input.getText().toString().trim().isEmpty()) {
                        player2Filled = false;
                        break;
                    }
                }
            }
        }

        continueButton.setEnabled(player1Filled);
        if (player1Filled && player2Filled) {
            continueButton.setBackgroundResource(R.drawable.green_fill_button_background);
        } else {
            continueButton.setBackgroundResource(R.drawable.data_capture_continue_button_background);
        }
    }

    private int getInputTypeFromKey(String keyboard) {
        String key = keyboard.toLowerCase();

        switch (key) {
            case "text":
                return InputType.TYPE_CLASS_TEXT; // General text input
            case "textcapwords":
                return InputType.TYPE_TEXT_FLAG_CAP_WORDS; // Capitalize first letter of each word
            case "textcapsentences":
                return InputType.TYPE_TEXT_FLAG_CAP_SENTENCES; // Capitalize first letter of sentences
            case "textcapcharacters":
                return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; // Capitalize all characters
            case "textpassword":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; // Password input
            case "textemail":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; // Email input
            case "texturi":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI; // URI input
            case "textpersonname":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME; // Person name input
            case "textpostaladdress":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS; // Postal address input
            case "textmultiline":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE; // Multi-line text input
            case "textautocomplete":
                return InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE; // Auto-complete text input
            case "phone":
                return InputType.TYPE_CLASS_PHONE; // Phone number input
            case "number":
                return InputType.TYPE_CLASS_NUMBER; // Numeric input
            case "numberpassword":
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD; // Numeric password input
            case "numberdecimal":
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL; // Decimal number input
            case "numbersigned":
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED; // Signed number input
            case "date":
                return InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE; // Date input
            case "time":
                return InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME; // Time input
            case "datetime":
                return InputType.TYPE_CLASS_DATETIME; // General datetime input
            case "textvisiblepassword":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD; // Password input with visible text
            case "textwebedittext":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT; // Web form input
            case "textwebemailaddress":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS; // Web email address input
            case "textwebpassword":
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD; // Web password input
            case "textimemultiline":
                return InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE; // IME multi-line input
            default:
                return InputType.TYPE_CLASS_TEXT; // Default to general text input if no match
        }
    }
}
