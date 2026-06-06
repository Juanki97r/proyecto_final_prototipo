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
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              CLASE: EQUIPOVIEW                                             ║
 * ║   Interfaz gráfica para la gestión CRUD de Equipos                         ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * PATRÓN MVC — Capa Vista (la más compleja de las vistas simples):
 *   • USA TRES controladores:
 *     - EquipoController      → operaciones CRUD del Equipo
 *     - EntrenadorController  → llena el ComboBox de entrenadores
 *     - DetalleEquipoController → muestra los Pokémons del equipo al seleccionarlo
 *
 * QUÉ GESTIONA:
 *   La tabla EQUIPO: la relación 1:1 entre Entrenador y su equipo de Pokémons.
 *   Un Equipo solo puede tener UN Entrenador y viceversa.
 *
 * CARACTERÍSTICA ESPECIAL — mostrarDetallesEquipo():
 *   Al seleccionar un Equipo en la tabla, abre un JDialog modal
 *   que muestra los Pokémons en cada slot (1-6) usando DetalleEquipoController.
 *   Permite ver los Pokémons del equipo sin salir de esta vista.
 *
 * VALIDACIÓN ESPECIAL — entrenadorYaTieneEquipo():
 *   Verifica que el Entrenador seleccionado no tenga ya un Equipo.
 *   Un Entrenador NO puede tener dos Equipos (relación 1:1).
 *
 * RESTRICCIÓN AL ELIMINAR:
 *   No se puede borrar un Equipo que tenga Pokémons (DetalleEquipo).
 */
public class EquipoView extends JFrame {

    /** Controlador principal: operaciones CRUD sobre la tabla EQUIPO */
    private EquipoController controller;

    /** Controlador auxiliar: para llenar el ComboBox de Entrenadores */
    private EntrenadorController entrenadorController;

    /**
     * detalleEquipoController: Controlador auxiliar para mostrar los Pokémons del equipo.
     * Se usa en mostrarDetallesEquipo() para obtener los slots al seleccionar un equipo.
     */
    private DetalleEquipoController detalleEquipoController;

    /** Tabla Swing con columnas: ID (codequipo) y Entrenador (nombre) */
    private JTable table;

    /** Modelo de datos de la tabla */
    private DefaultTableModel tableModel;

    /** txtId → codequipo (clave primaria del Equipo) */
    private JTextField txtId;

    /**
     * comboEntrenador: JComboBox<Entrenador> para seleccionar el Entrenador del Equipo.
     *
     * Al igual que comboRegion en GimnasioView:
     *   • Almacena objetos Entrenador completos
     *   • Swing usa Entrenador.toString() para mostrar el nombre
     *   • Obtenemos el Entrenador seleccionado con (Entrenador) comboEntrenador.getSelectedItem()
     */
    private JComboBox<Entrenador> comboEntrenador;

    /** Botones CRUD + feedback */
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
    private JLabel lblMensaje;

    /**
     * Constructor: Inicializa la ventana de gestión de Equipos.
     * Crea los tres controladores necesarios para esta vista.
     */
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

