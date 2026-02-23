package ejecucion;

public class Columna {
    private String nombre;
    private String tipo;

    public Columna(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
}