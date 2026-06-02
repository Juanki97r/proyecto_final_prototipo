package daw;

import controladores.JPAUtil;
import vistas.MainWindow;

/**
 * Clase principal para ejecutar la aplicación de gestión de Pokémon.
 */
public class Main {
    public static void main(String[] args) {
        // Configurar manejador global de excepciones en el hilo de Swing
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Error en hilo " + thread.getName() + ":");
            throwable.printStackTrace();
        });

        // Ejecutar la interfaz gráfica en el hilo de eventos de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                
                // Cerrar JPAUtil cuando se cierra la ventana
                window.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        JPAUtil.close();
                        System.exit(0);
                    }
                });
            } catch (Exception ex) {
                System.err.println("Error fatal al iniciar la aplicación:");
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación: " + ex.getMessage() + "\n" +
                    "Verifica que MySQL esté corriendo y accesible.",
                    "Error de Inicialización",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}