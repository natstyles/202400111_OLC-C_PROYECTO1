package ejecucion;

public class Limpiar implements Instruccion {
    private String nombreTabla;

    public Limpiar(String nombreTabla) {
        this.nombreTabla = nombreTabla;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede vaciar la tabla. No hay BD activa.");
            return null;
        }

        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        Tabla tablaDestino = bdActual.obtenerTabla(this.nombreTabla);

        if (tablaDestino == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: La tabla '" + this.nombreTabla + "' no existe en la BD.");
            return null;
        }

        // ¡Vaciamos la lista completa de registros!
        tablaDestino.getRegistros().clear();
        bdActual.autoGuardar();

        ent.imprimir(">> ÉXITO: Instrucción CLEAR ejecutada. La tabla '" + this.nombreTabla + "' ha sido vaciada por completo.");
        return null;
    }
}