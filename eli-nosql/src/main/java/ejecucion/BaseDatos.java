package ejecucion;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.util.HashMap;

public class BaseDatos {

    @SerializedName("database")
    private String database;

    private transient String ruta;

    @SerializedName("tables")
    private HashMap<String, Tabla> tables;

    public BaseDatos(String nombre, String ruta) {
        this.database = nombre;
        this.ruta = ruta;
        this.tables = new HashMap<>();
    }

    public String getNombre() { return database; }
    public String getRuta() { return ruta; }

    //Setter para devolverle la ruta después de cargarla ---
    public void setRuta(String ruta) { this.ruta = ruta; }

    public HashMap<String, Tabla> getTablas() {
        if (this.tables == null) this.tables = new HashMap<>();
        return tables;
    }

    // --- NUEVO: Blindaje anti-NullPointerException ---
    public void agregarTabla(Tabla tabla) {
        if (this.tables == null) this.tables = new HashMap<>();
        this.tables.put(tabla.getNombre(), tabla);
    }

    public Tabla obtenerTabla(String nombreTabla) {
        if (this.tables == null) this.tables = new HashMap<>();
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