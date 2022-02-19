package escom.practica1;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZip {

    private static final String INPUT_ZIP_FILE = "C:\\Users\\Betuc\\Documents\\ERDE.zip";
    private static final String OUTPUT_FOLDER = "C:\\Users\\Betuc\\Documents\\ERDE_UnZip";

    /*public static void main(String[] args) throws IOException {
        UnZip unZipDemo = new UnZip();
        unZipDemo.unZip(INPUT_ZIP_FILE, OUTPUT_FOLDER);
    }*/

    public void descomprimirZip(String ArchivoZip, String directorioSalida) throws IOException {

        crearDirectorio(directorioSalida);
        
        try ( ZipInputStream zis = new ZipInputStream(new FileInputStream(ArchivoZip))) {
            //Obtiene la lista de archivos en el archivo zip
            ZipEntry ze = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while (ze != null) {
                String nombreArchivo = ze.getName();
                File archivoNuevo = new File(directorioSalida + File.separator + nombreArchivo);

                System.out.println("Archivo descomprimido: " + archivoNuevo.getAbsoluteFile());

                //Crea los directorios no existentes
                new File(archivoNuevo.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(archivoNuevo);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();

            System.out.println("Descompresion completada...");
        }
    }

    private void crearDirectorio(String directorioSalida) {
        //Crea el directorio en caso de que no exista
        File folder = new File(directorioSalida);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
}
