package utils;


import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    // CONFIGURACIÓN - CAMBIA ESTOS VALORES
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USER = "brendaycc2001@gmail.com";  // Cambia por tu correo
    private static final String EMAIL_PASS = "snk20012022";     // Contraseña de aplicación de Gmail
    private static final String EMAIL_DESTINATARIO = "brendaycc2001@gmail.com"; // Correo destino
    
    public static void enviarAlertaIntruso(String sensor, String ubicacion, String mensaje) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASS);
            }
        });
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USER));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_DESTINATARIO));
            message.setSubject("🚨 ALERTA DE SEGURIDAD - Intruso Detectado");
            
            String contenido = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #ff4444; padding: 20px; color: white; text-align: center;'>" +
                "<h1>🚨 ALERTA DE SEGURIDAD 🚨</h1>" +
                "</div>" +
                "<div style='padding: 20px; border: 1px solid #ddd;'>" +
                "<h2 style='color: #d32f2f;'>¡Intruso Detectado!</h2>" +
                "<p><strong>Sensor:</strong> " + sensor + "</p>" +
                "<p><strong>Ubicación:</strong> " + ubicacion + "</p>" +
                "<p><strong>Mensaje:</strong> " + mensaje + "</p>" +
                "<p><strong>Fecha y Hora:</strong> " + new java.util.Date() + "</p>" +
                "<hr>" +
                "<p style='color: gray; font-size: 12px;'>Este es un mensaje automático del Sistema de Seguridad IoT</p>" +
                "<p style='color: gray; font-size: 10px;'>No responder a este correo</p>" +
                "</div>" +
                "</body>" +
                "</html>"
            );
            
            message.setContent(contenido, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("📧 Correo de alerta enviado correctamente a: " + EMAIL_DESTINATARIO);
            
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void enviarAlertaPersonalizada(String asunto, String contenidoTexto) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASS);
            }
        });
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USER));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_DESTINATARIO));
            message.setSubject(asunto);
            
            String contenido = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #ff4444; padding: 15px; color: white;'>" +
                "<h2>" + asunto + "</h2>" +
                "</div>" +
                "<div style='padding: 15px;'>" +
                "<p>" + contenidoTexto.replace("\n", "<br>") + "</p>" +
                "<hr>" +
                "<p style='color: gray; font-size: 10px;'>Sistema de Seguridad IoT</p>" +
                "</div>" +
                "</body>" +
                "</html>"
            );
            
            message.setContent(contenido, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("📧 Correo enviado: " + asunto);
            
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
        }
    }
}