package vistas;

import controladores.DetalleEquipoController;
import controladores.EquipoController;
import controladores.EntrenadorController;
import entidades.DetalleEquipo;
import entidades.Equipo;
import entidades.Entrenador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Equipos.
 */
public class EquipoView extends JFrame {

    private EquipoController controller;
    private EntrenadorController entrenadorController;
    private DetalleEquipoController detalleEquipoController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId;
    private JComboBox<Entrenador> comboEntrenador;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private JLabel lblMensaje;

    public EquipoView() {
        controller = new EquipoController();
        entrenadorController = new EntrenadorController();
        detalleEquipoController = new DetalleEquipoController();

        setTitle("Gestión de Equipos");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Entrenador"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarEquipo();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Equipo"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria del equipo.");
        panelForm.add(txtId);

        panelForm.add(new JLabel("Entrenador:"));
        comboEntrenador = new JComboBox<>();
        cargarEntrenadores();
        panelForm.add(comboEntrenador);

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

        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelInferior.add(lblMensaje, BorderLayout.NORTH);

        add(panelInferior, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearEquipo());
        btnActualizar.addActionListener(e -> actualizarEquipo());
        btnEliminar.addActionListener(e -> eliminarEquipo());
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

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Equipo> equipos = controller.findAll();
            for (Equipo e : equipos) {
                tableModel.addRow(new Object[]{
                    e.getCodequipo(),
                    e.getEntrenador() != null ? e.getEntrenador().getNombre() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar equipos: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean entrenadorYaTieneEquipo(Entrenador entrenador, Integer idEquipoExcluido) {
        if (entrenador == null) {
            return false;
        }
        try {
            for (Equipo e : controller.findAll()) {
                if (e.getEntrenador() != null &&
                    e.getEntrenador().getIdentrenador() == entrenador.getIdentrenador() &&
                    (idEquipoExcluido == null || e.getCodequipo() != idEquipoExcluido)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            // No interrumpir la validación por problemas de consulta; se maneja en la operación superior.
        }
        return false;
    }

    private boolean entrenadorYaTieneEquipo(Entrenador entrenador) {
        return entrenadorYaTieneEquipo(entrenador, null);
    }

    private void setMensaje(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje != null ? mensaje : " ");
            lblMensaje.setForeground(Color.RED);
        }
    }

    private void setMensajeExito(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje != null ? mensaje : " ");
            lblMensaje.setForeground(new Color(0, 128, 0));
        }
    }

    private void clearMensaje() {
        if (lblMensaje != null) {
            lblMensaje.setText(" ");
        }
    }

    private void seleccionarEquipo() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idStr = tableModel.getValueAt(row, 0).toString();
            txtId.setText(idStr);
            // Seleccionar entrenador en combo
            String entrenadorNombre = tableModel.getValueAt(row, 1).toString();
            for (int i = 0; i < comboEntrenador.getItemCount(); i++) {
                if (comboEntrenador.getItemAt(i).getNombre().equals(entrenadorNombre)) {
                    comboEntrenador.setSelectedIndex(i);
                    break;
                }
            }
            try {
                int equipoId = Integer.parseInt(idStr);
                mostrarDetallesEquipo(equipoId);
            } catch (NumberFormatException ignored) {
                // No debería ocurrir; el ID siempre es numérico.
            }
        }
    }

    private void mostrarDetallesEquipo(int equipoId) {
        try {
            java.util.List<DetalleEquipo> detalles = detalleEquipoController.findByEquipoId(equipoId);
            if (detalles == null || detalles.isEmpty()) {
                return;
            }

            String[] columns = {"Slot", "Pokémon"};
            DefaultTableModel detalleModel = new DefaultTableModel(columns, 0);
            detalles.sort((a, b) -> Integer.compare(a.getId().getNumslot(), b.getId().getNumslot()));
            for (DetalleEquipo detalle : detalles) {
                String pokemonNombre = detalle.getPokemon() != null ? detalle.getPokemon().getNompokemon() : "";
                detalleModel.addRow(new Object[]{detalle.getId().getNumslot(), pokemonNombre});
            }

            JTable detalleTable = new JTable(detalleModel);
            detalleTable.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(detalleTable);

            JDialog dialog = new JDialog(this, "Detalle de Equipo " + equipoId, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.add(new JLabel("Pokémons asociados al equipo:"), BorderLayout.NORTH);
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton cerrar = new JButton("Cerrar");
            cerrar.addActionListener(e -> dialog.dispose());
            JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelBoton.add(cerrar);
            dialog.add(panelBoton, BorderLayout.SOUTH);

            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles de equipo: " + ex.getMessage());
        }
    }

    private void crearEquipo() {
        String idStr = txtId.getText().trim();
        Entrenador entrenador = (Entrenador) comboEntrenador.getSelectedItem();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ID es obligatorio");
            return;
        }
        if (entrenador == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un entrenador");
            return;
        }
        if (entrenadorYaTieneEquipo(entrenador)) {
            setMensaje("El entrenador seleccionado ya tiene un equipo asociado");
            JOptionPane.showMessageDialog(this, "El entrenador seleccionado ya tiene un equipo asociado");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Equipo equipo = new Equipo(entrenador);
            equipo.setCodequipo(id);
            controller.create(equipo);
            cargarDatos();
            limpiarFormulario();
            setMensajeExito("Equipo creado exitosamente");
            JOptionPane.showMessageDialog(this, "Equipo creado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido");
        } catch (Exception ex) {
            if (ViewUtils.esClavePrimariaDuplicada(ex)) {
                String mensaje = ViewUtils.mensajeClaveDuplicada("el equipo");
                setMensaje(mensaje);
                JOptionPane.showMessageDialog(this, mensaje);
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear equipo: " + ex.getMessage());
            }
        }
    }

    private void actualizarEquipo() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un equipo para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            Entrenador entrenador = (Entrenador) comboEntrenador.getSelectedItem();

            if (entrenador == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un entrenador");
                return;
            }

            Equipo equipo = controller.findById(id);
            if (equipo != null) {
                if (entrenadorYaTieneEquipo(entrenador, id)) {
                    setMensaje("El entrenador seleccionado ya tiene un equipo distinto asociado");
                    JOptionPane.showMessageDialog(this, "El entrenador seleccionado ya tiene un equipo distinto asociado");
                    return;
                }
                equipo.setEntrenador(entrenador);
                controller.update(equipo);
                cargarDatos();
                setMensajeExito("Equipo actualizado exitosamente");
                JOptionPane.showMessageDialog(this, "Equipo actualizado exitosamente");
            } else {
                JOptionPane.showMessageDialog(this, "No puedes actualizar la clave primaria");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar equipo: " + ex.getMessage());
        }
    }

    private void eliminarEquipo() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un equipo para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);

            // Verificar integridad: no borrar si tiene detalles de equipo
            Equipo equipo = controller.findById(id);
            if (equipo != null && equipo.getDetallesEquipo() != null && !equipo.getDetallesEquipo().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar el equipo porque tiene Pokémons asociados");
                return;
            }

            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Equipo eliminado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar equipo: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        comboEntrenador.setSelectedIndex(-1);
        table.clearSelection();
        clearMensaje();
    }
}