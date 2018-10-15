package ec.edu.uce.smartmobuce.vista;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.controlador.Constantes;
import io.apptik.widget.multiselectspinner.MultiSelectSpinner;

public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";
    RequestQueue requestQueue;
    StringRequest request;
    private EditText _emailText;
    private EditText _passwordText;
    private EditText _reEnterPasswordText;
    private EditText _year;
    private Spinner _genero;
    private Spinner _facultad;
    private Spinner _tipo;
    private Spinner _sector;
    private MultiSelectSpinner _actividad;
    private Button _signupButton;
    private TextView _loginLink;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        ButterKnife.bind(this);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _reEnterPasswordText = findViewById(R.id.input_reEnterPassword);
        _year = findViewById(R.id.input_year);
        _genero = findViewById(R.id.input_genero);
        _facultad = findViewById(R.id.input_facultad);
        _tipo = findViewById(R.id.input_tipo);
        _sector = findViewById(R.id.input_sector);
        final List<String> list = Arrays.asList(getResources().getStringArray(R.array.actividad));
        _actividad = findViewById(R.id.input_actividad);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);
        requestQueue = Volley.newRequestQueue(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genero, android.R.layout.simple_spinner_item);
        _genero.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.facultad, android.R.layout.simple_spinner_item);
        _facultad.setAdapter(adapter1);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.tipo, android.R.layout.simple_spinner_item);
        _tipo.setAdapter(adapter2);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.sector, android.R.layout.simple_spinner_item);
        _sector.setAdapter(adapter3);
        String act = getString(R.string.activity);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, list);
        _actividad.setListAdapter(adapter4)
                .setListener(new MultiSelectSpinner.MultiSpinnerListener() {
                    @Override
                    public void onItemsSelected(boolean[] selected) {

                    }
                })
                .setAllCheckedText(getString(R.string.all_types))
                .setAllUncheckedText(act)
                .setSelectAll(false);
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    private void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();

            return;
        }
        // _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(RegistroActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.recrea1));
        progressDialog.show();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();
        final String year = _year.getText().toString();
        final int genero = _genero.getSelectedItemPosition();
        final String facultad = _facultad.getSelectedItem().toString();
        final int tipo = _tipo.getSelectedItemPosition();
        final int sector = _sector.getSelectedItemPosition();
        final String actividad = _actividad.getSelectedItem().toString();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        request = new StringRequest(Request.Method.POST, Constantes.URL_REGISTRO, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                try {

                                    JSONObject jsonObject = new JSONObject(response);
                                    Log.e(TAG, response);
                                    if (jsonObject.names().get(0).equals("success")) {
                                        onSignupSuccess();
                                    } else {
                                        onSignupFailed();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, e.toString());
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                hashMap.put("usu_email", email);
                                hashMap.put("usu_password", password);
                                hashMap.put("usu_year", year);
                                hashMap.put("usu_genero", String.valueOf(genero));
                                hashMap.put("usu_facultad", facultad);
                                hashMap.put("usu_tipo", String.valueOf(tipo));
                                hashMap.put("usu_sector", String.valueOf(sector));
                                hashMap.put("usu_actividades", actividad);
                                hashMap.put("usu_marca", String.valueOf(Build.MANUFACTURER));
                                hashMap.put("usu_modelo", String.valueOf(Build.MODEL));
                                hashMap.put("usu_version_android", String.valueOf(Build.VERSION.RELEASE));

                                return hashMap;
                            }

                        };

                        request.setShouldCache(false);
                        requestQueue.add(request);


                        progressDialog.dismiss();
                    }
                }, 5000);
    }


    private void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        mensajeEnvioCorreo(getBaseContext());
        emailNotification();
        Toast.makeText(getApplicationContext(), getString(R.string.action_register), Toast.LENGTH_SHORT).show();


    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.error_create_user), Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();
        String year = _year.getText().toString();

        _emailText.requestFocus();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.contains("@uce.edu.ec")) {
            _emailText.requestFocus();
            _emailText.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.requestFocus();
            _passwordText.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.requestFocus();
            _reEnterPasswordText.setError(getString(R.string.error_invalid_password_match));
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }


        if (year.isEmpty()) {
            _year.requestFocus();
            _year.setError(getString(R.string.re_year));
            valid = false;

        } else {
            int y = Integer.parseInt(_year.getText().toString());
            if (y >= 2000 || y <= 1940) {
                _year.requestFocus();
                _year.setError(getString(R.string.re_year_valid));
                valid = false;
            } else {
                _year.setError(null);
            }


        }


        if (_genero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.select_gender), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (_facultad.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.select_faculty), Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (_tipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.select_type), Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (_sector.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.select_sector), Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (_actividad.getSelectedItem().toString().trim().equals("Activity")
                || _actividad.getSelectedItem().toString().trim().equals("Actividad")) {

            Toast.makeText(this, getString(R.string.select_activity), Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    public void mensajeEnvioCorreo(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mail.uce.edu.ec"));
        PendingIntent pendingIntent = PendingIntent.getActivities(RegistroActivity.this, 01, new Intent[]{intent}, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setContentTitle("SmartMobUCE");
        mBuilder.setSmallIcon(android.R.drawable.ic_dialog_email);
        mBuilder.setContentText(getString(R.string.m_correo));
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(198317, mBuilder.build());

    }

    public void emailNotification() {

        new AlertDialog.Builder(RegistroActivity.this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_Instructive)
                .setPositiveButton(R.string.Accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        dialog.dismiss();
                    }
                }).show();
    }

}

