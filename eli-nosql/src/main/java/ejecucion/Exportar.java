package ejecucion;

public class Exportar implements Instruccion {
    private String rutaArchivo;

    public Exportar(String rutaArchivo) {
        // Le quitamos las comillas a la cadena si es que viene con ellas desde el lexer
        this.rutaArchivo = rutaArchivo.replace("\"", "");
    }

    @Override
    public Object ejecutar(Entorno ent) {
        Object dbActiva = ent.obtener("db_activa");
        if (dbActiva == null) {
            System.out.println(">> ERROR SEMANTICO: No se puede exportar. No hay ninguna BD activa.");
            return null;
        }

        System.out.println(">> EXITO: La base de datos '" + dbActiva + "' sera exportada al archivo: " + this.rutaArchivo);

        // Más adelante: aquí tomarás toda la BD de la memoria y usarás GSON o Jackson para escribir el archivo JSON.
        return null;
    }
}