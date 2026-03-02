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
        // 1. Siempre resolvemos el lado derecho (porque el NOT solo tiene lado derecho)
        Object valDer = (this.der != null) ? this.der.resolver(ent) : false;

        // --- CASO ESPECIAL: NOT (!) ---
        if (this.operador.equals("!")) {
            if (valDer instanceof Boolean) {
                return !(Boolean) valDer; // Invertimos el valor
            }
            return false;
        }

        // 2. Resolvemos el lado izquierdo (para el AND y el OR)
        Object valIzq = (this.izq != null) ? this.izq.resolver(ent) : false;

        // 3. Verificamos que ambas partes hayan devuelto un true/false
        if (valIzq instanceof Boolean && valDer instanceof Boolean) {
            boolean booleanoIzq = (Boolean) valIzq;
            boolean booleanoDer = (Boolean) valDer;

            switch (this.operador) {
                case "&&": return booleanoIzq && booleanoDer;
                case "||": return booleanoIzq || booleanoDer;
            }
        }

        return false;
    }
}