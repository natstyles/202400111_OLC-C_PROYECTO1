package ejecucion;

import java.util.LinkedList;

public class CrearTabla implements Instruccion {
    private String nombreTabla;
    private LinkedList<Columna> columnas;

    public CrearTabla(String nombreTabla, LinkedList<Columna> columnas) {
        this.nombreTabla = nombreTabla;
        this.columnas = columnas;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Validar que exista una base de datos en uso (regla del manual)
        Object dbActiva = ent.obtener("db_activa");

        if (dbActiva == null) {
            System.out.println(">> ERROR SEMANTICO: No se puede crear la tabla '" + this.nombreTabla + "'. No hay ninguna base de datos seleccionada (usa 'use <bd>;').");
            return null;
        }

        // 2. Si todo está bien, "creamos" la tabla
        System.out.println(">> EXITO: Tabla '" + this.nombreTabla + "' creada en la base de datos '" + dbActiva + "'.");
        System.out.println("   -> Columnas registradas: " + this.columnas.size());

        // (Más adelante, aquí guardaremos la estructura de la tabla en el Entorno)
        return null;
    }
}