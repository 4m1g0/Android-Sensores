package es.udc.psi14.prueba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddSensorActiv extends Activity implements View.OnClickListener {

    EditText et_nombre, et_identificador, et_unidades;
    Button but_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        et_nombre = (EditText) findViewById(R.id.et_nombre);
        et_identificador = (EditText) findViewById(R.id.et_identificador);
        et_unidades = (EditText) findViewById(R.id.et_unidades);
        but_add = (Button) findViewById(R.id.but_add);
        but_add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == but_add) {
            SensorTemplate sensor = new SensorTemplate(et_nombre.getText().toString(),et_unidades.getText().toString(),et_identificador.getText().toString());
            SensorTemplateDataBaseHelper dbTemplate = new SensorTemplateDataBaseHelper(this);
            dbTemplate.insertSensor(sensor);
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}
