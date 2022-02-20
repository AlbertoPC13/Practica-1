package escom.practica1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    private ArrayList<String> listArchivos;
    public final String archivoZip;
    public final String directorio;

    public Zip(String archivoZip, String directorio) {
        listArchivos = new ArrayList<>();
        this.archivoZip = archivoZip;
        this.directorio = directorio;
    }

    public void generarZip(String zipFile) throws IOException {

        byte[] buffer = new byte[1024];

        try ( FileOutputStream fos = new FileOutputStream(zipFile);  ZipOutputStream zos = new ZipOutputStream(fos)) {

            System.out.println("Generando zip: " + zipFile);

            for (String archivo : this.listArchivos) {

                System.out.println("Archivo agregado: " + archivo);
                ZipEntry ze = new ZipEntry(archivo);
                zos.putNextEntry(ze);

                FileInputStream fis = new FileInputStream(directorio + File.separator + archivo);

                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                fis.close();
            }

            zos.closeEntry();

            System.out.println("Archivo zip creado...\n\n");
        }

    }

    //Recorre el directorio, obtiene todos los archivos y los agrega a la lista de archivos
    public void generarListaArchivos(File f) {
        // Agregar archivo
        if (f.isFile()) {
            listArchivos.add(generarZipEntry(f.getAbsoluteFile().toString()));
        }

        // Agregar directorio
        if (f.isDirectory()) {
            String[] subArchivos = f.list();
            for (String nombreArchivo : subArchivos) {
                generarListaArchivos(new File(f, nombreArchivo));
            }
        }
    }

    //Cambia el formato de la ruta del directorio a ruta de archivo zip
    private String generarZipEntry(String file) {
        return file.substring(directorio.length() + 1, file.length());
    }
}
