package ec.edu.uce.smartmobuce.vista;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.controlador.ControladorSQLite;
import ec.edu.uce.smartmobuce.controlador.Metodos;

public class GPSActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    protected static final String LOG_TAG = "TestApp2";
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private String usr,fecha;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mAccuracyText;
    private TextView mAltitudeText;
    private TextView mSpeedText;
    private TextView mProviderText;
    private TextView mDatetext;
    private final String horaActualizacion = "01:00:00";// para sincronizar datos hora de inicio
    private final String horaActualizacionf = "01:30:00";//para sincronizar datos hora de fin
    private final String horaInicial = "06:00:00"; //horas de actividad inicio
    private final String horaFinal = "22:00:00";//horas de actividad fin



    protected LocationRequest mLocationRequest;
    private final Metodos m = new Metodos();
    private final ControladorSQLite controller = new ControladorSQLite(this);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLatitudeText = (TextView) findViewById(R.id.latitude_text);
        mLongitudeText = (TextView) findViewById(R.id.longitude_text);
        mAccuracyText = (TextView) findViewById(R.id.accuracy_text);
        mAltitudeText = (TextView) findViewById(R.id.altitude_text);
        mSpeedText = (TextView) findViewById(R.id.speed_text);
        mProviderText = (TextView) findViewById(R.id.provider_text);
        mDatetext = (TextView) findViewById(R.id.date_text);

        buildGoogleApiClient();

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(LOG_TAG,"Build Google API");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG,"Connection OK!!! ");

        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck==-1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            permissionCheck= ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if(permissionCheck==0){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation != null){
                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
                mAccuracyText.setText(String.valueOf(mLastLocation.getAccuracy()));
                mAltitudeText.setText(String.valueOf(mLastLocation.getAltitude()));
                mSpeedText.setText(String.valueOf(mLastLocation.getSpeed()));
                mProviderText.setText(String.valueOf(mLastLocation.getProvider()));
                mDatetext.setText(String.valueOf(m.getFechaActual()));


            }

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);


        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG,"Connection Suspended!!! " + i + " cause");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG,"Connection Failed!!! " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG,"Localizacion"+location.toString());
        usr = m.cargarPreferencias(getBaseContext());
        mLatitudeText.setText(String.valueOf(location.getLatitude()));
        mLongitudeText.setText(String.valueOf(location.getLongitude()));
        mAccuracyText.setText(String.valueOf(location.getAccuracy()));
        mAltitudeText.setText(String.valueOf(location.getAltitude()));
        mSpeedText.setText(String.valueOf(location.getSpeed()));
        mProviderText.setText(String.valueOf(location.getProvider()));
        mDatetext.setText(String.valueOf(m.getFechaActual()));
        fecha = m.getFechaActual();
        Boolean area1 = m.revisarArea(location.getLatitude(), location.getLongitude());
        //si se encuentra dentro del area capturamos los datos
        if (area1) {
            //si la aplicacion esta en el horario definido guardamos los datos
            if (m.rangoHoras(m.getHoraActual(), horaInicial, horaFinal)) {
                //prepara los datos a ser enviados al query de insertar datos a la base
                HashMap<String, String> queryValues = new HashMap<String, String>();
                queryValues.put("usu_id", usr);
                queryValues.put("dat_latitud", String.valueOf(location.getLatitude()));
                queryValues.put("dat_longitud", String.valueOf(location.getLongitude()));
                queryValues.put("dat_precision", String.valueOf(location.getAccuracy()));
                queryValues.put("dat_altitud", String.valueOf(location.getAltitude()));
                queryValues.put("dat_velocidad", String.valueOf(location.getSpeed()));
                queryValues.put("dat_proveedor", location.getProvider());
                queryValues.put("dat_fechahora_lectura", fecha);
                controller.insertDatos(queryValues);
            }

            //comprueba la hora para sincronizacón con la base de datos
            if (m.rangoHorassincronizacion(m.getHoraActual(), horaActualizacion, horaActualizacionf)) {
                //lista los datos para sincronizar
                ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                if (userList.size() != 0) {
                }
                m.syncSQLiteMySQLDB(getApplicationContext());
            }
        }
    }
    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
//seleccion de opciones del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.refresh:
                ControladorSQLite controller = new ControladorSQLite(this);
                //lista los datos para sincronizar
                ArrayList<HashMap<String, String>> userList = controller.getAllUsers();
                if (userList.size() != 0) {

                }
                //Sync SQLite DB data to remote MySQL DB
                m.syncSQLiteMySQLDB(getApplicationContext());
                return true;
            case R.id.action_settings:

                Toast.makeText(this, "Elaborado por Henry Guamán", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}





