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
        // 1. Validar que exista una base de datos en uso
        Object nombreDBActiva = ent.obtener("db_activa");
        if (nombreDBActiva == null) {
            ent.imprimir(">> ERROR SEMÁNTICO: No se puede exportar. No hay ninguna BD activa.");
            return null;
        }

        // 2. Extraer el objeto BaseDatos real de la memoria
        BaseDatos bdActual = (BaseDatos) ent.obtener("DB_" + nombreDBActiva);
        if (bdActual == null) {
            ent.imprimir(">> ERROR FATAL: No se encontró la estructura de la base de datos en memoria.");
            return null;
        }

        // 3. Convertir el objeto a JSON y guardarlo en el archivo
        try {
            // Configuramos Gson para que el JSON salga ordenado y legible (Pretty Printing)
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Escribimos el objeto directamente en el disco duro
            FileWriter writer = new FileWriter(this.rutaArchivo);
            gson.toJson(bdActual, writer);
            writer.close();

            ent.imprimir(">> ÉXITO: La base de datos '" + nombreDBActiva + "' fue exportada a: " + this.rutaArchivo);

        } catch (IOException e) {
            ent.imprimir(">> ERROR DE ARCHIVO: No se pudo escribir en la ruta: " + this.rutaArchivo);
            e.printStackTrace();
        }

        return null;
    }
}