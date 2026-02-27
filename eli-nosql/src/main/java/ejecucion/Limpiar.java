package ejecucion;

public class Limpiar implements Instruccion {
    private String nombreTabla;

    public Limpiar(String nombreTabla) {
        this.nombreTabla = nombreTabla;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Validar que exista una base de datos activa
        Object dbActiva = ent.obtener("db_activa");
        if (dbActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede vaciar la tabla '" + this.nombreTabla + "'. No hay BD activa.");
            return null;
        }

        ent.imprimir(">> EXITO: Instrucción CLEAR ejecutada. Todos los registros de la tabla '" + this.nombreTabla + "' seran eliminados.");

        // Más adelante: aquí buscarás la tabla en el Entorno y vaciarás su LinkedList de registros.
        return null;
    }
}