package vistas;

import controladores.EntrenadorController;
import entidades.Entrenador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Entrenadores (CRUD).
 * Permite consultar, crear, actualizar y eliminar entrenadores.
 */
public class EntrenadorView extends JFrame {

    private EntrenadorController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtNombre, txtEdad, txtId;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private JLabel lblMensaje;

    public EntrenadorView() {
        controller = new EntrenadorController();

        setTitle("Gestión de Entrenadores");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Tabla
        tableModel = new DefaultTableModel(new String[]{"ID", "Nombre", "Edad"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarEntrenador());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de formulario
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Entrenador"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria del entrenador.");
        panelForm.add(txtId);

        panelForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Edad:"));
        txtEdad = new JTextField();
        panelForm.add(txtEdad);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrear = new JButton("Crear");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        // Panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelForm, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panelInferior.add(lblMensaje, BorderLayout.NORTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Action listeners
        btnCrear.addActionListener(e -> crearEntrenador());
        btnActualizar.addActionListener(e -> actualizarEntrenador());
        btnEliminar.addActionListener(e -> eliminarEntrenador());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Entrenador> entrenadores = controller.findAll();
            for (Entrenador e : entrenadores) {
                tableModel.addRow(new Object[]{e.getIdentrenador(), e.getNombre(), e.getEdad()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar entrenadores: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarEntrenador() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            txtId.setText(tableModel.getValueAt(row, 0).toString());
            txtNombre.setText(tableModel.getValueAt(row, 1).toString());
            txtEdad.setText(tableModel.getValueAt(row, 2) != null ? tableModel.getValueAt(row, 2).toString() : "");
        }
    }

    private boolean nombreValido(String nombre) {
        return nombre != null && nombre.matches("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(\\s+[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*");
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

    private void crearEntrenador() {
        clearMensaje();
        try {
            String idStr = txtId.getText().trim();
            String nombre = txtNombre.getText().trim();
            String edadStr = txtEdad.getText().trim();

            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El ID es obligatorio");
                return;
            }
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
                return;
            }
            if (!nombreValido(nombre)) {
                setMensaje("El nombre solo puede contener letras y espacios");
                JOptionPane.showMessageDialog(this, "El nombre solo puede contener letras y espacios");
                return;
            }

            int id = Integer.parseInt(idStr);
            Integer edad = edadStr.isEmpty() ? null : Integer.parseInt(edadStr);

            Entrenador entrenador = new Entrenador(nombre, edad);
            entrenador.setIdentrenador(id);
            controller.create(entrenador);
            cargarDatos();
            limpiarFormulario();
            setMensajeExito("Entrenador creado exitosamente");
            JOptionPane.showMessageDialog(this, "Entrenador creado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los campos numéricos deben ser válidos");
        } catch (Exception ex) {
            if (esClavePrimariaDuplicada(ex)) {
                setMensaje("No se puede crear el entrenador porque la clave primaria ya existe");
                JOptionPane.showMessageDialog(this, "No se puede crear el entrenador porque la clave primaria ya existe");
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear entrenador: " + ex.getMessage());
            }
        }
    }

    private boolean esClavePrimariaDuplicada(Throwable ex) {
        while (ex != null) {
            String mensaje = ex.getMessage();
            if (ex instanceof javax.persistence.EntityExistsException ||
                (mensaje != null && mensaje.toLowerCase().contains("duplicate")) ||
                (mensaje != null && mensaje.toLowerCase().contains("unique")) ||
                (mensaje != null && mensaje.toLowerCase().contains("primary key"))) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    private void actualizarEntrenador() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un entrenador para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            String nombre = txtNombre.getText().trim();
            String edadStr = txtEdad.getText().trim();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
                return;
            }
            if (!nombreValido(nombre)) {
                setMensaje("El nombre solo puede contener letras y espacios");
                JOptionPane.showMessageDialog(this, "El nombre solo puede contener letras y espacios");
                return;
            }

            Integer edad = edadStr.isEmpty() ? null : Integer.parseInt(edadStr);

            Entrenador entrenador = controller.findById(id);
            if (entrenador != null) {
                entrenador.setNombre(nombre);
                entrenador.setEdad(edad);
                controller.update(entrenador);
                cargarDatos();
                setMensajeExito("Entrenador actualizado exitosamente");
                JOptionPane.showMessageDialog(this, "Entrenador actualizado exitosamente");
            } else {
                JOptionPane.showMessageDialog(this, "No es posible modificar el ID");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los campos numéricos deben tener valores válidos");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar entrenador: " + ex.getMessage());
        }
    }

    private void eliminarEntrenador() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un entrenador para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);

            // Verificar integridad referencial: no borrar si tiene un equipo asociado
            Entrenador entrenador = controller.findById(id);
            if (entrenador != null && entrenador.getEquipo() != null) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar el entrenador porque tiene un equipo asociado");
                return;
            }

            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Entrenador eliminado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El ID debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar entrenador: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtEdad.setText("");
        table.clearSelection();
        clearMensaje();
    }
}