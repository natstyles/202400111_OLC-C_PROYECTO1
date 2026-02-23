package ejecucion;

import java.util.LinkedList;

public class Insertar implements Instruccion {
    private String nombreTabla;
    private LinkedList<Asignacion> asignaciones;

    public Insertar(String nombreTabla, LinkedList<Asignacion> asignaciones) {
        this.nombreTabla = nombreTabla;
        this.asignaciones = asignaciones;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Validar que exista una base de datos en uso
        Object dbActiva = ent.obtener("db_activa");
        if (dbActiva == null) {
            System.out.println(">> ERROR SEMÁNTICO: No se puede insertar en '" + this.nombreTabla + "'. No hay BD activa.");
            return null;
        }

        System.out.println(">> ÉXITO: Registro insertado en la tabla '" + this.nombreTabla + "'. Datos:");

        // 2. Recorremos las asignaciones y RESOLVEMOS las expresiones para obtener su valor real
        for (Asignacion asig : asignaciones) {
            // Aquí llamamos al resolver() de la expresión (por ahora Literales)
            Object valorReal = asig.getValor().resolver(ent);
            System.out.println("   - " + asig.getCampo() + ": " + valorReal);
        }

        return null;
    }
}