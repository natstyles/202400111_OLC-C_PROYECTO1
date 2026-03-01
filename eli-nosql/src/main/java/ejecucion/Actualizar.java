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
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No hay BD activa para actualizar.");
            return null;
        }

        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        Tabla tablaDestino = bdActual.obtenerTabla(this.nombreTabla);

        if (tablaDestino == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: La tabla '" + this.nombreTabla + "' no existe.");
            return null;
        }

        java.util.LinkedList<java.util.HashMap<String, Object>> filas = tablaDestino.getRegistros();
        int filasAfectadas = 0;

        // Recorremos todas las filas de la tabla
        for (java.util.HashMap<String, Object> fila : filas) {
            boolean cumpleFiltro = true;

            // Evaluamos el filtro si es que existe
            if (this.filtro != null) {
                Entorno entornoFila = new Entorno(ent, null);
                for (String nombreColumna : fila.keySet()) {
                    entornoFila.guardar(nombreColumna, fila.get(nombreColumna));
                }

                Object resultadoFiltro = this.filtro.resolver(entornoFila);
                if (resultadoFiltro instanceof Boolean && !(Boolean)resultadoFiltro) {
                    cumpleFiltro = false; // No pasó el filtro, no la actualizamos
                }
            }

            // Si pasó el filtro (o si no había filtro), sobreescribimos los datos
            if (cumpleFiltro) {
                for (Asignacion asig : asignaciones) {
                    Object valorNuevo = asig.getValor().resolver(ent);
                    // Actualizamos el valor en el HashMap de esta fila específica
                    fila.put(asig.getCampo(), valorNuevo);
                }
                filasAfectadas++;
            }
        }
        bdActual.autoGuardar();
        ent.imprimir(">> ÉXITO: Instrucción UPDATE ejecutada en '" + this.nombreTabla + "'. Filas actualizadas: " + filasAfectadas);
        return null;
    }
}