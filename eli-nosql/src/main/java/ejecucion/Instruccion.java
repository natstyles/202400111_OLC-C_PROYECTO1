package ejecucion;

public interface Instruccion {
    // Toda instrucción ejecutará una acción sobre un Entorno (memoria)
    public Object ejecutar(Entorno ent);
}