package vistas;

import backup.BackupManager;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Ventana principal de la aplicación Pokémon.
 * Desde aquí se accede a la gestión de todas las entidades.
 */
public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Gestión Base de Datos Pokémon");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Botones para cada entidad
        JButton btnEntrenador = new JButton("Gestionar Entrenadores");
        JButton btnRegion = new JButton("Gestionar Regiones");
        JButton btnTipo = new JButton("Gestionar Tipos");
        JButton btnPokemon = new JButton("Gestionar Pokémons");
        JButton btnGimnasio = new JButton("Gestionar Gimnasios");
        JButton btnEquipo = new JButton("Gestionar Equipos");
        JButton btnMedallas = new JButton("Gestionar Medallas");
        JButton btnDetallePokemon = new JButton("Gestionar Detalles Pokémon");
        JButton btnDetalleEquipo = new JButton("Gestionar Detalles Equipo");
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

        // Action listeners para abrir las ventanas de gestión
        btnEntrenador.addActionListener(e -> abrirVentanaGestion(new EntrenadorView()));
        btnRegion.addActionListener(e -> abrirVentanaGestion(new RegionView()));
        btnTipo.addActionListener(e -> abrirVentanaGestion(new TipoView()));
        btnPokemon.addActionListener(e -> abrirVentanaGestion(new PokemonView()));
        btnGimnasio.addActionListener(e -> abrirVentanaGestion(new GimnasioView()));
        btnEquipo.addActionListener(e -> abrirVentanaGestion(new EquipoView()));
        btnMedallas.addActionListener(e -> abrirVentanaGestion(new MedallasView()));
        btnDetallePokemon.addActionListener(e -> abrirVentanaGestion(new DetallePokemonView()));
        btnDetalleEquipo.addActionListener(e -> abrirVentanaGestion(new DetalleEquipoView()));

        // Botón de copia de seguridad
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

        // Botón de restauración
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
     * Método auxiliar para abrir una ventana de gestión.
     * Hace que la ventana sea modal y se centre en la pantalla.
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}