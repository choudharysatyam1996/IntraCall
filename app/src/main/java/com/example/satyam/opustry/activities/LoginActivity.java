package com.example.satyam.opustry.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.satyam.opustry.R;
import com.example.satyam.opustry.fragments.LoginFragment;

/*
 * Created by shailesh.nandkule on 10/23/2018.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        showLoginFragment();
    }

    private void showLoginFragment() {
        getSupportFragmentManager().executePendingTransactions();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.loginFrameLayout, LoginFragment.newInstance())
                .addToBackStack("LoginFragment")
                .commit();
    }



    @Override
    public void onBackPressed() {
        String fragmentName = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            if (fragmentName.equals("LoginFragment")) {
                this.finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            this.finish();
        }
    }
}
