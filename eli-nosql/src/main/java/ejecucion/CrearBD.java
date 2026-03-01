package ejecucion;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;

public class CrearBD implements Instruccion {
    private String nombreBD;
    private String rutaArchivo;

    public CrearBD(String nombreBD, String rutaArchivo) {
        this.nombreBD = nombreBD;
        // Le quitamos las comillas a la ruta por si vienen desde el parser
        this.rutaArchivo = rutaArchivo.replace("\"", "");
    }

    @Override
    public Object ejecutar(Entorno ent) {
        File archivo = new File(this.rutaArchivo);

        // 1. Verificamos si el archivo ya existe en el disco duro
        if (archivo.exists()) {
            try {
                // Si existe, leemos el archivo y Gson hace la "magia inversa" (Deserialización)
                Gson gson = new Gson();
                FileReader reader = new FileReader(archivo);

                // Le decimos a Gson que convierta el texto del JSON a nuestro objeto BaseDatos
                BaseDatos bdCargada = gson.fromJson(reader, BaseDatos.class);
                reader.close();

                // Guardamos la base de datos recuperada en la memoria RAM
                ent.guardar("DB_" + this.nombreBD, bdCargada);
                ent.imprimir(">> ÉXITO: Base de datos '" + this.nombreBD + "' CARGADA desde el archivo: " + this.rutaArchivo);

            } catch (Exception e) {
                ent.imprimir(">> ERROR AL CARGAR: No se pudo leer el archivo JSON de la base de datos.");
                e.printStackTrace();
            }
        } else {
            // 2. Si no existe, creamos una nueva base de datos vacía (como lo hacíamos antes)
            BaseDatos nuevaBD = new BaseDatos(this.nombreBD, this.rutaArchivo);
            ent.guardar("DB_" + this.nombreBD, nuevaBD);
            ent.imprimir(">> ÉXITO: Base de datos '" + this.nombreBD + "' CREADA. Persistencia asignada a: " + this.rutaArchivo);
        }

        return null;
    }
}