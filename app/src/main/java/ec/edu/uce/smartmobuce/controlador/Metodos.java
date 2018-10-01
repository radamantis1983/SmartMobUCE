package ec.edu.uce.smartmobuce.controlador;

import android.content.Context;
import android.content.SharedPreferences;
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

    //permite guardar en archivo de preferencias
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

    //segundo archivo de preferencias
    //permite guardar en archivo de preferencias
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

    //compara horas de funcionamiento para guardar datos
    public boolean rangoHorassincronizacion(String horaActual, String horaInicial, String horaFinal) {

        try {

            Date horaAct;
            Date horaIni;
            Date horaFin;
            horaAct = hformat.parse(horaActual);
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);

            if ((horaAct.after(horaIni)) && (horaAct.before(horaFin))) {

                return true;
            } else {

                return false;
            }


        } catch (ParseException ex) {
            //Logger.getLogger(Main2.class.getName()).log(Level.SEVERE, null, ex);
            // System.out.println("La hora Posee errores");
            return false;
        }

    }


    //compara horas para proceder a la sincronizadion de la base de datos
    public boolean compararHoras(String horaInicial, String horaFinal) {

        try {


            Date horaIni;
            Date horaFin;
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);
            if (horaIni.equals(horaFin)) {
                // System.out.println("horas iguales procediendo a sincronizar");
                //  System.out.println("hora actual "+horaIni+" Hora final "+horaFin);
                return true;
            } else {
                //  System.out.println("no es hora de sincronizar");
                return false;
            }


        } catch (ParseException ex) {

            //System.out.println("Posee errores");
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

            if ((horaAct.after(horaIni)) && (horaAct.before(horaFin))) {

                return true;
            } else {

                return false;
            }


        } catch (ParseException ex) {

            // System.out.println("La hora Posee errores");
            return false;
        }

    }

    ////activa la funcion de guardar si se encuentra en el rango del area seleccionada
    public Boolean revisarArea(Double lat, Double longi) {
      /*  boolean estado=false;
        //gps area uce

        if(((lat>-0.20672000 && lat <-0.19329001 && longi>-78.51572000 && longi<-78.49808001)==true)
                //gps area casa
                ||((lat>-0.259357 && lat<-0.258592 && longi>-78.550617 && longi<-78.549941)==true)){
            estado =true;
            return estado;
        }
              return estado;
*/
        return true;
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

                    System.out.println("dato response" + response);
                    try {
                        JSONArray arr = new JSONArray(response);
                        System.out.println("arreglo de" + arr.length());
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


}