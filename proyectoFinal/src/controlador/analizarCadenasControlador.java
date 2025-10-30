package controlador;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Automata;
import vista.inicio;
import vista.grafica;

public class analizarCadenasControlador {

    private inicio vista;
    private grafica vistaGrafica;
    private List<String> cadenas; // almacenar las cadenas extraídas
    private int indiceActual = -1; // índice de la cadena que se está evaluando
    private int indiceSimboloActual = 0;
    private String cadenaActual = "";
    private Automata automata;

    public analizarCadenasControlador(inicio vista, grafica vistaGrafica) {
        this.vista = vista;
        this.vistaGrafica = vistaGrafica;
        this.cadenas = new ArrayList<>();
    }

    /**
     * Extrae las cadenas a analizar del texto del área y las muestra en la
     * tabla.
     */
    public void ingresarDatos() {
        String textoCompleto = vista.getjTextArea1().getText();

        if (textoCompleto == null || textoCompleto.isEmpty()) {
            System.out.println("⚠ No hay datos en el JTextArea");
            return;
        }

        // Dividir por líneas
        String[] lineas = textoCompleto.split("\\n");
        cadenas.clear(); // limpiar antes de volver a cargar

        boolean leyendoCadenas = false;
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }

            // Cuando llegamos a la sección de cadenas
            if (linea.toLowerCase().startsWith("cadenas a analizar")) {
                leyendoCadenas = true;
                continue;
            }

            // Si ya estamos leyendo cadenas y llegamos a una línea que no parece una cadena, paramos
            if (leyendoCadenas) {
                // Si empieza con un número o contiene comas, la tomamos como cadena válida
                if (linea.matches("^[0-9, ]+$")) {
                    cadenas.add(linea);
                } else {
                    // Si encontramos otra sección (por ejemplo "Transiciones:"), dejamos de leer
                    break;
                }
            }
        }

        // Mostrar las cadenas en la tabla
        mostrarCadenasEnTabla();

        // Reiniciar índice
        if (!cadenas.isEmpty()) {
            indiceActual = 0;
            mostrarCadenaActual();
        }
    }

    /**
     * Muestra las cadenas en la tabla de la vista 'grafica'. La tabla tendrá 2
     * columnas: "Cadenas a analizar" y "Estado de aceptación".
     */
    private void mostrarCadenasEnTabla() {
        JTable tabla = vistaGrafica.getTablaSentencias();

        // Crear modelo de tabla
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Cadenas a analizar", "Estado de aceptación"}, 0
        );

        // Agregar filas con las cadenas
        for (String cadena : cadenas) {
            modelo.addRow(new Object[]{cadena, ""}); // segunda columna vacía por ahora
        }

        tabla.setModel(modelo);
    }

    /**
     * Muestra en el JTextField la cadena que se está evaluando actualmente.
     */
private void mostrarCadenaActual() {
    // 💥 limpiar imágenes del automata anterior
    limpiarImagenes();  

    if (indiceActual >= 0 && indiceActual < cadenas.size()) {
        this.cadenaActual = cadenas.get(indiceActual).replaceAll("[,\\s]+", "");
        reiniciarSimulacion(); // 👈 ya genera la primera imagen del autómata
        System.out.println("✅ Cadena actual cargada: " + this.cadenaActual);
    } else {
        this.cadenaActual = "";
        vistaGrafica.getCadenaEvaluada().setText("");
        vistaGrafica.getElementoEvaluado().setText("");
    }

    vistaGrafica.getElementoEvaluado().setBackground(java.awt.Color.WHITE);
}

    /**
     * Avanza a la siguiente cadena y la muestra.
     */
    public void siguienteCadena() {
        if (cadenas.isEmpty()) {
            return;
        }

        if (indiceActual < cadenas.size() - 1) {
            indiceActual++;
            mostrarCadenaActual();
        } else {
            System.out.println("⚠ Ya estás en la última cadena.");
        }
    }

    /**
     * Retrocede a la cadena anterior y la muestra.
     */
    public void anteriorCadena() {
        if (cadenas.isEmpty()) {
            return;
        }

        if (indiceActual > 0) {
            indiceActual--;
            mostrarCadenaActual();
        } else {
            System.out.println("⚠ Ya estás en la primera cadena.");
        }
    }

    /**
     * Devuelve la lista de cadenas extraídas.
     *
     * @return lista de cadenas a analizar
     */
    public List<String> getCadenas() {
        return cadenas;
    }

    /**
     * Devuelve la cadena que se está evaluando actualmente.
     *
     * @return cadena actual o null si no hay ninguna seleccionada
     */
    public String getCadenaActual() {
        if (indiceActual >= 0 && indiceActual < cadenas.size()) {
            return cadenas.get(indiceActual);
        }
        return null;
    }

    ////avanzar en los simbolos de las cadenas
