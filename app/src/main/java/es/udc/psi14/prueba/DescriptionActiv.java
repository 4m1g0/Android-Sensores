package es.udc.psi14.prueba;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

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


public class DescriptionActiv extends Activity {
    TextView tv_name, tv_unidades;
    ListView lv_values;
    private XYPlot plot1;

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
        plot1.setTitle(sensorTemplate.getNombre());


        SensorValueDataBaseHelper dbValues = new SensorValueDataBaseHelper(this);
        Cursor sensorValues = dbValues.getNSensor(sensorTemplate.getId());
        LinkedList<SensorValue> values= new LinkedList<SensorValue>();
        for (sensorValues.moveToFirst(); !sensorValues.isAfterLast(); sensorValues.moveToNext()) {

            SensorValue value = new SensorValue(
                    sensorValues.getFloat(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_SID))
                    , sensorValues.getLong(sensorValues.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA)));
            values.add(value);
        }

        //print list
        ArrayList<Map<String, String>> sensorListValues = new ArrayList<Map<String, String>>();
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
        lv_values.setAdapter(new SensorValuesAdapter(this, sensorListValues));


        Float[] numSightings= valores;
        Long[] fecha= fechas;
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(fecha),
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
        plot1.setDomainStep(XYStepMode.SUBDIVIDE, fecha.length);

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



}
