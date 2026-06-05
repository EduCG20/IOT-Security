package neural;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class RobberyPredictor {
    private static NeuralNetwork network;
    private static Map<String, Boolean> holidaysCache;
    
    static {
        // Red: 4 entradas, 8 neuronas ocultas, 1 salida
        network = new NeuralNetwork(4, 8, 1, 0.15);
        holidaysCache = new HashMap<>();
        
        System.out.println("=== ENTRENANDO RED NEURONAL ===");
        System.out.println("Arquitectura: 4 → 8 → 1");
        System.out.println("Learning rate: 0.15");
        System.out.println("-------------------------------");
        
        double[][] inputs = RobberyDataGenerator.getTrainingInputs();
        double[][] outputs = RobberyDataGenerator.getTrainingOutputs();
        
        network.train(inputs, outputs, 3000);
        
        System.out.println("¡Entrenamiento completado!");
        System.out.println("============================\n");
    }
    
    public static double predictRisk(LocalDateTime dateTime) {
        // Normalizar entradas
        double hour = dateTime.getHour() / 24.0;
        double dayOfWeek = (dateTime.getDayOfWeek().getValue() - 1) / 6.0;
        double isWeekend = (dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || 
                           dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) ? 1.0 : 0.0;
        double isHoliday = isHoliday(dateTime) ? 1.0 : 0.0;
        
        double[] inputs = {hour, dayOfWeek, isWeekend, isHoliday};
        double risk = network.predict(inputs);
        
        return Math.min(1.0, Math.max(0.0, risk));
    }
    
    private static boolean isHoliday(LocalDateTime dateTime) {
        String key = dateTime.getMonth() + "-" + dateTime.getDayOfMonth();
        
        if (holidaysCache.containsKey(key)) {
            return holidaysCache.get(key);
        }
        
        boolean isHoliday = false;
        Month month = dateTime.getMonth();
        int day = dateTime.getDayOfMonth();
        
        // Festivos de ejemplo (puedes ampliarlo)
        if (month == Month.JANUARY && day == 1) isHoliday = true;      // Año Nuevo
        else if (month == Month.DECEMBER && day == 25) isHoliday = true; // Navidad
        else if (month == Month.DECEMBER && day == 24) isHoliday = true; // Nochebuena
        else if (month == Month.MAY && day == 1) isHoliday = true;       // Día del Trabajo
        
        holidaysCache.put(key, isHoliday);
        return isHoliday;
    }
    
    public static String getAlertMessage(double risk) {
        if (risk > 0.85) {
            return "🔴🔴 ALERTA MÁXIMA: RIESGO EXTREMO DE ROBO 🔴🔴\n" +
                   "▶ Activar todas las medidas de seguridad\n" +
                   "▶ Notificar a seguridad\n" +
                   "▶ Revisar todas las cámaras y sensores";
        } else if (risk > 0.70) {
            return "🔴 ALERTA ROJA: Alta probabilidad de robo\n" +
                   "▶ Activar alarmas perimetrales\n" +
                   "▶ Monitoreo intensivo";
        } else if (risk > 0.50) {
            return "🟡 PRECAUCIÓN: Riesgo moderado\n" +
                   "▶ Mantener vigilancia\n" +
                   "▶ Revisar puertas y ventanas";
        } else if (risk > 0.25) {
            return "🟢 RIESGO BAJO: Sin amenazas inmediatas\n" +
                   "▶ Monitoreo normal";
        } else {
            return "✅ SEGURO: Ambiente tranquilo\n" +
                   "▶ Sistema en modo normal";
        }
    }
    
    public static void printRiskAnalysis(LocalDateTime dateTime) {
        double risk = predictRisk(dateTime);
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│ ANÁLISIS DE RIESGO DE ROBO              │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.printf ("│ Fecha y hora: %-28s│\n", dateTime);
        System.out.printf ("│ Día: %-34s│\n", dateTime.getDayOfWeek());
        System.out.printf ("│ Hora: %-33d│\n", dateTime.getHour());
        System.out.printf ("│ Riesgo: %-31.1f%%│\n", risk * 100);
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ " + getAlertMessage(risk).replace("\n", "\n│ "));
        System.out.println("└─────────────────────────────────────────┘");
    }
    
    // Prueba
    public static void main(String[] args) {
        System.out.println("Probando diferentes horarios:\n");
        
        LocalDateTime[] tests = {
            LocalDateTime.of(2025, 4, 23, 3, 0),   // 3am miércoles
            LocalDateTime.of(2025, 4, 23, 14, 0),  // 2pm miércoles
            LocalDateTime.of(2025, 4, 26, 3, 0),   // 3am sábado
            LocalDateTime.of(2025, 4, 26, 22, 0),  // 10pm sábado
            LocalDateTime.of(2025, 4, 23, 8, 0),   // 8am miércoles
            LocalDateTime.of(2025, 12, 25, 2, 0),  // 2am Navidad
            LocalDateTime.of(2025, 5, 1, 15, 0),   // 3pm Día del Trabajo
        };
        
        for (LocalDateTime test : tests) {
            printRiskAnalysis(test);
            System.out.println();
        }
    }
}