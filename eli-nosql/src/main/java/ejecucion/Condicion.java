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
        // 1. Obtenemos el valor actual del campo en la memoria
        Object valorCampo = ent.obtener(this.campo);

        // 2. Resolvemos el valor literal contra el que vamos a comparar
        Object valorAComparar = this.valor.resolver(ent);

        if (valorCampo == null || valorAComparar == null) {
            return false;
        }

        // 3. Evaluamos la igualdad (==)
        if (this.operador.equals("==")) {
            try {
                // Intentamos convertirlos a números decimales para compararlos matemáticamente
                double numCampo = Double.parseDouble(valorCampo.toString());
                double numComparar = Double.parseDouble(valorAComparar.toString());
                return numCampo == numComparar; // ¡Aquí 101 será igual a 101.0!
            } catch (NumberFormatException e) {
                // Si da error, significa que no son números (ej. "Laptop"), así que los comparamos como texto
                return String.valueOf(valorCampo).equals(String.valueOf(valorAComparar));
            }
        }

        // (Aquí puedes agregar después el !=, >, < usando la misma lógica del try-catch)
        return false;
    }
}