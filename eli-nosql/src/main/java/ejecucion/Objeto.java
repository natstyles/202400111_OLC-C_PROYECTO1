package ejecucion;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Objeto implements Expresion {
    private LinkedList<Asignacion> propiedades;

    public Objeto(LinkedList<Asignacion> propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public Object resolver(Entorno ent) {
        // LinkedHashMap mantiene el orden en el que insertaste las propiedades
        LinkedHashMap<String, Object> resultados = new LinkedHashMap<>();
        for (Asignacion prop : propiedades) {
            // Evaluamos el valor y lo guardamos con su clave
            resultados.put(prop.getCampo(), prop.getValor().resolver(ent));
        }
        // Gson convertirá esto directamente en un objeto JSON: { "sede": "Central", "activo": true }
        return resultados;
    }
}