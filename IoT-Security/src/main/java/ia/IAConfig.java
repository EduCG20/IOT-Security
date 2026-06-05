package ia;


public class IAConfig {
    // Umbrales para detección de anomalías
    public static final double UMBRAL_ANOMALIA_ALTA = 0.8;      // 80% - Anomalía grave
    public static final double UMBRAL_ANOMALIA_MEDIA = 0.5;     // 50% - Comportamiento sospechoso
    public static final double UMBRAL_ANOMALIA_BAJA = 0.3;      // 30% - Leve desviación
    
    // Pesos para diferentes factores
    public static final double PESO_HORARIO = 0.35;             // Importancia del horario
    public static final double PESO_FRECUENCIA = 0.25;          // Frecuencia de eventos
    public static final double PESO_TIPO_SENSOR = 0.20;         // Tipo de sensor
    public static final double PESO_HISTORIAL = 0.20;           // Historial del sensor
    
    // Tiempos de aprendizaje
    public static final int DIAS_APRENDIZAJE = 30;              // Días para aprender patrones
    public static final int VENTANA_EVENTOS = 50;               // Últimos eventos a analizar
}