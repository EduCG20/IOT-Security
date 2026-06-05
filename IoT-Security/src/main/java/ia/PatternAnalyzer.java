package ia;

import java.util.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

public class PatternAnalyzer {
    
    public static class Patron {
        public String tipo;
        public double confianza;
        public List<String> condiciones;
        
        public Patron(String tipo, double confianza, List<String> condiciones) {
            this.tipo = tipo;
            this.confianza = confianza;
            this.condiciones = condiciones;
        }
    }
    
    public List<Patron> analizarPatrones(List<Map<String, Object>> eventos) {
        List<Patron> patronesEncontrados = new ArrayList<>();
        
        // Patrón 1: Horarios pico de actividad
        Map<Integer, Integer> actividadPorHora = new HashMap<>();
        for (Map<String, Object> evento : eventos) {
            LocalDateTime hora = (LocalDateTime) evento.get("hora");
            int horaInt = hora.getHour();
            actividadPorHora.merge(horaInt, 1, Integer::sum);
        }
        
        int horaPico = actividadPorHora.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(-1);
        
        if (horaPico != -1) {
            double confianza = (double) actividadPorHora.get(horaPico) / eventos.size();
            if (confianza > 0.3) {
                patronesEncontrados.add(new Patron(
                    "HORARIO_PICO",
                    confianza,
                    Arrays.asList("mayor actividad a las " + horaPico + ":00", 
                                 "confianza: " + String.format("%.2f", confianza * 100) + "%")
                ));
            }
        }
        
        // Patrón 2: Eventos sospechosos recurrentes
        long eventosSospechosos = eventos.stream()
            .filter(e -> "BROKEN".equals(e.get("estado")) || 
                        (Boolean.TRUE.equals(e.get("noAutorizado"))))
            .count();
        
        double porcentajeSospechosos = (double) eventosSospechosos / eventos.size();
        if (porcentajeSospechosos > 0.2) {
            patronesEncontrados.add(new Patron(
                "ALTO_RIESGO",
                porcentajeSospechosos,
                Arrays.asList(porcentajeSospechosos * 100 + "% de eventos sospechosos",
                             "revisar configuración de seguridad")
            ));
        }
        
        return patronesEncontrados;
    }
    
    public String generarRecomendacion(List<Patron> patrones) {
        StringBuilder recomendacion = new StringBuilder();
        
        for (Patron patron : patrones) {
            switch (patron.tipo) {
                case "HORARIO_PICO":
                    recomendacion.append("• Considere ajustar horarios de monitoreo\n");
                    break;
                case "ALTO_RIESGO":
                    recomendacion.append("• Aumentar medidas de seguridad\n");
                    recomendacion.append("• Revisar sensores críticos\n");
                    break;
            }
        }
        
        if (recomendacion.length() == 0) {
            recomendacion.append("• Patrones normales detectados\n• Sistema operando correctamente");
        }
        
        return recomendacion.toString();
    }
}