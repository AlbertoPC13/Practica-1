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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
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

    public File[] seleccionarArchivos(boolean mode) {

        JFileChooser jf;
        if (mode) {
            jf = new JFileChooser(rutaArchivo);
        } else {
            jf = new JFileChooser();
        }

        jf.setMultiSelectionEnabled(true);
        jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        jf.showOpenDialog(null);
        jf.setRequestFocusEnabled(true);

        return jf.getSelectedFiles();
    }

    public static void mandarData(ObjectOutputStream oos, int numArchivos) throws IOException {
        Data d = new Data(Data.OP_SUBIR_ARCHIVOS, numArchivos);

        oos.writeObject(d);
        oos.flush();
    }

    public static void mandarData(ObjectOutputStream oos, File[] archivos) throws IOException {
        Data d = new Data(Data.OP_SUBIR_ARCHIVOS, archivos);

        oos.writeObject(d);
        oos.flush();
    }

    public static void mandarData(ObjectOutputStream oos, int operacion, boolean fg) throws IOException {

        Data d;

        switch (operacion) {
            case Data.OP_CIERRE_CONEXION:
                d = new Data(Data.OP_CIERRE_CONEXION);
                break;
            case Data.OP_MOSTRAR_ARCHIVOS_DRIVE:
                d = new Data(Data.OP_MOSTRAR_ARCHIVOS_DRIVE);
                break;
            default:
                throw new AssertionError();
        }

        oos.writeObject(d);
        oos.flush();
    }

    public void mostrarArchivos() {
        File local = new File(rutaArchivo);
        System.out.println("\nContenido del directorio local: \n");

        for (File f : local.listFiles()) {
            if (f.isDirectory()) {
                System.out.println("\\" + f.getName());
            } else {
                System.out.println(f.getName());
            }
        }
        System.out.print("\n\n");
    }

    public static void mostrarArchivosDrive(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Data info = (Data) ois.readObject();

        System.out.println("\nContenido del directorio remoto: \n");

        for (File f : info.getArchivos()) {
            if (f.isDirectory()) {
                System.out.println("\\" + f.getName());
            } else {
                System.out.println(f.getName());
            }
        }
        System.out.print("\n\n");
    }

    public void borrarArchivos(File[] selected) {

        if (selected.length > 0) {
            System.out.println("Eliminando archivos\n");
            for (File f : selected) {
                System.out.println(f.getName() + " eliminado");
                if (f.isDirectory()) {
                    borrarDirectorio(f);
                }
                f.delete();
            }
            System.out.println("\n\n");
        }
    }

    public static void borrarDirectorio(File directorio) {

        File[] archivos = directorio.listFiles();

        for (File f : archivos) {
            if (f.isDirectory()) {
                borrarDirectorio(f);
            }
            f.delete();
        }
    }

    public static void enviarArchivos(File[] selected) throws IOException {

        HashSet<File> archivos = new HashSet<>();
        HashSet<File> archivosZip = new HashSet<>();

        for (File f : selected) {

            if (f.isDirectory()) {
                String zipPath = f.getAbsolutePath() + ".zip";
                Zip comZip = new Zip(zipPath, f.getAbsolutePath());
                comZip.generarListaArchivos(f);

                comZip.generarZip(zipPath);
                File zipFile = new File(zipPath);

                archivos.add(zipFile);
                archivosZip.add(zipFile);

            } else {
                archivos.add(f);
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
            ObjectOutputStream oos = new ObjectOutputStream(cl.s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(cl.s.getInputStream());

            System.out.println("Conexion establecida con el servidor...\n\n");

            //Prueba: En consola se usaria Scanner para obtener la opcion
            Scanner inputScanner = new Scanner(System.in);
            int opcion;

            do {
                System.out.println("Ingresa operacion a realizar: ");
                opcion = inputScanner.nextInt();

                //Seleccion de operacion
                switch (opcion) {
                    case Data.OP_SUBIR_ARCHIVOS: {
                        File[] selected = cl.seleccionarArchivos(false);
                        mandarData(oos, selected.length);
                        enviarArchivos(selected);
                        break;
                    }
                    case Data.OP_MOSTRAR_ARCHIVOS_LOCAL:
                        cl.mostrarArchivos();
                        break;
                    case Data.OP_MOSTRAR_ARCHIVOS_DRIVE: {
                        mandarData(oos, Data.OP_MOSTRAR_ARCHIVOS_DRIVE, true);
                        mostrarArchivosDrive(ois);
                        break;
                    }
                    case Data.OP_BORRAR_ARCHIVOS_LOCAL: {
                        File[] selected = cl.seleccionarArchivos(true);
                        cl.borrarArchivos(selected);
                        break;
                    }
                    case Data.OP_BORRAR_ARCHIVOS_DRIVE:
                        /*Falta implementar*/
                        break;
                    case Data.OP_DESCARGAR_ARCHIVOS:
                        /*Falta implementar*/
                        break;
                    case Data.OP_CIERRE_CONEXION: {
                        mandarData(oos, Data.OP_CIERRE_CONEXION, true);
                        System.out.println("Desconectando del servidor...");
                        cl.s.close();
                        break;
                    }
                    default:
                        System.out.println("Error: Operacion no soportada");
                        break;
                }
            } while (opcion != Data.OP_CIERRE_CONEXION);
        } catch (IOException ex) {
        }
    }
}
