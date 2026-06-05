package vistas;

import backup.BackupManager;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * CLASE MAINWINDOW
 * ═════════════════════════════════════════════════════════════════════════════════
 * Ventana principal de la aplicación Pokémon.
 * Desde aquí se accede a la gestión de todas las entidades.
 * 
 * PATRÓN: FRAME (ventana principal con Swing)
 *   • Hereda de JFrame
 *   • Contiene botones para cada operación principal
 *   • Abre ventanas secundarias (dialogs) para cada entidad
 * 
 * ARQUITECTURA Swing:
 *   • JFrame: Ventana principal
 *   • JPanel: Contenedor de componentes
 *   • JButton: Botones interactivos
 *   • GridLayout: Distribuye botones en filas/columnas
 * 
 * FLUJO:
 *   Usuario hace clic en botón → ActionListener se ejecuta
 *   → Se abre la ventana correspondiente (PokemonView, EntrenadorView, etc.)
 *   → Usuario interactúa con esa ventana
 *   → Vuelve a MainWindow cuando cierra la ventana secundaria
 */
public class MainWindow extends JFrame {

    /**
     * Constructor: Inicializa la interfaz gráfica principal
     * 
     * CONFIGURACIÓN:
     *   • setTitle(): Título de la ventana
     *   • setSize(): Tamaño inicial (600x400)
     *   • setDefaultCloseOperation(): Qué hacer al cerrar (EXIT_ON_CLOSE)
     *   • setLocationRelativeTo(): Centra en la pantalla
     */
    public MainWindow() {
        setTitle("Gestión Base de Datos Pokémon");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        /**
         * PANEL PRINCIPAL
         * 
         * JPanel: Contenedor para agrupar componentes
         * GridLayout(7, 2, 10, 10):
         *   • 7 filas x 2 columnas
         *   • 10 píxeles de espaciado horizontal
         *   • 10 píxeles de espaciado vertical
         * 
         * setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)):
         *   • Margen de 20 píxeles en todos los lados (N, W, S, E)
         */
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ═══════════════════════════════════════════════════════════════════════════════
        // CREACIÓN DE BOTONES PARA CADA ENTIDAD
        // ═══════════════════════════════════════════════════════════════════════════════
        
        /**
         * BOTONES DE GESTIÓN DE ENTIDADES
         * 
         * Cada botón abrirá una ventana (View) para gestionar esa entidad:
         *   • EntrenadorView: CRUD de Entrenadores
         *   • PokemonView: CRUD de Pokémons
         *   • EquipoView: CRUD de Equipos
         *   • etc.
         * 
         * Las vistas contienen:
         *   • Tabla con datos actuales
         *   • Formulario para crear/editar
         *   • Botones Create, Update, Delete
         */
        JButton btnEntrenador = new JButton("Gestionar Entrenadores");
        JButton btnRegion = new JButton("Gestionar Regiones");
        JButton btnTipo = new JButton("Gestionar Tipos");
        JButton btnPokemon = new JButton("Gestionar Pokémons");
        JButton btnGimnasio = new JButton("Gestionar Gimnasios");
        JButton btnEquipo = new JButton("Gestionar Equipos");
        JButton btnMedallas = new JButton("Gestionar Medallas");
        JButton btnDetallePokemon = new JButton("Gestionar Detalles Pokémon");
        JButton btnDetalleEquipo = new JButton("Gestionar Detalles Equipo");
        
        /**
         * BOTONES DE BACKUP/RESTAURACIÓN
         * 
         * Estas funciones están integradas en MainWindow (no en vistas separadas)
         * porque son operaciones globales de toda la BD
         */
        JButton btnBackup = new JButton("Crear Copia de Seguridad");
        JButton btnRestore = new JButton("Restaurar copia de seguridad");
        JButton btnEstadoBackup = new JButton("Estado de Backup");
        JButton btnSalir = new JButton("Salir");

        // Agregar botones al panel
        panel.add(btnEntrenador);
        panel.add(btnRegion);
        panel.add(btnTipo);
        panel.add(btnPokemon);
        panel.add(btnGimnasio);
        panel.add(btnEquipo);
        panel.add(btnMedallas);
        panel.add(btnDetallePokemon);
        panel.add(btnDetalleEquipo);
        panel.add(btnBackup);
        panel.add(btnRestore);
        panel.add(btnEstadoBackup);
        panel.add(btnSalir);

        // ═══════════════════════════════════════════════════════════════════════════════
        // ACTION LISTENERS (Manejadores de eventos)
        // ═══════════════════════════════════════════════════════════════════════════════
        
        /**
         * PATRÓN OBSERVER (escuchador de eventos)
         * 
         * Cada botón tiene un ActionListener que se ejecuta cuando el usuario lo clica.
         * 
         * btnEntrenador.addActionListener(e -> abrirVentanaGestion(new EntrenadorView()));
         *   • e: Evento del clic
         *   • new EntrenadorView(): Crea la ventana para gestionar Entrenadores
         *   • abrirVentanaGestion(): Método que abre la ventana
         */
        btnEntrenador.addActionListener(e -> abrirVentanaGestion(new EntrenadorView()));
        btnRegion.addActionListener(e -> abrirVentanaGestion(new RegionView()));
        btnTipo.addActionListener(e -> abrirVentanaGestion(new TipoView()));
        btnPokemon.addActionListener(e -> abrirVentanaGestion(new PokemonView()));
        btnGimnasio.addActionListener(e -> abrirVentanaGestion(new GimnasioView()));
        btnEquipo.addActionListener(e -> abrirVentanaGestion(new EquipoView()));
        btnMedallas.addActionListener(e -> abrirVentanaGestion(new MedallasView()));
        btnDetallePokemon.addActionListener(e -> abrirVentanaGestion(new DetallePokemonView()));
        btnDetalleEquipo.addActionListener(e -> abrirVentanaGestion(new DetalleEquipoView()));

        // ═══════════════════════════════════════════════════════════════════════════════
        // BOTÓN DE COPIA DE SEGURIDAD
        // ═══════════════════════════════════════════════════════════════════════════════
        
        /**
         * Botón BACKUP: Crea una copia de seguridad de toda la BD
         * 
         * FLUJO:
         *   1. Usuario clica el botón
         *   2. Se crea un BackupManager
         *   3. crearCopiaDeSeguridad() → Exporta todas las tablas a CSV
         *   4. Se crea carpeta con timestamp (ej: backups/20260604_153021/)
         *   5. Se muestra un mensaje al usuario con la ruta
         * 
         * MANEJO DE EXCEPCIONES:
         *   • try: Intenta crear backup
         *   • catch: Si falla (BD no accesible), muestra error
         */
        btnBackup.addActionListener(e -> {
            try {
                Path carpeta = new BackupManager().crearCopiaDeSeguridad();
                JOptionPane.showMessageDialog(this,
                        "Copia de seguridad creada en: " + carpeta.toAbsolutePath(),
                        "Backup completado",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al crear backup: " + ex.getMessage(),
                        "Error de Backup",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // ═══════════════════════════════════════════════════════════════════════════════
        // BOTÓN DE RESTAURACIÓN
        // ═══════════════════════════════════════════════════════════════════════════════
        
        /**
         * Botón RESTORE: Restaura una copia de seguridad anterior
         * 
         * FLUJO:
         *   1. Obtener lista de copias disponibles
         *   2. Mostrar diálogo para que usuario seleccione una
         *   3. Mostrar confirmación (advierte que borrará datos actuales)
         *   4. Si acepta: restaurar la copia seleccionada
         * 
         * IMPORTANTE:
         *   • Borra TODOS los datos actuales
         *   • Restaura exactamente los datos del backup elegido
         *   • Es una operación destructiva (de ahí el warning)
         */
        btnRestore.addActionListener(e -> {
            try {
                BackupManager backupManager = new BackupManager();
                List<Path> copias = backupManager.listarCopias();
                if (copias.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "No hay copias de seguridad disponibles para restaurar.",
                            "Restauración",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] opciones = copias.stream()
                        .map(path -> path.getFileName().toString())
                        .toArray(String[]::new);
                String seleccion = (String) JOptionPane.showInputDialog(this,
                        "Selecciona la copia de seguridad a restaurar:",
                        "Restaurar copia de seguridad",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones,
                        opciones[opciones.length - 1]);

                if (seleccion == null) {
                    return; // el usuario canceló
                }

                Path copiaSeleccionada = copias.stream()
                        .filter(path -> path.getFileName().toString().equals(seleccion))
                        .findFirst()
                        .orElse(null);

                if (copiaSeleccionada == null) {
                    JOptionPane.showMessageDialog(this,
                            "La copia seleccionada no se encontró.",
                            "Error de Restauración",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int opcion = JOptionPane.showConfirmDialog(this,
                        "Esta operación borrará todos los datos actuales y restaurará la copia seleccionada\n¿Deseas continuar?",
                        "Confirmar restauración",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (opcion == JOptionPane.YES_OPTION) {
                    Path carpeta = backupManager.restaurarCopia(copiaSeleccionada);
                    JOptionPane.showMessageDialog(this,
                            "Restauración completada desde: " + carpeta.toAbsolutePath(),
                            "Restauración completada",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al restaurar backup: " + ex.getMessage(),
                        "Error de Restauración",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Botón de estado de backup
        btnEstadoBackup.addActionListener(e -> {
            try {
                String info = new BackupManager().obtenerInformacionBackup();
                JOptionPane.showMessageDialog(this,
                        info,
                        "Estado de Backup",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al obtener el estado de backup: " + ex.getMessage(),
                        "Error de Backup",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Botón salir
        btnSalir.addActionListener(e -> System.exit(0));

        add(panel);
    }

    /**
     * Método auxiliar para abrir una ventana de gestión
     * 
     * @param ventana La ventana (View) a abrir
     */
    private void abrirVentanaGestion(JFrame ventana) {
        try {
            ventana.setLocationRelativeTo(this);
            ventana.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al abrir la ventana: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método main de prueba
     * Permite ejecutar MainWindow directamente (sin pasar por Main)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}