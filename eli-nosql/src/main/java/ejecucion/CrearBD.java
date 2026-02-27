package ejecucion;

public class CrearBD implements Instruccion {
    private String nombreBD;
    private String rutaArchivo;

    public CrearBD(String nombreBD, String rutaArchivo) {
        this.nombreBD = nombreBD;
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // Registramos en memoria que esta base de datos YA EXISTE
        // Guardamos su ruta por si la necesitamos después
        ent.guardar("DB_" + this.nombreBD, this.rutaArchivo);

        System.out.println(">> EXITO: Base de datos '" + this.nombreBD + "' definida. Persistencia en: " + this.rutaArchivo);
        return null;
    }
}