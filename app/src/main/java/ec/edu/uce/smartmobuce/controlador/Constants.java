package ec.edu.uce.smartmobuce.controlador;

/**
 * Created by Henry on 7/16/2018.
 */

public class Constants {
    public Constants(){

    }


    public static final String HOST = "movilidad.000webhostapp.com";
    public static final String SERVER = "https://"+HOST+"/";
    public static final String URL_LOGIN = SERVER+"login/login.php";
    public static final String URL_REGISTRO = SERVER+"login/registro.php";
    public static final String URL_CAPTURA_DATOS_GPS = SERVER+"movilidad/registrogps.php";
    //horas que permite guardar datos en la base interna
    public static final String horaInicial = "06:00:00";
    public static final String horaFinal = "22:00:00";
    //Horas en la cual se ejecuta automaticamente la actualizacion
    public static final String horaActualizacion = "01:00:00";//hora de inicio para sincronizar datos
    public static final String horaActualizacionf = "01:30:00";//hora de fin para sincronizar datos
    public static final long INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS = 1000; //1000 MILISEGUNDOS EQUIVALE A UN SEGUNDO(5*60*1000) EQUIVALE A 5 MIN



}
