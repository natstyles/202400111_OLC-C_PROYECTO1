package ejecucion;

public class Logica implements Expresion {
    private Expresion izq;
    private String operador;
    private Expresion der;

    public Logica(Expresion izq, String operador, Expresion der) {
        this.izq = izq;
        this.operador = operador;
        this.der = der;
    }

    @Override
    public Object resolver(Entorno ent) {
        // Por ahora solo retornamos false, más adelante le daremos la lógica real
        // evaluando this.izq.resolver(ent) y this.der.resolver(ent)
        return false;
    }
}