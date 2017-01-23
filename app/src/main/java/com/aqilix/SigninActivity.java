package com.aqilix;

import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.ValidationError;

/**
 * A login screen that offers login via email/password.
 */
public class SigninActivity extends AppCompatActivity implements Validator.ValidationListener {
    private TextView createAccountText;

    private TextView signinBtn;

    private Validator signinValidator;

    private TextInputLayout tilEmailWrapper;

    private TextInputLayout tilPasswordWrapper;

    private HashMap<Integer, TextInputLayout> inputLayoutMapper;

    @Email
    private EditText etEmail;

    @NotEmpty
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // inputTextLayout
        tilEmailWrapper = (TextInputLayout) findViewById(R.id.credEmailWrapper);
        tilPasswordWrapper = (TextInputLayout) findViewById(R.id.credPasswordWrapper);

        // inputText
        etEmail = (EditText)findViewById(R.id.credEmail);
        etPassword = (EditText)findViewById(R.id.credPassword);

        // map input text with TextInputLayout
        inputLayoutMapper = new HashMap<Integer, TextInputLayout>();
        inputLayoutMapper.put(etEmail.getId(), tilEmailWrapper);
        inputLayoutMapper.put(etPassword.getId(), tilPasswordWrapper);

        // Create Account link
        createAccountText = (TextView)findViewById(R.id.create);
        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(it);
            }
        });

        // validator
        signinValidator = new Validator(this);
        signinValidator.setValidationListener(this);

        // sign in button
        signinBtn = (TextView)findViewById(R.id.signin1);
        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Signin", "Process...");
                tilEmailWrapper.setErrorEnabled(false);
                tilPasswordWrapper.setErrorEnabled(false);
                signinValidator.validate();
            }
        });
    }

    @Override
    public void onValidationSucceeded() {
        // Code to execute once all your fields have been validated.
        // Show a Snackbar, display an other screen, do a network call, etc
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        // Display a message for each validation error
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Manage how you display your errors depending on the view type
            if (view instanceof EditText) {
                TextInputLayout inputLayout = inputLayoutMapper.get(view.getId());
                inputLayout.setErrorEnabled(true);
                inputLayout.setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


}

