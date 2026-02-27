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
        // 1. Validar qué base de datos está en uso
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede crear la tabla '" + this.nombreTabla + "'. No hay ninguna base de datos seleccionada (usa 'use <bd>;').");
            return null;
        }

        // 2. Extraer el objeto BaseDatos real de la memoria
        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        if (bdActual == null) {
            ent.imprimir(">> ERROR FATAL: La base de datos activa no se encontró en memoria.");
            return null;
        }

        // 3. Crear el objeto Tabla y guardarlo en la Base de Datos
        Tabla nuevaTabla = new Tabla(this.nombreTabla, this.columnas);
        bdActual.agregarTabla(nuevaTabla);

        ent.imprimir(">> ÉXITO: Tabla '" + this.nombreTabla + "' creada y guardada físicamente en la base de datos '" + nombreDBActiva + "'.");
        ent.imprimir("   -> Columnas registradas: " + this.columnas.size());

        return null;
    }
}