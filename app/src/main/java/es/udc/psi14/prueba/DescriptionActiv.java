package es.udc.psi14.prueba;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class DescriptionActiv extends Activity implements View.OnClickListener{
    TextView tv_name, tv_unidades;
    ListView lv_values;
    private XYPlot plot1;
    Button but_export, but_import;
    LinkedList<SensorValue> values;
    SensorTemplate sensorTemplate;
    SensorValueDataBaseHelper dbValues;
    SensorValuesAdapter adapter;
    ArrayList<Map<String, String>> sensorListValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        long id = intent.getLongExtra("sensor_id", 0);

        setContentView(R.layout.activity_description);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_unidades = (TextView) findViewById(R.id.tv_unidades);
        lv_values = (ListView) findViewById(R.id.lv_values);
        plot1 = (XYPlot) findViewById(R.id.plot1);
        but_export = (Button) findViewById(R.id.but_export);
        but_export.setOnClickListener(this);
        but_import = (Button) findViewById(R.id.but_import);
        but_import.setOnClickListener(this);


        SensorTemplateDataBaseHelper dbTemplate = new SensorTemplateDataBaseHelper(this);
        Cursor template = dbTemplate.getSensor(id);
        if (template.getCount() == 0) {
            Toast.makeText(this, getString(R.string.not_found_str),Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        template.moveToFirst();

        sensorTemplate = new SensorTemplate(
                template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_NOMBRE))
                , template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_UNIDADES))
                , template.getString(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_IDENTIFICADOR)),
                template.getLong(template.getColumnIndex(SensorTemplateDataBaseHelper.COL_ID)));
        tv_name.setText(sensorTemplate.getNombre());
        tv_unidades.setText(sensorTemplate.getUnidades());
        plot1.setTitle(sensorTemplate.getNombre());


        dbValues = new SensorValueDataBaseHelper(this);
        Cursor sensorValues = dbValues.getNSensor(sensorTemplate.getId());
        values= new LinkedList<SensorValue>();
        for (sensorValues.moveToFirst(); !sensorValues.isAfterLast(); sensorValues.moveToNext()) {

            SensorValue value = new SensorValue(
                    sensorValues.getFloat(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_SID))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA)));
            values.add(value);
        }

        //print list
        sensorListValues = new ArrayList<Map<String, String>>();
        Long[]fechas=new Long[values.size()];
        Float[]valores=new Float[values.size()];
        int cont=0;
        for (SensorValue value : values) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("medida", String.valueOf(value.getMedida()) + sensorTemplate.getUnidades());

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value.getFecha());
            item.put("fecha", formatter.format(calendar.getTime()));

            fechas[cont]=calendar.getTimeInMillis();
            valores[cont]=value.getMedida();

            sensorListValues.add(item);
        }
        adapter = new SensorValuesAdapter(this, sensorListValues);
        lv_values.setAdapter(adapter);


        //Float[] numSightings= valores;
        //Long[] fecha= fechas;

        Number[] numSightings = {5, 8, 9, 2, 5};

        // an array of years in milliseconds:
        Number[] years = {
                978307200,  // 2001
                1009843200, // 2002
                1041379200, // 2003
                1072915200, // 2004
                1104537600  // 2005
        };

        Log.e("pene", "Seclecionados: " + fechas.length + "valores" );
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(years),
                Arrays.asList(numSightings),
                sensorTemplate.getNombre());

        plot1.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 100, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                Color.rgb(100, 200, 0), null);                // fill color


        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);

        // ugly usage of LinearGradient. unfortunately there's no way to determine the actual size of
        // a View from within onCreate.  one alternative is to specify a dimension in resources
        // and use that accordingly.  at least then the values can be customized for the device type and orientation.
        lineFill.setShader(new LinearGradient(0, 0, 200, 200, Color.WHITE, Color.GREEN, Shader.TileMode.CLAMP));

        LineAndPointFormatter formatter  =
                new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED, null);
        formatter.setFillPaint(lineFill);
        plot1.getGraphWidget().setPaddingRight(2);
        plot1.addSeries(series2, formatter);

        // draw a domain tick for each year:
        plot1.setDomainStep(XYStepMode.SUBDIVIDE, years.length);

        // customize our domain/range labels
        plot1.setDomainLabel("hora");
        plot1.setRangeLabel(sensorTemplate.getUnidades());

        // get rid of decimal points in our range labels:
        plot1.setRangeValueFormat(new DecimalFormat("0"));

        plot1.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });

    }


    @Override
    public void onClick(View v) {
        if (v == but_export) {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, sensorTemplate.getNombre() + ".csv");

                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
                for (SensorValue value : values) {
                    myOutWriter.append(value.getId() + ", " + value.getFecha() + ", " + value.getSensorId() + ", " + value.getMedida() + "\n");
                }

                myOutWriter.close();
                fOut.close();
                Toast.makeText(v.getContext(),getString(R.string.exported_str), Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else if (v == but_import) {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, sensorTemplate.getNombre() + ".csv");
                FileInputStream fIn = new FileInputStream(file);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow = "";
                String aBuffer[];
                while ((aDataRow = myReader.readLine()) != null)
                {
                    aBuffer = aDataRow.split(",");
                    SensorValue value = new SensorValue(Float.parseFloat(aBuffer[3].trim()), Long.parseLong(aBuffer[2].trim()), Long.parseLong(aBuffer[1].trim()), Long.parseLong(aBuffer[0].trim()));
                    dbValues.insertSensor(value);
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("medida", String.valueOf(value.getMedida()) + sensorTemplate.getUnidades());

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(value.getFecha());
                    item.put("fecha", formatter.format(calendar.getTime()));

                    sensorListValues.add(item);
                }
                adapter.notifyDataSetChanged();
                myReader.close();
                Toast.makeText(v.getContext(),getString(R.string.imported_str),Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(v.getContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
