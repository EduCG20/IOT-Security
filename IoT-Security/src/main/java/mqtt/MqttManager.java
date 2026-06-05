package mqtt;


import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttManager {
    private static MqttManager instancia;
    private MqttClient cliente;
    private boolean conectado = false;
    
    // Constructor privado (patrón Singleton)
    private MqttManager() {
        conectar();
    }
    
    // Obtener la única instancia
    public static MqttManager getInstance() {
        if (instancia == null) {
            instancia = new MqttManager();
        }
        return instancia;
    }
    
    // Conectar al broker Mosquitto
    private void conectar() {
        try {
            cliente = new MqttClient(MqttConfig.BROKER, MqttConfig.CLIENT_ID, new MemoryPersistence());
            
            MqttConnectOptions opciones = new MqttConnectOptions();
            opciones.setAutomaticReconnect(true);  // Reconoce automáticamente si se pierde conexión
            opciones.setCleanSession(true);
            
            cliente.connect(opciones);
            conectado = true;
            System.out.println("✅ Conectado a Mosquitto en " + MqttConfig.BROKER);
            
        } catch (MqttException e) {
            conectado = false;
            System.out.println("❌ Error al conectar con Mosquitto: " + e.getMessage());
            System.out.println("   ¿Está ejecutándose Mosquitto?");
        }
    }
    
    // Enviar una alerta
    public void enviarAlerta(String mensaje) {
        if (!conectado || cliente == null || !cliente.isConnected()) {
            System.out.println("⚠️ No se puede enviar alerta: MQTT no conectado");
            // Intentar reconectar
            conectar();
            if (!conectado) return;
        }
        
        try {
            MqttMessage msg = new MqttMessage(mensaje.getBytes());
            msg.setQos(1);  // Calidad de servicio: asegura la entrega
            cliente.publish(MqttConfig.TOPIC, msg);
            System.out.println("📢 Alerta MQTT enviada: " + mensaje);
            
        } catch (MqttException e) {
            System.out.println("❌ Error al enviar alerta: " + e.getMessage());
        }
    }
    
    // Desconectar al cerrar la aplicación
    public void desconectar() {
        try {
            if (cliente != null && cliente.isConnected()) {
                cliente.disconnect();
                conectado = false;
                System.out.println("🔌 Desconectado de Mosquitto");
            }
        } catch (MqttException e) {
            System.out.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    // Verificar si está conectado
    public boolean isConectado() {
        return conectado && cliente != null && cliente.isConnected();
    }
}