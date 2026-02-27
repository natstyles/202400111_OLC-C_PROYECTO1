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
        // 1. Instanciamos el objeto BaseDatos real
        BaseDatos nuevaBD = new BaseDatos(this.nombreBD, this.rutaArchivo);

        // 2. Lo guardamos en el Entorno (memoria RAM)
        ent.guardar("DB_" + this.nombreBD, nuevaBD);

        ent.imprimir(">> ÉXITO: Base de datos '" + this.nombreBD + "' estructurada en memoria. Persistencia en: " + this.rutaArchivo);
        return null;
    }
}