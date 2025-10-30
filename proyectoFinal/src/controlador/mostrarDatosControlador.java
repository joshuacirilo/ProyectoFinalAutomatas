/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import vista.inicio;
import modelo.Automata; // 👈 Volvemos a usar Automata
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * Controlador encargado de leer los datos ingresados en la vista y mostrarlos
 * en las tablas correspondientes, además de crear un objeto Automata con toda
 * la información.
 *
 * @author TuNombre
 */
public class mostrarDatosControlador {

    private inicio vista;
    private Automata automata;

    public mostrarDatosControlador(inicio vista) {
        this.vista = vista;
    }

    public void ingresarDatos() {
        // Obtener el texto del JTextArea
        String textoCompleto = vista.getjTextArea1().getText();
        if (textoCompleto == null || textoCompleto.isEmpty()) {
            System.out.println("⚠ No hay datos en el JTextArea");
            return;
        }

        // Dividir por líneas
        String[] lineas = textoCompleto.split("\\n");

        String simbolos = "";
        String estados = "";
        String estadoInicial = "";
        String estadosAceptacion = "";
        List<String> transiciones = new ArrayList<>();

        // Procesar cada línea
        boolean leyendoTransiciones = false;
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }

            if (linea.startsWith("Simbolos:")) {
                simbolos = linea.replace("Simbolos:", "").trim();
            } else if (linea.startsWith("Estados:")) {
                estados = linea.replace("Estados:", "").trim();
            } else if (linea.startsWith("Estado Inicial:")) {
                estadoInicial = linea.replace("Estado Inicial:", "").trim();
            } else if (linea.startsWith("Estados de aceptacion:")) {
                estadosAceptacion = linea.replace("Estados de aceptacion:", "").trim();
            } else if (linea.startsWith("Transiciones:")) {
                leyendoTransiciones = true;
            } else if (leyendoTransiciones) {
                // Guardamos las líneas de transiciones
                transiciones.add(linea);
            }
        }

        // ---- MOSTRAR DATOS EN LAS TABLAS ----
        // Estado inicial → JTextField
        vista.getEstadoInicial().setText(estadoInicial);

        // Estados de aceptación → JTable
        String[] aceptacionArr = estadosAceptacion.split(",");
        DefaultTableModel modeloAceptacion = (DefaultTableModel) vista.getTablaAceptacion1().getModel();
        modeloAceptacion.setRowCount(0);
        int i = 1;
        for (String e : aceptacionArr) {
            modeloAceptacion.addRow(new Object[]{i++, e.trim()});
        }

        // Símbolos → JTable
        String[] simbolosArr = simbolos.split(",");
        DefaultTableModel modeloSimbolos = (DefaultTableModel) vista.getTablasimbolos1().getModel();
        modeloSimbolos.setRowCount(0);
        i = 1;
        for (String s : simbolosArr) {
            modeloSimbolos.addRow(new Object[]{i++, s.trim()});
        }

        // Transiciones → JTable con encabezados dinámicos
        String[] estadosArr = estados.split(",");
        String[] columnas = new String[simbolosArr.length + 1];
        columnas[0] = "Estado";
        for (int j = 0; j < simbolosArr.length; j++) {
            columnas[j + 1] = simbolosArr[j].trim();
        }

        DefaultTableModel modeloTrans = new DefaultTableModel(columnas, 0);
        vista.getTablaTransicciones().setModel(modeloTrans);

        // Llenar filas con los estados y sus transiciones
        for (int fila = 0; fila < estadosArr.length; fila++) {
            Object[] filaDatos = new Object[simbolosArr.length + 1];
            filaDatos[0] = estadosArr[fila].trim();

            if (fila < transiciones.size()) {
                String[] partes = transiciones.get(fila).split(",");
                for (int j = 0; j < simbolosArr.length; j++) {
                    if (j < partes.length) {
                        filaDatos[j + 1] = partes[j].trim();
                    }
                }
            }
            modeloTrans.addRow(filaDatos);
        }

        System.out.println("✅ Datos cargados en las tablas correctamente");

        // ---- CREAR OBJETO Automata ----
        crearObjetoAutomata(simbolosArr, estadosArr, estadoInicial, aceptacionArr, transiciones);
    }

    /**
     * Crea el objeto Automata con los datos procesados desde la vista.
     */
    public void crearObjetoAutomata(String[] simbolosArr, String[] estadosArr,
            String estadoInicial, String[] aceptacionArr,
            List<String> transiciones) {

        Map<String, Map<String, String>> mapaTrans = new HashMap<>();

        for (int i = 0; i < estadosArr.length; i++) {
            String estado = estadosArr[i].trim();
            Map<String, String> mapaInterno = new HashMap<>();

            if (i < transiciones.size()) {
                String[] partes = transiciones.get(i).split(",");
                for (int j = 0; j < simbolosArr.length; j++) {
                    if (j < partes.length) {
                        mapaInterno.put(simbolosArr[j].trim(), partes[j].trim());
                    }
                }
            }
            mapaTrans.put(estado, mapaInterno);
        }

        automata = new Automata(
                Arrays.asList(simbolosArr),
                Arrays.asList(estadosArr),
                estadoInicial,
                Arrays.asList(aceptacionArr),
                mapaTrans
        );

        System.out.println("🤖 Objeto Automata creado correctamente:");
        System.out.println(automata);
    }

    public Automata getAutomata() {
        return automata;
    }

    /**
     * 🔹 Limpia el JTextArea, las tablas y el campo de estado inicial
     */
    public void limpiarVista() {
        // Limpiar área de texto
        vista.getjTextArea1().setText("");

        // Limpiar campo de estado inicial
        vista.getEstadoInicial().setText("");

        // Limpiar tablas
        DefaultTableModel modeloSimbolos = (DefaultTableModel) vista.getTablasimbolos1().getModel();
        modeloSimbolos.setRowCount(0);

        DefaultTableModel modeloAceptacion = (DefaultTableModel) vista.getTablaAceptacion1().getModel();
        modeloAceptacion.setRowCount(0);

        DefaultTableModel modeloTrans = (DefaultTableModel) vista.getTablaTransicciones().getModel();
        modeloTrans.setRowCount(0);

        System.out.println("🧹 Vista limpiada antes de cargar nuevo archivo.");
    }

    /**
     * 🔹 Método general para abrir un archivo y mostrarlo
     */
    private void abrirArchivo(String rutaArchivo) {
        try {
            // 🧼 Limpiar todo antes de cargar
            limpiarVista();

            StringBuilder contenido = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }
            }

            // 🔹 Mostrar el contenido del archivo en el JTextArea
            vista.getjTextArea1().setText(contenido.toString());

            JOptionPane.showMessageDialog(null,
                    "✅ Archivo del autómata cargado correctamente.",
                    "Archivo cargado",
                    JOptionPane.INFORMATION_MESSAGE);

            System.out.println("📄 Archivo leído correctamente desde: " + rutaArchivo);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error al leer el archivo del autómata:\n" + rutaArchivo + "\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 🔹 Métodos públicos para cada archivo
    public void abrirArchivoAutomata1() {
        String rutaArchivo = "C:/Users/jciri/OneDrive/Escritorio/Joshua/universidadMariano/2025 semestre 2/Automatas/ejemplos/ejemplo1";
        abrirArchivo(rutaArchivo);
    }

    public void abrirArchivoAutomata2() {
        String rutaArchivo = "C:/Users/jciri/OneDrive/Escritorio/Joshua/universidadMariano/2025 semestre 2/Automatas/ejemplos/ejemplo2";
        abrirArchivo(rutaArchivo);
    }

    public void abrirArchivoAutomata3() {
        String rutaArchivo = "C:/Users/jciri/OneDrive/Escritorio/Joshua/universidadMariano/2025 semestre 2/Automatas/ejemplos/ejemplo3";
        abrirArchivo(rutaArchivo);
    }
}