    /**
     * initComponents(): Construye y dispone todos los componentes Swing.
     *
     * COMPORTAMIENTO ESPECIAL DEL LISTENER DE SELECCIÓN:
     *   Usa e.getValueIsAdjusting() para evitar llamadas dobles durante la selección.
     *   getValueIsAdjusting() devuelve true mientras el usuario todavía está
     *   desplazando el ratón; solo procesamos la selección cuando es false (definitiva).
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // ── TABLA ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(new String[]{"ID", "Entrenador"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            // getValueIsAdjusting(): evita múltiples llamadas durante un drag de ratón.
            // Solo procesamos la selección cuando el evento es definitivo (false).
            if (!e.getValueIsAdjusting()) {
                seleccionarEquipo();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── FORMULARIO ─────────────────────────────────────────────────────
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Equipo"));

        panelForm.add(new JLabel("ID (clave primaria):"));
        txtId = new JTextField();
        txtId.setEditable(true);
        txtId.setToolTipText("Introduce la clave primaria del equipo.");
        panelForm.add(txtId);

        panelForm.add(new JLabel("Entrenador:"));
        comboEntrenador = new JComboBox<>();
        cargarEntrenadores(); // Llena el combo con todos los Entrenadores
        panelForm.add(comboEntrenador);

        // ── BOTONES ────────────────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrear     = new JButton("Crear");
        btnActualizar = new JButton("Actualizar");
        btnEliminar  = new JButton("Eliminar");
        btnLimpiar   = new JButton("Limpiar");

        panelBotones.add(btnCrear);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelForm, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        // lblMensaje: feedback en rojo/verde al usuario
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

    /**
     * cargarEntrenadores(): Llena el JComboBox con todos los Entrenadores de la BD.
     * Se llama una vez al iniciar la vista.
     */
    private void cargarEntrenadores() {
        try {
            List<Entrenador> entrenadores = entrenadorController.findAll();
            for (Entrenador e : entrenadores) {
                comboEntrenador.addItem(e); // Swing usa Entrenador.toString() → muestra el nombre
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar entrenadores: " + ex.getMessage() + "\n" +
                "Verifica la conexión a la base de datos.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * cargarDatos(): Recarga la tabla con todos los Equipos de la BD.
     *
     * La columna "Entrenador" muestra el nombre del Entrenador asociado,
     * navegando la relación JPA: equipo.getEntrenador().getNombre()
     */
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

    /**
     * entrenadorYaTieneEquipo(): Verifica si un Entrenador ya tiene un Equipo asignado.
     *
     * PARÁMETRO idEquipoExcluido:
     *   Al actualizar, excluimos el equipo actual de la búsqueda.
     *   Ejemplo: Si el Entrenador ya tiene el Equipo 1 y estamos ACTUALIZANDO el Equipo 1,
     *   no debe fallar (no es un segundo equipo). Por eso excluimos el equipo que
     *   estamos editando en la búsqueda.
     *
     *   Al crear (sin equipo previo), idEquipoExcluido = null.
     *
     * MECANISMO:
     *   Itera todos los Equipos y comprueba si alguno tiene ese Entrenador.
     *   Si idEquipoExcluido != null, salta ese Equipo.
     *
     * @param entrenador       El Entrenador a verificar
     * @param idEquipoExcluido ID del Equipo que se está editando (null al crear)
     * @return true si el Entrenador YA tiene otro Equipo
     */
    private boolean entrenadorYaTieneEquipo(Entrenador entrenador, Integer idEquipoExcluido) {
        if (entrenador == null) {
            return false;
        }
        try {
            for (Equipo e : controller.findAll()) {
                if (e.getEntrenador() != null &&
                    e.getEntrenador().getIdentrenador() == entrenador.getIdentrenador() &&
                    (idEquipoExcluido == null || e.getCodequipo() != idEquipoExcluido)) {
                    return true; // Encontró un equipo de ese entrenador que no es el que estamos editando
                }
            }
        } catch (Exception ex) {
            // Si falla la consulta, no interrumpimos la operación principal
        }
        return false;
    }

    /** Sobrecarga sin idEquipoExcluido → para uso al crear (no hay equipo previo) */
    private boolean entrenadorYaTieneEquipo(Entrenador entrenador) {
        return entrenadorYaTieneEquipo(entrenador, null);
    }

    /** Muestra mensaje de error (rojo) en la etiqueta de feedback */
    private void setMensaje(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje != null ? mensaje : " ");
            lblMensaje.setForeground(Color.RED);
        }
    }

    /** Muestra mensaje de éxito (verde) en la etiqueta de feedback */
    private void setMensajeExito(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje != null ? mensaje : " ");
            lblMensaje.setForeground(new Color(0, 128, 0));
        }
    }

    /** Limpia el mensaje de feedback */
    private void clearMensaje() {
        if (lblMensaje != null) {
            lblMensaje.setText(" ");
        }
    }

    /**
     * seleccionarEquipo(): Rellena el formulario y muestra detalles al seleccionar fila.
     *
     * COMPORTAMIENTO ESPECIAL:
     *   Además de rellenar el formulario (ID + Entrenador en combo),
     *   llama a mostrarDetallesEquipo(equipoId) que abre un JDialog modal
     *   mostrando todos los Pokémons del equipo con su slot (posición 1-6).
     *
     *   La búsqueda del Entrenador en el combo es igual que en GimnasioView:
     *   iteramos los items buscando el que tiene el mismo nombre que la tabla.
     */
    private void seleccionarEquipo() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String idStr = tableModel.getValueAt(row, 0).toString();
            txtId.setText(idStr);
            // Buscar y seleccionar el Entrenador correspondiente en el ComboBox
            String entrenadorNombre = tableModel.getValueAt(row, 1).toString();
            for (int i = 0; i < comboEntrenador.getItemCount(); i++) {
                if (comboEntrenador.getItemAt(i).getNombre().equals(entrenadorNombre)) {
                    comboEntrenador.setSelectedIndex(i);
                    break;
                }
            }
            try {
                int equipoId = Integer.parseInt(idStr);
                mostrarDetallesEquipo(equipoId); // Abre el JDialog con los Pokémons del equipo
            } catch (NumberFormatException ignored) {
                // El ID siempre es numérico; esta excepción no debería ocurrir
            }
        }
    }

    /**
     * mostrarDetallesEquipo(): Abre un JDialog modal con los Pokémons del equipo.
     *
     * FLUJO:
     *   1. detalleEquipoController.findByEquipoId(equipoId)
     *      → Consulta JPQL con filtro y ORDER BY numslot
     *      → Devuelve los slots del equipo ordenados (1, 2, 3...)
     *   2. Crea un nuevo JTable solo para este diálogo (no el principal)
     *   3. Muestra el dialog modal (setModal(true) implícito en JDialog con parent)
     *
     * DIFERENCIA CON LOS DATOS DE LA TABLA PRINCIPAL:
     *   La tabla principal muestra: Equipo 1 → Ash
     *   El diálogo muestra los Pokémons: Slot 1 → Pikachu, Slot 2 → Charizard...
     *
     * detalleTable.setEnabled(false): La tabla del diálogo es de solo lectura
     * (no se puede seleccionar ni editar, solo visualizar).
     *
     * @param equipoId El codequipo del Equipo a visualizar
     */
    private void mostrarDetallesEquipo(int equipoId) {
        try {
            java.util.List<DetalleEquipo> detalles = detalleEquipoController.findByEquipoId(equipoId);
            if (detalles == null || detalles.isEmpty()) {
                return; // Si el equipo está vacío, no abrimos el diálogo
            }

            // Construimos un modelo para la tabla del diálogo
            String[] columns = {"Slot", "Pokémon"};
            DefaultTableModel detalleModel = new DefaultTableModel(columns, 0);
            // Ordenamos por slot (ya viene ordenado de la consulta, pero garantizamos el orden)
            detalles.sort((a, b) -> Integer.compare(a.getId().getNumslot(), b.getId().getNumslot()));
            for (DetalleEquipo detalle : detalles) {
                String pokemonNombre = detalle.getPokemon() != null ? detalle.getPokemon().getNompokemon() : "";
                detalleModel.addRow(new Object[]{detalle.getId().getNumslot(), pokemonNombre});
            }

            JTable detalleTable = new JTable(detalleModel);
            detalleTable.setEnabled(false); // Solo lectura: no se puede interactuar

            // JDialog: ventana modal secundaria (bloquea la ventana padre mientras está abierta)
            JDialog dialog = new JDialog(this, "Detalle de Equipo " + equipoId, true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.add(new JLabel("Pokémons asociados al equipo:"), BorderLayout.NORTH);
            dialog.add(new JScrollPane(detalleTable), BorderLayout.CENTER);

            JButton cerrar = new JButton("Cerrar");
            cerrar.addActionListener(e -> dialog.dispose()); // Cierra el diálogo
            JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelBoton.add(cerrar);
            dialog.add(panelBoton, BorderLayout.SOUTH);

            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this); // Centra el diálogo respecto a esta ventana
            dialog.setVisible(true); // Muestra el diálogo (bloquea hasta que se cierra)
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles de equipo: " + ex.getMessage());
        }
    }

    /**
     * crearEquipo(): Crea un nuevo Equipo asociado a un Entrenador.
     *
     * VALIDACIÓN ESPECIAL — entrenadorYaTieneEquipo():
     *   Un Entrenador solo puede tener UN Equipo (relación 1:1).
     *   Antes de crear, verificamos que el Entrenador seleccionado no tenga ya un Equipo.
     *   Si lo tiene, mostramos el error sin llegar a hacer el INSERT.
     *
     * FLUJO:
     *   1. Valida ID y Entrenador seleccionado
     *   2. Verifica que el Entrenador no tenga ya un Equipo
     *   3. new Equipo(entrenador) → constructor con Entrenador
     *   4. equipo.setCodequipo(id) → asigna la PK
     *   5. controller.create(equipo) → INSERT en BD
     */
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
        // Validación de regla de negocio: 1 Entrenador → 1 Equipo (máximo)
        if (entrenadorYaTieneEquipo(entrenador)) {
            setMensaje("El entrenador seleccionado ya tiene un equipo asociado");
            JOptionPane.showMessageDialog(this, "El entrenador seleccionado ya tiene un equipo asociado");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Equipo equipo = new Equipo(entrenador); // Constructor con Entrenador
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

    /**
     * actualizarEquipo(): Cambia el Entrenador asignado a un Equipo.
     *
     * VALIDACIÓN CON EXCLUSIÓN:
     *   Llama a entrenadorYaTieneEquipo(entrenador, id) con el ID del equipo actual.
     *   Así, si el Entrenador ya tiene el Equipo que estamos editando, no falla
     *   (solo falla si tiene OTRO equipo distinto).
     *
     * FLUJO:
     *   1. findById(id) → obtiene el Equipo managed de la BD
     *   2. equipo.setEntrenador(entrenador) → cambia el Entrenador (actualiza FK)
     *   3. controller.update(equipo) → merge() + commit → UPDATE en BD
     */
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
                // Verificamos que el Entrenador no tenga otro Equipo (excluyendo el actual)
                if (entrenadorYaTieneEquipo(entrenador, id)) {
                    setMensaje("El entrenador seleccionado ya tiene un equipo distinto asociado");
                    JOptionPane.showMessageDialog(this, "El entrenador seleccionado ya tiene un equipo distinto asociado");
                    return;
                }
                equipo.setEntrenador(entrenador); // Actualiza la relación 1:1
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

    /**
     * eliminarEquipo(): Elimina el Equipo indicado.
     *
     * VERIFICACIÓN DE INTEGRIDAD:
     *   Si el Equipo tiene Pokémons (DetalleEquipo no vacío), no se puede borrar.
     *   equipo.getDetallesEquipo() devuelve la colección cargada por JPA.
     *   Si no está vacía, muestra aviso y cancela.
     *
     *   El usuario debe primero quitar todos los Pokémons del equipo
     *   desde DetalleEquipoView antes de poder eliminar el Equipo.
     */
    private void eliminarEquipo() {
        try {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un equipo para eliminar");
                return;
            }

            int id = Integer.parseInt(idStr);

            // Integridad: no borrar si tiene Pokémons asociados (DetalleEquipo)
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

    /**
     * limpiarFormulario(): Resetea el formulario al estado inicial.
     */
    private void limpiarFormulario() {
        txtId.setText("");
        comboEntrenador.setSelectedIndex(-1); // Sin entrenador seleccionado
        table.clearSelection();
        clearMensaje();
    }
}