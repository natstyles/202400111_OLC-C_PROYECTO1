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
        // 1. Validar que exista una base de datos activa
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede leer. No hay BD activa.");
            return null;
        }

        // 2. Extraer la Base de Datos y buscar la Tabla
        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        Tabla tablaDestino = bdActual.obtenerTabla(this.nombreTabla);

        // 3. Validar que la tabla exista en la memoria
        if (tablaDestino == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: La tabla '" + this.nombreTabla + "' no existe en la base de datos '" + nombreDBActiva + "'.");
            return null;
        }

        ent.imprimir(">> ÉXITO: Leyendo datos de la tabla '" + this.nombreTabla + "'...");

        // 4. Extraer las filas (registros) de la tabla
        java.util.LinkedList<java.util.HashMap<String, Object>> filas = tablaDestino.getRegistros();

        if (filas.isEmpty()) {
            ent.imprimir("   -> La tabla está vacía.");
            return null;
        }

// 5. Recorrer, Filtrar e Imprimir cada fila
        int contador = 1;
        for (java.util.HashMap<String, Object> fila : filas) {

            //FILTRAMOS
            if (this.filtro != null) {
                // Creamos un entorno temporal (hijo del entorno global)
                Entorno entornoFila = new Entorno(ent, null);

                // Cargamos todas las columnas de esta fila como si fueran variables
                for (String nombreColumna : fila.keySet()) {
                    entornoFila.guardar(nombreColumna, fila.get(nombreColumna));
                }

                // Le pedimos a la condición que se evalúe usando los datos de esta fila
                Object resultadoFiltro = this.filtro.resolver(entornoFila);

                // Si el filtro devuelve falso, saltamos a la siguiente fila ignorando esta
                if (resultadoFiltro instanceof Boolean && !(Boolean)resultadoFiltro) {
                    continue;
                }
            }
            //TERMINA FILTRO

            // Si llegamos aquí, es porque NO HAY FILTRO o la fila SÍ CUMPLIÓ la condición
            StringBuilder resultadoFila = new StringBuilder("   Fila " + contador + ": { ");

            if (campos == null || campos.isEmpty()) {
                // Todos los campos (*)
                for (String columna : fila.keySet()) {
                    resultadoFila.append(columna).append(": ").append(fila.get(columna)).append(", ");
                }
            } else {
                // Campos específicos
                for (String campoReq : campos) {
                    if (fila.containsKey(campoReq)) {
                        resultadoFila.append(campoReq).append(": ").append(fila.get(campoReq)).append(", ");
                    }
                }
            }

            String res = resultadoFila.toString();
            if (res.endsWith(", ")) res = res.substring(0, res.length() - 2);
            res += " }";

            ent.imprimir(res);
            contador++;
        }

        // (La lógica del filtro la integraremos en el siguiente paso)
        return null;
    }
}