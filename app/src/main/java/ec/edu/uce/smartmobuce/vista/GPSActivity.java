package ec.edu.uce.smartmobuce.vista;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.R;
import ec.edu.uce.smartmobuce.controlador.ControladorSQLite;
import ec.edu.uce.smartmobuce.controlador.GpsService;
import ec.edu.uce.smartmobuce.controlador.Metodos;

public class GPSActivity extends AppCompatActivity  {

    protected static final String LOG_TAG = "TestApp2";
    private final Metodos m = new Metodos();
    private final ControladorSQLite controller = new ControladorSQLite(this);
    private BroadcastReceiver broadcastReceiver;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mAccuracyText;
    private TextView mAltitudeText;
    private TextView mSpeedText;
    private TextView mProviderText;
    private TextView mDatetext;




    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    mLatitudeText.setText(""+intent.getExtras().get("Latitud"));
                    mLongitudeText.setText(""+intent.getExtras().get("Longitud"));
                    mAccuracyText.setText(""+intent.getExtras().get("Precision"));
                    mAltitudeText.setText(""+intent.getExtras().get("Altitud"));
                    mSpeedText.setText(""+intent.getExtras().get("Velocidad"));
                    mProviderText.setText(""+intent.getExtras().get("Proveedor"));
                    mDatetext.setText(""+intent.getExtras().get( "fecha"));
                }
            };
        }


        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }


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

        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck==-1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            permissionCheck= ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);

        }
        if (permissionCheck==-0) {
            Intent i = new Intent(this, GpsService.class);
            startService(i);


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