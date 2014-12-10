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
public class SensorTemplateDataBaseHelper extends SQLiteOpenHelper {

    public SensorTemplateDataBaseHelper(Context context) {
        super(context, SensoresAndroid.DB_NAME, null, SensoresAndroid.DB_VERSION);
    }

    public static final String TABLE_NOMBRE = "tabla_Templates";
    public static final String COL_ID = "_id"; // critical for Adapters
    public static final String COL_IDENTIFICADOR = "identificador";
    public static final String COL_UNIDADES = "unidades";
    public static final String COL_NOMBRE = "fecha";

    String DATABASE_CREATE = "create table " + TABLE_NOMBRE + " ( "
            + COL_ID + " integer primary key autoincrement, "
            + COL_IDENTIFICADOR + " integer not null, "
            + COL_NOMBRE + " text not null, "
            + COL_UNIDADES + " text not null );";

    //TODO: añandir coordenadas gps?

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
        Log.d(SensoresAndroid.TAG, "INICIALIZANDO TEMPLATES");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SensorTemplateDataBaseHelper.class.getName(), "Upgrading db from version "
                + oldVersion + " to " + newVersion + ", which will destroy old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOMBRE);
        onCreate(db);
    }

    public void  deleteSensor(String sensor){
        getWritableDatabase().delete(TABLE_NOMBRE, COL_IDENTIFICADOR + "=?",new String[] {sensor});
    }



    public long insertSensor(SensorTemplate sensorValue) {
        ContentValues cv = new ContentValues();
        cv.put(COL_IDENTIFICADOR, sensorValue.getIdentificador());
        cv.put(COL_NOMBRE, sensorValue.getNombre());
        cv.put(COL_UNIDADES, sensorValue.getUnidades());

        return getWritableDatabase().insert(TABLE_NOMBRE, null, cv);
    }

    public void deleteSensor(long id) {
        getWritableDatabase().delete(TABLE_NOMBRE, COL_ID + "=?", new String[]
                { String.valueOf(id) });
    }
    public void updateSensor(SensorTemplate sensorValue) {
        long idNot = sensorValue.getId();
        ContentValues cv = new ContentValues();
        cv.put(COL_IDENTIFICADOR, sensorValue.getIdentificador());
        cv.put(COL_NOMBRE, sensorValue.getNombre());
        cv.put(COL_UNIDADES, sensorValue.getUnidades());

        getWritableDatabase().update(TABLE_NOMBRE,cv,COL_ID+" = "+idNot,null);
    }

    public Cursor getSensores() {
        return getWritableDatabase().query(TABLE_NOMBRE, null, null, null,
                null,null, null);
    }

    private void inicializar(){
        //String[] array = getResources().getStringArray(R.array.updateInterval);
        //insertSensor();

    }

    //TODO: Obtener sesnores en un intervalo de tiempo

    //TODO: Actualizar el nombre o identificador de un sensor
}
