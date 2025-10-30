package controlador;

import modelo.Automata;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class AutomataControlador {

    private Automata automata;

    public AutomataControlador(Automata automata) {
        this.automata = automata;
    }

    /**
     * Genera el archivo DOT y crea la imagen del autómata utilizando Graphviz.
     *
     * @param pathDot Ruta donde está instalado Graphviz (dot.exe)
     * @param rutaEntrada Archivo temporal .txt para instrucciones DOT
     * @param rutaSalida Archivo PNG de salida
     * @param etiqueta JLabel donde se mostrará la imagen
     * @param estadosActivos Conjunto de estados que deben resaltarse
     */
    public void generarImagenAutomata(String pathDot, String rutaEntrada, String rutaSalida, JLabel etiqueta, Set<String> estadosActivos) {
        try {
            // 🔹 1️⃣ Validar ruta de Graphviz
            File dotFile = new File(pathDot);
            if (!dotFile.exists()) {
                JOptionPane.showMessageDialog(null,
                        "⚠ No se encontró Graphviz en la ruta especificada:\n" + pathDot,
                        "Error Graphviz", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 🔹 2️⃣ Crear carpeta si no existe
            File carpetaSalida = new File(rutaSalida).getParentFile();
            if (!carpetaSalida.exists()) {
                carpetaSalida.mkdirs();
            }

            // 🔹 3️⃣ Borrar imagen anterior (si existe)
            File imagenAnterior = new File(rutaSalida);
            if (imagenAnterior.exists()) {
                boolean eliminado = imagenAnterior.delete();
                if (eliminado) {
                    System.out.println("🗑 Imagen anterior eliminada: " + rutaSalida);
                }
            }

            // 🔹 4️⃣ Generar contenido DOT
            String contenidoDOT = generarContenidoDOT(estadosActivos);

            // 🔹 5️⃣ Guardar el archivo DOT
            File archivoDOT = new File(rutaEntrada);
            try (FileWriter writer = new FileWriter(archivoDOT)) {
                writer.write(contenidoDOT);
            }

            // 🔹 6️⃣ Ejecutar Graphviz con ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(
                    pathDot,
                    "-Tpng",
                    "-Gdpi=100",
                    archivoDOT.getAbsolutePath(),
                    "-o",
                    rutaSalida
            );
            pb.redirectErrorStream(true);
            Process proceso = pb.start();
            int codigo = proceso.waitFor();

            if (codigo != 0) {
                JOptionPane.showMessageDialog(null,
                        "❌ Error al ejecutar Graphviz.\nCódigo de salida: " + codigo,
                        "Error Graphviz", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 🔹 7️⃣ Verificar que la imagen se generó correctamente
            File imagen = new File(rutaSalida);
            if (!imagen.exists() || imagen.length() == 0) {
                JOptionPane.showMessageDialog(null,
                        "❌ No se generó la imagen del autómata. Revisa el archivo DOT:\n" + rutaEntrada,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 🔹 8️⃣ Mostrar imagen generada
            ImageIcon icono = new ImageIcon(rutaSalida);
            etiqueta.setIcon(icono);
            etiqueta.revalidate();
            etiqueta.repaint();

            System.out.println("✅ Imagen del autómata generada correctamente en: " + rutaSalida);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error al leer/escribir archivos:\n" + e.getMessage(),
                    "Error de archivo", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error al esperar la finalización de Graphviz:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Error inesperado al generar la imagen:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Construye el código DOT del autómata con colores fijos.
     * Todos los estados se dibujan siempre (para no alterar el layout).
     * Los estados activos se resaltan.
     */
    private String generarContenidoDOT(Set<String> estadosActivos) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Automata {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=circle, style=filled, fillcolor=lightgray, fontname=\"Arial\"];\n\n");

        // 🔹 1️⃣ Dibujar todos los estados con color base
        for (String estado : automata.getEstados()) {
            String color = "lightgray";

            // Si el estado está activo → verde
            if (estadosActivos != null && estadosActivos.contains(estado)) {
                color = "palegreen";
            }

            // Si el estado es de aceptación → doble círculo
            if (automata.getEstadosAceptacion().contains(estado)) {
                sb.append("  ").append(estado)
                        .append(" [shape=doublecircle, fillcolor=\"").append(color).append("\"];\n");
            } else {
                sb.append("  ").append(estado)
                        .append(" [shape=circle, fillcolor=\"").append(color).append("\"];\n");
            }
        }

        // 🔹 2️⃣ Estado inicial (punto invisible)
        sb.append("\n  inicio [shape=point];\n");
        sb.append("  inicio -> ").append(automata.getEstadoInicial()).append(";\n\n");

        // 🔹 3️⃣ Dibujar transiciones (sin alterar forma)
        for (String origen : automata.getEstados()) {
            if (automata.getTransiciones().containsKey(origen)) {
                for (String simbolo : automata.getSimbolos()) {
                    String destino = automata.getTransiciones().get(origen).get(simbolo);
                    if (destino != null && !destino.isEmpty()) {
                        sb.append("  ").append(origen)
                                .append(" -> ")
                                .append(destino)
                                .append(" [label=\"").append(simbolo).append("\"];\n");
                    }
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }
}
