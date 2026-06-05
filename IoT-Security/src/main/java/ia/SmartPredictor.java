package ia;


import java.time.LocalDateTime;
import java.util.*;

public class SmartPredictor {
    private Map<Integer, List<LocalDateTime>> historialPorSensor;
    
    public SmartPredictor() {
        this.historialPorSensor = new HashMap<>();
    }
    
    public double predecirRiesgo(int sensorId, LocalDateTime momento) {
        List<LocalDateTime> historial = historialPorSensor.getOrDefault(sensorId, new ArrayList<>());
        
        if (historial.size() < 5) {
            return 0.3; // Riesgo bajo por falta de datos
        }
        
        double riesgo = 0.0;
        
        // Factor 1: Hora del día
        int hora = momento.getHour();
        if (hora >= 22 || hora <= 6) {
            riesgo += 0.4; // Mayor riesgo en la noche
        }
        
        // Factor 2: Día de la semana
        int diaSemana = momento.getDayOfWeek().getValue();
        if (diaSemana >= 6) {
            riesgo += 0.2; // Mayor riesgo en fin de semana
        }
        
        // Factor 3: Historial reciente
        LocalDateTime haceSemana = momento.minusDays(7);
        long eventosRecientes = historial.stream()
            .filter(fecha -> fecha.isAfter(haceSemana))
            .count();
        
        if (eventosRecientes > 10) {
            riesgo += 0.3;
        } else if (eventosRecientes > 5) {
            riesgo += 0.1;
        }
        
        return Math.min(riesgo, 1.0);
    }
    
    public String obtenerNivelRiesgo(double riesgo) {
        if (riesgo >= 0.7) return "CRÍTICO 🚨";
        if (riesgo >= 0.5) return "ALTO ⚠️";
        if (riesgo >= 0.3) return "MEDIO 📊";
        return "BAJO ✅";
    }
    
    public void registrarEvento(int sensorId, LocalDateTime fecha) {
        historialPorSensor.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(fecha);
        
        // Limitar historial
        List<LocalDateTime> historial = historialPorSensor.get(sensorId);
        if (historial.size() > 1000) {
            historial.remove(0);
        }
    }
}