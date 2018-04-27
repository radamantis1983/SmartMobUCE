package ec.edu.uce.smartmobuce.controlador;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Henry on 1/17/2018.
 */

public class Metodos {
    SimpleDateFormat hformat = new SimpleDateFormat("HH:mm:ss"); //formato para la hora 24h
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//formato de fecha

    //permite guardar en archivo de preferencias
    public void guardarPreferencias(Context context, String text) {

        SharedPreferences preferencia = context.getSharedPreferences("PreferenciasDeUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = preferencia.edit();
        editor.putString("usu_id", text);
        editor.commit();
    }


    //permite cargar el archivo de preferencias (email usuario)
    public String cargarPreferencias(Context context) {
        SharedPreferences preferencia = context.getSharedPreferences("PreferenciasDeUsuario", MODE_PRIVATE);
        return  preferencia.getString("usu_id", "");
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
    public boolean rangoHorassincronizacion(String horaActual,String horaInicial,String horaFinal) {

        try {

            Date horaAct;
            Date horaIni;
            Date horaFin;
            horaAct = hformat.parse(horaActual);
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);

            if ((horaAct.after(horaIni))&&(horaAct.before(horaFin))) {

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
    public boolean compararHoras(String horaInicial,String horaFinal) {

        try {


            Date horaIni;
            Date horaFin;
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);
            if (horaIni.equals(horaFin)) {
                // System.out.println("horas iguales procediendo a sincronizar");
                System.out.println("hora actual "+horaIni+" Hora final "+horaFin);
                return true;
            } else {
                //  System.out.println("no es hora de sincronizar");
                return false;
            }


        } catch (ParseException ex) {
            //Logger.getLogger(Main2.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("Posee errores");
            return false;
        }
    }
    //compara horas de funcionamiento para guardar datos
    public boolean rangoHoras(String horaActual,String horaInicial,String horaFinal) {
    /*
        try {

            Date horaAct;
            Date horaIni;
            Date horaFin;
            horaAct = hformat.parse(horaActual);
            horaIni = hformat.parse(horaInicial);
            horaFin = hformat.parse(horaFinal);

            if ((horaAct.after(horaIni))&&(horaAct.before(horaFin))) {

                return true;
            } else {

                return false;
            }


        } catch (ParseException ex) {
            //Logger.getLogger(Main2.class.getName()).log(Level.SEVERE, null, ex);
           // System.out.println("La hora Posee errores");
            return false;
        }
        */
        return true;
    }

    ////activa la funcion de guardar si se encuentra en el rango del area seleccionada
    public Boolean revisarArea(Double lat,Double longi){
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
    //saca una copia de la base de datos y lo guarda en mis documentos

    public void backupdDatabase(Context aplicationcontext){

        try {
            Boolean sdDisponible=false;
            Boolean sdEscritura=false;
            File sd = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            }else{
                sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            }
            File data = Environment.getDataDirectory();

            String packageName  = aplicationcontext.getPackageName();
            String sourceDBName = "datosGPS.db";
            String targetDBName = "vistadb";


            String estado=Environment.getExternalStorageState();
            if(estado.equals(Environment.MEDIA_MOUNTED)) {
                sdDisponible=true;
                sdEscritura=true;

            }else
            if(estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                sdDisponible=true;
                sdEscritura=false;

            }else {
                sdDisponible=true;
                sdEscritura=false;
            }

            if (sdDisponible.equals(true)&&sdEscritura.equals(true)) {

                String currentDBPath = "data/" + packageName + "/databases/" + sourceDBName;
                //Date now = new Date();
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                //String backupDBPath = targetDBName +"_"+ dateFormat.format(now) + ".db";
                String backupDBPath = targetDBName+".db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);


                Log.i("backup","backupDB=" + backupDB.getAbsolutePath());
                Log.i("backup","sourceDB=" + currentDB.getAbsolutePath());

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            Log.i("Backup", e.toString());
        }
    }


    ///sincronizacion de la base de datos
    public void syncSQLiteMySQLDB(final Context appContext){
        final ControladorSQLite controller = new ControladorSQLite(appContext);

        //Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> userList =  controller.getAllUsers();
        if(userList.size()!=0){
            if(controller.dbSyncCount() != 0){

                params.put("usersJSON", controller.composeJSONfromSQLite());
                client.post("https://smartmobuce.000webhostapp.com/smartgps/registrogps.php",params ,new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {


                        try {

                            JSONArray arr = new JSONArray(response);
                            System.out.println("dato response"+response);
                            for(int i=0; i<arr.length();i++){
                                JSONObject obj = (JSONObject)arr.get(i);
                               //   System.out.println(obj.get("usu_id"));
                               //   System.out.println(obj.get("dat_fechahora_lectura"));
                                // System.out.println(obj.get("status"));

                                controller.updateSyncStatus(obj.get("usu_id").toString(),obj.get("dat_fechahora_lectura").toString(),obj.get("status").toString());
                            }
                            Toast.makeText(appContext, "DB Sincronización completada!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Toast.makeText(appContext, "Error Ocurrido [Server's JSON solicitud posiblemente invalida]!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // TODO Auto-generated method stub
                        //  prgDialog.hide();
                        if(statusCode == 404){
                            Toast.makeText(appContext, "solicitud del recurso not found", Toast.LENGTH_LONG).show();
                        }else if(statusCode == 500){
                            Toast.makeText(appContext, "algo puedo haber ocurrido en el server end", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(appContext, "Error inerperado a ocurido! [Error mas común: podría el dispositivo no esta conectado al Internet]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else{
                Toast.makeText(appContext, "SQLite y Remote MySQL DBs estan Sincronizada!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(appContext, "No data in SQLite DB, \n por favor espere la accion \nde Sincronizacion ", Toast.LENGTH_LONG).show();
        }
    }


}
