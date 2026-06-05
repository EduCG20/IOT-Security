package ia;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataAnalyzer {
    private Map<Integer, List<EventData>> eventosPorSensor;
    private List<EventData> todosLosEventos;
    private Map<String, Integer> eventosPorTipo;
    private Map<Integer, Integer> eventosPorHora;
    private List<Double> historialRiesgo;
    
    public DataAnalyzer() {
        this.eventosPorSensor = new ConcurrentHashMap<>();
        this.todosLosEventos = new ArrayList<>();
        this.eventosPorTipo = new ConcurrentHashMap<>();
        this.eventosPorHora = new ConcurrentHashMap<>();
        this.historialRiesgo = new ArrayList<>();
    }
    
    public static class EventData {
        public int sensorId;
        public String sensorNombre;
        public String tipo;
        public String estado;
        public String mensaje;
        public LocalDateTime hora;
        public boolean esNoAutorizado;
        
        public EventData(int sensorId, String sensorNombre, String tipo, String estado, 
                        String mensaje, LocalDateTime hora, boolean esNoAutorizado) {
            this.sensorId = sensorId;
            this.sensorNombre = sensorNombre;
            this.tipo = tipo;
            this.estado = estado;
            this.mensaje = mensaje;
            this.hora = hora;
            this.esNoAutorizado = esNoAutorizado;
        }
    }
    
    public void registrarEvento(int sensorId, String sensorNombre, String tipo, 
                                String estado, String mensaje, boolean esNoAutorizado) {
        EventData evento = new EventData(sensorId, sensorNombre, tipo, estado, 
                                         mensaje, LocalDateTime.now(), esNoAutorizado);
        
        todosLosEventos.add(evento);
        eventosPorSensor.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(evento);
        
        String keyTipo = tipo + "_" + estado;
        eventosPorTipo.merge(keyTipo, 1, Integer::sum);
        
        int hora = LocalDateTime.now().getHour();
        eventosPorHora.merge(hora, 1, Integer::sum);
        
        double riesgoActual = calcularRiesgoActual();
        historialRiesgo.add(riesgoActual);
        if (historialRiesgo.size() > 20) {
            historialRiesgo.remove(0);
        }
        
        if (todosLosEventos.size() > 500) {
            todosLosEventos.remove(0);
        }
    }
    
    private double calcularRiesgoActual() {
        if (todosLosEventos.isEmpty()) return 0;
        double porcentajeAnomalias = getPorcentajeAnomalias();
        if (porcentajeAnomalias > 50) return 0.9;
        if (porcentajeAnomalias > 30) return 0.7;
        if (porcentajeAnomalias > 15) return 0.5;
        if (porcentajeAnomalias > 5) return 0.3;
        return 0.1;
    }
    
    public int getTotalEventos() {
        return todosLosEventos.size();
    }
    
    public int getEventosNoAutorizados() {
        return (int) todosLosEventos.stream().filter(e -> e.esNoAutorizado).count();
    }
    
    public double getPorcentajeAnomalias() {
        if (todosLosEventos.isEmpty()) return 0;
        return (double) getEventosNoAutorizados() / todosLosEventos.size() * 100;
    }
    
    public Map<String, Integer> getEventosPorTipo() {
        return new HashMap<>(eventosPorTipo);
    }
    
    public Map<Integer, Integer> getEventosPorHora() {
        return new HashMap<>(eventosPorHora);
    }
    
    public Map<Integer, Integer> getEventosPorSensor() {
        Map<Integer, Integer> resultado = new HashMap<>();
        for (Map.Entry<Integer, List<EventData>> entry : eventosPorSensor.entrySet()) {
            resultado.put(entry.getKey(), entry.getValue().size());
        }
        return resultado;
    }
    
    public List<EventData> getUltimosEventos(int cantidad) {
        int size = todosLosEventos.size();
        int start = Math.max(0, size - cantidad);
        return new ArrayList<>(todosLosEventos.subList(start, size));
    }
    
    public String getSensorMasActivo() {
        return eventosPorSensor.entrySet().stream()
            .max(Map.Entry.comparingByValue(Comparator.comparingInt(List::size)))
            .map(entry -> "Sensor ID: " + entry.getKey() + " (" + entry.getValue().size() + " evts)")
            .orElse("Ninguno");
    }
    
    public String getHoraMasActiva() {
        return eventosPorHora.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + ":00 (" + entry.getValue() + " evts)")
            .orElse("Ninguna");
    }
    
    public double[] getHistorialRiesgo() {
        double[] array = new double[historialRiesgo.size()];
        for (int i = 0; i < historialRiesgo.size(); i++) {
            array[i] = historialRiesgo.get(i);
        }
        return array;
    }
    
    public double getRiesgoActual() {
        return calcularRiesgoActual();
    }
}