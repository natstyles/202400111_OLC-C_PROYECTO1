package ejecucion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class Exportar implements Instruccion {
    private String rutaArchivo;

    public Exportar(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo.replace("\"", "");
    }

    @Override
    public Object ejecutar(Entorno ent) {
        // 1. Buscamos en la memoria si se ejecutó un READ anteriormente
        Object consultaPrevia = ent.obtener("ultima_consulta");

        if (consultaPrevia == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No hay ninguna consulta previa (read) para exportar.");
            return null;
        }

        // 2. Convertimos el objeto ResultadoConsulta a JSON
        try {
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            java.io.FileWriter writer = new java.io.FileWriter(this.rutaArchivo);

            // Le pasamos la consulta en lugar de la base de datos completa
            gson.toJson(consultaPrevia, writer);
            writer.close();

            ent.imprimir(">> ÉXITO: Consulta exportada con el formato oficial a: " + this.rutaArchivo);

        } catch (java.io.IOException e) {
            ent.imprimir(">> ERROR DE ARCHIVO: No se pudo escribir en la ruta: " + this.rutaArchivo);
            e.printStackTrace();
        }

        return null;
    }
}