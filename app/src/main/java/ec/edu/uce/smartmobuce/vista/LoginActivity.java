package ec.edu.uce.smartmobuce.vista;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.controlador.Metodos;



/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private final Metodos m = new Metodos();
    private Locale locale;
    private Configuration config = new Configuration();

    private EditText email, password;
    private Button sign_in_register;
    private RequestQueue requestQueue;
    private static final String URL = "https://movilidad.000webhostapp.com/login/login.php";
    private StringRequest request;
    boolean cancel = false;
    View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        sign_in_register = (Button) findViewById(R.id.sign_in_register);

        requestQueue = Volley.newRequestQueue(this);
//comprobar idioma crea archivo de config
        String sIdioma = "/data/data/" + getPackageName() + "/shared_prefs/IdiomaDeUsuario.xml";
        File idioma = new File(sIdioma);
        if (idioma.exists()) {}else{
            showDialog();
            m.guardarIdioma(getBaseContext(),"idioma seleccionado");
        }

        //comprobar archivo
        String sFichero = "/data/data/" + getPackageName() + "/shared_prefs/PreferenciasDeUsuario.xml";
        File fichero = new File(sFichero);

        if (fichero.exists()) {
            System.out.println("El fichero " + sFichero + " existe"+getBaseContext());
            //  mEmailView.setText(m.cargarPreferencias(getBaseContext()).toString());
            Intent intent = new Intent(getApplicationContext(), GPSActivity.class);
            startActivity(intent);
            finish();

        } else {
            sign_in_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    final String emailvalido = email.getText().toString();
                    if (emailvalido.contains("@uce.edu.ec")) {
                        request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.names().get(0).equals("success")) {
                                        Toast.makeText(getApplicationContext(), "SUCCESS " + jsonObject.getString("success"), Toast.LENGTH_SHORT).show();
                                        //m.guardarPreferencias(getBaseContext(), email.getText().toString());
                                        m.guardarPreferencias(getBaseContext(),emailvalido);
                                        startActivity(new Intent(getApplicationContext(), GPSActivity.class));
                                        finish();
                                    } else {
                                        password.setError(getString(R.string.error_invalid_email));
                                        focusView = password;
                                        cancel = true;
                                        Toast.makeText(getApplicationContext(), "Error" + jsonObject.getString("error"), Toast.LENGTH_SHORT).show();

                                    }
                                    if (cancel) {
                                        // There was an error; don't attempt login and focus the first
                                        // form field with an error.
                                        focusView.requestFocus();
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                hashMap.put("email", email.getText().toString());
                                hashMap.put("password", password.getText().toString());

                                return hashMap;
                            }
                        };

                        requestQueue.add(request);
                    } else {
                        email.setError(getString(R.string.error_invalid_email));
                        focusView = email;
                        cancel = true;
                    }
                    if (cancel) {
                        // There was an error; don't attempt login and focus the first
                        // form field with an error.
                        focusView.requestFocus();
                    }
                }

            });


        }
    }
    /**
     * Muestra una ventana de dialogo para elegir el nuevo idioma de la aplicacion
     * Cuando se hace clic en uno de los idiomas, se cambia el idioma de la aplicacion
     * y se recarga la actividad para ver los cambios
     * */
    private void showDialog(){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getResources().getString(R.string.str_languaje));
        //obtiene los idiomas del array de string.xml
        String[] types = getResources().getStringArray(R.array.languages);
        b.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                switch(which){
                    case 0:
                        locale = new Locale("en");
                        config.locale =locale;
                        break;
                    case 1:
                        locale = new Locale("es");
                        config.locale =locale;
                        break;

                }
                getResources().updateConfiguration(config, null);
                Intent refresh = new Intent(LoginActivity.this, LoginActivity.class);
                startActivity(refresh);
                finish();
            }

        });

        b.show();
    }

}

