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
        Object dbActiva = ent.obtener("db_activa");
        if (dbActiva == null) {
            System.out.println(">> ERROR SEMANTICO: No se puede leer de '" + this.nombreTabla + "'. No hay BD activa.");
            return null;
        }

        System.out.println(">> EXITO: Instruccion READ ejecutada en la tabla '" + this.nombreTabla + "'.");

        // 2. Verificar qué columnas quiere ver el usuario
        if (campos == null || campos.isEmpty()) {
            System.out.println("   -> Campos a mostrar: TODOS (*)");
        } else {
            System.out.println("   -> Campos a mostrar: " + campos.toString());
        }

        // 3. Verificar si hay condiciones de filtrado
        if (this.filtro != null) {
            System.out.println("   -> Filtro detectado. Se imprimiran solo los registros que cumplan la condicion.");
        } else {
            System.out.println("   -> No hay filtro. Se imprimiran TODOS los registros de la tabla.");
        }

        return null;
    }
}