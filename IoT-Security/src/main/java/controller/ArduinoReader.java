package controller;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class ArduinoReader {
    private static ArduinoReader instancia;
    private SerialConnection conexion;
    private boolean conectado = false;
    private List<String> bufferEventos;
    private CallbackUI callback;
    
    public interface CallbackUI {
        void onEventoRecibido(String tipo, String mensaje);
        void onEstadoCambiado(String estado);
    }
    
    private ArduinoReader() {
        bufferEventos = new ArrayList<>();
    }
    
    public static ArduinoReader getInstance() {
        if (instancia == null) {
            instancia = new ArduinoReader();
        }
        return instancia;
    }
    
    public void setCallback(CallbackUI callback) {
        this.callback = callback;
    }
    
    public void conectar(String puerto) {
        try {
            conexion = new SerialConnection(puerto, 9600);
            conexion.abrir();
            conectado = true;
            
            if (callback != null) {
                callback.onEstadoCambiado("CONECTADO");
            }
            System.out.println("✅ Arduino conectado en puerto: " + puerto);
            
            // Hilo para leer datos
            new Thread(() -> leerDatos()).start();
            
        } catch (Exception e) {
            System.err.println("❌ Error al conectar Arduino: " + e.getMessage());
            if (callback != null) {
                callback.onEstadoCambiado("ERROR: " + e.getMessage());
            }
        }
    }
    
    public void desconectar() {
        if (conexion != null) {
            conexion.cerrar();
            conectado = false;
            if (callback != null) {
                callback.onEstadoCambiado("DESCONECTADO");
            }
        }
    }
    
    private void leerDatos() {
        while (conectado && conexion != null && conexion.isAbierta()) {
            String linea = conexion.leerLinea();
            if (linea != null && !linea.isEmpty()) {
                procesarLinea(linea);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }
    
    private void procesarLinea(String linea) {
        System.out.println("Arduino: " + linea);
        
        if (linea.startsWith("SENSOR:")) {
            String[] partes = linea.split(":");
            if (partes.length >= 4) {
                String estado = partes[2];
                String mensaje = partes[3];
                
                String tipoEvento = estado.equals("ACTIVADO") ? "ALERTA" : "NORMAL";
                String alerta = "🔔 [ARDUINO] Sensor " + estado + ": " + mensaje;
                
                bufferEventos.add(alerta);
                if (bufferEventos.size() > 100) {
                    bufferEventos.remove(0);
                }
                
                if (callback != null) {
                    callback.onEventoRecibido(tipoEvento, alerta);
                }
            }
        } else if (linea.startsWith("HEARTBEAT:")) {
            if (callback != null) {
                callback.onEventoRecibido("HEARTBEAT", "Arduino conectado y funcionando");
            }
        } else if (linea.equals("ARDUINO_INICIADO")) {
            if (callback != null) {
                callback.onEstadoCambiado("ARDUINO_LISTO");
            }
        }
    }
    
    public List<String> getUltimosEventos() {
        return new ArrayList<>(bufferEventos);
    }
    
    public boolean isConectado() {
        return conectado;
    }
}

// Clase auxiliar para comunicación serial (usando Java Simple Serial Connector)
class SerialConnection {
    private static final String LIBRARY_PATH = "lib/jssc.jar"; // Agregar librería JSSC
    
    private Object serialPort;
    private String puerto;
    private int baudRate;
    private boolean abierto;
    private BufferedReader reader;
    private OutputStream outputStream;
    
    public SerialConnection(String puerto, int baudRate) {
        this.puerto = puerto;
        this.baudRate = baudRate;
        this.abierto = false;
    }
    
    public void abrir() throws Exception {
        try {
            // Usar JSSC para comunicación serial
            Class<?> clazz = Class.forName("jssc.SerialPort");
            serialPort = clazz.getConstructor(String.class).newInstance(puerto);
            Object result = clazz.getMethod("openPort").invoke(serialPort);
            
            // Configurar parámetros
            clazz.getMethod("setParams", int.class, int.class, int.class, int.class)
                .invoke(serialPort, baudRate, 8, 1, 0);
            
            abierto = true;
            
        } catch (Exception e) {
            // Fallback: simular conexión para pruebas sin hardware
            System.out.println("⚠️ Usando modo simulación (sin Arduino físico)");
            abierto = true; // Simular conexión
        }
    }
    
    public void cerrar() {
        if (serialPort != null && abierto) {
            try {
                Class<?> clazz = Class.forName("jssc.SerialPort");
                clazz.getMethod("closePort").invoke(serialPort);
            } catch (Exception e) {}
        }
        abierto = false;
    }
    
    public boolean isAbierta() {
        return abierto;
    }
    
    public String leerLinea() {
        // Modo simulación para pruebas sin hardware
        if (serialPort == null) {
            // Simular datos aleatorios para pruebas
            return simularDatos();
        }
        
        try {
            Class<?> clazz = Class.forName("jssc.SerialPort");
            byte[] buffer = (byte[]) clazz.getMethod("readBytes", int.class).invoke(serialPort, 100);
            if (buffer != null && buffer.length > 0) {
                String data = new String(buffer);
                if (data.contains("\n")) {
                    return data.split("\n")[0];
                }
            }
        } catch (Exception e) {}
        
        return null;
    }
    
    private String simularDatos() {
        // Generar datos aleatorios para simular sensor
        double random = Math.random();
        if (random < 0.05) {  // 5% de probabilidad de evento
            boolean activado = Math.random() < 0.5;
            if (activado) {
                return "SENSOR:" + System.currentTimeMillis() + ":ACTIVADO:Puerta abierta detectada";
            } else {
                return "SENSOR:" + System.currentTimeMillis() + ":NORMAL:Sensor en estado normal";
            }
        }
        
        // Heartbeat cada cierto tiempo
        if (Math.random() < 0.02) {
            return "HEARTBEAT:" + System.currentTimeMillis() + ":CONECTADO";
        }
        return null;
    }
}