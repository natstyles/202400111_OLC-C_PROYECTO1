package ejecucion;

import java.util.HashMap;

public class Entorno {
    // Tabla hash para almacenar la base de datos activa o variables
    private HashMap<String, Object> valores;
    private Entorno anterior;

    public Entorno(Entorno anterior) {
        this.anterior = anterior;
        this.valores = new HashMap<>();
    }

    // Método para guardar un valor en memoria
    public void guardar(String id, Object valor) {
        this.valores.put(id, valor);
    }

    // Método para recuperar un valor
    public Object obtener(String id) {
        if (this.valores.containsKey(id)) {
            return this.valores.get(id);
        }
        if (this.anterior != null) {
            return this.anterior.obtener(id);
        }
        return null;
    }
}