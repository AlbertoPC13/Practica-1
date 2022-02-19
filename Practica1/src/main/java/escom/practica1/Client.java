/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escom.practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JFileChooser;

/**
 *
 * @author Betuc
 */
public class Client {

    public static final int puertoServidor = 8000;
    public static final int puertoTransferencia = 8080;

    Socket s;
    File f;
    String rutaArchivo;

    public Client(int puerto) {

        try {
            s = new Socket("localhost", puerto);
            s.setReuseAddress(true);

            File aux = new File("");
            rutaArchivo = aux.getAbsolutePath() + "\\localDrive\\";
            f = new File(rutaArchivo);
            f.mkdirs();
            f.setReadable(true);
            f.setWritable(true);
        } catch (IOException ex) {
        }
    }

    public static File[] seleccionarArchivos() {

        JFileChooser jf = new JFileChooser();
        jf.setMultiSelectionEnabled(true);
        jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        jf.showOpenDialog(null);
        jf.setRequestFocusEnabled(true);

        return jf.getSelectedFiles();
    }

    //Metodo en prueba
    public void mandarData(ObjectOutputStream oos, int numArchivos) throws IOException {
        Data d = new Data(Data.OP_SUBIR_ARCHIVOS, numArchivos);

        oos.writeObject(d);
        oos.flush();
        oos.reset();
    }

    //Metodo en prueba
    public void mandarData(ObjectOutputStream oos, ArrayList<String> archivos) throws IOException {
        Data d = new Data(Data.OP_SUBIR_ARCHIVOS, archivos);

        oos.writeObject(d);
        oos.flush();
        oos.reset();
    }

    public static void enviarArchivos(File[] selected) throws IOException {

        //File[] selected = jf.getSelectedFiles();
        HashSet<File> archivos = new HashSet<>();
        HashSet<File> archivosZip = new HashSet<>();

        for (File f : selected) {

            if (f.isDirectory()) {
                String zipPath = f.getAbsolutePath() + ".zip";
                Zip comZip = new Zip(zipPath, f.getAbsolutePath());
                comZip.generarListaArchivos(f);

                comZip.generarZip(zipPath);
                File zipFile = new File(zipPath);

                //System.out.println("Nombre del directorio: " + f.getName());
                archivos.add(zipFile);
                archivosZip.add(zipFile);

            } else {
                archivos.add(f);

                /*System.out.println("Nombre de archivo: " + f.getName());
                String[] nombreArchivo = f.getName().split("\\.");
                String extension = nombreArchivo[nombreArchivo.length - 1];
                System.out.println("Extension de archivo: " + extension);*/
            }
        }

        for (File f : archivos) {
            Socket dataTransfer = new Socket("localhost", 8080);
            System.out.println("Conectado con servidor para transferencia de datos...");
            String nombre = f.getName();
            String path = f.getAbsolutePath();
            long tam = f.length();
            System.out.println("Preparandose pare enviar archivo " + path + " de " + tam + " bytes\n");
            DataOutputStream dos = new DataOutputStream(dataTransfer.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(path));
            dos.writeUTF(nombre);
            dos.flush();
            dos.writeLong(tam);
            dos.flush();
            long enviados = 0;
            int l = 0, porcentaje = 0;

            while (enviados < tam) {
                //MTU de Ethernet == 1500
                byte[] b = new byte[1500];
                l = dis.read(b);
                //System.out.println("enviados: " + l);
                dos.write(b, 0, l);
                dos.flush();
                enviados = enviados + l;
                porcentaje = (int) ((enviados * 100) / tam);
                System.out.print("\rEnviado el " + porcentaje + " % del archivo");
            }

            System.out.println("\nArchivo enviado...\n\n");
            dis.close();
            dos.close();
            dataTransfer.close();
        }

        for (File f : archivosZip) {
            f.delete();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        try {
            Client cl = new Client(puertoServidor);
            System.out.println("Conexion establecida con el servidor...\n\n");

            for (int i = 0; i < 3; i++) {

                //Prueba: En consola se usaria Scanner para obtener la opcion
                int opcion = Data.OP_SUBIR_ARCHIVOS;

                //Seleccion de operacion
                switch (opcion) {
                    case Data.OP_SUBIR_ARCHIVOS: {
                        ObjectOutputStream oos = new ObjectOutputStream(cl.s.getOutputStream());

                        File[] selected = seleccionarArchivos();
                        
                        Data d = new Data(Data.OP_SUBIR_ARCHIVOS, selected.length);

                        oos.writeObject(d);
                        oos.flush();
                        
                        //cl.mandarData(oos, selected.length);
                        enviarArchivos(selected);
                        break;
                    }
                    case Data.OP_MOSTRAR_ARCHIVOS_LOCAL:
                        /*Falta implementar*/
                        break;
                    case Data.OP_MOSTRAR_ARCHIVOS_DRIVE:
                        /*Falta implementar*/
                        break;
                    case Data.OP_BORRAR_ARCHIVOS_LOCAL:
                        /*Falta implementar*/
                        break;
                    case Data.OP_BORRAR_ARCHIVOS_DRIVE:
                        /*Falta implementar*/
                        break;
                    default:
                        System.out.println("Error: Operacion no soportada");
                        break;
                }
                //Delay para prueba
                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException ex) {
                }
            }
        } catch (IOException ex) {
        }
    }
}
