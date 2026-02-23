package ejecucion;

public class UseDB implements Instruccion {
    private String nombreDB;

    public UseDB(String nombreDB) {
        this.nombreDB = nombreDB;
    }

    @Override
    public Object ejecutar(Entorno ent) {
        ent.guardar("db_activa", this.nombreDB);
        System.out.println(">> EXITO: Base de datos '" + this.nombreDB + "' seleccionada para su uso.");
        return null;
    }
}