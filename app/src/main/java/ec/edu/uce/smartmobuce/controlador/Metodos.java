package ec.edu.uce.smartmobuce.controlador;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ec.edu.uce.smartmobuce.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Henry on 1/17/2018.
 */

public class Metodos {
    private final SimpleDateFormat hformat = new SimpleDateFormat("HH:mm:ss"); //formato para la hora 24h
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//formato de fecha
    private double aux1 = 0, aux2 = 0;
    private static final String TAG = "Metodos";

    public boolean lastlocation(double last_latitud, double last_longitud) {

        double a = truncateDecimal(last_latitud, 4);
        double b = truncateDecimal(last_longitud, 4);

        if (aux1 != a || aux2 != b) {
            aux1 = truncateDecimal(last_latitud, 4);
            aux2 = truncateDecimal(last_longitud, 4);
            return true;
        } else {
            return false;
        }

    }

    private static double truncateDecimal(double x, int numberofDecimals) {
        if (x > 0) {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue();
        } else {
            return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue();
        }
    }

    //permite guardar en archivo de preferencias (usuario)
    public void guardarPreferencias(Context context, String text) {

        SharedPreferences preferencia = context.getSharedPreferences("PreferenciasDeUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencia.edit();
        editor.putString("usu_id", text);
        editor.commit();
    }


    //permite cargar el archivo de preferencias (usuario)
    public String cargarPreferencias(Context context) {
        SharedPreferences preferencia = context.getSharedPreferences("PreferenciasDeUsuario", MODE_PRIVATE);
        return preferencia.getString("usu_id", "");
    }


    //permite guardar en archivo de preferencias (Acuerdo de confidencialidad)
    public void guardarPreferenciasAcuerdo(Context context, Boolean est) {

        SharedPreferences preferencia = context.getSharedPreferences("Acuerdo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencia.edit();
        editor.putBoolean("acuerdo", est);
        editor.apply();
    }


    //permite cargar el archivo de preferencias (Acuerdo de confidencialidad)
    public boolean cargarPreferenciasAcuerdo(Context context) {
        SharedPreferences preferencia = context.getSharedPreferences("Acuerdo", MODE_PRIVATE);
        return preferencia.getBoolean("acuerdo", false);
    }


    //recupera la hora actual
    public String getHoraActual() {
        Date ahora = new Date();

        return hformat.format(ahora);
    }

    //recupera la fecha actual
    public String getFechaActual() {
        Date afecha = new Date();

        return dateFormat.format(afecha);
    }

    //compara horas para proceder a la sincronizadion de la base de datos
    public boolean rangoHorassincronizacion(String horaActual, String horaInicial, String horaFinal) {

        try {

            Date horaAct;
            Date horaIni;
            Date horaFin;
            horaAct = hformat.parse(horaActual);
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);

            return (horaAct.after(horaIni)) && (horaAct.before(horaFin));


        } catch (ParseException ex) {
            Log.e(TAG, "rangoHorassincronizacion:");
            return false;
        }

    }

  //compara horas de funcionamiento para guardar datos
    public boolean rangoHoras(String horaActual, String horaInicial, String horaFinal) {

        try {

            Date horaAct;
            Date horaIni;
            Date horaFin;
            horaAct = hformat.parse(horaActual);
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);

            return (horaAct.after(horaIni)) && (horaAct.before(horaFin));


        } catch (ParseException ex) {
            Log.e(TAG, "rangoHoras:");

            return false;
        }

    }

    ////activa la funcion de registrar en la base si se encuentra en el rango del area seleccionada
    public Boolean revisarArea(Double lat, Double longi) {

        //gps area uce
        if ((lat > -0.20672000 && lat < -0.19329001 && longi > -78.51572000 && longi < -78.49808001) == true) {
            return true;
        } else {
            return false;
        }
    }

    ///sincronizacion de la base de datos
    public void syncSQLiteMySQLDB(final Context appContext) {
        final ControladorSQLite controller = new ControladorSQLite(appContext);

        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();


        if (controller.dbSyncCount() != 0) {

            params.put("gpsJSON", controller.composeJSONfromSQLite());
            client.post(Constantes.URL_CAPTURA_DATOS_GPS, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                    try {
                        JSONArray arr = new JSONArray(response);

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = (JSONObject) arr.get(i);
                            controller.updateSyncStatus(obj.get("usu_id").toString(), obj.get("dat_fechahora_lectura").toString(), obj.get("status").toString());
                        }
                        Toast.makeText(appContext, R.string.db_synchronization, Toast.LENGTH_SHORT).show();
                        controller.eraseSync();

                    } catch (JSONException e) {
                        Toast.makeText(appContext, R.string.error_synchronization, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    if (statusCode == 404) {
                        Toast.makeText(appContext, R.string.error_request, Toast.LENGTH_LONG).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(appContext, R.string.error_server, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(appContext, R.string.error_server1, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(appContext, R.string.error1_db_synchronization, Toast.LENGTH_LONG).show();
        }
    }


    public void mensajeAcuerdo(final Activity activity, final Context context) {

        final AlertDialog b;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.acuerdo_confidencialidad, null);
        dialogBuilder.setView(dialogView);

        final CheckBox _check = dialogView.findViewById(R.id.checkBox_acuerdo);
        Button _acuerdo = dialogView.findViewById(R.id.button_aceptar);
        Button _reject = dialogView.findViewById(R.id.button_cancelar);
        dialogBuilder.setTitle(R.string.Acuerdo);

        dialogBuilder.setCancelable(false);
        b = dialogBuilder.create();
        b.show();

        _acuerdo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_check.isChecked() == true) {

                    b.dismiss();
                    guardarPreferenciasAcuerdo(context, true);
                } else {
                    Toast.makeText(context, R.string.check, Toast.LENGTH_SHORT).show();
                }
            }
        });
        _reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.dismiss();
            }
        });

    }
}