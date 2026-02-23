package ejecucion;

public class CrearBD implements Instruccion {
    private String nombreBD;
    private String rutaArchivo;

    // El constructor recibe el ID de la base de datos y la ruta del archivo JSON
    public CrearBD(String nombreBD, String rutaArchivo) {
        this.nombreBD = nombreBD;
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // Por ahora solo imprimiremos el mensaje.
        // Más adelante, en la "Capa de Persistencia", aquí llamaremos al método que crea el archivo JSON real.
        System.out.println(">> EXITO: Base de datos '" + this.nombreBD + "' definida. Persistencia en: " + this.rutaArchivo);
        return null;
    }
}