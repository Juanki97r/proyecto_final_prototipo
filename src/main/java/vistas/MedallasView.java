package vistas;

import controladores.MedallasController;
import controladores.EntrenadorController;
import controladores.GimnasioController;
import entidades.Medallas;
import entidades.MedallasPK;
import entidades.Entrenador;
import entidades.Gimnasio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Ventana para gestionar Medallas.
 */
public class MedallasView extends JFrame {

    private MedallasController controller;
    private EntrenadorController entrenadorController;
    private GimnasioController gimnasioController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Entrenador> comboEntrenador;
    private JComboBox<Gimnasio> comboGimnasio;
    private JTextField txtFecha;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;

    public MedallasView() {
        controller = new MedallasController();
        entrenadorController = new EntrenadorController();
        gimnasioController = new GimnasioController();

        setTitle("Gestión de Medallas");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Entrenador", "Gimnasio", "Fecha"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarMedalla());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos de la Medalla"));

        panelForm.add(new JLabel("Entrenador:"));
        comboEntrenador = new JComboBox<>();
        cargarEntrenadores();
        panelForm.add(comboEntrenador);

        panelForm.add(new JLabel("Gimnasio:"));
        comboGimnasio = new JComboBox<>();
        cargarGimnasios();
        panelForm.add(comboGimnasio);

        panelForm.add(new JLabel("Fecha (YYYY-MM-DD):"));
        txtFecha = new JTextField();
        panelForm.add(txtFecha);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrear = new JButton("Crear");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelForm, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearMedalla());
        btnActualizar.addActionListener(e -> actualizarMedalla());
        btnEliminar.addActionListener(e -> eliminarMedalla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarEntrenadores() {
        try {
            List<Entrenador> entrenadores = entrenadorController.findAll();
            for (Entrenador e : entrenadores) {
                comboEntrenador.addItem(e);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar entrenadores: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarGimnasios() {
        try {
            List<Gimnasio> gimnasios = gimnasioController.findAll();
            for (Gimnasio g : gimnasios) {
                comboGimnasio.addItem(g);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar gimnasios: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Medallas> medallas = controller.findAll();
            for (Medallas m : medallas) {
                tableModel.addRow(new Object[]{
                    m.getEntrenador() != null ? m.getEntrenador().getNombre() : "",
                    m.getGimnasio() != null ? m.getGimnasio().getNomMedalla() : "",
                    m.getFecha() != null ? m.getFecha().toString() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar medallas: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarMedalla() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            // Seleccionar entrenador
            String entrenadorNombre = tableModel.getValueAt(row, 0).toString();
            for (int i = 0; i < comboEntrenador.getItemCount(); i++) {
                if (comboEntrenador.getItemAt(i).getNombre().equals(entrenadorNombre)) {
                    comboEntrenador.setSelectedIndex(i);
                    break;
                }
            }
            // Seleccionar gimnasio
            String gimnasioNombre = tableModel.getValueAt(row, 1).toString();
            for (int i = 0; i < comboGimnasio.getItemCount(); i++) {
                if (comboGimnasio.getItemAt(i).getNomMedalla() != null &&
                    comboGimnasio.getItemAt(i).getNomMedalla().equals(gimnasioNombre)) {
                    comboGimnasio.setSelectedIndex(i);
                    break;
                }
            }
            txtFecha.setText(tableModel.getValueAt(row, 2).toString());
        }
    }

    private void crearMedalla() {
        Entrenador entrenador = (Entrenador) comboEntrenador.getSelectedItem();
        Gimnasio gimnasio = (Gimnasio) comboGimnasio.getSelectedItem();
        String fechaStr = txtFecha.getText().trim();

        if (entrenador == null || gimnasio == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar entrenador y gimnasio");
            return;
        }

        LocalDate fecha = null;
        if (!fechaStr.isEmpty()) {
            try {
                fecha = LocalDate.parse(fechaStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD");
                return;
            }
        }

        try {
            MedallasPK pk = new MedallasPK(entrenador.getIdentrenador(), gimnasio.getCodgym());
            Medallas medalla = new Medallas(pk, fecha, entrenador, gimnasio);
            controller.create(medalla);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Medalla creada exitosamente");
        } catch (Exception ex) {
            String mensaje = "Error al crear medalla";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("duplicate") || 
                    ex.getMessage().toLowerCase().contains("constraint")) {
                    mensaje = "Este entrenador ya tiene esta medalla."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void actualizarMedalla() {
        // No se permite actualizar porque la clave primaria (Entrenador + Gimnasio) es inmutable
        JOptionPane.showMessageDialog(this,
                "La clave primaria (Entrenador + Gimnasio) no puede modificarse.\n\n" +
                "Para cambiar estos datos, debe eliminar la medalla y crear una nueva.");
    }
                
    private void eliminarMedalla() {
        Entrenador entrenador = (Entrenador) comboEntrenador.getSelectedItem();
        Gimnasio gimnasio = (Gimnasio) comboGimnasio.getSelectedItem();

        if (entrenador == null || gimnasio == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una medalla de la tabla");
            return;
        }

        try {
            MedallasPK pk = new MedallasPK(entrenador.getIdentrenador(), gimnasio.getCodgym());
            controller.delete(pk);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Medalla eliminada exitosamente");
        } catch (Exception ex) {
            String mensaje = "Error al eliminar medalla";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("no row") ||
                    ex.getMessage().toLowerCase().contains("not found")) {
                    mensaje = "La medalla no existe. Actualiza la lista y vuelve a intentarlo.";
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }
    
    private void limpiarFormulario() {
        comboEntrenador.setSelectedIndex(-1);
        comboGimnasio.setSelectedIndex(-1);
        txtFecha.setText("");
        table.clearSelection();
    }
}