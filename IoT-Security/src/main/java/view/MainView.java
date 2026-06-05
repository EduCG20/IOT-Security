package view;

import controller.AuthController;
import controller.SensorController;
import entities.*;
import mqtt.MqttManager;
import ia.DataAnalyzer;
import ia.ChartGenerator;
import utils.EmailSender;
import utils.JsonExporter;
import controller.ArduinoReader;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.*;

public class MainView extends JFrame {
    private AuthController authController;
    private SensorController sensorController;
    private HouseController houseController;
    private DataAnalyzer dataAnalyzer;
    
    // Colores
    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SECONDARY = new Color(52, 152, 219);
    private static final Color COLOR_SUCCESS = new Color(46, 204, 113);
    private static final Color COLOR_WARNING = new Color(241, 196, 15);
    private static final Color COLOR_DANGER = new Color(231, 76, 60);
    private static final Color COLOR_DARK = new Color(44, 62, 80);
    private static final Color COLOR_WHITE = new Color(255, 255, 255);
    private static final Color COLOR_BACKGROUND = new Color(248, 249, 250);
    private static final Color COLOR_BLACK = new Color(0, 0, 0);
    
    // Componentes principales
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel menuPanel;
    private CardLayout cardLayout;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;
    
    private JLabel userLabel;
    private JLabel roleLabel;
    private DefaultListModel<String> sensorsListModel;
    private JList<String> sensorsList;
    private JLabel mqttStatusLabel;
    private int currentHouseId = 1;
    private JComboBox<String> houseSelector;
    
    // Componentes de alertas
    private JTextArea alertasArea;
    private JButton btnIniciarSimulacion;
    private JButton btnDetenerSimulacion;
    private JLabel lblSimulacionEstado;
    private Timer timerSimulacion;
    private Random random;
    private boolean simulacionActiva = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Componentes de IA
    private JTabbedPane tabbedPaneIA;
    private JLabel lblTotalEventos;
    private JLabel lblEventosNoAutorizados;
    private JLabel lblPorcentajeAnomalias;
    private JLabel lblSensorMasActivo;
    private JLabel lblHoraMasActiva;
    private JLabel lblRiesgoGeneral;
    private JTextArea txtAnalisisIA;
    private javax.swing.Timer timerAnalisisIA;
    private JPanel graficasContainer;
    
    // Para exportación de datos
    private List<Map<String, Object>> historialEventosExportacion;
    
    // Componentes de Arduino
    private ArduinoReader arduinoReader;
    private boolean arduinoConectado = false;
    private JLabel lblArduinoEstado;
    private JTextArea areaEventosArduino;
    private JDialog dialogCasaFisica;
    
    public MainView() {
        this.authController = new AuthController();
        this.random = new Random();
        this.dataAnalyzer = new DataAnalyzer();
        this.inicializarExportacion();
        this.inicializarArduino();
        iniciarMQTT();
        initializeUI();
    }
    
