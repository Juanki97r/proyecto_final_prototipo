package vistas;

import controladores.TipoController;
import entidades.Tipo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Tipos.
 */
public class TipoView extends JFrame {

    private TipoController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtNombre;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private Integer selectedTipoId;

    public TipoView() {
        controller = new TipoController();

        setTitle("Gestión de Tipos");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarTipo());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Tipo"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria del tipo.");
        panelForm.add(txtId);

        panelForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

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

        btnCrear.addActionListener(e -> crearTipo());
        btnActualizar.addActionListener(e -> actualizarTipo());
        btnEliminar.addActionListener(e -> eliminarTipo());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Tipo> tipos = controller.findAll();
            for (Tipo t : tipos) {
                tableModel.addRow(new Object[]{t.getCodtipo(), t.getNomtipo()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar tipos: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarTipo() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            selectedTipoId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            txtId.setText(selectedTipoId.toString());
            txtId.setEditable(false);
            txtNombre.setText(tableModel.getValueAt(row, 1).toString());
        }
    }

    private void crearTipo() {
        String idStr = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ID es obligatorio");
            return;
        }
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
            return;
        }
        if (!esNombreValido(nombre)) {
            JOptionPane.showMessageDialog(this, "El nombre no puede contener números ni caracteres especiales.");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Tipo tipo = new Tipo(nombre);
            tipo.setCodtipo(id);
            controller.create(tipo);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Tipo creado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido");
        } catch (Exception ex) {
            if (ViewUtils.esClavePrimariaDuplicada(ex)) {
                JOptionPane.showMessageDialog(this, ViewUtils.mensajeClaveDuplicada("el tipo"));
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear tipo: " + ex.getMessage());
            }
        }
    }

    private void actualizarTipo() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty() || selectedTipoId == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un tipo para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            if (!idStr.equals(selectedTipoId.toString())) {
                JOptionPane.showMessageDialog(this, "El ID es clave primaria y no puede modificarse.");
                txtId.setText(selectedTipoId.toString());
                return;
            }

            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
                return;
            }
            if (!esNombreValido(nombre)) {
                JOptionPane.showMessageDialog(this, "El nombre no puede contener números ni caracteres especiales.");
                return;
            }

            Tipo tipo = controller.findById(id);
            if (tipo != null) {
                tipo.setNomtipo(nombre);
                controller.update(tipo);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Tipo actualizado exitosamente");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar tipo: " + ex.getMessage());
        }
    }

    private boolean esNombreValido(String nombre) {
        return nombre.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+");
    }

    private void eliminarTipo() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un tipo para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);
            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Tipo eliminado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            String mensaje = "Error al eliminar tipo";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("constraint") || 
                    ex.getMessage().toLowerCase().contains("violat")) {
                    mensaje = "No se puede eliminar este tipo porque está asociado a Pokémons."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void limpiarFormulario() {
        selectedTipoId = null;
        txtId.setText("");
        txtId.setEditable(true);
        txtNombre.setText("");
        table.clearSelection();
    }
}