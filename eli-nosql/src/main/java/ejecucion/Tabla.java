package ejecucion;

import java.util.HashMap;
import java.util.LinkedList;

public class Tabla {
    private String nombre;
    private LinkedList<Columna> columnas;
    // Cada fila será un HashMap donde la llave es el nombre de la columna y el Object es el valor
    private LinkedList<HashMap<String, Object>> registros;

    public Tabla(String nombre, LinkedList<Columna> columnas) {
        this.nombre = nombre;
        this.columnas = columnas;
        this.registros = new LinkedList<>();
    }

    public String getNombre() { return nombre; }
    public LinkedList<Columna> getColumnas() { return columnas; }
    public LinkedList<HashMap<String, Object>> getRegistros() { return registros; }

    // Método para insertar una nueva fila
    public void insertarRegistro(HashMap<String, Object> fila) {
        this.registros.add(fila);
    }
}