package vistas;

import controladores.DetallePokemonController;
import controladores.PokemonController;
import controladores.TipoController;
import entidades.DetallePokemon;
import entidades.DetallePokemonPK;
import entidades.Pokemon;
import entidades.Tipo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para gestionar Detalles de Pokémon (tipos).
 */
public class DetallePokemonView extends JFrame {

    private DetallePokemonController controller;
    private PokemonController pokemonController;
    private TipoController tipoController;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Pokemon> comboPokemon;
    private JComboBox<Tipo> comboTipo;
    private JButton btnCrear, btnEliminar, btnLimpiar;

    public DetallePokemonView() {
        controller = new DetallePokemonController();
        pokemonController = new PokemonController();
        tipoController = new TipoController();

        setTitle("Gestión de Detalles Pokémon");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Pokémon", "Tipo"}, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Asociar Tipo a Pokémon"));

        panelForm.add(new JLabel("Pokémon:"));
        comboPokemon = new JComboBox<>();
        cargarPokemons();
        panelForm.add(comboPokemon);

        panelForm.add(new JLabel("Tipo:"));
        comboTipo = new JComboBox<>();
        cargarTipos();
        panelForm.add(comboTipo);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrear = new JButton("Crear Asociación");
        btnEliminar = new JButton("Eliminar Asociación");
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

    private void cargarTipos() {
        try {
            List<Tipo> tipos = tipoController.findAll();
            for (Tipo t : tipos) {
                comboTipo.addItem(t);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar tipos: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        try {
            tableModel.setRowCount(0);
            List<DetallePokemon> detalles = controller.findAll();
            detalles.sort((a, b) -> {
                String nombreA = a.getPokemon() != null ? a.getPokemon().getNompokemon() : "";
                String nombreB = b.getPokemon() != null ? b.getPokemon().getNompokemon() : "";
                int cmp = nombreA.compareToIgnoreCase(nombreB);
                if (cmp != 0) {
                    return cmp;
                }
                String tipoA = a.getTipo() != null ? a.getTipo().getNomtipo() : "";
                String tipoB = b.getTipo() != null ? b.getTipo().getNomtipo() : "";
                return tipoA.compareToIgnoreCase(tipoB);
            });
            for (DetallePokemon d : detalles) {
                tableModel.addRow(new Object[]{
                    d.getPokemon() != null ? d.getPokemon().getNompokemon() : "",
                    d.getTipo() != null ? d.getTipo().getNomtipo() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar detalles de pokémon: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearDetalle() {
        Pokemon pokemon = (Pokemon) comboPokemon.getSelectedItem();
        Tipo tipo = (Tipo) comboTipo.getSelectedItem();

        if (pokemon == null || tipo == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Pokémon y Tipo");
            return;
        }

        try {
            DetallePokemonPK pk = new DetallePokemonPK(pokemon.getNumpokedex(), tipo.getCodtipo());
            DetallePokemon detalle = new DetallePokemon(pk, pokemon, tipo);
            controller.create(detalle);
            cargarDatos();
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Asociación creada exitosamente");
        } catch (Exception ex) {
            String mensaje = "Error al crear asociación";
            if (ex.getMessage() != null) {
                if (ex.getMessage().toLowerCase().contains("duplicate") || 
                    ex.getMessage().toLowerCase().contains("constraint")) {
                    mensaje = "Este Pokémon ya tiene asociado este tipo."; 
                }
            }
            JOptionPane.showMessageDialog(this, mensaje);
        }
    }

    private void eliminarDetalle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una asociación de la tabla");
            return;
        }

        String pokemonNombre = tableModel.getValueAt(row, 0).toString();
        String tipoNombre = tableModel.getValueAt(row, 1).toString();

        // Buscar IDs
        Pokemon pokemon = null;
        Tipo tipo = null;
        for (Pokemon p : pokemonController.findAll()) {
            if (p.getNompokemon().equals(pokemonNombre)) {
                pokemon = p;
                break;
            }
        }
        for (Tipo t : tipoController.findAll()) {
            if (t.getNomtipo().equals(tipoNombre)) {
                tipo = t;
                break;
            }
        }

        if (pokemon != null && tipo != null) {
            try {
                DetallePokemonPK pk = new DetallePokemonPK(pokemon.getNumpokedex(), tipo.getCodtipo());
                controller.delete(pk);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Asociación eliminada exitosamente");
            } catch (Exception ex) {
                String mensaje = "Error al eliminar asociación";
                if (ex.getMessage() != null) {
                    if (ex.getMessage().toLowerCase().contains("no row") || 
                        ex.getMessage().toLowerCase().contains("not found")) {
                        mensaje = "El registro no existe. Actualiza la lista y vuelve a intentarlo."; 
                    }
                }
                JOptionPane.showMessageDialog(this, mensaje);
            }
        }
    }

    private void limpiarFormulario() {
        comboPokemon.setSelectedIndex(-1);
        comboTipo.setSelectedIndex(-1);
    }
}