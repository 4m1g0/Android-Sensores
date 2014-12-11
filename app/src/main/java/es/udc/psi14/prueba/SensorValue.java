package es.udc.psi14.prueba;

/**
 * Created by Santiago on 26/11/2014.
 */
public class SensorValue {
    private float medida;
    private String identificador;
    private long id, fecha;


    //TODO: añandir coordenadas gps?

    public SensorValue(float medida, String identificador, long fecha){
        this.medida=medida;
        this.identificador=identificador;
        this.fecha=fecha;
    }

    public SensorValue(float medida, String identificador, long fecha, long id){
        this.medida=medida;
        this.identificador=identificador;
        this.fecha=fecha;
        this.id=id;
    }

    public void setMedida(int medida){
        this.medida=medida;
    }

    public void setIdentificador(String identificador){
        this.identificador=identificador;
    }

    public void setFecha(long fecha){
        this.fecha=fecha;
    }

    public void setId(long id){
        this.id=id;
    }

    public float getMedida(){
        return medida;
    }

    public String getIdentificador(){
        return identificador;
    }

    public long getFecha(){
        return fecha;
    }

    public long getId(){
        return id;
    }
}

