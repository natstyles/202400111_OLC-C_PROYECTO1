package ejecucion;

import java.util.HashMap;

public class BaseDatos {
    private String nombre;
    private String ruta;
    // Usamos un HashMap para buscar tablas rápidamente por su nombre (O(1))
    private HashMap<String, Tabla> tablas;

    public BaseDatos(String nombre, String ruta) {
        this.nombre = nombre;
        this.ruta = ruta;
        this.tablas = new HashMap<>();
    }

    public String getNombre() { return nombre; }
    public String getRuta() { return ruta; }
    public HashMap<String, Tabla> getTablas() { return tablas; }

    public void agregarTabla(Tabla tabla) {
        this.tablas.put(tabla.getNombre(), tabla);
    }

    public Tabla obtenerTabla(String nombreTabla) {
        return this.tablas.get(nombreTabla);
    }
}