package vistas;

import controladores.RegionController;
import entidades.Region;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Regiones.
 */
public class RegionView extends JFrame {

    private RegionController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtNombre;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private Integer selectedRegionId;

    public RegionView() {
        controller = new RegionController();

        setTitle("Gestión de Regiones");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarRegion());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos de la Región"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria de la región.");
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

        btnCrear.addActionListener(e -> crearRegion());
        btnActualizar.addActionListener(e -> actualizarRegion());
        btnEliminar.addActionListener(e -> eliminarRegion());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Region> regiones = controller.findAll();
            for (Region r : regiones) {
                tableModel.addRow(new Object[]{r.getCodregion(), r.getNomregion()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar regiones: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarRegion() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            selectedRegionId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            txtId.setText(selectedRegionId.toString());
            txtId.setEditable(false);
            txtNombre.setText(tableModel.getValueAt(row, 1).toString());
        }
    }

    private boolean nombreValido(String nombre) {
        return nombre != null && nombre.matches("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(\\s+[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*");
    }

    private void crearRegion() {
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
        if (!nombreValido(nombre)) {
            JOptionPane.showMessageDialog(this, "El nombre de la región solo puede contener letras y espacios");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Region region = new Region(nombre);
            region.setCodregion(id);
            controller.create(region);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Región creada exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido");
        } catch (Exception ex) {
            if (ViewUtils.esClavePrimariaDuplicada(ex)) {
                JOptionPane.showMessageDialog(this, ViewUtils.mensajeClaveDuplicada("la región"));
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear región: " + ex.getMessage());
            }
        }
    }

    private void actualizarRegion() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty() || selectedRegionId == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una región para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            if (!idStr.equals(selectedRegionId.toString())) {
                JOptionPane.showMessageDialog(this, "El ID es clave primaria y no puede modificarse.");
                txtId.setText(selectedRegionId.toString());
                return;
            }

            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
                return;
            }
            if (!nombreValido(nombre)) {
                JOptionPane.showMessageDialog(this, "El nombre de la región solo puede contener letras y espacios");
                return;
            }

            Region region = controller.findById(id);
            if (region != null) {
                region.setNomregion(nombre);
                controller.update(region);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Región actualizada exitosamente");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar región: " + ex.getMessage());
        }
    }

    private void eliminarRegion() {
        try {
            int id = Integer.parseInt(txtId.getText());

            // Verificar integridad: no borrar si tiene gimnasios
            Region region = controller.findById(id);
            if (region != null && region.getGimnasios() != null && !region.getGimnasios().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar la región porque tiene gimnasios asociados");
                return;
            }

            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Región eliminada exitosamente");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar región: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        selectedRegionId = null;
        txtId.setText("");
        txtId.setEditable(true);
        txtNombre.setText("");
        table.clearSelection();
    }
}