package ec.edu.uce.smartmobuce.vista;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.controlador.Constantes;
import ec.edu.uce.smartmobuce.controlador.Metodos;
import ec.edu.uce.smartmobuce.modelo.Usuarios;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    Metodos m = new Metodos();
    static  String TAG = "LoginActivity";
    public RequestQueue requestQueue;

    public StringRequest request;
    View focusView = null;
    boolean cancel = false;
    String usuarioid="";
    EditText _emailText,_passwordText;
    Button _loginButton;
    TextView _signupLink;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        _signupLink = findViewById(R.id.link_signup);


        requestQueue = Volley.newRequestQueue(this);
        String sFichero = "/data/data/" + getPackageName() + "/shared_prefs/PreferenciasDeUsuario.xml";
        File fichero = new File(sFichero);

        if (fichero.exists()) {
            System.out.println("El fichero " + sFichero + " existe" + getBaseContext());
            //  mEmailView.setText(m.cargarPreferencias(getBaseContext()).toString());
            Intent intent = new Intent(getApplicationContext(), GPSActivity.class);
            startActivity(intent);
            finish();

        } else {
            _loginButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    login();


                }
            });

            _signupLink.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Start the Signup activity
                    startActivity(new Intent(getApplicationContext(), RegistroActivity.class));
                    //Intent intent = new Intent(getApplicationContext(), RegistroActivity.class);
                    //startActivityForResult(intent, REQUEST_SIGNUP);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            });
        }
    }
    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }



        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        //onLoginSuccess();
                        // On complete call either onLoginSuccess or onLoginFailed
                        System.out.println(Constantes.URL_LOGIN);
                        request = new StringRequest(Request.Method.POST,Constantes.URL_LOGIN, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {

                                try {
                                    //me permite obtener el id del usuario para registrar en el gps
                                    JSONObject jsonObject = new JSONObject(response);
                                    //System.out.println(response);
                                   // System.out.println("response usado en el try"+jsonObject);
                                    if (jsonObject.names().get(0).equals("usuarios")) {
                                        Toast.makeText(getApplicationContext(), "SUCCESS " + jsonObject.getString("usuarios"), Toast.LENGTH_SHORT).show();
                                        //m.guardarPreferencias(getBaseContext(), email.getText().toString());
                                        JSONArray jArray=jsonObject.getJSONArray("usuarios");
                                        for(int i=0;i<jArray.length();i++){
                                            JSONObject object =jArray.getJSONObject(i);
                                            Usuarios rev=new Usuarios();
                                            rev.setUsu_id(object.getString("usu_id"));
                                            System.out.println("usuario codigo"+rev.getUsu_id());
                                            usuarioid=rev.getUsu_id();

                                        }

                                        System.out.println("ejecuta json"+jsonObject);

                                        m.guardarPreferencias(getBaseContext(),usuarioid);
                                        onLoginSuccess();
                                        startActivity(new Intent(getApplicationContext(), GPSActivity.class));

                                        finish();
                                    } else {
                                        _passwordText.setError(getString(R.string.error_incorrect_password));
                                        focusView = _passwordText;
                                        Toast.makeText(getApplicationContext(), "usuario no registrado o " + jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                        cancel = true;
                                        onLoginFailed();
                                    }
                                    if (cancel) {
                                        // There was an error; don't attempt login and focus the first
                                        // form field with an error.
                                        focusView.requestFocus();
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, e.toString());
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                                error.printStackTrace();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("usu_email", email);
                                hashMap.put("usu_password", password);

                                return hashMap;
                            }
                        };

                        requestQueue.add(request);


                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 2000);


    }




    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.contains("@uce.edu.ec")) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
