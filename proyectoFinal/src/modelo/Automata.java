package modelo;

import java.util.List;
import java.util.Map;

/**
 * Clase modelo que representa un autómata finito.
 * 
 * Contiene los símbolos, estados, estado inicial, estados de aceptación
 * y las transiciones entre estados.
 * 
 * También mantiene el estado actual para poder simular el autómata paso a paso.
 * 
 * @author Joshua
 */
public class Automata {
    
    // --- Atributos principales ---
    private List<String> simbolos;               // Σ: conjunto de símbolos
    private List<String> estados;                // Q: conjunto de estados
    private String estadoInicial;                // q0: estado inicial
    private List<String> estadosAceptacion;      // F: estados de aceptación
    private Map<String, Map<String, String>> transiciones; // δ: transiciones

    // --- Nuevo atributo ---
    private String estadoActual; // Estado en el que se encuentra actualmente

    // --- Constructores ---
    public Automata() {
    }

    public Automata(List<String> simbolos, List<String> estados, String estadoInicial,
                    List<String> estadosAceptacion, Map<String, Map<String, String>> transiciones) {
        this.simbolos = simbolos;
        this.estados = estados;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
        this.transiciones = transiciones;
        this.estadoActual = estadoInicial; // al crear el autómata, el estado actual es el inicial
    }

    // --- Getters y Setters ---
    public List<String> getSimbolos() {
        return simbolos;
    }

    public void setSimbolos(List<String> simbolos) {
        this.simbolos = simbolos;
    }

    public List<String> getEstados() {
        return estados;
    }

    public void setEstados(List<String> estados) {
        this.estados = estados;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
        this.estadoActual = estadoInicial; // cuando cambia el inicial, actualizamos también
    }

    public List<String> getEstadosAceptacion() {
        return estadosAceptacion;
    }

    public void setEstadosAceptacion(List<String> estadosAceptacion) {
        this.estadosAceptacion = estadosAceptacion;
    }

    public Map<String, Map<String, String>> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(Map<String, Map<String, String>> transiciones) {
        this.transiciones = transiciones;
    }

    // --- NUEVOS MÉTODOS ---

    /** Devuelve el estado actual del autómata */
    public String getEstadoActual() {
        return estadoActual;
    }

    /** Define manualmente el estado actual */
    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    /** Reinicia el autómata al estado inicial */
    public void reiniciar() {
        this.estadoActual = estadoInicial;
    }

    /**
     * Avanza al siguiente estado según el símbolo dado.
     * Retorna el nuevo estado o null si la transición no existe.
     */
    public String transicionar(String simbolo) {
        if (transiciones.containsKey(estadoActual)) {
            Map<String, String> mapa = transiciones.get(estadoActual);
            String siguiente = mapa.get(simbolo);
            if (siguiente != null) {
                this.estadoActual = siguiente;
                return siguiente;
            }
        }
        return null; // transición inválida
    }

    /** Verifica si el estado actual es de aceptación */
    public boolean esEstadoDeAceptacion() {
        return estadosAceptacion.contains(estadoActual);
    }

    // --- Método auxiliar ---
    @Override
    public String toString() {
        return "Automata{" +
                "simbolos=" + simbolos +
                ", estados=" + estados +
                ", estadoInicial='" + estadoInicial + '\'' +
                ", estadosAceptacion=" + estadosAceptacion +
                ", estadoActual='" + estadoActual + '\'' +
                ", transiciones=" + transiciones +
                '}';
    }
}
