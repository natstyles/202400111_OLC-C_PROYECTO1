package ejecucion;

public interface Expresion {
    // Toda expresión devuelve un valor (Integer, String, Boolean, etc.)
    public Object resolver(Entorno ent);
}