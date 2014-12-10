package es.udc.psi14.prueba;

/**
 * Created by Santiago on 10/12/2014.
 */
public class SensorTemplate{
    private String nombre, unidades;
    private long id, identificador;



    public SensorTemplate(String nombre, String unidades, long identificador){
        this.nombre=nombre;
        this.identificador=identificador;
        this.unidades=unidades;
    }

    public SensorTemplate(String nombre, String unidades, long identificador, long id){
        this.nombre=nombre;
        this.identificador=identificador;
        this.unidades=unidades;
        this.id=id;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public void setIdentificador(long identificador){
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

    public long getIdentificador(){
        return identificador;
    }

    public String getUnidades(){
        return unidades;
    }

    public long getId(){
        return id;
    }
}