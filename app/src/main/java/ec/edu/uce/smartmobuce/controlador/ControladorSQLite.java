package ec.edu.uce.smartmobuce.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Henry on 1/17/2018.
 */

public class ControladorSQLite extends SQLiteOpenHelper {
    //variable para almecenar
     String sqlCreate = "CREATE TABLE `DatosGPS` (`id_usuario` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            "`usuario` TEXT," +
            "`latitud` REAL," +
            "`longitud` REAL," +
            "`conducta` REAL," +
            "`press` REAL," +
            "`altitud` REAL," +
            "`velocidad` REAL," +
            "`proveedor` TEXT," +
            "`fecha` TEXT," +
            "`udpateStatus` TEXT"+
            ");";

    //contexto referencia al activity, name nombre de base de datos SQLiteDatabase m=no utilizamos ponemos null
    public ControladorSQLite(Context applicationcontext) {
        super(applicationcontext, "datosGPS.db", null, 1);
    }

    /*public ControladorSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }*/

    //se ejecuta en el momento que llamemos a la base de datos
    //construira por primera vez la base de datos

    @Override
    public void onCreate(SQLiteDatabase db) {
        //crea la dase de datos si no existe y ejecuta los comandos sql
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS DatosGPS";
        db.execSQL(query);
        onCreate(db);
    }
    /**
     * Inserts User into SQLite DB
     * @param queryValues
     */
    public void insertUser(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_usuario", queryValues.get("id_usuario"));
        values.put("usuario", queryValues.get("usuario"));
        values.put("latitud", queryValues.get("latitud"));
        values.put("longitud", queryValues.get("longitud"));
        values.put("conducta", queryValues.get("conducta"));
        values.put("press", queryValues.get("press"));
        values.put("altitud", queryValues.get("altitud"));
        values.put("velocidad", queryValues.get("velocidad"));
        values.put("proveedor", queryValues.get("proveedor"));
        values.put("fecha", queryValues.get("fecha"));
        values.put("udpateStatus", "no");
        database.insert("DatosGPS", null, values);
        database.close();
    }

    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM DatosGPS";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id_usuario", cursor.getString(0));
                map.put("usuario", cursor.getString(1));
                map.put("latitud",cursor.getString(2));
                map.put("longitud",cursor.getString(3));
                map.put("conducta",cursor.getString(4));
                map.put("press",cursor.getString(5));
                map.put("altitud",cursor.getString(6));
                map.put("velocidad",cursor.getString(7));
                map.put("proveedor",cursor.getString(8));
                map.put("fecha",cursor.getString(9));
                map.put("udpateStatus",cursor.getString(10));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }


    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM DatosGPS where udpateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id_usuario", cursor.getString(0));
                map.put("usuario", cursor.getString(1));
                map.put("latitud",cursor.getString(2));
                map.put("longitud",cursor.getString(3));
                map.put("conducta",cursor.getString(4));
                map.put("press",cursor.getString(5));
                map.put("altitud",cursor.getString(6));
                map.put("velocidad",cursor.getString(7));
                map.put("proveedor",cursor.getString(8));
                map.put("fecha",cursor.getString(9));
                map.put("udpateStatus",cursor.getString(10));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs \nestan sincronizados!";
        }else{
            msg = "La Base de Datos \nnecesita Sincronizar";
        }
        return msg;
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count =0;
        String selectQuery = "SELECT  * FROM DatosGPS where udpateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    /**
     * Update Sync status against each User ID
     * @param id_usuario
     * @param usuario
     * @param status
     */
    public void updateSyncStatus(String id_usuario,String usuario, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update DatosGPS set udpateStatus = '"+ status +"' where id_usuario="+"'"+ id_usuario +"'and usuario="+"'"+ usuario +"'";
        //Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

}
