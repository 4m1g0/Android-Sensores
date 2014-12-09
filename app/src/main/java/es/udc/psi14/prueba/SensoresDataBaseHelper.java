package es.udc.psi14.prueba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Santiago on 26/11/2014.
 */
public class SensoresDataBaseHelper extends SQLiteOpenHelper {

    public SensoresDataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private static final String DB_NAME = "Sensores.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NOMBRE = "tabla_sensores";
    public static final String COL_ID = "_id"; // critical for Adapters
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_IDENTIFICADOR = "identificador";
    public static final String COL_MEDIDA = "medida";
    public static final String COL_FECHA = "FECHA";

    String DATABASE_CREATE = "create table " + TABLE_NOMBRE + " ( "
            + COL_ID + " integer primary key autoincrement, "
            + COL_NOMBRE + " text not null, "
            + COL_IDENTIFICADOR + " text not null, "
            + COL_MEDIDA + " integer not null, "
            + COL_FECHA + " integer not null );";

    //TODO: añandir coordenadas gps?

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SensoresDataBaseHelper.class.getName(), "Upgrading db from version "
                + oldVersion + " to " + newVersion + ", which will destroy old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOMBRE);
        onCreate(db);
    }

    public void  deleteSensor(String sensor){
        getWritableDatabase().delete(TABLE_NOMBRE, COL_NOMBRE + "=?",new String[] {sensor});
    }

    public Cursor getNSensor (int N, String sensor){
        String[] campos = new String[] {COL_MEDIDA, COL_FECHA};
        String[] args = new String[] {"sensor"};
        return getWritableDatabase().query(TABLE_NOMBRE, campos, COL_NOMBRE+"=?", args, null,null,COL_ID+" DESC" , String.valueOf(N));
    }

    public long insertSensor(Sensor sensor) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE, sensor.getNombre());
        cv.put(COL_IDENTIFICADOR, sensor.getIdentificador());
        cv.put(COL_MEDIDA, sensor.getMedida());
        cv.put(COL_FECHA, sensor.getFecha());

        return getWritableDatabase().insert(TABLE_NOMBRE, null, cv);
    }

    public void deleteSensor(long id) {
        getWritableDatabase().delete(TABLE_NOMBRE, COL_ID + "=?", new String[]
                { String.valueOf(id) });
    }
    public void updateSensor(Sensor sensor) {
        long idNot = sensor.getId();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOMBRE, sensor.getNombre());
        cv.put(COL_IDENTIFICADOR, sensor.getIdentificador());
        cv.put(COL_MEDIDA, sensor.getMedida());
        cv.put(COL_FECHA, sensor.getFecha());

        getWritableDatabase().update(TABLE_NOMBRE,cv,COL_ID+" = "+idNot,null);
    }

    public Cursor getSesnores() {
        return getWritableDatabase().query(TABLE_NOMBRE, null, null, null,
                null,null, null);
    }

    //TODO: Obtener sesnores en un intervalo de tiempo

    //TODO: Actualizar el nombre o identificador de un sensor
}
