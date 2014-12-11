package es.udc.psi14.prueba;

/**
 * Created by Santiago on 10/12/2014.
 */
public class SensorTemplate{
    private String nombre, unidades, identificador;
    private long id;



    public SensorTemplate(String nombre, String unidades, String identificador){
        this.nombre=nombre;
        this.identificador=identificador;
        this.unidades=unidades;
    }

    public SensorTemplate(String nombre, String unidades, String identificador, long id){
        this.nombre=nombre;
        this.identificador=identificador;
        this.unidades=unidades;
        this.id=id;
    }


    @Override
    public String toString() {
        return "Sensor " + nombre + " con identificador " + identificador + " y unidades: " + unidades;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public void setIdentificador(String identificador){
        this.identificador=identificador;
    }

    public void setUnidades(String unidades){
        this.unidades=unidades;
    }

    public void setId(long id){
        this.id=id;
    }

    public String getNombre(){
        return nombre;
    }

    public String getIdentificador(){
        return identificador;
    }

    public String getUnidades(){
        return unidades;
    }

    public long getId(){
        return id;
    }
}