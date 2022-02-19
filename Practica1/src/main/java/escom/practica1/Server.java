/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escom.practica1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Betuc
 */
public class Server {

    public static final int puertoServidor = 8000;
    public static final int puertoTransferencia = 8080;

    ServerSocket s;
    File f;
    String rutaArchivo;

    public Server(int puerto) {
        try {
            s = new ServerSocket(puerto);
            s.setReuseAddress(true);

            File aux = new File("");
            rutaArchivo = aux.getAbsolutePath() + "\\Drive\\";
            f = new File(rutaArchivo);
            f.mkdirs();
            f.setReadable(true);
            f.setWritable(true);
        } catch (IOException e) {
        }
    }

    public void recibirArchivos(int numArchivos) throws IOException {

        System.out.println("Numero de archivos por recibir: " + numArchivos + "\n");

        for (int i = 0; i < numArchivos; i++) {

            Socket dataSocket = s.accept();

            System.out.println("Cliente enviando archivo desde " + dataSocket.getInetAddress() + ":" + dataSocket.getPort());

            DataInputStream dis = new DataInputStream(dataSocket.getInputStream());
            String nombre = dis.readUTF();
            long tam = dis.readLong();

            System.out.println("Comienza descarga del archivo " + nombre + " de " + tam + " bytes\n");

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(rutaArchivo + nombre));

            long recibidos = 0;
            int l = 0, porcentaje = 0;

            while (recibidos < tam) {
                byte[] b = new byte[1500];
                l = dis.read(b);
                dos.write(b, 0, l);
                dos.flush();
                recibidos = recibidos + l;
                porcentaje = (int) ((recibidos * 100) / tam);
                System.out.print("\rRecibido el " + porcentaje + " % del archivo");
            }

            System.out.println("\nArchivo recibido...\n");
            dos.close();
            dis.close();
            dataSocket.close();

            String[] nombreArchivo = nombre.split("\\.");
            if (nombreArchivo[nombreArchivo.length - 1].equals("zip")) {
                String nuevoNombre = nombreArchivo[0];
                UnZip unZipFile = new UnZip();
                unZipFile.descomprimirZip(rutaArchivo + nombre, rutaArchivo + nuevoNombre);

                File f = new File(rutaArchivo + nombre);
                f.delete();
            }

        }
    }

    public static void main(String[] args) throws ClassNotFoundException {

        try {
            //Creacion de sockets para transmision de info y archivos
            Server servidor = new Server(puertoServidor);
            Server dataTransfer = new Server(puertoTransferencia);

            System.out.println("Servidor iniciado en puerto " + servidor.s.getLocalPort());
            System.out.println("Transferencia de datos iniciada en puerto " + dataTransfer.s.getLocalPort());
            System.out.println("Ruta de directorio: " + servidor.rutaArchivo + "\n\n");

            while (true) {
                //Se conecta cliente al servidor
                Socket cl = servidor.s.accept();
                System.out.println("Cliente conectado desde " + cl.getInetAddress() + ":" + cl.getPort());

                //Se recibe objeto de tipo Data con la informacion sobre la operacion a realizar
                ObjectInputStream ois = new ObjectInputStream(cl.getInputStream());
                Data info = (Data) ois.readObject();

                System.out.println("Opcion recibida: " + info.getOpcion());
                //Seleccion de operacion
                switch (info.getOpcion()) {
                    case Data.OP_SUBIR_ARCHIVOS:
                        dataTransfer.recibirArchivos(info.getNumArchivos());
                        break;
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

            }
        } catch (IOException ex) {
        }
    }
}
