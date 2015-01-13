package es.udc.psi14.prueba;

public class SensorValue {
    private float medida;
    private long sensorId;
    private long id, fecha;

    public SensorValue(float medida, long sid, long fecha){
        this.medida=medida;
        this.sensorId=sid;
        this.fecha=fecha;
    }

    public SensorValue(float medida, long sid, long fecha, long id){
        this.medida=medida;
        this.sensorId=sid;
        this.fecha=fecha;
        this.id=id;
    }

    public void setMedida(int medida){
        this.medida=medida;
    }

    public void setSensorId(long sid){
        this.sensorId=sid;
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

    public long getSensorId(){
        return sensorId;
    }

    public long getFecha(){
        return fecha;
    }

    public long getId(){
        return id;
    }
}

