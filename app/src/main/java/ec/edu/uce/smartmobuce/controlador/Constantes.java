package ec.edu.uce.smartmobuce.controlador;

/**
 * Created by Henry on 7/22/2018.
 */

public class Constantes {

    public Constantes() {

    }

    //para uso modo local para manual de instalacion
    public static final String HOST = "gmoncayoresearch.com";//colocar ip o host que se haya definido como servidor
    public static final String URL_LOGIN = "http://" + HOST + "/smartmobuce1/login.php";
    public static final String URL_REGISTRO = "http://" + HOST + "/smartmobuce1/registro.php";
    public static final String URL_CAPTURA_DATOS_GPS = "http://" + HOST + "/smartmobuce1/registrogps.php";

    //horas que permite guardar datos en la base interna
    public static final String horaInicial = "06:00:00";
    public static final String horaFinal = "22:00:00";
    //Horas en la cual se ejecuta automaticamente la actualizacion
    public static final String horaActualizacion = "01:00:00";//hora de inicio para sincronizar datos
    public static final String horaActualizacionf = "01:03:00";//hora de fin para sincronizar datos
    public static final long INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS = 10 * 1000; //10*1000 MILISEGUNDOS EQUIVALE A 10 SEGUNDOS

}
