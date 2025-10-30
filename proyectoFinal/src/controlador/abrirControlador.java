/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import java.io.File;
import modelo.abrirModelo;

public class abrirControlador {
    
    private final abrirModelo modelo;

    public abrirControlador() {
        this.modelo = new abrirModelo();
    }

    public String abrirArchivo(File archivo) throws Exception {
        return modelo.leerArchivo(archivo);
    }

    public void guardarArchivo(File archivo, String contenido) throws Exception {
        modelo.guardarArchivo(archivo, contenido);
    }
}
