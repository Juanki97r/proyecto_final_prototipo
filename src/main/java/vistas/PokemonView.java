package vistas;

import controladores.PokemonController;
import entidades.Pokemon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Pokémons.
 */
public class PokemonView extends JFrame {

    private PokemonController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtNombre;
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private Integer selectedPokemonId;

    public PokemonView() {
        controller = new PokemonController();

        setTitle("Gestión de Pokémons");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"NumPokedex", "Nombre"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> seleccionarPokemon());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Pokémon"));

        panelForm.add(new JLabel("Número Pokédex:"));
        txtId = new JTextField();
        txtId.setToolTipText("Introduce el número de Pokédex del Pokémon.");
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

        btnCrear.addActionListener(e -> crearPokemon());
        btnActualizar.addActionListener(e -> actualizarPokemon());
        btnEliminar.addActionListener(e -> eliminarPokemon());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<Pokemon> pokemones = controller.findAll();
            for (Pokemon p : pokemones) {
                tableModel.addRow(new Object[]{p.getNumpokedex(), p.getNompokemon()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar pokémons: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarPokemon() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            selectedPokemonId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            txtId.setText(selectedPokemonId.toString());
            txtId.setEditable(false);
            txtNombre.setText(tableModel.getValueAt(row, 1).toString());
        }
    }

    private void crearPokemon() {
        String idStr = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El número de Pokédex es obligatorio");
            return;
        }

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Pokemon pokemon = new Pokemon(id, nombre);
            controller.create(pokemon);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Pokémon creado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El número de Pokédex debe ser un número válido");
        } catch (Exception ex) {
            if (ViewUtils.esClavePrimariaDuplicada(ex)) {
                JOptionPane.showMessageDialog(this, ViewUtils.mensajeClaveDuplicada("el Pokémon"));
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear Pokémon: " + ex.getMessage());
            }
        }
    }

    private void actualizarPokemon() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty() || selectedPokemonId == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Pokémon para actualizar");
                return;
            }

            int id = Integer.parseInt(idStr);
            if (!idStr.equals(selectedPokemonId.toString())) {
                JOptionPane.showMessageDialog(this, "El número de Pokédex es clave primaria y no puede modificarse.");
                txtId.setText(selectedPokemonId.toString());
                return;
            }

            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio");
                return;
            }

            Pokemon pokemon = controller.findById(id);
            if (pokemon != null) {
                pokemon.setNompokemon(nombre);
                controller.update(pokemon);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Pokémon actualizado exitosamente");
            } else {
                JOptionPane.showMessageDialog(this, "Pokémon no encontrado");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El número de Pokédex debe ser un número válido");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar Pokémon: " + ex.getMessage());
        }
    }

    private void eliminarPokemon() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Pokémon para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);
            controller.delete(id);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Pokémon eliminado exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: El número de Pokédex debe ser un número válido");
        } catch (Exception ex) {
            String mensaje = "Error al eliminar Pokémon";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("constraint") || 
                    ex.getMessage().toLowerCase().contains("violat")) {
                    mensaje = "No se puede eliminar este Pokémon porque está asociado a tipos o equipos."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void limpiarFormulario() {
        selectedPokemonId = null;
        txtId.setText("");
        txtId.setEditable(true);
        txtNombre.setText("");
        table.clearSelection();
    }
}