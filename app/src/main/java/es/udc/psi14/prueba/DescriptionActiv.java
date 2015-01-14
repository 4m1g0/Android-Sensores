package es.udc.psi14.prueba;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class DescriptionActiv extends Activity {
    TextView tv_name, tv_unidades;
    ListView lv_values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        long id = intent.getLongExtra("sensor_id", 0);

        setContentView(R.layout.activity_description);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_unidades = (TextView) findViewById(R.id.tv_unidades);
        lv_values = (ListView) findViewById(R.id.lv_values);

        SensorTemplateDataBaseHelper dbTemplate = new SensorTemplateDataBaseHelper(this);
        Cursor template = dbTemplate.getSensor(id);
        if (template.getCount() == 0) {
            Toast.makeText(this, getString(R.string.not_found_str),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        SensorTemplate sensorTemplate = null;
        template.moveToFirst();

        sensorTemplate = new SensorTemplate(
                template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_NOMBRE))
                , template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_UNIDADES))
                , template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_IDENTIFICADOR)),
                template.getLong(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_ID)));
        tv_name.setText(sensorTemplate.getNombre());
        tv_unidades.setText(sensorTemplate.getUnidades());


        SensorValueDataBaseHelper dbValues = new SensorValueDataBaseHelper(this);
        Cursor sensorValues = dbValues.getNSensor(sensorTemplate.getId());
        LinkedList<SensorValue> values= new LinkedList<SensorValue>();
        for (sensorValues.moveToFirst(); !sensorValues.isAfterLast(); sensorValues.moveToNext()) {

            SensorValue value = new SensorValue(
                    sensorValues.getFloat(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_SID))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA)));
;
            values.add(value);
        }

        //print list
        ArrayList<Map<String, String>> sensorListValues = new ArrayList<Map<String, String>>();
        for (SensorValue value : values) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("medida", String.valueOf(value.getMedida()) + sensorTemplate.getUnidades());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value.getFecha());
            item.put("fecha", formatter.format(calendar.getTime()));

            sensorListValues.add(item);
        }
        lv_values.setAdapter(new SensorValuesAdapter(this, sensorListValues));

    }



}
