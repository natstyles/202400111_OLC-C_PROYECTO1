package ejecucion;

import java.util.LinkedList;

public class Arreglo implements Expresion {
    private LinkedList<Expresion> valores;

    public Arreglo(LinkedList<Expresion> valores) {
        this.valores = valores;
    }

    @Override
    public Object resolver(Entorno ent) {
        // Resolvemos cada valor de la lista
        LinkedList<Object> resultados = new LinkedList<>();
        for (Expresion exp : valores) {
            resultados.add(exp.resolver(ent));
        }
        // Gson convertirá este LinkedList directamente en un array de JSON: [85, 90, 100]
        return resultados;
    }
}