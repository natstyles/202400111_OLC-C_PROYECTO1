package ejecucion;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.HashMap;

public class Tabla {
    // transient para que no salga en el JSON (ya que la llave del diccionario ya tiene el nombre)
    private transient String nombre;

    @SerializedName("schema")
    private LinkedHashMap<String, String> schema;

    @SerializedName("records")
    private LinkedList<HashMap<String, Object>> records;

    public Tabla(String nombre, LinkedList<Columna> columnas) {
        this.nombre = nombre;
        this.schema = new LinkedHashMap<>();

        // Convertimos la lista de columnas al formato {"columna": "tipo"} que pide el manual
        for(Columna col : columnas) {
            this.schema.put(col.getNombre(), col.getTipo());
        }

        this.records = new LinkedList<>();
    }

    public String getNombre() { return nombre; }
    public LinkedHashMap<String, String> getSchema() { return schema; }
    public LinkedList<HashMap<String, Object>> getRegistros() { return records; }

    public void insertarRegistro(HashMap<String, Object> fila) {
        this.records.add(fila);
    }
}