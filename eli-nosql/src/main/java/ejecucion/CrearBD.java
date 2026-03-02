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
                Gson gson = new Gson();
                FileReader reader = new FileReader(archivo);

                BaseDatos bdCargada = gson.fromJson(reader, BaseDatos.class);
                reader.close();

                //Le devolvemos la ruta porque Gson la ignoró al ser transient
                if (bdCargada != null) {
                    bdCargada.setRuta(this.rutaArchivo);
                } else {
                    // Si el JSON estaba completamente vacío/corrupto, creamos una nueva
                    bdCargada = new BaseDatos(this.nombreBD, this.rutaArchivo);
                }

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
            nuevaBD.autoGuardar();
            ent.imprimir(">> ÉXITO: Base de datos '" + this.nombreBD + "' CREADA. Persistencia asignada a: " + this.rutaArchivo);
        }

        return null;
    }
}