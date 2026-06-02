package vistas;

import controladores.DetalleEquipoController;
import controladores.EquipoController;
import controladores.PokemonController;
import entidades.DetalleEquipo;
import entidades.DetalleEquipoPK;
import entidades.Equipo;
import entidades.Pokemon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Detalles de Equipo (Pokémons en equipos).
 */
public class DetalleEquipoView extends JFrame {

    private DetalleEquipoController controller;
    private EquipoController equipoController;
    private PokemonController pokemonController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Equipo> comboEquipo;
    private JComboBox<Pokemon> comboPokemon;
    private JTextField txtSlot;
    private JButton btnCrear, btnEliminar, btnLimpiar;

    public DetalleEquipoView() {
        controller = new DetalleEquipoController();
        equipoController = new EquipoController();
        pokemonController = new PokemonController();

        setTitle("Gestión de Detalles Equipo");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Equipo", "Pokémon", "Slot"}, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(3, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Agregar Pokémon a Equipo"));

        panelForm.add(new JLabel("Equipo:"));
        comboEquipo = new JComboBox<>();
        cargarEquipos();
        panelForm.add(comboEquipo);

        panelForm.add(new JLabel("Pokémon:"));
        comboPokemon = new JComboBox<>();
        cargarPokemons();
        panelForm.add(comboPokemon);

        panelForm.add(new JLabel("Slot (1-6):"));
        txtSlot = new JTextField();
        panelForm.add(txtSlot);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrear = new JButton("Agregar Pokémon");
        btnEliminar = new JButton("Quitar Pokémon");
        btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelForm, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearDetalle());
        btnEliminar.addActionListener(e -> eliminarDetalle());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void cargarEquipos() {
        try {
            List<Equipo> equipos = equipoController.findAll();
            for (Equipo e : equipos) {
                comboEquipo.addItem(e);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar equipos: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarPokemons() {
        try {
            List<Pokemon> pokemons = pokemonController.findAll();
            for (Pokemon p : pokemons) {
                comboPokemon.addItem(p);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar pokémons: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<DetalleEquipo> detalles = controller.findAll();
            detalles.sort((a, b) -> {
                int equipoA = a.getEquipo() != null ? a.getEquipo().getCodequipo() : Integer.MAX_VALUE;
                int equipoB = b.getEquipo() != null ? b.getEquipo().getCodequipo() : Integer.MAX_VALUE;
                return Integer.compare(equipoA, equipoB);
            });
            for (DetalleEquipo d : detalles) {
                tableModel.addRow(new Object[]{
                    d.getEquipo() != null ? "Equipo " + d.getEquipo().getCodequipo() : "",
                    d.getPokemon() != null ? d.getPokemon().getNompokemon() : "",
                    d.getId().getNumslot()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar detalles de equipo: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearDetalle() {
        Equipo equipo = (Equipo) comboEquipo.getSelectedItem();
        Pokemon pokemon = (Pokemon) comboPokemon.getSelectedItem();
        String slotStr = txtSlot.getText().trim();

        if (equipo == null || pokemon == null || slotStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar equipo, Pokémon y slot");
            return;
        }

        try {
            int slot = Integer.parseInt(slotStr);
            if (slot < 1 || slot > 6) {
                JOptionPane.showMessageDialog(this, "El slot debe estar entre 1 y 6");
                return;
            }

            DetalleEquipoPK pk = new DetalleEquipoPK(equipo.getCodequipo(), slot);
            DetalleEquipo detalle = new DetalleEquipo(pk, equipo, pokemon);
            controller.create(detalle);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Pokémon agregado al equipo exitosamente");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El slot debe ser un número");
        } catch (Exception ex) {
            String mensaje = "Error al agregar Pokémon";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("duplicate") || 
                    ex.getMessage().toLowerCase().contains("constraint")) {
                    mensaje = "El equipo ya tiene ocupado este slot."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void eliminarDetalle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un detalle de la tabla");
            return;
        }

        String equipoStr = tableModel.getValueAt(row, 0).toString();
        int slot = (Integer) tableModel.getValueAt(row, 2);

        // Extraer ID del equipo
        int equipoId = Integer.parseInt(equipoStr.replace("Equipo ", ""));

        try {
            DetalleEquipoPK pk = new DetalleEquipoPK(equipoId, slot);
            controller.delete(pk);
            cargarDatos();
            JOptionPane.showMessageDialog(this, "Pokémon quitado del equipo exitosamente");
        } catch (Exception ex) {
            String mensaje = "Error al quitar Pokémon";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("no row") || 
                    ex.getMessage().toLowerCase().contains("not found")) {
                    mensaje = "El registro no existe. Actualiza la lista y vuelve a intentarlo."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void limpiarFormulario() {
        comboEquipo.setSelectedIndex(-1);
        comboPokemon.setSelectedIndex(-1);
        txtSlot.setText("");
    }
}