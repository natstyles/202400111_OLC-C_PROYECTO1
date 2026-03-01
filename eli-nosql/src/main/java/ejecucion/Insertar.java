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
        // 1. Validar la base de datos activa
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede insertar en '" + this.nombreTabla + "'. No hay BD activa.");
            return null;
        }

        // 2. Obtener el objeto BaseDatos y luego buscar la Tabla
        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        Tabla tablaDestino = bdActual.obtenerTabla(this.nombreTabla);

        // 3. Validar que la tabla exista
        if (tablaDestino == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: La tabla '" + this.nombreTabla + "' no existe en la base de datos '" + nombreDBActiva + "'.");
            return null;
        }

        // 4. Crear la nueva "fila" (un HashMap de Columna -> Valor)
        java.util.HashMap<String, Object> nuevaFila = new java.util.HashMap<>();

        ent.imprimir(">> ÉXITO: Guardando registro en la tabla '" + this.nombreTabla + "'. Datos:");

        // 5. Recorrer las asignaciones, resolverlas y guardarlas en la fila
        for (Asignacion asig : asignaciones) {
            Object valorReal = asig.getValor().resolver(ent);
            nuevaFila.put(asig.getCampo(), valorReal);
            ent.imprimir("   - [" + asig.getCampo() + "] guardado con valor: " + valorReal);
        }

        // 6. ¡Insertar la fila en la tabla real!
        tablaDestino.insertarRegistro(nuevaFila);
        bdActual.autoGuardar();

        return null;
    }
}