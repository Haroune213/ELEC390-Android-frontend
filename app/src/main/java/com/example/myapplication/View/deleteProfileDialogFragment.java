package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class deleteProfileDialogFragment extends DialogFragment {

    public deleteProfileDialogFragment() {
        // Required empty public constructor
    }

    protected Button delete_btn, cancel_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete_profile_dialog, container, false);


        delete_btn = view.findViewById(R.id.delete_btn);
        cancel_btn = view.findViewById(R.id.cancel_btn);

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        delete_btn.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
            String userId = prefs.getString("userId", "");

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.deleteUser(userId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Effacer la session locale
                        prefs.edit().clear().apply();
                        Toast.makeText(getContext(),
                                "Account deleted", Toast.LENGTH_SHORT).show();

                        // Retourner au login
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(),
                                "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(),
                            "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int widthInPx  = (int) (320 * getResources().getDisplayMetrics().density);
            int heightInPx = (int) (220 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(widthInPx, heightInPx);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}