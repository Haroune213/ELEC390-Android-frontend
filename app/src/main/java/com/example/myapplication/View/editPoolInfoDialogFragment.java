package com.example.myapplication.View;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.PoolInfo;
import com.example.myapplication.R;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class editPoolInfoDialogFragment extends DialogFragment {

    // Callback to ProfileActivity
    public interface OnPoolInfoUpdatedListener {
        void onPoolInfoUpdated(String type, Double width,
                               Double depth, Double length, String unit);
    }
    private OnPoolInfoUpdatedListener listener;

    public void setOnPoolInfoUpdatedListener(OnPoolInfoUpdatedListener l) {
        this.listener = l;
    }

    // Security limits
    private static final double DIM_MIN = 1.0, DIM_MAX = 50.0;
    private static final double DEPTH_MIN = 0.5, DEPTH_MAX = 5.0;

    // init variables
    protected Button cancelPoolInfo_btn, savePoolInfo_btn;
    private EditText widthEdit, depthEdit, lengthEdit;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_pool_info_dialog,
                container, false);

        // Link views
        cancelPoolInfo_btn = view.findViewById(R.id.cancelPoolInfo_btn);
        savePoolInfo_btn   = view.findViewById(R.id.savePoolInfo_btn);
        widthEdit          = view.findViewById(R.id.editTextText);
        depthEdit          = view.findViewById(R.id.editTextText2);
        lengthEdit         = view.findViewById(R.id.editTextText3);
        Spinner spinnerType  = view.findViewById(R.id.spinner_pool_type);
        Spinner spinnerUnits = view.findViewById(R.id.spinner_units);

        // Dropdown menu for TYPE
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Chlorine", "Salt"}
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Dropdown menu for UNITS
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Feet", "Meters"}
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(unitAdapter);

        // Button Initialization: Cancel
        cancelPoolInfo_btn.setOnClickListener(v -> dismiss());

        // fetch and pre-fill values in text fields
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getPoolInfo(userId).enqueue(new Callback<PoolInfo>() {
            @Override
            public void onResponse(Call<PoolInfo> call, Response<PoolInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PoolInfo p = response.body();
                    if (p.getWidth()  != null) widthEdit.setText(String.valueOf(p.getWidth()));
                    if (p.getDepth()  != null) depthEdit.setText(String.valueOf(p.getDepth()));
                    if (p.getLength() != null) lengthEdit.setText(String.valueOf(p.getLength()));
                    spinnerType.setSelection("salt".equals(p.getPoolType()) ? 1 : 0);
                    spinnerUnits.setSelection("meters".equals(p.getUnit()) ? 1 : 0);
                }
            }
            @Override
            public void onFailure(Call<PoolInfo> call, Throwable t) {}
        });

        // save
        savePoolInfo_btn.setOnClickListener(v -> {
            if (!validateAll()) return;

            String poolType = spinnerType.getSelectedItem().toString().toLowerCase();
            String unit     = spinnerUnits.getSelectedItem().toString().toLowerCase();
            Double width    = parseDouble(widthEdit);
            Double depth    = parseDouble(depthEdit);
            Double length   = parseDouble(lengthEdit);

            PoolInfo poolInfo = new PoolInfo(userId, poolType, width, depth, length, unit);

            api.updatePoolInfo(userId, poolInfo)
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call,
                                               Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                if (listener != null) {
                                    listener.onPoolInfoUpdated(poolType, width,
                                            depth, length, unit);
                                }
                                Toast.makeText(getContext(),
                                        "Pool info saved!", Toast.LENGTH_SHORT).show();
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

    // validate changes
    private boolean validateAll() {
        if (isEmpty(widthEdit) || isEmpty(depthEdit) || isEmpty(lengthEdit)) {
            Toast.makeText(getContext(),
                    "Please fill all dimension fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        double w = parseDouble(widthEdit);
        double d = parseDouble(depthEdit);
        double l = parseDouble(lengthEdit);

        if (w < DIM_MIN || w > DIM_MAX || l < DIM_MIN || l > DIM_MAX) {
            Toast.makeText(getContext(),
                    "Width and length must be between " + DIM_MIN + "m and " + DIM_MAX + "m",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (d < DEPTH_MIN || d > DEPTH_MAX) {
            Toast.makeText(getContext(),
                    "Depth must be between " + DEPTH_MIN + "m and " + DEPTH_MAX + "m",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEmpty(EditText f) {
        return f.getText().toString().trim().isEmpty();
    }

    private Double parseDouble(EditText f) {
        String t = f.getText().toString().trim();
        return t.isEmpty() ? null : Double.parseDouble(t);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int w = (int) (360 * getResources().getDisplayMetrics().density);
            int h = (int) (480 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(w, h);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}