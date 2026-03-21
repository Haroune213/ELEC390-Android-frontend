package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;

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

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create method for deleting account
            }
        });

        return view;
    }
}