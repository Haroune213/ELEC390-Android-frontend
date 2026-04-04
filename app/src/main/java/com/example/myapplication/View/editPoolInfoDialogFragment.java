package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;

public class editPoolInfoDialogFragment extends DialogFragment {

    public editPoolInfoDialogFragment() {
        // Required empty public constructor
    }

    protected Button cancelPoolInfo_btn, savePoolInfo_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_pool_info_dialog, container, false);

        cancelPoolInfo_btn = view.findViewById(R.id.cancelPoolInfo_btn);
        savePoolInfo_btn = view.findViewById(R.id.savePoolInfo_btn);

        cancelPoolInfo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        savePoolInfo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create method for saving change
            }
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Converts DP to Pixels so it looks the same on all screens
            int widthInPx = (int) (360 * getResources().getDisplayMetrics().density);
            int heightInPx = (int) (480 * getResources().getDisplayMetrics().density);

            getDialog().getWindow().setLayout(widthInPx, heightInPx);

            // Makes the background transparent so your XML background shows correctly
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}