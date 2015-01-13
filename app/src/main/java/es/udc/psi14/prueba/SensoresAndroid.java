package es.udc.psi14.prueba;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SensoresAndroid extends Activity implements View.OnClickListener{

    public static final String TAG = SensoresAndroid.class.getName();
    private static final String CONECTIVIDAD ="estadoConectevidad";


    //  Variables GUI
    Button but_conectar, but_addSensor;
    ListView lv_sensor_list;
    ArrayList<Map<String, String>> sensorListValues;
    Switch but_led;
    SeekBar servo_bar;
    boolean estadoLed, permissionGranted, conectado;
    SensorValueDataBaseHelper dbValues;
    SensorTemplateDataBaseHelper dbTemplate;
    SensorListAdapter adapter;

    private int vecesActualizado;

    TextView tv_temperatura, tv_humedad, tv_altitud, tv_ruido, tv_luminusidad, tv_presion;

    //  Variables USB
    UsbManager mUsbManager;
    UsbDevice mUsbDevice;
    PendingIntent mPermissionIntent;
    UsbDeviceConnection mUsbDeviceConnection;
    UsbEndpoint epIN = null;
    UsbEndpoint epOUT = null;
    ArrayList<SensorTemplate> sensores;

    //  Al conectar a un dispositvo USB se solicita un permiso al usuario
    // este broadcast se encarga de recoger la respuesta del usuario.
    private static final String ACTION_USB_PERMISSION = "com.android.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, android.content.Intent intent) {
            String action = intent.getAction();

            //  Al aceptar el permiso del usuario.
            if (ACTION_USB_PERMISSION.equals(action)){
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        Log.d(TAG, getString(R.string.permisoAceptado));
                        permissionGranted = true;
                        conectado=true;
                        but_conectar.setVisibility(View.GONE);
                        but_led.setVisibility(View.VISIBLE);
                        servo_bar.setVisibility(View.VISIBLE);
                        configureComunicationUSB();
                        new UpdateSensors().execute();
                    }else{
                        Log.e(TAG, getString(R.string.permisoDenegado));
                    }
                }
            }

            //  Al desconectar el dispositivo USB cerramos las conexiones y liberamos la variables.
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    but_led.setVisibility(View.GONE);
                    servo_bar.setVisibility(View.GONE);
                    permissionGranted = false;
                    conectado = false;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState!=null) {
            permissionGranted = savedInstanceState.getBoolean(CONECTIVIDAD, false);
        }

        tv_temperatura = (TextView) findViewById(R.id.tv_temperatura);
        tv_humedad = (TextView) findViewById(R.id.tv_humedad );
        tv_ruido = (TextView) findViewById(R.id.tv_ruido);
        tv_altitud = (TextView) findViewById(R.id.tv_altitud);
        tv_luminusidad =(TextView) findViewById(R.id.tv_luminusidad);
        tv_presion =(TextView) findViewById(R.id.tv_presion);
        lv_sensor_list = (ListView) findViewById(R.id.sensor_list);


        dbValues=new SensorValueDataBaseHelper(this);
        dbTemplate=new SensorTemplateDataBaseHelper(this);

        sensores= cargaTemplates();
        sensorListValues = new ArrayList<Map<String, String>>();
        for (SensorTemplate sensor : sensores) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("name", sensor.getNombre());
            item.put("value", "0"+sensor.getUnidades());
            sensorListValues.add(item);
        }
        adapter = new SensorListAdapter(this, sensorListValues);
        lv_sensor_list.setAdapter(adapter);

        vecesActualizado=0;

        but_conectar = (Button) findViewById(R.id.but_conectar);
        but_conectar.setOnClickListener(this);
        but_addSensor = (Button) findViewById(R.id.but_addSensor);
        but_addSensor.setOnClickListener(this);
        but_led = (Switch) findViewById(R.id.led);
        but_led.setOnClickListener(this);
        lv_sensor_list = (ListView) findViewById(R.id.sensor_list);
        servo_bar = (SeekBar) findViewById(R.id.servo_bar);

        servo_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int angle;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                enviarMsg("S" + String.valueOf(angle), mUsbDeviceConnection, epOUT);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CONECTIVIDAD, permissionGranted);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registro del Broadcast
        registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

        if (permissionGranted && !conectado) {
            conectar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
    }

    protected void configureComunicationUSB() {

        mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
        if(mUsbDeviceConnection == null){
            Log.e(TAG, getString(R.string.noPosibleConect));
            finish();
        }

        //  getInterfase(1) Obtiene el tipo de comunicacion CDC (USB_CLASS_CDC_DATA)
        UsbInterface mUsbInterface = mUsbDevice.getInterface(1);

        //  Obtenemos los Endpoints de entrada y salida para el interface que hemos elegido.
        for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
            if (mUsbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (mUsbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                    epIN = mUsbInterface.getEndpoint(i);
                else
                    epOUT = mUsbInterface.getEndpoint(i);
            }
        }

        mUsbDeviceConnection.claimInterface(mUsbInterface, true);

        //  Mensaje de configuración para el Device.
        int baudRate = 115200;
        byte stopBitsByte = 1;
        byte parityBitesByte = 0;
        byte dataBits = 8;
        byte[] msg = {
                (byte) (baudRate & 0xff),
                (byte) ((baudRate >> 8) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff),
                stopBitsByte,
                parityBitesByte,
                dataBits
        };

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x20, 0, 0, msg, msg.length, 5000);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x20 -> paramtro/mensaje SetLineCoding

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x22, 0x1, 0, null, 0, 0);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x22 -> paramtro/mensaje SET_CONTROL_LINE_STATE (DTR)
        // 0x1  -> Activado.
        // Mas info: http://www.usb.org/developers/devclass_docs/usbcdc11.pdf
    }

    private void conectar() {
        // Obtemos el Manager USB del sistema Android
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //  Recuperamos todos los dispositvos USB detectados
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            mUsbDevice = deviceIterator.next();
            Log.d(TAG, getString(R.string.nombre)+ ": " + mUsbDevice.getDeviceName());
            Log.d(TAG, getString(R.string.protocolo)+ ": "+ mUsbDevice.getDeviceProtocol());

            // Solicitamos el permiso al usuario.
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);

        } else {
            Log.e(TAG, getString(R.string.dispositvoUSBNoDetec));
        }
    }

    private void enviarMsg(String msg, UsbDeviceConnection mUsbDeviceConnection, UsbEndpoint epOUT){
        int bufferMaxLength = epOUT.getMaxPacketSize();
        ByteBuffer buffer = ByteBuffer.allocate(bufferMaxLength);
        UsbRequest request = new UsbRequest(); // create an URB
        request.initialize(mUsbDeviceConnection, epOUT);

        buffer.put(msg.getBytes());

        //queue the outbound request
        boolean retval = request.queue(buffer, 1);
    }

    @Override
    public void onClick(View v) {
        if (v == but_conectar) {
            conectar();
        }else if (v == but_led) {
            String msg;
            estadoLed= but_led.isChecked();
            if (estadoLed){
                msg = "L1";
            }else{
                msg = "L0";
            }
            enviarMsg(msg, mUsbDeviceConnection, epOUT);


        }else if (v== but_addSensor){
            Intent intent = new Intent(this, AddSensorActiv.class);
            startActivityForResult(intent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // actualizamos la lista de sensores para incluir el nuevo
                sensores= cargaTemplates();
                sensorListValues.clear();
                for (SensorTemplate sensor : sensores) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("name", sensor.getNombre());
                    item.put("value", "0"+sensor.getUnidades());
                    sensorListValues.add(item);
                }
                // notificamos al adapter que se ha añadido un sensor
                adapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<SensorTemplate> cargaTemplates(){

        ArrayList<SensorTemplate> sensores= new ArrayList<SensorTemplate>();
        Cursor templates=dbTemplate.getSensores();
        Log.d(SensoresAndroid.TAG, "Cargando TEMPLATES");
        if (templates.moveToFirst()) {
            Log.d(SensoresAndroid.TAG, "Cargando TEMPLATES");
            for (templates.moveToFirst(); !templates.isAfterLast(); templates.moveToNext()) {

                SensorTemplate sensorTemplate= new SensorTemplate(
                        templates.getString(templates.getColumnIndex(SensorTemplateDataBaseHelper.COL_NOMBRE))
                        ,templates.getString(templates.getColumnIndex(SensorTemplateDataBaseHelper.COL_UNIDADES))
                        ,templates.getString(templates.getColumnIndex(SensorTemplateDataBaseHelper.COL_IDENTIFICADOR)),
                        templates.getLong(templates.getColumnIndex(SensorTemplateDataBaseHelper.COL_ID)));

                //Toast.makeText(this, sensorTemplate.toString(), Toast.LENGTH_LONG).show();
                Log.d(SensoresAndroid.TAG, "Cargando TEMPLATE: "+sensorTemplate.toString());
                sensores.add(sensorTemplate);
            }
            if (!templates.isClosed()) {
                templates.close();
            }
        }

        return sensores;
    }

    private class UpdateSensors extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {

            String line="";


            int bufferMaxLength=epIN.getMaxPacketSize();
            ByteBuffer mBuffer = ByteBuffer.allocate(bufferMaxLength);
            UsbRequest inRequest = new UsbRequest();
            inRequest.initialize(mUsbDeviceConnection, epIN);

            while(inRequest.queue(mBuffer, bufferMaxLength)){

                mUsbDeviceConnection.requestWait();

                try {
                    String salida= "";
                    // Recogemos los datos que recibimos en un
                    line = line + new String(mBuffer.array(), "UTF-8").trim();

                    if (line.length()>0){
                        //Toast.makeText(SensoresAndorid.this, line, Toast.LENGTH_LONG).show();
                        String[] msg= (line.split(";"));


                        String temperature="";
                        String humidity="";

                        String altitud="";
                        String luminusidad="";
                        String noise="";
                        String preasure="";
                        String confLed="";
                        //Log.d(TAG, getString(R.string.lineaFinal)+": " + line);
                        for(int i=0; i<msg.length; i++){
                            String[] medida = msg[i].split(":");
                            //String[] medida = msg[0].split(":");
                            SensorTemplate[] arraySensores = new SensorTemplate[sensores.size()];
                            arraySensores=sensores.toArray(arraySensores);
                            for(int j=0; j<arraySensores.length; j++){
                                if (medida[0].equals(arraySensores[j].getIdentificador()) ) {
                                    salida=salida+j+";";
                                    salida=salida+medida[1]+";";
                                    break;
                                }
                            }

                            /*
                            if (medida[0].equals("LED")){
                                confLed=medida[1];
                            }

                            if (medida[0].equals("T")){
                                temperature=medida[1];
                            }
                            if (medida[0].equals("H")){
                                humidity=medida[1];
                            }
                            if (medida[0].equals("P")) {
                                preasure = medida[1];
                            }
                            if (medida[0].equals("N")){
                                noise=medida[1];
                            }
                            if (medida[0].equals("L")){
                                luminusidad=medida[1];
                            }
                            if (medida[0].equals("A")){
                                altitud=medida[1];
                            }
                            */
                        }
                        //  Actualizamos el GUI
                        //publishProgress(humidity, temperature, altitud, noise, luminusidad, preasure,confLed);
                        publishProgress(salida);
                        line = "";

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            SensorValue sensorValue;
            String msg="";
            String[] procesar = values[0].split(";");
            Log.d(TAG, values.length+" :"+values[0]+"Nº=DATOS: " + procesar.length);
            Calendar c = Calendar.getInstance();
            for(int i=0; i<procesar.length; i+=2){

                if (!(procesar[i].isEmpty())){
                    Log.d(TAG,"Procesando: "+procesar[i]+" "+procesar[i+1]);
                    SensorTemplate sensor= sensores.get(Integer.parseInt(procesar[i]));
                    // Update adapter to change list vew information
                    sensorListValues.get(Integer.parseInt(procesar[i])).put("value", procesar[i+1]);
                    adapter.notifyDataSetChanged();

                    sensorValue = new SensorValue(Float.parseFloat(procesar[i+1]), sensor.getIdentificador(), c.getTimeInMillis());
                    dbValues.insertSensor(sensorValue);
                }

            }

            // FIXME: a partir de aqui ya esta implementado en el list view

            Cursor valores=dbValues.getNSensor(1,"H");
            Log.d(SensoresAndroid.TAG, "Cargando Humedad");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {

                    SensorValue sensorvalue= new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_humedad.setText(getString(R.string.humedad)+ ": " + sensorvalue.getMedida()+" %");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }



            valores=dbValues.getNSensor(1,"T");
            Log.d(SensoresAndroid.TAG, "Cargando temperatura");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {


                    SensorValue sensorvalue= new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_temperatura.setText(getString(R.string.temperatura)+ ": " + sensorvalue.getMedida()+" Cº");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }
            valores=dbValues.getNSensor(1,"A");
            Log.d(SensoresAndroid.TAG, "Cargando Altitud");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {

                    SensorValue sensorvalue= new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_altitud.setText(getString(R.string.altitud)+ ": " + sensorvalue.getMedida()+" m");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }

            valores=dbValues.getNSensor(1,"N");
            Log.d(SensoresAndroid.TAG, "Cargando Ruido");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {

                    SensorValue sensorvalue = new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_ruido.setText(getString(R.string.ruido) + ": " + sensorvalue.getMedida() + " db");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }

            valores=dbValues.getNSensor(1,"L");
            Log.d(SensoresAndroid.TAG, "Cargando Luminusidad");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {

                    SensorValue sensorvalue = new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_luminusidad.setText(getString(R.string.luminosidad) + ": " + sensorvalue.getMedida() + " %");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }

            valores=dbValues.getNSensor(1,"P");
            Log.d(SensoresAndroid.TAG, "Cargando Presion");
            if (valores.moveToFirst()) {
                for (valores.moveToFirst(); !valores.isAfterLast(); valores.moveToNext()) {

                    SensorValue sensorvalue = new SensorValue(
                            valores.getFloat(valores.getColumnIndex(SensorValueDataBaseHelper.COL_MEDIDA))
                            ,""//,valores.getString(valores.getColumnIndex(SensorValueDataBaseHelper.COL_IDENTIFICADOR))
                            ,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_FECHA))
                            ,1);//,valores.getLong(valores.getColumnIndex(SensorValueDataBaseHelper.COL_ID)));

                    tv_presion.setText(getString(R.string.presion) + ": " + sensorvalue.getMedida() + " atm");
                }
                if (!valores.isClosed()) {
                    valores.close();
                }

            }

            /*
            if (!(values[6].isEmpty())) {
                if (values[6].equals("H")){
                    estadoLed=true;
                }
                else {
                    estadoLed=false;
                }
                but_led.setChecked(estadoLed);
            }
            */
            //FIXME: hasta aqui!
        }

    }

}