    private void iniciarMQTT() {
        MqttManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MqttManager.getInstance().desconectar();
            detenerSimulacion();
        }));
    }
    
    private void initializeUI() {
        setTitle("Sistema de Seguridad IoT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BACKGROUND);
        
        createLoginPanel();
        createMenuPanel();
        
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(menuPanel, "MENU");
        
        add(mainPanel);
        showLoginScreen();
    }
    
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, COLOR_PRIMARY, w, h, COLOR_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel iconLabel = new JLabel("🔒");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setForeground(COLOR_WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(iconLabel, gbc);
        
        JLabel titleLabel = new JLabel("SISTEMA DE SEGURIDAD IoT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(COLOR_WHITE);
        gbc.gridy = 1;
        loginPanel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("Acceso Seguro al Panel de Control");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(COLOR_WHITE);
        gbc.gridy = 2;
        loginPanel.add(subtitleLabel, gbc);
        
        JPanel loginFormPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        loginFormPanel.setOpaque(false);
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(8, 8, 8, 8);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.gridwidth = GridBagConstraints.REMAINDER;
        
        JLabel userIconLabel = new JLabel("👤");
        userIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        formGbc.gridy = 0;
        loginFormPanel.add(userIconLabel, formGbc);
        
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridy = 1;
        loginFormPanel.add(usernameField, formGbc);
        
        JLabel passwordIconLabel = new JLabel("🔑");
        passwordIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        formGbc.gridy = 2;
        loginFormPanel.add(passwordIconLabel, formGbc);
        
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formGbc.gridy = 3;
        loginFormPanel.add(passwordField, formGbc);
        
        loginButton = new JButton("INICIAR SESIÓN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(COLOR_SUCCESS);
        loginButton.setForeground(COLOR_BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());
        
        formGbc.gridy = 4;
        formGbc.insets = new Insets(20, 8, 8, 8);
        loginFormPanel.add(loginButton, formGbc);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(COLOR_DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formGbc.gridy = 5;
        formGbc.insets = new Insets(5, 8, 8, 8);
        loginFormPanel.add(statusLabel, formGbc);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginPanel.add(loginFormPanel, gbc);
    }
    
    private void createMenuPanel() {
        menuPanel = new JPanel(new BorderLayout(0, 0));
        menuPanel.setBackground(COLOR_BACKGROUND);
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_DARK);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1));
        userInfoPanel.setBackground(COLOR_DARK);
        
        userLabel = new JLabel();
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userLabel.setForeground(COLOR_WHITE);
        
        roleLabel = new JLabel();
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(200, 200, 200));
        
        userInfoPanel.add(userLabel);
        userInfoPanel.add(roleLabel);
        
        // Selector de casas
        JPanel housePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        housePanel.setBackground(COLOR_DARK);
        
        JLabel houseLabel = new JLabel("🏠 Casa:");
        houseLabel.setForeground(COLOR_WHITE);
        houseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        houseSelector = new JComboBox<>();
        houseSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        houseSelector.setBackground(COLOR_WHITE);
        houseSelector.addActionListener(e -> cambiarCasa());
        
        housePanel.add(houseLabel);
        housePanel.add(houseSelector);
        
        JPanel mqttPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        mqttPanel.setBackground(COLOR_DARK);
        
        mqttStatusLabel = new JLabel();
        actualizarStatusMQTT();
        mqttStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        mqttPanel.add(mqttStatusLabel);
        
        JButton logoutButton = new JButton("CERRAR SESIÓN");
        logoutButton.setBackground(COLOR_DANGER);
        logoutButton.setForeground(COLOR_BLACK);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.addActionListener(e -> logout());
        
        topPanel.add(userInfoPanel, BorderLayout.WEST);
        topPanel.add(housePanel, BorderLayout.CENTER);
        topPanel.add(mqttPanel, BorderLayout.EAST);
        topPanel.add(logoutButton, BorderLayout.SOUTH);
        
        menuPanel.add(topPanel, BorderLayout.NORTH);
        
        // Panel central - Ahora con 5 columnas para incluir Arduino
        JPanel contentPanel = new JPanel(new GridLayout(1, 5, 8, 0));
        contentPanel.setBackground(COLOR_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        contentPanel.add(createSensorsListPanel());
        contentPanel.add(createActionsPanel());
        contentPanel.add(createAlertasPanel());
        contentPanel.add(createIAPanel());
        contentPanel.add(createArduinoPanel());
        
        menuPanel.add(contentPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = createStatsPanel();
        menuPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createArduinoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Título
        JLabel titleLabel = new JLabel("🔌 ARDUINO UNO - SENSOR PIN 7");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Estado
        lblArduinoEstado = new JLabel("⚪ Desconectado");
        lblArduinoEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblArduinoEstado.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblArduinoEstado, BorderLayout.CENTER);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        buttonPanel.setBackground(COLOR_WHITE);
        
        JButton btnConectar = new JButton("🔌 CONECTAR");
        btnConectar.setBackground(COLOR_SUCCESS);
        btnConectar.setForeground(COLOR_BLACK);
        btnConectar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnConectar.addActionListener(e -> conectarArduino());
        
        JButton btnTest = new JButton("🧪 TEST");
        btnTest.setBackground(COLOR_WARNING);
        btnTest.setForeground(COLOR_BLACK);
        btnTest.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTest.addActionListener(e -> {
            if (arduinoReader != null) {
                arduinoReader.getUltimosEventos();
                agregarAlerta("🧪 Prueba enviada al Arduino");
            }
        });
        
        buttonPanel.add(btnConectar);
        buttonPanel.add(btnTest);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Área de eventos de Arduino (scroll)
        areaEventosArduino = new JTextArea();
        areaEventosArduino.setEditable(false);
        areaEventosArduino.setFont(new Font("Monospaced", Font.PLAIN, 10));
        areaEventosArduino.setBackground(new Color(44, 62, 80));
        areaEventosArduino.setForeground(Color.WHITE);
        areaEventosArduino.setMargin(new Insets(5, 5, 5, 5));
        areaEventosArduino.setText("📡 Esperando datos del Arduino...\n");
        
        JScrollPane scrollArduino = new JScrollPane(areaEventosArduino);
        scrollArduino.setPreferredSize(new Dimension(200, 120));
        panel.add(scrollArduino, BorderLayout.EAST);
        
        return panel;
    }
    
    private void inicializarArduino() {
        arduinoReader = ArduinoReader.getInstance();
        arduinoReader.setCallback(new ArduinoReader.CallbackUI() {
            @Override
            public void onEventoRecibido(String tipo, String mensaje) {
                SwingUtilities.invokeLater(() -> {
                    String timestamp = LocalDateTime.now().format(formatter);
                    if (tipo.equals("ALERTA")) {
                        areaEventosArduino.append("[" + timestamp + "] 🔴 " + mensaje + "\n");
                        agregarAlerta("🔴 [ARDUINO] " + mensaje);
                        MqttManager.getInstance().enviarAlerta("🔴 ARDUINO: " + mensaje);
                    } else {
                        areaEventosArduino.append("[" + timestamp + "] 🟢 " + mensaje + "\n");
                    }
                    areaEventosArduino.setCaretPosition(areaEventosArduino.getDocument().getLength());
                });
            }
            
            @Override
            public void onEstadoCambiado(String estado) {
                SwingUtilities.invokeLater(() -> {
                    if (estado.contains("CONECTADO") || estado.equals("ARDUINO_LISTO")) {
                        lblArduinoEstado.setText("🟢 Conectado - Puerto activo");
                        lblArduinoEstado.setForeground(COLOR_SUCCESS);
                        arduinoConectado = true;
                        areaEventosArduino.append("✅ Arduino conectado correctamente\n");
                    } else if (estado.contains("ERROR")) {
                        lblArduinoEstado.setText("🔴 " + estado);
                        lblArduinoEstado.setForeground(COLOR_DANGER);
                        arduinoConectado = false;
                    } else {
                        lblArduinoEstado.setText("⚪ " + estado);
                        lblArduinoEstado.setForeground(Color.GRAY);
                    }
                });
            }
        });
    }
    
    private void conectarArduino() {
        String puerto = JOptionPane.showInputDialog(this, 
            "Ingrese el puerto COM del Arduino:\n\n" +
            "Windows: COM3, COM4, COM5\n" +
            "Linux: /dev/ttyUSB0\n" +
            "macOS: /dev/cu.usbmodem\n\n" +
            "Para encontrar el puerto:\n" +
            "1. Abra el IDE de Arduino\n" +
            "2. Herramientas → Puerto\n" +
            "3. Seleccione el puerto que aparece",
            "Conectar Arduino Real",
            JOptionPane.QUESTION_MESSAGE);
        
        if (puerto != null && !puerto.trim().isEmpty()) {
            arduinoReader.conectar(puerto.trim());
        }
    }
    
    // ==================== MÉTODOS DE EXPORTACIÓN ====================
    
    private void inicializarExportacion() {
        historialEventosExportacion = new ArrayList<>();
    }
    
    private void registrarEventoParaExportacion(int sensorId, String sensorNombre, String estado, 
                                                 String mensaje, boolean esNoAutorizado, String casa) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("timestamp", LocalDateTime.now().format(formatter));
        evento.put("sensor_id", sensorId);
        evento.put("sensor_nombre", sensorNombre);
        evento.put("estado", estado);
        evento.put("mensaje", mensaje);
        evento.put("es_no_autorizado", esNoAutorizado);
        evento.put("casa", casa);
        evento.put("tipo_evento", esNoAutorizado ? "ALERTA" : "NORMAL");
        
        historialEventosExportacion.add(evento);
        
        if (historialEventosExportacion.size() > 500) {
            historialEventosExportacion.remove(0);
        }
    }
    
    private void exportarEventosJSON() {
        if (historialEventosExportacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay eventos para exportar", 
                "Exportar JSON", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JsonExporter.exportarEventosAJson(historialEventosExportacion);
        JOptionPane.showMessageDialog(this, 
            "✅ Eventos exportados a JSON correctamente\nCarpeta: exportaciones_iot/", 
            "Exportación Exitosa", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exportarEstadisticasJSON() {
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total_eventos", dataAnalyzer.getTotalEventos());
        estadisticas.put("eventos_no_autorizados", dataAnalyzer.getEventosNoAutorizados());
        estadisticas.put("porcentaje_anomalias", dataAnalyzer.getPorcentajeAnomalias());
        estadisticas.put("sensor_mas_activo", dataAnalyzer.getSensorMasActivo());
        estadisticas.put("hora_mas_activa", dataAnalyzer.getHoraMasActiva());
        estadisticas.put("riesgo_actual", dataAnalyzer.getRiesgoActual());
        estadisticas.put("fecha_analisis", LocalDateTime.now().toString());
        
        JsonExporter.exportarEstadisticasIA(estadisticas);
        JOptionPane.showMessageDialog(this, 
            "✅ Estadísticas exportadas a JSON correctamente\nCarpeta: exportaciones_iot/", 
            "Exportación Exitosa", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ==================== IA Y GRÁFICAS ====================
    
    private JPanel createIAPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel("🤖 IA - ANÁLISIS INTELIGENTE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(155, 89, 182));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        tabbedPaneIA = new JTabbedPane();
        tabbedPaneIA.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        tabbedPaneIA.addTab("📊 Stats", createEstadisticasPanel());
        tabbedPaneIA.addTab("📈 Gráficas", createGraficasPanel());
        tabbedPaneIA.addTab("🤖 Análisis", createAnalisisIAPanel());
        
        panel.add(tabbedPaneIA, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        panel.add(crearCardEstadistica("📊", "Eventos", "0", true));
        panel.add(crearCardEstadistica("🚨", "Alertas", "0", true));
        panel.add(crearCardEstadistica("📈", "Anomalías", "0%", true));
        panel.add(crearCardEstadistica("🎯", "Sensor + Activo", "Ninguno", false));
        panel.add(crearCardEstadistica("⏰", "Hora Pico", "Ninguna", false));
        panel.add(crearCardEstadistica("⚠️", "Riesgo", "Bajo", true));
        
        return panel;
    }
    
    private JPanel crearCardEstadistica(String icono, String titulo, String valorInicial, boolean esValorNumerico) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icono);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tituloLabel.setForeground(Color.GRAY);
        
        headerPanel.add(iconLabel);
        headerPanel.add(tituloLabel);
        
        JLabel valorLabel = new JLabel(valorInicial);
        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valorLabel.setForeground(COLOR_PRIMARY);
        valorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valorLabel, BorderLayout.CENTER);
        
        if (titulo.equals("Eventos")) lblTotalEventos = valorLabel;
        else if (titulo.equals("Alertas")) lblEventosNoAutorizados = valorLabel;
        else if (titulo.equals("Anomalías")) lblPorcentajeAnomalias = valorLabel;
        else if (titulo.equals("Sensor + Activo")) lblSensorMasActivo = valorLabel;
        else if (titulo.equals("Hora Pico")) lblHoraMasActiva = valorLabel;
        else if (titulo.equals("Riesgo")) lblRiesgoGeneral = valorLabel;
        
        return card;
    }
    
    private JPanel createGraficasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        graficasContainer = new JPanel(new GridLayout(2, 1, 8, 8));
        graficasContainer.setBackground(COLOR_WHITE);
        
        JLabel lblInfo = new JLabel("Generando gráficas...");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(graficasContainer, BorderLayout.CENTER);
        panel.add(lblInfo, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAnalisisIAPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        txtAnalisisIA = new JTextArea();
        txtAnalisisIA.setEditable(false);
        txtAnalisisIA.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        txtAnalisisIA.setBackground(new Color(245, 245, 245));
        txtAnalisisIA.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        txtAnalisisIA.setText("⏳ Iniciando análisis...");
        
        JScrollPane scroll = new JScrollPane(txtAnalisisIA);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void actualizarEstadisticasIA() {
        if (lblTotalEventos != null) {
            lblTotalEventos.setText(String.valueOf(dataAnalyzer.getTotalEventos()));
        }
        if (lblEventosNoAutorizados != null) {
            lblEventosNoAutorizados.setText(String.valueOf(dataAnalyzer.getEventosNoAutorizados()));
        }
        if (lblPorcentajeAnomalias != null) {
            double pct = dataAnalyzer.getPorcentajeAnomalias();
            lblPorcentajeAnomalias.setText(String.format("%.1f%%", pct));
            if (pct > 40) lblPorcentajeAnomalias.setForeground(COLOR_DANGER);
            else if (pct > 20) lblPorcentajeAnomalias.setForeground(COLOR_WARNING);
            else lblPorcentajeAnomalias.setForeground(COLOR_SUCCESS);
        }
        if (lblSensorMasActivo != null) {
            lblSensorMasActivo.setText(dataAnalyzer.getSensorMasActivo());
        }
        if (lblHoraMasActiva != null) {
            lblHoraMasActiva.setText(dataAnalyzer.getHoraMasActiva());
        }
        if (lblRiesgoGeneral != null) {
            double riesgo = dataAnalyzer.getRiesgoActual();
            String nivel;
            Color color;
            if (riesgo >= 0.7) { nivel = "CRÍTICO"; color = COLOR_DANGER; }
            else if (riesgo >= 0.5) { nivel = "ALTO"; color = COLOR_WARNING; }
            else if (riesgo >= 0.3) { nivel = "MEDIO"; color = COLOR_SECONDARY; }
            else { nivel = "BAJO"; color = COLOR_SUCCESS; }
            lblRiesgoGeneral.setText(nivel);
            lblRiesgoGeneral.setForeground(color);
        }
    }
    
    private void actualizarGraficasIA() {
        if (graficasContainer != null) {
            graficasContainer.removeAll();
            
            Map<Integer, Integer> eventosPorHora = dataAnalyzer.getEventosPorHora();
            Map<String, Integer> eventosPorTipo = dataAnalyzer.getEventosPorTipo();
            
            if (!eventosPorHora.isEmpty()) {
                graficasContainer.add(ChartGenerator.crearGraficoBarrasEventos(eventosPorHora));
            }
            if (!eventosPorTipo.isEmpty()) {
                graficasContainer.add(ChartGenerator.crearGraficoTortaTipos(eventosPorTipo));
            }
            
            graficasContainer.revalidate();
            graficasContainer.repaint();
        }
    }
    
    private void actualizarAnalisisIA() {
        if (txtAnalisisIA == null) return;
        
        int total = dataAnalyzer.getTotalEventos();
        int noAut = dataAnalyzer.getEventosNoAutorizados();
        double pct = dataAnalyzer.getPorcentajeAnomalias();
        double riesgo = dataAnalyzer.getRiesgoActual();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 DATOS:\n");
        sb.append("   Eventos: ").append(total).append("\n");
        sb.append("   Alertas: ").append(noAut).append("\n");
        sb.append("   Anomalías: ").append(String.format("%.1f", pct)).append("%\n\n");
        
        sb.append("⚠️ RIESGO: ");
        if (riesgo >= 0.7) sb.append("CRÍTICO\n   → Activar alarmas\n");
        else if (riesgo >= 0.5) sb.append("ALTO\n   → Monitorear\n");
        else if (riesgo >= 0.3) sb.append("MEDIO\n   → Revisar patrones\n");
        else sb.append("BAJO\n   → Normal\n");
        
        sb.append("\n💡 RECOMENDACIONES:\n");
        if (pct > 30) sb.append("   • Activar seguridad máxima\n");
        else if (pct > 15) sb.append("   • Aumentar monitoreo\n");
        else sb.append("   • Mantener monitoreo normal\n");
        
        txtAnalisisIA.setText(sb.toString());
    }
    
    private void actualizarPanelIACompleto() {
        SwingUtilities.invokeLater(() -> {
            actualizarEstadisticasIA();
            actualizarGraficasIA();
            actualizarAnalisisIA();
        });
    }
    
    // ==================== ALERTAS Y SIMULACIÓN ====================
    
    private JPanel createAlertasPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_WHITE);
        
        JLabel titleLabel = new JLabel("🚨 ALERTAS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_DANGER);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        lblSimulacionEstado = new JLabel("⚪ DETENIDA");
        lblSimulacionEstado.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSimulacionEstado.setForeground(Color.GRAY);
        titlePanel.add(lblSimulacionEstado, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        alertasArea = new JTextArea();
        alertasArea.setEditable(false);
        alertasArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        alertasArea.setBackground(new Color(44, 62, 80));
        alertasArea.setForeground(Color.WHITE);
        alertasArea.setMargin(new Insets(5, 5, 5, 5));
        
        JScrollPane scrollAlertas = new JScrollPane(alertasArea);
        scrollAlertas.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollAlertas, BorderLayout.CENTER);
        
        JPanel simPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        simPanel.setBackground(COLOR_WHITE);
        
        btnIniciarSimulacion = new JButton("▶ INICIAR");
        btnIniciarSimulacion.setBackground(COLOR_SUCCESS);
        btnIniciarSimulacion.setForeground(COLOR_BLACK);
        btnIniciarSimulacion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnIniciarSimulacion.addActionListener(e -> iniciarSimulacion());
        
        btnDetenerSimulacion = new JButton("⏹ DETENER");
        btnDetenerSimulacion.setBackground(COLOR_DANGER);
        btnDetenerSimulacion.setForeground(COLOR_BLACK);
        btnDetenerSimulacion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDetenerSimulacion.setEnabled(false);
        btnDetenerSimulacion.addActionListener(e -> detenerSimulacion());
        
        JButton btnLimpiar = new JButton("🗑 LIMPIAR");
        btnLimpiar.setBackground(COLOR_SECONDARY);
        btnLimpiar.setForeground(COLOR_BLACK);
        btnLimpiar.addActionListener(e -> alertasArea.setText(""));
        
        simPanel.add(btnIniciarSimulacion);
        simPanel.add(btnDetenerSimulacion);
        simPanel.add(btnLimpiar);
        
        panel.add(simPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void iniciarSimulacion() {
        if (simulacionActiva) return;
        
        simulacionActiva = true;
        btnIniciarSimulacion.setEnabled(false);
        btnDetenerSimulacion.setEnabled(true);
        lblSimulacionEstado.setText("🔴 ACTIVA");
        lblSimulacionEstado.setForeground(COLOR_DANGER);
        
        agregarAlerta("🚀 SIMULACIÓN INICIADA");
        
        timerSimulacion = new Timer();
        timerSimulacion.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (simulacionActiva) {
                    SwingUtilities.invokeLater(() -> generarEventoAleatorio());
                }
            }
        }, 2000, 3500);
    }
    
    private void detenerSimulacion() {
        simulacionActiva = false;
        if (timerSimulacion != null) {
            timerSimulacion.cancel();
            timerSimulacion = null;
        }
        btnIniciarSimulacion.setEnabled(true);
        btnDetenerSimulacion.setEnabled(false);
        lblSimulacionEstado.setText("⚪ DETENIDA");
        lblSimulacionEstado.setForeground(Color.GRAY);
        agregarAlerta("⏹ SIMULACIÓN DETENIDA");
    }
    
    private void generarEventoAleatorio() {
        try {
            List<BaseSensor> sensores = sensorController.getSensorsForHouse(currentHouseId);
            if (sensores == null || sensores.isEmpty()) return;
            
            BaseSensor sensor = sensores.get(random.nextInt(sensores.size()));
            String[] estados = {"OPEN", "CLOSED", "BROKEN"};
            String estado = estados[random.nextInt(estados.length)];
            boolean esNoAutorizado = random.nextDouble() < 0.35;
            
            String mensaje;
            if (estado.equals("OPEN")) {
                if (esNoAutorizado) {
                    mensaje = "🚨 ALERTA! " + sensor.getName() + " abierta SIN AUTORIZACIÓN";
                } else {
                    mensaje = "✓ " + sensor.getName() + " abierta correctamente";
                }
            } else if (estado.equals("BROKEN")) {
                mensaje = "💥 ROTURA! " + sensor.getName();
                esNoAutorizado = true;
            } else {
                mensaje = "🔒 " + sensor.getName() + " cerrada";
            }
            
            sensorController.registerSensorEvent(sensor.getId(), estado, mensaje);
            
            dataAnalyzer.registrarEvento(
                sensor.getId(), sensor.getName(), sensor.getSensorType(),
                estado, mensaje, esNoAutorizado
            );
            
            String casaNombre = houseSelector.getSelectedItem() != null ? 
                               houseSelector.getSelectedItem().toString() : "Casa " + currentHouseId;
            registrarEventoParaExportacion(
                sensor.getId(), sensor.getName(), estado, mensaje, 
                esNoAutorizado, casaNombre
            );
            
            String hora = LocalDateTime.now().format(formatter);
            String nivel = esNoAutorizado ? "🔴" : "🟢";
            String alerta = String.format("[%s] %s | ID:%d | %s", hora, nivel, sensor.getId(), mensaje);
            agregarAlerta(alerta);
            updateSensorsList();
            actualizarPanelIACompleto();
            
        } catch (Exception e) {
            System.err.println("Error en simulación: " + e.getMessage());
        }
    }
    
    private void agregarAlerta(String alerta) {
        SwingUtilities.invokeLater(() -> {
            String linea = "[" + LocalDateTime.now().format(formatter) + "] " + alerta + "\n";
            alertasArea.append(linea);
            alertasArea.setCaretPosition(alertasArea.getDocument().getLength());
            MqttManager.getInstance().enviarAlerta(alerta);
        });
    }
    
    private void iniciarActualizacionIA() {
        timerAnalisisIA = new javax.swing.Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sensorController != null) {
                    actualizarPanelIACompleto();
                }
            }
        });
        timerAnalisisIA.start();
    }
    
    // ==================== SENSORES Y ACCIONES ====================
    
    private JPanel createSensorsListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel("📊 SENSORES");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        sensorsListModel = new DefaultListModel<>();
        sensorsList = new JList<>(sensorsListModel);
        sensorsList.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sensorsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(sensorsList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("🔄 Refrescar");
        refreshButton.addActionListener(e -> updateSensorsList());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("⚡ ACCIONES");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);
        
        JButton viewBtn = new JButton("🔍 Ver Detalles");
        viewBtn.setBackground(COLOR_PRIMARY);
        viewBtn.setForeground(COLOR_BLACK);
        viewBtn.addActionListener(e -> showSensorDetails());
        gbc.gridy = 1;
        panel.add(viewBtn, gbc);
        
        JButton simulateBtn = new JButton("⚡ Evento Manual");
        simulateBtn.setBackground(COLOR_WARNING);
        simulateBtn.setForeground(COLOR_BLACK);
        simulateBtn.addActionListener(e -> simulateSensorEvent());
        gbc.gridy = 2;
        panel.add(simulateBtn, gbc);
        
        JButton mqttBtn = new JButton("📡 Probar MQTT");
        mqttBtn.setBackground(COLOR_SECONDARY);
        mqttBtn.setForeground(COLOR_BLACK);
        mqttBtn.addActionListener(e -> probarMQTT());
        gbc.gridy = 3;
        panel.add(mqttBtn, gbc);
        
        JButton btnExportarJSON = new JButton("📁 Exportar JSON");
        btnExportarJSON.setBackground(new Color(52, 152, 219));
        btnExportarJSON.setForeground(COLOR_BLACK);
        btnExportarJSON.addActionListener(e -> exportarEventosJSON());
        gbc.gridy = 4;
        panel.add(btnExportarJSON, gbc);
        
        JButton btnExportarStats = new JButton("📊 Exportar Stats");
        btnExportarStats.setBackground(new Color(46, 204, 113));
        btnExportarStats.setForeground(COLOR_BLACK);
        btnExportarStats.addActionListener(e -> exportarEstadisticasJSON());
        gbc.gridy = 5;
        panel.add(btnExportarStats, gbc);
        
        JPanel helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBackground(new Color(245, 245, 245));
        JLabel helpLabel = new JLabel("<html><center>Seleccione un sensor</center></html>");
        helpLabel.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        helpPanel.add(helpLabel, BorderLayout.CENTER);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(helpPanel, gbc);
        
        return panel;
    }
    
    private void cargarCasas() {
        houseSelector.removeAllItems();
        houseSelector.addItem("1 - Casa Principal");
        houseSelector.addItem("2 - Casa de Playa");
        houseSelector.addItem("3 - Casa de Campo");
        houseSelector.addItem("4 - Oficina");
        currentHouseId = 1;
    }
    
    private void cambiarCasa() {
        if (houseSelector.getSelectedItem() != null) {
            String selected = houseSelector.getSelectedItem().toString();
            currentHouseId = Integer.parseInt(selected.split(" - ")[0]);
            updateSensorsList();
            agregarAlerta("🏠 Cambiando a: " + selected);
        }
    }
    
    private void actualizarStatusMQTT() {
        if (MqttManager.getInstance().isConectado()) {
            mqttStatusLabel.setText("✅ MQTT OK");
            mqttStatusLabel.setForeground(COLOR_SUCCESS);
        } else {
            mqttStatusLabel.setText("❌ MQTT OFF");
            mqttStatusLabel.setForeground(COLOR_DANGER);
        }
    }
    
    private void probarMQTT() {
        String msg = JOptionPane.showInputDialog(this, "Mensaje MQTT:");
        if (msg != null && !msg.isEmpty()) {
            MqttManager.getInstance().enviarAlerta(msg);
            agregarAlerta("📡 MQTT: " + msg);
        }
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(COLOR_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        panel.add(createStatCard("📊", "Sensores", "0"));
        panel.add(createStatCard("🟢", "Activos", "0"));
        panel.add(createStatCard("🚨", "Alertas", "0"));
        panel.add(createStatCard("🤖", "IA", "OK"));
        
        return panel;
    }
    
    private JPanel createStatCard(String icon, String label, String value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(60, 80, 100));
        card.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setForeground(COLOR_WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(60, 80, 100));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(COLOR_WHITE);
        
        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        descLabel.setForeground(new Color(200, 200, 200));
        
        textPanel.add(valueLabel);
        textPanel.add(descLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updateSensorsList() {
        SwingUtilities.invokeLater(() -> {
            sensorsListModel.clear();
            try {
                List<BaseSensor> sensores = sensorController.getSensorsForHouse(currentHouseId);
                if (sensores == null || sensores.isEmpty()) {
                    sensorsListModel.addElement("📟 No hay sensores");
                } else {
                    for (BaseSensor s : sensores) {
                        String icono = s.getSensorType().equals("DOOR") ? "🚪" : "🪟";
                        String estado = s.isActive() ? "✅" : "❌";
                        sensorsListModel.addElement(String.format("%s %s - %s %s [ID:%d]", 
                            icono, s.getName(), s.getLocation(), estado, s.getId()));
                    }
                }
            } catch (Exception e) {
                sensorsListModel.addElement("❌ Error: " + e.getMessage());
            }
        });
    }
    
    private void showSensorDetails() {
        String selected = sensorsList.getSelectedValue();
        if (selected == null) {
            agregarAlerta("⚠️ Seleccione un sensor");
            return;
        }
        try {
            int id = Integer.parseInt(selected.split("\\[ID:")[1].split("\\]")[0]);
            BaseSensor s = sensorController.getSensorDetails(id);
            if (s != null) {
                String msg = String.format("ID:%d | %s | %s | %s", 
                    s.getId(), s.getSensorType(), s.getName(), s.getLocation());
                agregarAlerta("📋 " + msg);
                JOptionPane.showMessageDialog(this, msg, "Sensor", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            agregarAlerta("❌ Error al obtener detalles");
        }
    }
    
    private void simulateSensorEvent() {
        String idStr = JOptionPane.showInputDialog(this, "ID del sensor:");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                String[] opts = {"OPEN", "CLOSED", "BROKEN"};
                String estado = (String) JOptionPane.showInputDialog(this, "Estado:", "Simular", 
                    JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                String msg = JOptionPane.showInputDialog(this, "Mensaje:");
                if (msg == null) msg = "Evento manual";
                
                sensorController.registerSensorEvent(id, estado, msg);
                
                dataAnalyzer.registrarEvento(id, "Sensor " + id, "MANUAL", estado, msg, true);
                
                agregarAlerta("🎮 Evento manual: Sensor " + id + " -> " + estado);
                updateSensorsList();
                actualizarPanelIACompleto();
            } catch (NumberFormatException e) {
                agregarAlerta("❌ ID inválido");
            }
        }
    }
    
    // ==================== LOGIN Y NAVEGACIÓN ====================
    
    private void login() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("❌ Ingrese usuario y contraseña");
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("INICIANDO...");
        
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            SwingUtilities.invokeLater(() -> {
                if (authController.login(user, pass)) {
                    statusLabel.setText(" ");
                    sensorController = new SensorController(authController);
                    houseController = new HouseController(authController);
                    userLabel.setText("Usuario: " + authController.getCurrentUser().getUsername());
                    roleLabel.setText("Rol: " + authController.getCurrentUser().getRole());
                    cargarCasas();
                    updateSensorsList();
                    showMenuScreen();
                    iniciarActualizacionIA();
                    agregarAlerta("✅ Bienvenido " + authController.getCurrentUser().getUsername());
                } else {
                    statusLabel.setText("❌ Credenciales inválidas");
                    passwordField.setText("");
                }
                loginButton.setEnabled(true);
                loginButton.setText("INICIAR SESIÓN");
            });
        }).start();
    }
    
    private void logout() {
        detenerSimulacion();
        if (arduinoReader != null) {
            arduinoReader.desconectar();
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authController.logout();
            usernameField.setText("");
            passwordField.setText("");
            statusLabel.setText(" ");
            alertasArea.setText("");
            showLoginScreen();
        }
    }
    
    private void showLoginScreen() {
        cardLayout.show(mainPanel, "LOGIN");
        setTitle("IoT Security - Login");
    }
    
    private void showMenuScreen() {
        cardLayout.show(mainPanel, "MENU");
        setTitle("IoT Security - Panel Principal");
        updateSensorsList();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new MainView().setVisible(true);
        });
    }
}