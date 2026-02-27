package ejecucion;

import java.util.LinkedList;

public class Actualizar implements Instruccion {
    private String nombreTabla;
    private LinkedList<Asignacion> asignaciones;
    private Expresion filtro; // Puede ser null si el update no tiene filter

    public Actualizar(String nombreTabla, LinkedList<Asignacion> asignaciones, Expresion filtro) {
        this.nombreTabla = nombreTabla;
        this.asignaciones = asignaciones;
        this.filtro = filtro;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Validar que exista una base de datos en uso
        Object dbActiva = ent.obtener("db_activa");
        if (dbActiva == null) {
            ent.imprimir(">> ERROR SEMANTICO: No se puede actualizar en '" + this.nombreTabla + "'. No hay BD activa.");
            return null;
        }

        ent.imprimir(">> EXITO: Instrucción UPDATE ejecutada en la tabla '" + this.nombreTabla + "'.");

        // 2. Comprobar si hay un filtro o si afecta a toda la tabla
        if (this.filtro != null) {
            ent.imprimir("   -> Filtro detectado. Se buscarán los registros que cumplan la condición.");
            // NOTA PARA EL FUTURO: Aquí es donde recorrerás el arreglo de tu tabla,
            // meterás la fila en un Entorno temporal y harás:
            // if ((boolean) this.filtro.resolver(entornoTemporal)) { /* Actualizar fila */ }
        } else {
            ent.imprimir("   -> ADVERTENCIA: No hay filtro. Se actualizaran TODOS los registros.");
        }

        // 3. Mostrar qué valores se van a sobreescribir
        ent.imprimir("   -> Valores a asignar:");
        for (Asignacion asig : asignaciones) {
            // Resolvemos la expresión para obtener su valor real
            Object valorNuevo = asig.getValor().resolver(ent);
            System.out.println("      - " + asig.getCampo() + " = " + valorNuevo);
        }

        return null;
    }
}