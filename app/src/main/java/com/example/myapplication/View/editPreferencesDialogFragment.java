package com.example.myapplication.View;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.UpdatePreferencesRequest;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.R;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class editPreferencesDialogFragment extends DialogFragment {

    public interface OnPreferencesUpdatedListener {
        void onPreferencesUpdated(
                Double tempMin, Double tempMax,
                Double phMin,   Double phMax,
                Double tdsMin,  Double tdsMax,
                Double depthMin,Double depthMax
        );
    }

    private OnPreferencesUpdatedListener listener;

    public void setOnPreferencesUpdatedListener(OnPreferencesUpdatedListener listener) {
        this.listener = listener;
    }

    // Security Limits (The user cannot input a value beyond these ranges due to security concerns)
    private static final double PH_MIN_SAFE    = 6.8,  PH_MAX_SAFE    = 8.2;
    private static final double TEMP_MIN_SAFE  = 10.0, TEMP_MAX_SAFE  = 40.0;
    private static final double TDS_MIN_SAFE   = 0.0,  TDS_MAX_SAFE   = 6000.0;
    private static final double DEPTH_MIN_SAFE = 0.0,  DEPTH_MAX_SAFE = 500;

    // Default Values
    private static final double PH_MIN_DEFAULT    = 7.2,  PH_MAX_DEFAULT    = 7.8;
    private static final double TEMP_MIN_DEFAULT  = 24.0, TEMP_MAX_DEFAULT  = 28.0;
    private static final double TDS_MIN_DEFAULT   = 500.0,TDS_MAX_DEFAULT   = 1500.0;
    private static final double DEPTH_MIN_DEFAULT = 100,  DEPTH_MAX_DEFAULT = 200;

    public editPreferencesDialogFragment() {}

    private EditText tempMax, tempMin, depthMax, depthMin, tdsMax, tdsMin, phMax, phMin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_preferences, container, false);

        // Temperature
        tempMax = view.findViewById(R.id.editTextNumberDecimal);
        tempMin = view.findViewById(R.id.editTextNumberDecimal1);
        // Depth
        depthMax = view.findViewById(R.id.editTextNumberDecimal2);
        depthMin = view.findViewById(R.id.editTextNumberDecimal3);
        // TDS
        tdsMax = view.findViewById(R.id.editTextNumberDecimal4);
        tdsMin = view.findViewById(R.id.editTextNumberDecimal5);
        // pH
        phMax = view.findViewById(R.id.editTextNumberDecimal6);
        phMin = view.findViewById(R.id.editTextNumberDecimal7);
        // Buttons
        Button saveBtn   = view.findViewById(R.id.savePoolInfo_btn);
        Button cancelBtn = view.findViewById(R.id.cancelPoolInfo_btn);
        Button resetBtn = view.findViewById(R.id.reset_defaults_btn);

        // Fills values in TextEdits with current values.
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getUserPreferences(userId).enqueue(new Callback<UserPreferences>() {
            @Override
            public void onResponse(Call<UserPreferences> call,
                                   Response<UserPreferences> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserPreferences p = response.body();
                    // If value exists, fill
                    if (p.getTempMin() != null) tempMin.setText(String.valueOf(p.getTempMin()));
                    if (p.getTempMax() != null) tempMax.setText(String.valueOf(p.getTempMax()));
                    if (p.getPhMin()   != null) phMin.setText(String.valueOf(p.getPhMin()));
                    if (p.getPhMax()   != null) phMax.setText(String.valueOf(p.getPhMax()));
                    if (p.getTdsMin()  != null) tdsMin.setText(String.valueOf(p.getTdsMin()));
                    if (p.getTdsMax()  != null) tdsMax.setText(String.valueOf(p.getTdsMax()));
                    if (p.getDepthMin()!= null) depthMin.setText(String.valueOf(p.getDepthMin()));
                    if (p.getDepthMax()!= null) depthMax.setText(String.valueOf(p.getDepthMax()));
                }
            }
            @Override
            public void onFailure(Call<UserPreferences> call, Throwable t) {
                // Keep fields empty if nothing.
            }
        });

        // Buttons logic (reset,  cancel and save)
        resetBtn.setOnClickListener(v -> resetToDefaults());
        cancelBtn.setOnClickListener(v -> dismiss());

        saveBtn.setOnClickListener(v -> {

            if (!validateAll())
                return;

            UpdatePreferencesRequest request = new UpdatePreferencesRequest(
                    parseDouble(tempMin), parseDouble(tempMax),
                    parseDouble(phMin),   parseDouble(phMax),
                    parseDouble(tdsMin),  parseDouble(tdsMax),
                    parseDouble(depthMin),parseDouble(depthMax)
                );

            api.updatePreferences(userId, request)
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call,
                                               Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                // Notify ProfileActivity
                                if (listener != null) {
                                    listener.onPreferencesUpdated(
                                            parseDouble(tempMin), parseDouble(tempMax),
                                            parseDouble(phMin),   parseDouble(phMax),
                                            parseDouble(tdsMin),  parseDouble(tdsMax),
                                            parseDouble(depthMin),parseDouble(depthMax)
                                    );
                                }
                                Toast.makeText(getContext(),
                                        "Preferences saved!", Toast.LENGTH_SHORT).show();
                                dismiss();

                            } else {
                                Toast.makeText(getContext(),
                                        "Save failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Toast.makeText(getContext(),
                                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
        });

        return view;
    }

    // DATA VALIDATION
    private boolean validateAll() {

        // NO EMPTY FIELD CHECK
        if (isEmpty(tempMin) || isEmpty(tempMax) || isEmpty(phMin) || isEmpty(phMax) ||
                isEmpty(tdsMin)  || isEmpty(tdsMax)  || isEmpty(depthMin) || isEmpty(depthMax)) {
            Toast.makeText(getContext(),
                    "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        double tMinVal  = parseDouble(tempMin),  tMaxVal  = parseDouble(tempMax);
        double phMinVal = parseDouble(phMin),     phMaxVal = parseDouble(phMax);
        double tdsMinVal= parseDouble(tdsMin),    tdsMaxVal= parseDouble(tdsMax);
        double dMinVal  = parseDouble(depthMin),  dMaxVal  = parseDouble(depthMax);

        // LOGIC CHECK
        if (tMinVal >= tMaxVal) {
            Toast.makeText(getContext(),
                    "Temperature: min must be less than max", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phMinVal >= phMaxVal) {
            Toast.makeText(getContext(),
                    "pH: min must be less than max", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tdsMinVal >= tdsMaxVal) {
            Toast.makeText(getContext(),
                    "TDS: min must be less than max", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dMinVal >= dMaxVal) {
            Toast.makeText(getContext(),
                    "Depth: min must be less than max", Toast.LENGTH_SHORT).show();
            return false;
        }

        // SAFETY CHECK
        if (tMinVal < TEMP_MIN_SAFE || tMaxVal > TEMP_MAX_SAFE) {
            Toast.makeText(getContext(),
                    "For your safety, Temperature must be between " + TEMP_MIN_SAFE
                            + "°C and " + TEMP_MAX_SAFE + "°C", Toast.LENGTH_LONG).show();
            return false;
        }
        if (phMinVal < PH_MIN_SAFE || phMaxVal > PH_MAX_SAFE) {
            Toast.makeText(getContext(),
                    "For your safety, pH must be between " + PH_MIN_SAFE
                            + " and " + PH_MAX_SAFE, Toast.LENGTH_LONG).show();
            return false;
        }
        if (tdsMinVal < TDS_MIN_SAFE || tdsMaxVal > TDS_MAX_SAFE) {
            Toast.makeText(getContext(),
                    "For your safety, TDS must be between " + (int)TDS_MIN_SAFE
                            + " and " + (int)TDS_MAX_SAFE + " ppm", Toast.LENGTH_LONG).show();
            return false;
        }
        if (dMinVal < DEPTH_MIN_SAFE || dMaxVal > DEPTH_MAX_SAFE) {
            Toast.makeText(getContext(),
                    "For your safety, Depth must be between " + DEPTH_MIN_SAFE
                            + "m and " + DEPTH_MAX_SAFE + "m", Toast.LENGTH_LONG).show();
            return false;
        }

        return true; // Everything is ok.
    }

    // Reset to default values
    private void resetToDefaults() {
        tempMin.setText(String.valueOf(TEMP_MIN_DEFAULT));
        tempMax.setText(String.valueOf(TEMP_MAX_DEFAULT));
        phMin.setText(String.valueOf(PH_MIN_DEFAULT));
        phMax.setText(String.valueOf(PH_MAX_DEFAULT));
        tdsMin.setText(String.valueOf(TDS_MIN_DEFAULT));
        tdsMax.setText(String.valueOf(TDS_MAX_DEFAULT));
        depthMin.setText(String.valueOf(DEPTH_MIN_DEFAULT));
        depthMax.setText(String.valueOf(DEPTH_MAX_DEFAULT));
        Toast.makeText(getContext(),
                "Values reset to defaults", Toast.LENGTH_SHORT).show();
    }

    // Functions to avoid errors
    private boolean isEmpty(EditText field) {
        return field.getText().toString().trim().isEmpty();
    }

    private Double parseDouble(EditText field) {
        String text = field.getText().toString().trim();
        return text.isEmpty() ? null : Double.parseDouble(text);
    }

    // Start Fragment

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int widthInPx  = (int) (375 * getResources().getDisplayMetrics().density);
            int heightInPx = (int) (500 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(widthInPx, heightInPx);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}

