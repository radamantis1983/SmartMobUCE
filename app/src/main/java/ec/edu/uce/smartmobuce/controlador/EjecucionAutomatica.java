package ec.edu.uce.smartmobuce.controlador;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Henry on 7/23/2018.
 */

public class EjecucionAutomatica extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, GpsService.class);
        context.startService(service);

    }
}
