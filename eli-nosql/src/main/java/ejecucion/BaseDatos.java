package ejecucion;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;

public class BaseDatos {

    @SerializedName("database") // El manual exige que la llave se llame "database"
    private String database;

    // transient significa que Gson NO lo pondrá en el archivo JSON
    private transient String ruta;

    @SerializedName("tables") // El manual exige "tables"
    private HashMap<String, Tabla> tables;

    public BaseDatos(String nombre, String ruta) {
        this.database = nombre;
        this.ruta = ruta;
        this.tables = new HashMap<>();
    }

    public String getNombre() { return database; }
    public String getRuta() { return ruta; }
    public HashMap<String, Tabla> getTablas() { return tables; }

    public void agregarTabla(Tabla tabla) {
        this.tables.put(tabla.getNombre(), tabla);
    }

    public Tabla obtenerTabla(String nombreTabla) {
        return this.tables.get(nombreTabla);
    }

    public void autoGuardar() {
        if (this.ruta == null || this.ruta.isEmpty()) return;

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(this.ruta);
            gson.toJson(this, writer);
            writer.close();
        } catch (Exception e) {
            System.out.println(">> ERROR FATAL DE PERSISTENCIA: No se pudo guardar el archivo " + this.ruta);
            e.printStackTrace();
        }
    }
}