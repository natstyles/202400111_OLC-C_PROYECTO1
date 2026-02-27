package ejecucion;

import java.util.HashMap;
import java.util.function.Consumer; // Importante agregar esto

public class Entorno {
    private HashMap<String, Object> valores;
    private Entorno anterior;

    // NUEVO: Referencia a la consola de la interfaz gráfica
    private Consumer<String> consola;

    // Actualizamos el constructor para recibir la consola
    public Entorno(Entorno anterior, Consumer<String> consola) {
        this.anterior = anterior;
        this.consola = consola;
        this.valores = new HashMap<>();
    }

    public void guardar(String id, Object valor) {
        this.valores.put(id, valor);
    }

    public Object obtener(String id) {
        if (this.valores.containsKey(id)) {
            return this.valores.get(id);
        }
        if (this.anterior != null) {
            return this.anterior.obtener(id);
        }
        return null;
    }

    // NUEVO: Método que usarán todas las instrucciones para imprimir
    public void imprimir(String mensaje) {
        if (this.consola != null) {
            this.consola.accept(mensaje);
        } else {
            System.out.println(mensaje); // Respaldo por si acaso
        }
    }
}