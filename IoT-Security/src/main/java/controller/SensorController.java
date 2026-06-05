package controller;


import dao.SensorDAO;
import dao.SensorEventDAO;
import entities.*;
import mqtt.MqttManager;
import ia.AnomalyDetector;
import ia.SmartPredictor;
import java.time.LocalDateTime;
import java.util.List;

public class SensorController {
    private SensorDAO sensorDAO;
    private SensorEventDAO eventDAO;
    private AuthController authController;
    private AnomalyDetector anomalyDetector;
    private SmartPredictor predictor;
    
    public SensorController(AuthController authController) {
        this.sensorDAO = new SensorDAO();
        this.eventDAO = new SensorEventDAO();
        this.authController = authController;
        this.anomalyDetector = new AnomalyDetector();
        this.predictor = new SmartPredictor();
    }
    
    public List<BaseSensor> getSensorsForHouse(int houseId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return sensorDAO.getSensorsByHouse(houseId);
    }
    
    public void registerSensorEvent(int sensorId, String status, String message) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        BaseSensor sensor = sensorDAO.getSensorById(sensorId);
        if (sensor == null) return;
        
        boolean isUnauthorized = sensor.checkUnauthorizedAccess(LocalDateTime.now());
        
        SensorEvent event = new SensorEvent(sensorId, status, message, isUnauthorized);
        eventDAO.createEvent(event);
        
        if (isUnauthorized) {
            sendAlert(sensor, event);
            enviarAlertaMQTT(sensor, event);
            
            // === IA: Detectar anomalía y predecir riesgo ===
            double puntuacionAnomalia = anomalyDetector.detectarAnomalia(
                sensorId,
                sensor.getSensorType(),
                status,
                LocalDateTime.now()
            );
            
            String nivelAnomalia = anomalyDetector.obtenerNivelAnomalia(puntuacionAnomalia);
            
            double riesgo = predictor.predecirRiesgo(sensorId, LocalDateTime.now());
            String nivelRiesgo = predictor.obtenerNivelRiesgo(riesgo);
            
            // Enviar alerta enriquecida con IA
            String alertaIA = String.format(
                "{\"sensorId\":%d,\"evento\":\"%s\",\"anomalia\":%.2f,\"nivel\":\"%s\",\"riesgo\":\"%s\"}",
                sensorId, message, puntuacionAnomalia, nivelAnomalia, nivelRiesgo
            );
            
            MqttManager.getInstance().enviarAlerta(alertaIA);
            
            // Registrar para predicciones
            predictor.registrarEvento(sensorId, LocalDateTime.now());
            
            System.out.println("🤖 IA: Anomalía detectada - Nivel: " + nivelAnomalia +
                              " | Riesgo: " + nivelRiesgo +
                              " | Puntuación: " + String.format("%.2f", puntuacionAnomalia));
        }
    }
    
    private void sendAlert(BaseSensor sensor, SensorEvent event) {
        System.out.println("\n🚨 🚨 🚨 ALERTA DE SEGURIDAD 🚨 🚨 🚨");
        System.out.println("======================================");
        System.out.println("¡Acceso no autorizado detectado!");
        System.out.println("Sensor: " + sensor.getName());
        System.out.println("Tipo: " + sensor.getSensorType());
        System.out.println("Ubicación: " + sensor.getLocation());
        System.out.println("Mensaje: " + event.getMessage());
        System.out.println("Hora: " + event.getEventTime());
        System.out.println("======================================\n");
    }
    
    private void enviarAlertaMQTT(BaseSensor sensor, SensorEvent event) {
        try {
            String mensaje = String.format(
                "{\"tipo\":\"%s\",\"nombre\":\"%s\",\"ubicacion\":\"%s\",\"mensaje\":\"%s\",\"hora\":\"%s\"}",
                sensor.getSensorType(),
                sensor.getName(),
                sensor.getLocation(),
                event.getMessage(),
                event.getEventTime()
            );
            
            MqttManager.getInstance().enviarAlerta(mensaje);
            System.out.println("📡 Alerta MQTT enviada: " + mensaje);
            
        } catch (Exception e) {
            System.err.println("❌ Error MQTT: " + e.getMessage());
        }
    }
    
    public boolean addSensor(BaseSensor sensor) {
        if (!authController.isAuthenticated() || !authController.isAdmin()) {
            throw new SecurityException("Admin privileges required");
        }
        return sensorDAO.saveSensor(sensor);
    }
    
    public BaseSensor getSensorDetails(int sensorId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return sensorDAO.getSensorById(sensorId);
    }
    
    // Métodos adicionales para la IA
    public AnomalyDetector getAnomalyDetector() {
        return anomalyDetector;
    }
    
    public SmartPredictor getPredictor() {
        return predictor;
    }
    
    public double getRiesgoActual(int sensorId) {
        if (!authController.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return predictor.predecirRiesgo(sensorId, LocalDateTime.now());
    }
    
    public String getNivelRiesgoActual(int sensorId) {
        double riesgo = getRiesgoActual(sensorId);
        return predictor.obtenerNivelRiesgo(riesgo);
    }
}