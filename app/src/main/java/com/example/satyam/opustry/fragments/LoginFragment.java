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
import com.example.satyam.opustry.GETRequest;
import com.example.satyam.opustry.R;
import com.example.satyam.opustry.ResponseHandler;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;

import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;


import butterknife.Bind;
import butterknife.ButterKnife;


/*
 * Created by shailesh.nandkule on 10/23/2018.
 */

public class LoginFragment extends Fragment implements Validator.ValidationListener, View.OnClickListener, ResponseHandler {
    private static final int REQ_REGISTER = 1;
    @Order(value = 1)
    @NotEmpty(messageResId = R.string.error_login_enter_user_name, sequence = 1)
    @Bind(R.id.edittxtUserName)
    EditText edittxtUserName;

    @Order(value = 2)
    @NotEmpty(messageResId = R.string.error_login_enter_mail, sequence = 1)
    @Bind(R.id.edittxtMailId)
    EditText edittxtMailId;

    @Bind(R.id.btnForgotPassword)
    TextView btnForgotPassword;

    @Bind(R.id.btnLogin)
    Button btnLogin;

    @Bind(R.id.btnCreateAccount)
    TextView btnCreateAccount;
    private Validator validator;
    private String name;
    private String mail;


    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        btnForgotPassword.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnCreateAccount.setOnClickListener(this);
        return view;
    }


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnForgotPassword:
                Toast.makeText(getActivity(), "Coming soon..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnLogin:
                submitLogin();
                break;
            case R.id.btnCreateAccount:
                alreadyRegistered();
                break;

        }
    }

    private void alreadyRegistered() {

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.loginFrameLayout, OtpFragment.newInstance("", ""));
        fragmentTransaction.addToBackStack("OtpFragment");
        fragmentTransaction.commit();
    }

    private void submitLogin() {

        name = edittxtUserName.getText().toString();
        mail = edittxtMailId.getText().toString();
        if (name == null || name.trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.error_login_enter_user_name, Toast.LENGTH_SHORT).show();

        }
        if (mail == null || mail.trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.error_login_enter_mail, Toast.LENGTH_SHORT).show();
        }

        if (!isValidEmailAddress(mail.trim())) {
            Toast.makeText(getActivity(), R.string.error_login_invalid_emailid, Toast.LENGTH_SHORT).show();
        } else {
            String urlString = Config.SERVER_IP_ADDRESS + Config.API_REGISTER_USER + "?name=" + name + "&mailid=" + mail + "&pubkey=" + "";
            new GETRequest(this, REQ_REGISTER).execute(urlString);
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
            case REQ_REGISTER: {
                if (response.equals("true")) {
                    askOtp();
                } else {
                    Toast.makeText(getActivity(), R.string.Registration_failure, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void askOtp() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.loginFrameLayout, OtpFragment.newInstance(mail, name));
        fragmentTransaction.addToBackStack("OtpFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onError(int reqCode) {
        //askOtp();
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
