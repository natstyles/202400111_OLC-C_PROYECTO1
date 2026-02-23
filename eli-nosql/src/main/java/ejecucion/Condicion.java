package ejecucion;

public class Condicion implements Expresion {
    private String campo;     // Ej. "id"
    private String operador;  // Ej. "==", ">", "<"
    private Expresion valor;  // Ej. Literal(1)

    public Condicion(String campo, String operador, Expresion valor) {
        this.campo = campo;
        this.operador = operador;
        this.valor = valor;
    }

    @Override
    public Object resolver(Entorno ent) {
        // 1. Obtenemos el valor actual del campo en la memoria (fila actual)
        Object valorCampo = ent.obtener(this.campo);

        // 2. Resolvemos el valor literal contra el que vamos a comparar
        Object valorAComparar = this.valor.resolver(ent);

        // 3. Evaluamos (Por ahora solo haremos el '==' para probar)
        if (this.operador.equals("==")) {
            // Convertimos a String para hacer una comparación rápida y segura en Java
            return String.valueOf(valorCampo).equals(String.valueOf(valorAComparar));
        }

        // Más adelante agregaremos aquí los '>', '<', '!=', etc.
        return false;
    }
}