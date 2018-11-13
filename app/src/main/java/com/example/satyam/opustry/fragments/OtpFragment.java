package com.example.satyam.opustry.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.satyam.opustry.Config;
import com.example.satyam.opustry.DialActivity;
import com.example.satyam.opustry.GETRequest;
import com.example.satyam.opustry.R;
import com.example.satyam.opustry.ResponseHandler;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;


import butterknife.Bind;
import butterknife.ButterKnife;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


/*
 * Created by shailesh.nandkule on 10/23/2018.
 */

public class OtpFragment extends Fragment implements Validator.ValidationListener, View.OnClickListener, ResponseHandler {
    private static final int REQ_VERIFY_OTP = 1;
    @Order(value = 1)
    @NotEmpty(messageResId = R.string.error_login_enter_user_name, sequence = 1)
    @Bind(R.id.edittxtUserName)
    EditText edittxtUserName;

    @Order(value = 2)
    @NotEmpty(messageResId = R.string.error_login_enter_mail, sequence = 1)
    @Bind(R.id.edittxtMailId)
    EditText edittxtMailId;

    @Bind(R.id.btnForgotOTP)
    TextView btnForgotPassword;

    @Bind(R.id.btnVerifyOtp)
    Button btnLogin;


    private Validator validator;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public OtpFragment() {
        // Required empty public constructor
    }

    public static OtpFragment newInstance(String param1, String param2) {
        OtpFragment fragment = new OtpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        validator = new Validator(this);
        validator.setValidationListener(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_otp, container, false);
        ButterKnife.bind(this, view);
        btnForgotPassword.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        edittxtMailId.setText(mParam1);
        return view;
    }


    public static OtpFragment newInstance() {
        return new OtpFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnForgotPassword:
                Toast.makeText(getActivity(), "Coming soon..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnVerifyOtp:
                submitLogin();
                break;

        }
    }

    private void submitLogin() {
        final String otp = edittxtUserName.getText().toString();
        final String mail = edittxtMailId.getText().toString();
        if (otp == null || otp.trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.error_login_enter_user_name, Toast.LENGTH_SHORT).show();

        }
        if (mail == null || mail.trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.error_login_enter_mail, Toast.LENGTH_SHORT).show();
        }

        if (!isValidEmailAddress(mail.trim())) {
            Toast.makeText(getActivity(), R.string.error_login_invalid_emailid, Toast.LENGTH_SHORT).show();
        } else {
            String urlString = Config.SERVER_IP_ADDRESS + Config.API_VERIFY_OTP + "?mailid=" + mail + "&otp=" + otp;
            new GETRequest(this, REQ_VERIFY_OTP).execute(urlString);
        }
    }


    public boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    @Override
    public void onResponse(String response, int reqCode) {
        switch (reqCode) {
            case REQ_VERIFY_OTP: {
                if (response.equals("success")) {
                    startActivity(new Intent(getActivity(), DialActivity.class));
                    //todo save preferences
                } else {
                    Toast.makeText(getActivity(), R.string.verifyOtp_faulure, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onError(int reqCode) {
        Toast.makeText(getActivity(), "Oops, Something went wrong", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onValidationSucceeded() {
        Toast.makeText(getActivity(), "Coming soon..", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        if (errors != null && !errors.isEmpty()) {
            String errorMessage = errors.get(0).getCollatedErrorMessage(getActivity());
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}

