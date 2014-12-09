package es.udc.psi14.prueba;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class SensoresAndorid extends Activity implements View.OnClickListener{

    private static final String TAG = "USB-OTG";
    private static final String CONECTIVIDAD ="estadoConectevidad";

    //  Variables GUI
    Button conecta;
    Switch led;
    boolean estadoLed,estadoConectividad, conectado;
    SensoresDataBaseHelper db;


    TextView tv_temperatura, tv_humedad, tv_altitud, tv_ruido, tv_luminusidad, tv_presion;

    //  Variables USB
    UsbManager mUsbManager;
    UsbDevice mUsbDevice;
    PendingIntent mPermissionIntent;
    UsbDeviceConnection mUsbDeviceConnection;
    UsbEndpoint epIN = null;
    UsbEndpoint epOUT = null;

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
                        processComunicationUSB();
                    }else{
                        Log.e(TAG, getString(R.string.permisoDenegado));
                    }
                }
            }

            //  Al desconectar el dispositivo USB cerramos las conexiones y liberamos la variables.
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    desconectado();
                }


            }

        }
    };

    private void conectado() {
        estadoConectividad=true;
        conecta.setVisibility(View.GONE);
        led.setVisibility(View.VISIBLE);


    }

    private void desconectado() {
        led.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conectado=false;
        if (savedInstanceState!=null) {
            estadoConectividad = savedInstanceState.getBoolean(CONECTIVIDAD, false);
        }
        tv_temperatura = (TextView) findViewById(R.id.tv_temperatura);
        tv_humedad = (TextView) findViewById(R.id.tv_humedad );
        tv_ruido = (TextView) findViewById(R.id.tv_ruido);
        tv_altitud = (TextView) findViewById(R.id.tv_altitud);
        tv_luminusidad =(TextView) findViewById(R.id.tv_luminusidad);
        tv_presion =(TextView) findViewById(R.id.tv_presion);
        db=new SensoresDataBaseHelper(this);




        //  Boton Conectar.
        conecta = (Button) findViewById(R.id.conectar);
        conecta.setOnClickListener(this);
        led = (Switch) findViewById(R.id.led);
        led.setOnClickListener(this);
        led.setChecked(estadoLed);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CONECTIVIDAD,estadoConectividad);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Solicitamos permiso al usuario
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // Registro del Broadcast
        registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

        if (estadoConectividad&&!conectado) {

            conectar();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!(this.mUsbReceiver == null)){
            unregisterReceiver(mUsbReceiver);
        }

    }

    protected void processComunicationUSB() {

        boolean forceClaim = true;

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

        mUsbDeviceConnection.claimInterface(mUsbInterface, forceClaim);

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
                (byte) dataBits
        };

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x20, 0, 0, msg, msg.length, 5000);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x20 -> paramtro/mensaje SetLineCoding

        mUsbDeviceConnection.controlTransfer(UsbConstants.USB_TYPE_CLASS | 0x01, 0x22, 0x1, 0, null, 0, 0);
        // (UsbConstants.USB_TYPE_CLASS | 0x01) 0x21 -> Indica que se envia un parametro/mensaje del Host al Device (movil a la placa leonardo)
        // 0x22 -> paramtro/mensaje SET_CONTROL_LINE_STATE (DTR)
        // 0x1  -> Activado.
        // Mas info: http://www.usb.org/developers/devclass_docs/usbcdc11.pdf

        //  Ejecutar en un hilo
        new UpdateHumidityTemperature().execute();

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
            mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
            estadoConectividad=true;
            conectado=true;

            conectado();

        } else {
            Log.e(TAG, getString(R.string.dispositvoUSBNoDetec));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == conecta){
            conectar();
        } else if (v == led) {
            int bufferMaxLength = epOUT.getMaxPacketSize();
            ByteBuffer buffer = ByteBuffer.allocate(bufferMaxLength);
            UsbRequest request = new UsbRequest(); // create an URB
            request.initialize(mUsbDeviceConnection, epOUT);
            String msg;
            estadoLed=led.isChecked();
            if (estadoLed){
                msg = "1";
            }else{
                msg = "0";
            }

            buffer.put(msg.getBytes());

            //queue the outbound request
            boolean retval = request.queue(buffer, 1);
            if (mUsbDeviceConnection.requestWait() == request) {
                Toast.makeText(this, getString(R.string.enviado), Toast.LENGTH_LONG).show();

            }
        }
    }

    private class UpdateHumidityTemperature extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {

            String line="";


            int bufferMaxLength=epIN.getMaxPacketSize();
            ByteBuffer mBuffer = ByteBuffer.allocate(bufferMaxLength);
            UsbRequest inRequest = new UsbRequest();
            inRequest.initialize(mUsbDeviceConnection, epIN);

            while(inRequest.queue(mBuffer, bufferMaxLength) == true){

                mUsbDeviceConnection.requestWait();

                try {

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
                        }
                        //  Actualizamos el GUI
                        publishProgress(humidity, temperature, altitud, noise, luminusidad, preasure,confLed);

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
            Sensor sensor;

            Calendar c = Calendar.getInstance();
            if (!(values[0].isEmpty())){
                tv_humedad.setText(getString(R.string.humedad)+ ": " + values[0]+" %");
                //TODO: ocuparse del identificador
                sensor= new Sensor(Integer.parseInt(values[0]), "humedad", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[1].isEmpty())){
                tv_temperatura.setText(getString(R.string.temperatura)+ ": " +  values[1]+" Cº");
                sensor= new Sensor(Integer.parseInt(values[1]), "temperatura", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[2].isEmpty())){
                tv_altitud.setText(getString(R.string.altitud)+ ": " +  values[2]+" m");
                sensor= new Sensor(Integer.parseInt(values[2]), "altitud", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[3].isEmpty())){
                tv_ruido.setText(getString(R.string.ruido)+ ": " +  values[3]+" db");
                sensor= new Sensor(Integer.parseInt(values[3]), "ruido", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[4].isEmpty())){
                tv_luminusidad.setText(getString(R.string.luminosidad)+ ": " +  values[4]+" Cd");
                sensor= new Sensor(Integer.parseInt(values[4]), "luminosidad", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[5].isEmpty())){
                tv_presion.setText(getString(R.string.presion)+ ": " +  values[5]+" atm");
                sensor= new Sensor(Integer.parseInt(values[5]), "presion", "", c.getTimeInMillis());
                long code = db.insertSensor(sensor);
                if (code!=-1){
                }
            }
            if (!(values[6].isEmpty())) {
                if (values[6].equals("H")){
                    estadoLed=true;
                }
                else {
                    estadoLed=false;
                }
                led.setChecked(estadoLed);
            }
        }

    }

}