package ejecucion;

import java.util.LinkedList;

public class Leer implements Instruccion {
    private String nombreTabla;
    private LinkedList<String> campos; // Si la lista viene vacía, asumiremos que es un '*'
    private Expresion filtro;          // Puede ser null

    public Leer(String nombreTabla, LinkedList<String> campos, Expresion filtro) {
        this.nombreTabla = nombreTabla;
        this.campos = campos;
        this.filtro = filtro;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede leer. No hay BD activa.");
            return null;
        }

        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        Tabla tablaDestino = bdActual.obtenerTabla(this.nombreTabla);

        if (tablaDestino == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: La tabla '" + this.nombreTabla + "' no existe.");
            return null;
        }

        ent.imprimir(">> ÉXITO: Leyendo datos de la tabla '" + this.nombreTabla + "'...");

        // --- NUEVO: Instanciamos el objeto que guardará el resultado para exportar ---
        ResultadoConsulta resultadoExportacion = new ResultadoConsulta(this.nombreTabla);

        // --- NUEVO: Llenamos el "fields" (esquema) del resultado ---
        java.util.LinkedHashMap<String, String> esquemaOriginal = tablaDestino.getSchema();
        if (campos == null || campos.isEmpty()) {
            // Si pidieron todos los campos (*), copiamos todo el esquema original
            for (String col : esquemaOriginal.keySet()) {
                resultadoExportacion.agregarCampo(col, esquemaOriginal.get(col));
            }
        } else {
            // Si pidieron campos específicos, solo agregamos esos al esquema de exportación
            for (String campoReq : campos) {
                if (esquemaOriginal.containsKey(campoReq)) {
                    resultadoExportacion.agregarCampo(campoReq, esquemaOriginal.get(campoReq));
                }
            }
        }

        java.util.LinkedList<java.util.HashMap<String, Object>> filas = tablaDestino.getRegistros();
        if (filas.isEmpty()) {
            ent.imprimir("   -> La tabla está vacía.");
        } else {
            int contador = 1;
            for (java.util.HashMap<String, Object> fila : filas) {

                // Evaluamos el filtro
                if (this.filtro != null) {
                    Entorno entornoFila = new Entorno(ent, null);
                    for (String nombreColumna : fila.keySet()) {
                        entornoFila.guardar(nombreColumna, fila.get(nombreColumna));
                    }
                    Object resultadoFiltro = this.filtro.resolver(entornoFila);
                    if (resultadoFiltro instanceof Boolean && !(Boolean)resultadoFiltro) {
                        continue; // No cumple el filtro, saltamos a la siguiente
                    }
                }

                // --- NUEVO: Preparamos la fila filtrada para la exportación ---
                java.util.HashMap<String, Object> filaExportar = new java.util.HashMap<>();

                StringBuilder resultadoFila = new StringBuilder("   Fila " + contador + ": { ");

                if (campos == null || campos.isEmpty()) {
                    for (String columna : fila.keySet()) {
                        resultadoFila.append(columna).append(": ").append(fila.get(columna)).append(", ");
                        filaExportar.put(columna, fila.get(columna)); // Agregamos a la exportación
                    }
                } else {
                    for (String campoReq : campos) {
                        if (fila.containsKey(campoReq)) {
                            resultadoFila.append(campoReq).append(": ").append(fila.get(campoReq)).append(", ");
                            filaExportar.put(campoReq, fila.get(campoReq)); // Agregamos a la exportación
                        }
                    }
                }

                String res = resultadoFila.toString();
                if (res.endsWith(", ")) res = res.substring(0, res.length() - 2);
                res += " }";

                ent.imprimir(res);

                // --- NUEVO: Guardamos la fila en el objeto de resultado ---
                resultadoExportacion.agregarRegistro(filaExportar);
                contador++;
            }
        }

        // --- NUEVO: Guardamos toda la consulta en la memoria para que el 'export' la encuentre ---
        ent.guardar("ultima_consulta", resultadoExportacion);

        return null;
    }
}