package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonExporter {
    private static final String CARPETA_EXPORTACION = "exportaciones_iot/";
    
    public static void exportarEventosAJson(List<Map<String, Object>> eventos) {
        try {
            File directorio = new File(CARPETA_EXPORTACION);
            if (!directorio.exists()) {
                directorio.mkdirs();
                System.out.println("📁 Carpeta creada: " + CARPETA_EXPORTACION);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = CARPETA_EXPORTACION + "eventos_iot_" + timestamp + ".json";
            
            Map<String, Object> datosExportacion = new HashMap<>();
            datosExportacion.put("fecha_exportacion", LocalDateTime.now().toString());
            datosExportacion.put("total_eventos", eventos.size());
            datosExportacion.put("eventos", eventos);
            datosExportacion.put("version_sistema", "1.0");
            datosExportacion.put("tipo_exportacion", "eventos_simulacion");
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(datosExportacion);
            
            try (FileWriter writer = new FileWriter(nombreArchivo)) {
                writer.write(json);
            }
            
            System.out.println("📁 Datos exportados a: " + nombreArchivo);
            System.out.println("📊 Total eventos exportados: " + eventos.size());
            
        } catch (IOException e) {
            System.err.println("❌ Error al exportar JSON: " + e.getMessage());
        }
    }
    
    public static void exportarEstadisticasIA(Map<String, Object> estadisticas) {
        try {
            File directorio = new File(CARPETA_EXPORTACION);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = CARPETA_EXPORTACION + "estadisticas_ia_" + timestamp + ".json";
            
            Map<String, Object> datosExportacion = new HashMap<>();
            datosExportacion.put("fecha_exportacion", LocalDateTime.now().toString());
            datosExportacion.put("estadisticas", estadisticas);
            datosExportacion.put("version_sistema", "1.0");
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(datosExportacion);
            
            try (FileWriter writer = new FileWriter(nombreArchivo)) {
                writer.write(json);
            }
            
            System.out.println("📊 Estadísticas exportadas a: " + nombreArchivo);
            
        } catch (IOException e) {
            System.err.println("❌ Error al exportar estadísticas: " + e.getMessage());
        }
    }
    
    public static void exportarReporteCompleto(List<Map<String, Object>> eventos, 
                                                Map<String, Object> estadisticas,
                                                Map<String, Object> configuracion) {
        try {
            File directorio = new File(CARPETA_EXPORTACION);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nombreArchivo = CARPETA_EXPORTACION + "reporte_completo_" + timestamp + ".json";
            
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("fecha_generacion", LocalDateTime.now().toString());
            reporte.put("total_eventos", eventos.size());
            reporte.put("eventos", eventos);
            reporte.put("estadisticas_ia", estadisticas);
            reporte.put("configuracion_sistema", configuracion);
            reporte.put("version_sistema", "1.0");
            
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(reporte);
            
            try (FileWriter writer = new FileWriter(nombreArchivo)) {
                writer.write(json);
            }
            
            System.out.println("📋 Reporte completo exportado a: " + nombreArchivo);
            
        } catch (IOException e) {
            System.err.println("❌ Error al exportar reporte: " + e.getMessage());
        }
    }
}