/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package escom.practica1;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Betuc
 */
public class Data implements Serializable {

    public static final int OP_SUBIR_ARCHIVOS = 1;
    public static final int OP_MOSTRAR_ARCHIVOS_DRIVE = 2;
    public static final int OP_MOSTRAR_ARCHIVOS_LOCAL = 3;
    public static final int OP_BORRAR_ARCHIVOS_DRIVE = 4;
    public static final int OP_BORRAR_ARCHIVOS_LOCAL = 5;
   
    private int opcion;
    private int numArchivos;
    private ArrayList<String> archivos;

    public Data(int opcion) {
        this.opcion = opcion;
    }

    public Data(int opcion, int numArchivos) {
        this.opcion = opcion;
        this.numArchivos = numArchivos;
    }

    public Data(int opcion, ArrayList<String> archivos) {
        this.opcion = opcion;
        this.archivos = archivos;
    }
    
    public int getNumArchivos() {
        return numArchivos;
    }

    public int getOpcion() {
        return opcion;
    }

    public ArrayList<String> getArchivos() {
        return archivos;
    }
}