package es.udc.psi14.prueba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SensorValueDataBaseHelper extends SQLiteOpenHelper {



    private static final String DB_NAME = "SensorValues.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NOMBRE = "tabla_Values";
    public static final String COL_ID = "_id"; // critical for Adapters
    public static final String COL_SID = "identificador";
    public static final String COL_MEDIDA = "medida";
    public static final String COL_FECHA = "FECHA";

    String DATABASE_CREATE = "create table " + TABLE_NOMBRE + " ( "
            + COL_ID + " integer primary key autoincrement, "
            + COL_SID + " integer not null, "
            + COL_MEDIDA + " real not null, "
            + COL_FECHA + " integer not null );";


    public SensorValueDataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SensorTemplateDataBaseHelper.class.getName(), "Upgrading db from version "
                + oldVersion + " to " + newVersion + ", which will destroy old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOMBRE);
        onCreate(db);
    }

    public void deleteSensor(long sid) {
        getWritableDatabase().delete(TABLE_NOMBRE, COL_SID + "=?", new String[]
                { String.valueOf(sid) });
    }

    public Cursor getNSensor (int sid){
        String[] campos = new String[] {COL_MEDIDA, COL_FECHA};
        return getWritableDatabase().query(TABLE_NOMBRE, null, COL_SID+"=?", new String[] { String.valueOf(sid) }, null,null,COL_ID+" DESC" );
    }

    public long insertSensor(SensorValue sensorValue) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SID, sensorValue.getSensorId());
        cv.put(COL_MEDIDA, sensorValue.getMedida());
        cv.put(COL_FECHA, sensorValue.getFecha());

        return getWritableDatabase().insert(TABLE_NOMBRE, null, cv);
    }

    public void deleteValue(long id) {
        getWritableDatabase().delete(TABLE_NOMBRE, COL_ID + "=?", new String[]
                { String.valueOf(id) });
    }
    public void updateSensor(SensorValue sensorValue) {
        long idNot = sensorValue.getId();
        ContentValues cv = new ContentValues();
        cv.put(COL_SID, sensorValue.getSensorId());
        cv.put(COL_MEDIDA, sensorValue.getMedida());
        cv.put(COL_FECHA, sensorValue.getFecha());

        getWritableDatabase().update(TABLE_NOMBRE,cv,COL_ID+" = "+idNot,null);
    }

    public Cursor getSensores() {
        return getWritableDatabase().query(TABLE_NOMBRE, null, null, null,
                null,null, null);
    }

}