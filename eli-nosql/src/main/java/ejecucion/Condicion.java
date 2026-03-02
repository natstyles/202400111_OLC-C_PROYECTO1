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
        // 1. Obtenemos el valor actual de la columna en la memoria
        Object valorCampo = ent.obtener(this.campo);

        // 2. Resolvemos el valor literal contra el que vamos a comparar
        Object valorAComparar = this.valor.resolver(ent);

        // Validación de seguridad por si algún campo viene nulo
        if (valorCampo == null || valorAComparar == null) {
            if (this.operador.equals("!=")) return true; // Si uno es nulo y el otro no, son diferentes
            return false;
        }

        try {
            // INTENTO 1: Comparación Matemática (Para int y float)
            double numCampo = Double.parseDouble(valorCampo.toString());
            double numComparar = Double.parseDouble(valorAComparar.toString());

            switch(this.operador) {
                case "==": return numCampo == numComparar;
                case "!=": return numCampo != numComparar;
                case ">":  return numCampo > numComparar;
                case "<":  return numCampo < numComparar;
                case ">=": return numCampo >= numComparar;
                case "<=": return numCampo <= numComparar;
            }

        } catch (NumberFormatException e) {
            // INTENTO 2: Comparación de Texto (Para strings o booleanos)
            String strCampo = String.valueOf(valorCampo);
            String strComparar = String.valueOf(valorAComparar);

            // El método compareTo devuelve 0 si son iguales,
            // un positivo si el primero es alfabéticamente mayor, y un negativo si es menor.
            int comparacion = strCampo.compareTo(strComparar);

            switch(this.operador) {
                case "==": return strCampo.equals(strComparar);
                case "!=": return !strCampo.equals(strComparar);
                case ">":  return comparacion > 0;
                case "<":  return comparacion < 0;
                case ">=": return comparacion >= 0;
                case "<=": return comparacion <= 0;
            }
        }

        return false;
    }
}