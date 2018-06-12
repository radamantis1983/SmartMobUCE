package ec.edu.uce.smartmobuce.vista;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ec.edu.uce.smartmobuce.controlador.ControladorSQLite;
import ec.edu.uce.smartmobuce.controlador.GPSService;
import ec.edu.uce.smartmobuce.controlador.Metodos;
import ec.edu.uce.smartmobuce.R;

public class GPSActivity extends AppCompatActivity {
    private TextView textView,textView2;

    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiver1;
    private final Metodos m = new Metodos();

    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    textView.setText("\n" + intent.getExtras().get("coordenadas"));

                }
            };
        }


        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

        if (broadcastReceiver1 == null) {

            broadcastReceiver1 = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    textView2.setText("\n" + intent.getExtras().get("acelerometro"));


                }
            };
        }

        registerReceiver(broadcastReceiver1, new IntentFilter("acelerometro_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (broadcastReceiver1 != null) {
            unregisterReceiver(broadcastReceiver1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            Intent i = new Intent(getApplicationContext(), GPSService.class);
            startService(i);
            System.out.println("start service");
            //startService(new Intent(getApplicationContext(), GPSService.class));

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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



