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
import com.example.myapplication.Model.UpdateProfileRequest;
import com.example.myapplication.R;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class editProfileInfoDialogFragment extends DialogFragment {

    // Interface Callback (refresh page once info is edited)
    public interface OnProfileUpdatedListener {
        void onProfileUpdated(String newUsername, String newEmail);
    }

    private OnProfileUpdatedListener listener;

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    public editProfileInfoDialogFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_profile_info, container, false);

        EditText usernameEdit = view.findViewById(R.id.username_txt);
        EditText emailEdit    = view.findViewById(R.id.email_txt);
        Button   saveBtn      = view.findViewById(R.id.save_button);
        Button   cancelBtn    = view.findViewById(R.id.cancel_button);

        // Change with current values
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MyAppPrefs", requireActivity().MODE_PRIVATE);
        usernameEdit.setText(prefs.getString("username", ""));
        emailEdit.setText(prefs.getString("email", ""));

        cancelBtn.setOnClickListener(v -> dismiss());

        saveBtn.setOnClickListener(v -> {
            String newUsername = usernameEdit.getText().toString().trim();
            String newEmail    = emailEdit.getText().toString().trim();
            String userId      = prefs.getString("userId", "");

            // Only username is mandatory
            if (newUsername.isEmpty()) {
                Toast.makeText(getContext(),
                        "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService api = ApiClient.getClient().create(ApiService.class);
            api.updateUserProfile(userId, new UpdateProfileRequest(newUsername, newEmail))
                    .enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call,
                                               Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                prefs.edit()
                                        .putString("username", newUsername)
                                        .putString("email", newEmail)
                                        .apply();
                                if (listener != null) {
                                    listener.onProfileUpdated(newUsername, newEmail);
                                }
                                Toast.makeText(getContext(),
                                        "Profile updated!", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else if (response.code() == 400) {
                                // Read backend error
                                try {
                                    String errorBody = response.errorBody().string();
                                    if (errorBody.contains("Username")) {
                                        Toast.makeText(getContext(),
                                                "Username already taken", Toast.LENGTH_SHORT).show();
                                    } else if (errorBody.contains("Email")) {
                                        Toast.makeText(getContext(),
                                                "Email already taken", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Update failed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getContext(),
                                            "Update failed", Toast.LENGTH_SHORT).show();
                                }
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

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int widthInPx  = (int) (360 * getResources().getDisplayMetrics().density);
            int heightInPx = (int) (320 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setLayout(widthInPx, heightInPx);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}