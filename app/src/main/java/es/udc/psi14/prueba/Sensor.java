package es.udc.psi14.prueba;

/**
 * Created by Santiago on 26/11/2014.
 */
public class Sensor {
    private int medida;
    private String nombre,identificador;
    private long id, fecha;


    //TODO: añandir coordenadas gps?

    public Sensor(int medida, String nombre, String identificador, long fecha ){
        this.medida=medida;
        this.nombre=nombre;
        this.identificador=identificador;
        this.fecha=fecha;
    }

    public Sensor(int medida, String nombre, String identificador, long fecha, long id ){
        this.medida=medida;
        this.nombre=nombre;
        this.identificador=identificador;
        this.fecha=fecha;
        this.id=id;
    }

    public void setMedida(int medida){
        this.medida=medida;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
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

    public int getMedida(){
        return medida;
    }

    public String getNombre(){
        return nombre;
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

