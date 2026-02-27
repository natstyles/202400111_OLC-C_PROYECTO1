package ejecucion;

public class UseDB implements Instruccion {
    private String nombreDB;

    public UseDB(String nombreDB) {
        this.nombreDB = nombreDB;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Preguntamos a la memoria si existe una BD registrada con este nombre
        Object bdExiste = ent.obtener("DB_" + this.nombreDB);

        // 2. Si nos devuelve null, significa que no existe
        if (bdExiste == null) {
            System.out.println(">> ERROR SEMANTICO: La base de datos '" + this.nombreDB + "' no ha sido definida. Imposible seleccionarla.");
            return null; // Detenemos la ejecución de esta instrucción
        }

        // 3. Si pasó la validación, la establecemos como activa
        ent.guardar("db_activa", this.nombreDB);
        System.out.println(">> EXITO: Base de datos '" + this.nombreDB + "' seleccionada para su uso.");

        return null;
    }
}