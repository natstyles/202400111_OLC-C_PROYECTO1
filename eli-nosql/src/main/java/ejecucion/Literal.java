package ejecucion;

public class Literal implements Expresion {
    private Object valor;
    private String tipo; // "int", "string", "bool", etc.

    // ¡Este es el constructor que Java te está pidiendo!
    public Literal(Object valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    @Override
    public Object resolver(Entorno ent) {
        // Al ejecutar, un literal simplemente devuelve su valor real (ej. 23, "Luis", true)
        return this.valor;
    }
}