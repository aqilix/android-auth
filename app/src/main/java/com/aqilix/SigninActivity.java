package com.aqilix;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.aqilix.volley.VolleySingleton;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.ValidationError;

import org.json.JSONException;
import org.json.JSONObject;

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
        String url = getString(R.string.aqilix_api_url) + getString(R.string.aqilix_api_auth_uri);
        // compose body in json
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", this.etEmail.getText().toString().trim().toLowerCase(Locale.US))
                    .put("password", this.etPassword.getText().toString())
                    .put("grant_type", getString(R.string.aqilix_api_auth_grant_type))
                    .put("client_secret", getString(R.string.aqilix_api_auth_client_secret))
                    .put("client_id", getString(R.string.aqilix_api_auth_client_id));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String signinRequestBody = jsonBody.toString();
        Log.i("Signin.Request", signinRequestBody);
        // compose headers
        final Map<String, String> headers = new ArrayMap<String, String>();
        headers.put("Accept", getString(R.string.aqilix_api_header_accept));

        // compose request
        StringRequest signinRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Signin.Response", response.toString());
                        JSONObject jsonResponse = null;

                        try {
                            jsonResponse = new JSONObject(response.toString());
                            Log.d("Signin.Token", jsonResponse.get("access_token").toString());
                            Log.d("Signin.TokenExpiration", jsonResponse.get("expires_in").toString());
                            Log.d("Signin.TokenRefresh", jsonResponse.get("refresh_token").toString());

                            Intent it = new Intent(getApplication(), DashboardActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(it);
                            finish();
                        } catch (JSONException e) {
                            Log.e("Signin.Response.Parsing", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Signin.Response", error.getMessage());
                        JSONObject jsonResponse = null;

                        try {
                            jsonResponse = new JSONObject(error.getMessage());
                            Log.e("Signin.Error.Title", jsonResponse.get("title").toString());
                            Log.e("Signin.Error.Detail", jsonResponse.get("detail").toString());
                            Toast.makeText(getApplication(), jsonResponse.get("detail").toString(), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Log.e("Signin.Response.Parsing", error.getMessage());
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Log.d("Signin.Headers", headers.toString());
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return signinRequestBody == null ? null : signinRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", signinRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            public String getBodyContentType()
            {
                return getString(R.string.aqilix_api_header_content_type) + "; charset=utf-8";
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError){
                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    Log.e("Signin.Response.Status", Integer.toString(volleyError.networkResponse.statusCode));
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }

                return volleyError;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i("Signin.Response.Status", Integer.toString(response.statusCode));
                return super.parseNetworkResponse(response);
            }
        };

        VolleySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(signinRequest, "Test");
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

