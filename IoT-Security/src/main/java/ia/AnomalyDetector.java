package ia;

//package com.iot.ia;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnomalyDetector {
    private Map<Integer, SensorPattern> patronesSensor;
    private Map<Integer, List<Double>> historialAnomalias;
    
    public AnomalyDetector() {
        this.patronesSensor = new ConcurrentHashMap<>();
        this.historialAnomalias = new ConcurrentHashMap<>();
    }
    
    public double detectarAnomalia(int sensorId, String tipo, String estado, LocalDateTime hora) {
        SensorPattern patron = patronesSensor.getOrDefault(sensorId, new SensorPattern(sensorId));
        
        // Calcular puntuaciones de diferentes factores
        double puntuacionHorario = analizarHorario(patron, hora);
        double puntuacionFrecuencia = analizarFrecuencia(patron, hora);
        double puntuacionTipo = analizarTipoEvento(patron, tipo, estado);
        double puntuacionHistorial = analizarHistorial(patron);
        
        // Ponderar puntuaciones
        double puntuacionFinal = 
            (puntuacionHorario * IAConfig.PESO_HORARIO) +
            (puntuacionFrecuencia * IAConfig.PESO_FRECUENCIA) +
            (puntuacionTipo * IAConfig.PESO_TIPO_SENSOR) +
            (puntuacionHistorial * IAConfig.PESO_HISTORIAL);
        
        // Registrar evento para aprendizaje
        patron.registrarEvento(hora, estado, puntuacionFinal);
        patronesSensor.put(sensorId, patron);
        
        // Guardar en historial
        historialAnomalias.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(puntuacionFinal);
        
        return puntuacionFinal;
    }
    
    private double analizarHorario(SensorPattern patron, LocalDateTime hora) {
        LocalTime horaIngreso = hora.toLocalTime();
        Map<LocalTime, Integer> horariosTipicos = patron.getHorariosTipicos();
        
        if (horariosTipicos.isEmpty()) {
            return 0.3; // Puntuación neutral si no hay datos
        }
        
        // Buscar horarios similares en el patrón
        for (LocalTime horario : horariosTipicos.keySet()) {
            if (Math.abs(horario.getHour() - horaIngreso.getHour()) <= 2) {
                double similitud = 1.0 - (Math.abs(horario.getHour() - horaIngreso.getHour()) / 24.0);
                return 1.0 - similitud; // Mayor diferencia = mayor anomalía
            }
        }
        
        return 0.7; // Horario no típico
    }
    
    private double analizarFrecuencia(SensorPattern patron, LocalDateTime hora) {
        int eventosRecientes = patron.getEventosUltimasHoras(24);
        double promedioHistorico = patron.getPromedioEventosDiarios();
        
        if (promedioHistorico == 0) {
            return 0.2;
        }
        
        double desviacion = eventosRecientes / promedioHistorico;
        
        if (desviacion > 3.0) return 0.9;  // Muy superior al promedio
        if (desviacion > 2.0) return 0.7;  // Superior al promedio
        if (desviacion < 0.3) return 0.6;  // Mucho menos actividad
        return 0.2;  // Normal
    }
    
    private double analizarTipoEvento(SensorPattern patron, String tipo, String estado) {
        if ("BROKEN".equals(estado)) {
            return 0.95; // Evento de rotura es alta anomalía
        }
        
        if ("OPEN".equals(estado)) {
            double porcentajeAperturas = patron.getPorcentajeAperturasNocturnas();
            if (porcentajeAperturas > 0.5) {
                return 0.8; // Muchas aperturas nocturnas
            }
            return 0.3;
        }
        
        return 0.1; // Cierres normales
    }
    
    private double analizarHistorial(SensorPattern patron) {
        double promedioAnomalias = patron.getPromedioAnomaliasHistoricas();
        
        if (promedioAnomalias > IAConfig.UMBRAL_ANOMALIA_ALTA) {
            return 0.9; // Historial de alta anomalía
        }
        if (promedioAnomalias > IAConfig.UMBRAL_ANOMALIA_MEDIA) {
            return 0.6; // Historial de media anomalía
        }
        return 0.2; // Historial normal
    }
    
    public String obtenerNivelAnomalia(double puntuacion) {
        if (puntuacion >= IAConfig.UMBRAL_ANOMALIA_ALTA) {
            return "CRÍTICA";
        } else if (puntuacion >= IAConfig.UMBRAL_ANOMALIA_MEDIA) {
            return "ALTA";
        } else if (puntuacion >= IAConfig.UMBRAL_ANOMALIA_BAJA) {
            return "MEDIA";
        }
        return "BAJA";
    }
    
    public class SensorPattern {
        private int sensorId;
        private List<LocalDateTime> historialEventos;
        private Map<LocalTime, Integer> horariosTipicos;
        private List<Double> historialAnomalias;
        
        public SensorPattern(int sensorId) {
            this.sensorId = sensorId;
            this.historialEventos = new ArrayList<>();
            this.horariosTipicos = new HashMap<>();
            this.historialAnomalias = new ArrayList<>();
        }
        
        public void registrarEvento(LocalDateTime hora, String estado, double puntuacion) {
            historialEventos.add(hora);
            historialAnomalias.add(puntuacion);
            
            // Mantener solo los últimos eventos
            if (historialEventos.size() > IAConfig.VENTANA_EVENTOS) {
                historialEventos.remove(0);
                historialAnomalias.remove(0);
            }
            
            // Actualizar horarios típicos
            LocalTime horario = hora.toLocalTime();
            int horaRedondeada = horario.getHour();
            horariosTipicos.merge(LocalTime.of(horaRedondeada, 0), 1, Integer::sum);
        }
        
        public Map<LocalTime, Integer> getHorariosTipicos() {
            return horariosTipicos;
        }
        
        public int getEventosUltimasHoras(int horas) {
            LocalDateTime limite = LocalDateTime.now().minusHours(horas);
            return (int) historialEventos.stream()
                .filter(fecha -> fecha.isAfter(limite))
                .count();
        }
        
        public double getPromedioEventosDiarios() {
            if (historialEventos.isEmpty()) return 0;
            
            LocalDateTime primera = historialEventos.get(0);
            LocalDateTime ultima = historialEventos.get(historialEventos.size() - 1);
            long dias = java.time.Duration.between(primera, ultima).toDays();
            
            if (dias < 1) return historialEventos.size();
            return (double) historialEventos.size() / dias;
        }
        
        public double getPorcentajeAperturasNocturnas() {
            long nocturnas = historialEventos.stream()
                .filter(hora -> hora.getHour() >= 22 || hora.getHour() <= 6)
                .count();
            return historialEventos.isEmpty() ? 0 : (double) nocturnas / historialEventos.size();
        }
        
        public double getPromedioAnomaliasHistoricas() {
            return historialAnomalias.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        }
    }
}