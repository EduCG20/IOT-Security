package ia;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.awt.*;
import java.util.Map;

public class ChartGenerator {
    
    public static ChartPanel crearGraficoBarrasEventos(Map<Integer, Integer> eventosPorHora) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (int hora = 0; hora < 24; hora++) {
            int valor = eventosPorHora.getOrDefault(hora, 0);
            dataset.addValue(valor, "Eventos", hora + ":00");
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Eventos por Hora",
            "Hora del día",
            "Número de Eventos",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false);
        
        chart.setBackgroundPaint(Color.WHITE);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(350, 250));
        return panel;
    }
    
    public static ChartPanel crearGraficoTortaTipos(Map<String, Integer> eventosPorTipo) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Integer> entry : eventosPorTipo.entrySet()) {
            String key = entry.getKey().replace("WINDOW_", "Ventana ").replace("DOOR_", "Puerta ");
            dataset.setValue(key, entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Distribución de Eventos",
            dataset,
            true, true, false);
        
        chart.setBackgroundPaint(Color.WHITE);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(350, 250));
        return panel;
    }
    
    public static ChartPanel crearGraficoLineasRiesgo(double[] riesgos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (int i = 0; i < riesgos.length; i++) {
            dataset.addValue(riesgos[i] * 100, "Riesgo %", String.valueOf(i + 1));
        }
        
        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Riesgo",
            "Muestra",
            "Riesgo (%)",
            dataset,
            PlotOrientation.VERTICAL,
            false, true, false);
        
        chart.setBackgroundPaint(Color.WHITE);
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(350, 200));
        return panel;
    }
}