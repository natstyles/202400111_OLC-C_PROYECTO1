package ejecucion;

public class Asignacion {
    private String campo;
    private Expresion valor;

    public Asignacion(String campo, Expresion valor) {
        this.campo = campo;
        this.valor = valor;
    }

    public String getCampo() { return campo; }
    public Expresion getValor() { return valor; }
}