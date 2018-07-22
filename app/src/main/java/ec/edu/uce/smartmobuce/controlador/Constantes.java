package ec.edu.uce.smartmobuce.controlador;

/**
 * Created by Henry on 7/22/2018.
 */

public class Constantes {

    public Constantes(){

    }

    public static final String URL_LOGIN="http://movilidad.000webhostapp.com/movilidad1/login.php";
    public static final String URL_REGISTRO="http://movilidad.000webhostapp.com/movilidad1/registro.php";
    public static final String URL_CAPTURA_DATOS_GPS="http://movilidad.000webhostapp.com/movilidad1/registrogps.php";
    //para uso modo local para manual de instalacion
    //public static final String HOST="192.168.2.156";//colocar ip que se haya definido como ip del servidor
    //public static final String URL_LOGIN="http://"+HOST+"/movilidad/login.php";
    //public static final String URL_REGISTRO="http://"+HOST+"/movilidad/registro.php";
    //public static final String URL_CAPTURA_DATOS_GPS="http://"+HOST+"/movilidad/registrogps.php";
    //horas que permite guardar datos en la base interna
    public static final String horaInicial = "06:00:00";
    public static final String horaFinal = "22:00:00";
    //Horas en la cual se ejecuta automaticamente la actualizacion
    public static final String horaActualizacion = "01:00:00";//hora de inicio para sincronizar datos
    public static final String horaActualizacionf = "01:30:00";//hora de fin para sincronizar datos
    public static final long INTERVALOS_DETECCION_GPS_EN_MILISEGUNDOS = 1000; //1000 MILISEGUNDOS EQUIVALE A UN SEGUNDO(5*60*1000) EQUIVALE A 5 MIN

}
