package mqtt;


public class MqttConfig {
    // Dirección del broker Mosquitto (localhost porque está en tu misma PC)
    public static final String BROKER = "tcp://localhost:1883";
    
    // Topic donde se publicarán las alertas
    public static final String TOPIC = "smarthome/alerta";
    
    // ID del cliente (identificador único de esta aplicación)
    public static final String CLIENT_ID = "IoTSecurityApp";
}