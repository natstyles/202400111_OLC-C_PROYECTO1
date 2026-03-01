package ejecucion;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.HashMap;

public class ResultadoConsulta {
    @SerializedName("table")
    private String table;

    @SerializedName("fields")
    private LinkedHashMap<String, String> fields;

    @SerializedName("records")
    private LinkedList<HashMap<String, Object>> records;

    public ResultadoConsulta(String table) {
        this.table = table;
        this.fields = new LinkedHashMap<>();
        this.records = new LinkedList<>();
    }

    public void agregarCampo(String nombre, String tipo) {
        this.fields.put(nombre, tipo);
    }

    public void agregarRegistro(HashMap<String, Object> registro) {
        this.records.add(registro);
    }
}