// Este método se llama una vez que seleccionas una cadena a evaluar
    public void setAutomata(Automata automata) {
        this.automata = automata;
    }

    public void setCadenaActual(String cadena) {
        // Limpiamos comas y espacios
        this.cadenaActual = cadena.replaceAll("[,\\s]+", "");

        // Reiniciamos el índice del símbolo actual
        this.indiceSimboloActual = 0;

        // Si la cadena no está vacía, mostramos automáticamente el primer símbolo
        if (!this.cadenaActual.isEmpty()) {
            char primerSimbolo = this.cadenaActual.charAt(0);
            vistaGrafica.getElementoEvaluado().setText(String.valueOf(primerSimbolo));
            System.out.println("✅ Primer símbolo mostrado automáticamente: " + primerSimbolo);
        } else {
            vistaGrafica.getElementoEvaluado().setText("");
            System.out.println("⚠ La cadena actual está vacía.");
        }

    }
// 🔹 Historial de estados recorridos
    private final List<String> historialEstados = new ArrayList<>();

// ✅ Avanzar un carácter en la cadena actual
    public void avanzarSimbolo() {
        if (cadenaActual == null || cadenaActual.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No hay ninguna cadena cargada actualmente.\nAsegúrate de haber seleccionado una cadena primero.",
                    "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (indiceSimboloActual >= cadenaActual.length()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Ya estás en el último carácter de la cadena.",
                    "Información", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 🔹 Obtener símbolo actual
        char simboloActual = cadenaActual.charAt(indiceSimboloActual);
        vistaGrafica.getElementoEvaluado().setText(String.valueOf(simboloActual));

        // 🔹 Estado actual y transición
        String estadoAntes = automata.getEstadoActual();
        String nuevoEstado = automata.transicionar(String.valueOf(simboloActual));

        if (nuevoEstado == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No existe una transición desde '" + estadoAntes + "' con el símbolo '" + simboloActual + "'.",
                    "Transición inválida", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        historialEstados.add(nuevoEstado);
        generarImagenPaso();

        indiceSimboloActual++;

        // ✅ Si llegamos al final de la cadena
        if (indiceSimboloActual == cadenaActual.length()) {
            boolean aceptada = automata.esEstadoDeAceptacion();

            // 🔹 Cambiar color visual del JTextField
            if (aceptada) {
                vistaGrafica.getElementoEvaluado().setBackground(java.awt.Color.GREEN);
                javax.swing.JOptionPane.showMessageDialog(null,
                        "✅ Cadena ACEPTADA.\nEl estado final (" + nuevoEstado + ") es de aceptación.",
                        "Resultado de la cadena", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } else {
                vistaGrafica.getElementoEvaluado().setBackground(java.awt.Color.RED);
                javax.swing.JOptionPane.showMessageDialog(null,
                        "❌ Cadena RECHAZADA.\nEl estado final (" + nuevoEstado + ") no es de aceptación.",
                        "Resultado de la cadena", javax.swing.JOptionPane.WARNING_MESSAGE);
            }

            // 🔹 Marcar el resultado en la tabla
            marcarResultadoEnTabla(aceptada);

        } else {
            // Mostrar siguiente símbolo y mantener fondo blanco
            char siguiente = cadenaActual.charAt(indiceSimboloActual);
            vistaGrafica.getElementoEvaluado().setText(String.valueOf(siguiente));
            vistaGrafica.getElementoEvaluado().setBackground(java.awt.Color.WHITE);
        }
    }

// ✅ Retroceder un carácter en la cadena actual
    public void retrocederSimbolo() {
        if (cadenaActual == null || cadenaActual.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "No hay ninguna cadena cargada actualmente.\nAsegúrate de haber seleccionado una cadena primero.",
                    "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (indiceSimboloActual <= 0) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Ya estás en el primer carácter de la cadena.",
                    "Información", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // retrocedemos el puntero (volvemos a "apuntar" al símbolo anterior)
        indiceSimboloActual--;

        // quitar el último estado alcanzado
        if (!historialEstados.isEmpty()) {
            historialEstados.remove(historialEstados.size() - 1);
        }

        // estado actual pasa a ser el último del historial (o inicial si vacío)
        String estadoActual = historialEstados.isEmpty()
                ? automata.getEstadoInicial()
                : historialEstados.get(historialEstados.size() - 1);

        automata.setEstadoActual(estadoActual);

        // mostrar el símbolo sobre el que ahora "estamos parados"
        char simbolo = cadenaActual.charAt(indiceSimboloActual);
        vistaGrafica.getElementoEvaluado().setText(String.valueOf(simbolo));

        // re-pintar imagen con el estado actual
        generarImagenPaso();
    }

// 🔹 Mostrar solo el carácter actual en el JTextField elementoEvaluado
    private void actualizarVistaElementoActual() {
        if (cadenaActual != null && !cadenaActual.isEmpty()
                && indiceSimboloActual >= 0 && indiceSimboloActual < cadenaActual.length()) {

            // Tomar solo un carácter, sin comas ni corchetes
            String elementoActual = String.valueOf(cadenaActual.charAt(indiceSimboloActual));

            // Mostrarlo directamente
            vistaGrafica.getElementoEvaluado().setText(elementoActual);

            System.out.println("🟡 Carácter actual: " + elementoActual + " (posición " + indiceSimboloActual + ")");
        } else {
            vistaGrafica.getElementoEvaluado().setText("");
        }
    }

// Generar imagen según el paso actual de la cadena
// ✅ Generar imagen según el estado actual del autómata
// 👇 campo nuevo en la clase
    private boolean dibujando = false;

// ✅ SOLO pinta el estado actual. NO llama a transicionar.
// ✅ Generar imagen según el estado actual del autómata

private void generarImagenPaso() {
    if (automata == null) {
        System.out.println("⚠ Automata no definido aún.");
        return;
    }

    // ⚙ Evita dibujar doble pero desbloquea en todo caso
    if (dibujando) {
        return;
    }
    dibujando = true;

    try {
        // 📁 Rutas dinámicas
        String pathDot = "C:/Program Files/Graphviz/bin/dot.exe";
        String carpeta = "C:/AutomataFinito/tmp/";

        File dir = new File(carpeta);
        if (!dir.exists()) dir.mkdirs();

        // 🔹 Usa nombre único por automata/paso
        String rutaEntrada = carpeta + "entradaPaso_" + System.nanoTime() + ".txt";
        String rutaSalida = carpeta + "paso_" + System.currentTimeMillis() + "_" + indiceSimboloActual + ".png";

        // 🔹 Limpia imágenes viejas (opcional)
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith("paso_") && f.getName().endsWith(".png")) {
                f.delete();
            }
        }

        // 🔹 Estado actual a resaltar
        String estadoActual = automata.getEstadoActual();
        Set<String> estadosActivos = new java.util.HashSet<>();
        estadosActivos.add(estadoActual);

        // 🔹 Generar imagen
        AutomataControlador automataCtrl = new AutomataControlador(automata);
        automataCtrl.generarImagenAutomata(
                pathDot, rutaEntrada, rutaSalida, vistaGrafica.getLabelImagen(), estadosActivos
        );

        // 🔹 Esperar a que Graphviz termine
        File img = new File(rutaSalida);
        int intentos = 0;
        while ((!img.exists() || img.length() == 0) && intentos < 20) {
            Thread.sleep(100);
            intentos++;
        }

        // 🔹 Mostrar la imagen solo si está completa
        if (img.exists() && img.length() > 1000) { // >1KB para asegurar que no está vacía
            javax.swing.ImageIcon icono = new javax.swing.ImageIcon(rutaSalida);
            vistaGrafica.getLabelImagen().setIcon(icono);
            vistaGrafica.getLabelImagen().revalidate();
            vistaGrafica.getLabelImagen().repaint();

            System.out.println("✅ Imagen generada correctamente para estado: " + estadoActual);
        } else {
            System.out.println("❌ Imagen no generada o incompleta en el paso " + indiceSimboloActual);
        }

        // 🔹 Mostrar si es estado de aceptación
        if (automata.esEstadoDeAceptacion()) {
            System.out.println("🎉 Estado de aceptación: " + estadoActual);
        }

    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("❌ Error al generar la imagen del paso: " + e.getMessage());
    } finally {
        dibujando = false; // 💪 se libera siempre, incluso si hubo error
    }
}

// 🔁 Reinicia simulación para la cadena cargada
    public void reiniciarSimulacion() {
        if (automata == null) {
            System.out.println("⚠ Automata no definido aún.");
            return;
        }
        if (cadenaActual == null) {
            cadenaActual = "";
        }
        cadenaActual = cadenaActual.replaceAll("[,\\s]+", "");

        indiceSimboloActual = 0;
        automata.reiniciar();

        historialEstados.clear();
        historialEstados.add(automata.getEstadoActual()); // sembrar inicial para retroceso

        vistaGrafica.getCadenaEvaluada().setText(cadenaActual);
        if (!cadenaActual.isEmpty()) {
            vistaGrafica.getElementoEvaluado().setText(String.valueOf(cadenaActual.charAt(0)));
        } else {
            vistaGrafica.getElementoEvaluado().setText("");
        }
        vistaGrafica.getElementoEvaluado().setBackground(java.awt.Color.WHITE);

        generarImagenPaso(); // pinta estado inicial
    }

    /**
     * Marca en la tabla de 'grafica' si la cadena actual fue aceptada o
     * rechazada.
     */
    private void marcarResultadoEnTabla(boolean aceptada) {
        JTable tabla = vistaGrafica.getTablaSentencias();

        if (indiceActual >= 0 && indiceActual < tabla.getRowCount()) {
            String resultado = aceptada ? "✅ Aceptada" : "❌ Rechazada";
            tabla.setValueAt(resultado, indiceActual, 1); // Columna 1 = “Estado de aceptación”
            System.out.println("🟢 Resultado actualizado en la tabla: " + resultado);
        }
    }

    
    
    // 🧹 Borra todas las imágenes PNG generadas previamente
private void limpiarImagenes() {
    try {
        File dir = new File("C:/AutomataFinito/tmp/");
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".png")) {
                    f.delete();
                }
            }
            System.out.println("🧹 Imágenes antiguas eliminadas correctamente.");
        }
    } catch (Exception e) {
        System.err.println("⚠ Error al limpiar imágenes: " + e.getMessage());
    }
}

}
