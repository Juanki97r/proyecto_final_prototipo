package vistas;

import controladores.GimnasioController;
import controladores.RegionController;
import entidades.Gimnasio;
import entidades.Region;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Gimnasios.
 */
public class GimnasioView extends JFrame {

    private GimnasioController controller;
    private RegionController regionController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtNomMedalla, txtTipoMedalla;
    private JComboBox<Region> comboRegion;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;

    public GimnasioView() {
        controller = new GimnasioController();
        regionController = new RegionController();

        setTitle("Gestión de Gimnasios");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Región", "Medalla", "Tipo Medalla"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarGimnasio());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Gimnasio"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria del gimnasio.");
        panelForm.add(txtId);

        panelForm.add(new JLabel("Región:"));
        comboRegion = new JComboBox<>();
        cargarRegiones();
        panelForm.add(comboRegion);

        panelForm.add(new JLabel("Nombre Medalla:"));
        txtNomMedalla = new JTextField();
        panelForm.add(txtNomMedalla);

        panelForm.add(new JLabel("Tipo Medalla:"));
        txtTipoMedalla = new JTextField();
        panelForm.add(txtTipoMedalla);

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

        btnCrear.addActionListener(e -> crearGimnasio());
        btnActualizar.addActionListener(e -> actualizarGimnasio());
        btnEliminar.addActionListener(e -> eliminarGimnasio());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarRegiones() {
        try {
            List<Region> regiones = regionController.findAll();
            for (Region r : regiones) {
                comboRegion.addItem(r);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar regiones: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Gimnasio> gimnasios = controller.findAll();
            for (Gimnasio g : gimnasios) {
                tableModel.addRow(new Object[]{
                    g.getCodgym(),
                    g.getRegion() != null ? g.getRegion().getNomregion() : "",
                    g.getNomMedalla(),
                    g.getTipoMedalla()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar gimnasios: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarGimnasio() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            txtId.setText(tableModel.getValueAt(row, 0).toString());
            // Seleccionar región en combo
            String regionNombre = tableModel.getValueAt(row, 1).toString();
            for (int i = 0; i < comboRegion.getItemCount(); i++) {
                if (comboRegion.getItemAt(i).getNomregion().equals(regionNombre)) {
                    comboRegion.setSelectedIndex(i);
                    break;
                }
            }
            txtNomMedalla.setText(tableModel.getValueAt(row, 2) != null ? tableModel.getValueAt(row, 2).toString() : "");
            txtTipoMedalla.setText(tableModel.getValueAt(row, 3).toString());
        }
    }

    private boolean nombreValido(String nombre) {
        return nombre != null && nombre.matches("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(\\s+[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*");
    }

    private void crearGimnasio() {
        String idStr = txtId.getText().trim();
        Region region = (Region) comboRegion.getSelectedItem();
        String nomMedalla = txtNomMedalla.getText().trim();
        String tipoMedalla = txtTipoMedalla.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El ID es obligatorio");
            return;
        }
        if (region == null || tipoMedalla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Región y tipo de medalla son obligatorios");
            return;
        }
        if (!nomMedalla.isEmpty() && !nombreValido(nomMedalla)) {
            JOptionPane.showMessageDialog(this, "El nombre de la medalla solo puede contener letras y espacios");
            return;
        }
        if (!nombreValido(tipoMedalla)) {
            JOptionPane.showMessageDialog(this, "El tipo de medalla solo puede contener letras y espacios");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Gimnasio gimnasio = new Gimnasio(nomMedalla.isEmpty() ? null : nomMedalla, tipoMedalla, region);
            gimnasio.setCodgym(id);
            controller.create(gimnasio);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Gimnasio creado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido");
        } catch (Exception ex) {
            if (ViewUtils.esClavePrimariaDuplicada(ex)) {
                JOptionPane.showMessageDialog(this, ViewUtils.mensajeClaveDuplicada("el gimnasio"));
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear gimnasio: " + ex.getMessage());
            }
        }
    }

    private void actualizarGimnasio() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un gimnasio para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            Region region = (Region) comboRegion.getSelectedItem();
            String nomMedalla = txtNomMedalla.getText().trim();
            String tipoMedalla = txtTipoMedalla.getText().trim();

            if (region == null || tipoMedalla.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Región y tipo de medalla son obligatorios");
                return;
            }
            if (!nomMedalla.isEmpty() && !nombreValido(nomMedalla)) {
                JOptionPane.showMessageDialog(this, "El nombre de la medalla solo puede contener letras y espacios");
                return;
            }
            if (!nombreValido(tipoMedalla)) {
                JOptionPane.showMessageDialog(this, "El tipo de medalla solo puede contener letras y espacios");
                return;
            }

            Gimnasio gimnasio = controller.findById(id);
            if (gimnasio != null) {
                gimnasio.setRegion(region);
                gimnasio.setNomMedalla(nomMedalla.isEmpty() ? null : nomMedalla);
                gimnasio.setTipoMedalla(tipoMedalla);
                controller.update(gimnasio);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Gimnasio actualizado exitosamente");
            } else {
                JOptionPane.showMessageDialog(this, "Gimnasio no encontrado");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar gimnasio: " + ex.getMessage());
        }
    }

    private void eliminarGimnasio() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un gimnasio para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);

            // Verificar integridad: no borrar si tiene medallas
            Gimnasio gimnasio = controller.findById(id);
            if (gimnasio != null && gimnasio.getMedallas() != null && !gimnasio.getMedallas().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar el gimnasio porque tiene medallas asociadas");
                return;
            }

            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Gimnasio eliminado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar gimnasio: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        comboRegion.setSelectedIndex(-1);
        txtNomMedalla.setText("");
        txtTipoMedalla.setText("");
        table.clearSelection();
    }
}