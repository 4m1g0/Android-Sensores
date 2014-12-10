package es.udc.psi14.prueba;

/**
 * Created by Santiago on 26/11/2014.
 */
public class SensorValue {
    private float medida;
    private long identificador;
    private long id, fecha;


    //TODO: añandir coordenadas gps?

    public SensorValue(float medida, long identificador, long fecha){
        this.medida=medida;
        this.identificador=identificador;
        this.fecha=fecha;
    }

    public SensorValue(float medida, long identificador, long fecha, long id){
        this.medida=medida;
        this.identificador=identificador;
        this.fecha=fecha;
        this.id=id;
    }

    public void setMedida(int medida){
        this.medida=medida;
    }

    public void setIdentificador(long identificador){
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

    public long getIdentificador(){
        return identificador;
    }

    public long getFecha(){
        return fecha;
    }

    public long getId(){
        return id;
    }
}

