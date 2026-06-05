package daw;

import controladores.JPAUtil;
import vistas.MainWindow;

/**
 * CLASE MAIN
 * ═════════════════════════════════════════════════════════════════════════════════
 * Punto de entrada principal de la aplicación.
 * 
 * RESPONSABILIDADES:
 *   1. Configurar manejo global de excepciones
 *   2. Iniciar la interfaz gráfica (MainWindow)
 *   3. Configurar el cierre correcto de la aplicación
 * 
 * FLUJO DE EJECUCIÓN:
 *   1. JVM inicia → ejecuta main()
 *   2. Configura manejador de excepciones global
 *   3. Lanza MainWindow en el hilo de eventos Swing
 *   4. Espera a que el usuario cierre la ventana
 *   5. Cierra JPAUtil (BD) y termina
 * 
 * ARQUITECTURA:
 *   • Swing: Framework para interfaz gráfica (Java)
 *   • EDT (Event Dispatch Thread): Hilo especial para componentes Swing
 *   • Swing utilities: Clases para ejecutar código en el EDT
 */
public class Main {
    /**
     * Método main: Punto de entrada de la aplicación
     * 
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        
        /**
         * PASO 1: Configurar manejador global de excepciones
         * 
         * ¿POR QUÉ?
         *   • Si ocurre una excepción en un hilo (ej: EDT), la capturamos
         *   • En lugar de que la aplicación falle silenciosamente
         *   • Imprimimos el stack trace para depuración
         * 
         * Thread.setDefaultUncaughtExceptionHandler()
         *   • Intercepta excepciones no capturadas en CUALQUIER hilo
         *   • thread: El hilo donde ocurrió
         *   • throwable: La excepción no capturada
         */
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Error en hilo " + thread.getName() + ":");
            throwable.printStackTrace();
        });

        /**
         * PASO 2: Ejecutar la interfaz gráfica en el EDT (Event Dispatch Thread)
         * 
         * ¿POR QUÉ invokerLater()?
         *   • Swing NO es thread-safe
         *   • TODOS los componentes Swing deben crearse/modificarse en el EDT
         *   • SwingUtilities.invokeLater() encola una tarea para el EDT
         *   • El EDT la ejecutará cuando esté disponible
         * 
         * ¿POR QUÉ NO directamente new MainWindow()?
         *   • Si ejecutamos en el hilo main, la aplicación se bloquea
         *   • El EDT corre en paralelo y gestiona eventos Swing
         *   • main() termina, pero el EDT sigue activo
         */
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                /**
                 * Crear la ventana principal (interfaz gráfica)
                 * 
                 * MainWindow es un JFrame que contiene:
                 *   • Botones para gestionar Entrenadores, Pokémons, etc.
                 *   • Botones para Backup y Restauración
                 *   • Manejo de conexión a BD
                 */
                MainWindow window = new MainWindow();
                window.setVisible(true);
                
                /**
                 * PASO 3: Configurar cierre correcto de la aplicación
                 * 
                 * Cuando el usuario cierra la ventana (botón X):
                 *   1. Evento windowClosed() se dispara
                 *   2. Cerramos JPAUtil (EntityManagerFactory)
                 *   3. Llamamos System.exit(0) para terminar
                 * 
                 * WindowAdapter: Clase que implementa todos los listeners de ventana
                 * Solo sobrescribimos windowClosed() (no necesitamos los demás)
                 */
                window.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        /**
                         * CIERRE CORRECTO DE JPA:
                         *   1. JPAUtil.close() → Cierra EntityManagerFactory
                         *   2. Cierra todos los EntityManagers activos
                         *   3. Cierra el pool de conexiones a BD
                         *   4. Libera recursos (conexiones MySQL)
                         */
                        JPAUtil.close();
                        
                        /**
                         * System.exit(0):
                         *   • 0 = éxito (terminación normal)
                         *   • Termina la JVM completamente
                         *   • Detiene el EDT y todos los threads
                         */
                        System.exit(0);
                    }
                });
            } catch (Exception ex) {
                /**
                 * Captura excepciones durante la inicialización
                 * 
                 * ERRORES COMUNES:
                 *   • MySQL no está corriendo
                 *   • Configuración de persistence.xml incorrecta
                 *   • Problemas de conexión de red
                 * 
                 * Mostramos un diálogo de error al usuario
                 */
                System.err.println("Error fatal al iniciar la aplicación:");
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación: " + ex.getMessage() + "\n" +
                    "Verifica que MySQL esté corriendo y accesible.",
                    "Error de Inicialización",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                System.exit(1); // 1 = error (terminación anormal)
            }
        });
    }
}