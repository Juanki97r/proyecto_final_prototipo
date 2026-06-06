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
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              CLASE: GIMNASIOVIEW                                           ║
 * ║   Interfaz gráfica para la gestión CRUD de Gimnasios                       ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * PATRÓN MVC — Capa Vista:
 *   • Hereda de JFrame → ventana Swing independiente
 *   • Usa DOS controladores: GimnasioController (CRUD) y RegionController (para el ComboBox)
 *   • Se abre desde MainWindow al pulsar "Gestionar Gimnasios"
 *
 * DIFERENCIA CON VISTAS SIMPLES:
 *   Esta vista usa un JComboBox<Region> para seleccionar la Región del Gimnasio.
 *   Necesita cargar las Regiones de la BD al iniciarse (cargarRegiones()).
 *   Esto introduce el concepto de "FK gestionada como ComboBox" en la UI.
 *
 * CAMPOS DEL FORMULARIO:
 *   • ID          → codgym (clave primaria)
 *   • Región      → JComboBox con objetos Region (muestra Region.toString())
 *   • Nom. Medalla → nomMedalla (opcional, VARCHAR 20)
 *   • Tipo Medalla → tipoMedalla (obligatorio, VARCHAR 20)
 *
 * RESTRICCIÓN AL ELIMINAR:
 *   Un Gimnasio no puede borrarse si tiene Medallas registradas (historial de victorias).
 *   La vista verifica gimnasio.getMedallas() antes de llamar a delete().
 */
public class GimnasioView extends JFrame {
 /** Controlador principal: operaciones CRUD sobre la tabla GIMNASIO */
    private GimnasioController controller;
    /**
     * regionController: Controlador auxiliar para cargar las Regiones.
     * Se usa SOLO para llenar el JComboBox, no para gestionar Regiones.
     * La gestión de Regiones se hace desde RegionView.
     */
    private RegionController regionController;
    private JTable table;
    private DefaultTableModel tableModel;
 /**
     * txtId         → codgym (clave primaria del Gimnasio)
     * txtNomMedalla → nombre de la medalla que otorga (opcional)
     * txtTipoMedalla→ tipo elemental de la medalla (obligatorio)
     */

    private JTextField txtId, txtNomMedalla, txtTipoMedalla;
    /**
     * comboRegion: JComboBox<Region> para seleccionar la Región del Gimnasio.
     *
     * ¿Por qué JComboBox<Region> y no JComboBox<String>?
     *   • Almacena objetos Region completos, no solo nombres
     *   • Al crear/actualizar, obtenemos la Region con getSelectedItem()
     *   • Swing usa Region.toString() para mostrar el texto en el combo
     *   • Así pasamos directamente el objeto Region al Gimnasio sin buscar por ID
     */
    
    private JComboBox<Region> comboRegion;
    
    private JButton btnCrear, btnActualizar, btnEliminar, btnLimpiar;
/**
     * Constructor: Inicializa la ventana de gestión de Gimnasios.
     *
     * Se crean DOS controladores porque necesitamos:
     *   1. GimnasioController → para operaciones CRUD del Gimnasio
     *   2. RegionController → para cargar las Regiones en el ComboBox
     */
    public GimnasioView() {
        controller = new GimnasioController();
        regionController = new RegionController();

        setTitle("Gestión de Gimnasios");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarDatos();
    }
/**
     * initComponents(): Construye todos los componentes y asigna listeners.
     *
     * FORMULARIO con GridLayout 4x2 (más filas que en las vistas simples):
     *   Fila 1: ID (clave primaria)
     *   Fila 2: Región (JComboBox)
     *   Fila 3: Nombre Medalla
     *   Fila 4: Tipo Medalla
     */
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
         // El JComboBox se llena con objetos Region.
        // Swing llama Region.toString() para mostrar el texto.
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
/**
     * cargarRegiones(): Llena el JComboBox con todas las Regiones de la BD.
     *
     * Se llama UNA VEZ al inicio (en initComponents).
     * Los objetos Region se añaden directamente al combo.
     * Swing usa Region.toString() para mostrar el texto de cada opción.
     *
     * PATRÓN:
     *   ComboBox con objetos de entidad permite obtener el objeto completo
     *   con (Region) comboRegion.getSelectedItem() al crear/actualizar.
     */
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
/**
     * cargarDatos(): Recarga la tabla con todos los Gimnasios de la BD.
     *
     * NOTA: La columna "Región" muestra el nombre (nomregion), no el ID.
     * Para eso accedemos a g.getRegion().getNomregion(), navegando la relación JPA.
     * Si por algún motivo la Región es null, mostramos cadena vacía.
     */
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
/**
     * seleccionarGimnasio(): Rellena el formulario con los datos de la fila seleccionada.
     *
     * COMPORTAMIENTO ESPECIAL CON EL COMBOBOX:
     *   La tabla almacena el NOMBRE de la región (col 1), no el objeto Region.
     *   Para seleccionar la Región correcta en el ComboBox, iteramos todos los
     *   items buscando el que tiene ese nombre.
     *
     *   Alternativa más robusta: guardar el ID del gimnasio en la fila y
     *   hacer findById() para obtener la Región directamente.
     *   Esta implementación usa el nombre como clave de búsqueda (más sencilla pero
     *   podría fallar si dos regiones tienen el mismo nombre).
     */
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
/**
     * crearGimnasio(): Crea un nuevo Gimnasio con los datos del formulario.
     *
     * PUNTOS CLAVE:
     *   • Region region = (Region) comboRegion.getSelectedItem()
     *     → Obtiene el objeto Region seleccionado en el ComboBox (cast necesario)
     *   • nomMedalla puede ser null (campo opcional)
     *     → Ternario: nomMedalla.isEmpty() ? null : nomMedalla
     *   • new Gimnasio(nomMedalla, tipoMedalla, region) → constructor completo
     *   • gimnasio.setCodgym(id) → asigna la PK manualmente
     */
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
/**
     * actualizarGimnasio(): Actualiza los datos del Gimnasio seleccionado.
     *
     * Se pueden cambiar: la Región, el nombre de la medalla y el tipo.
     * El ID (codgym) no puede modificarse.
     *
     * FLUJO:
     *   1. findById(id) → obtiene el Gimnasio managed de la BD
     *   2. gimnasio.setRegion(region) → actualiza la FK
     *   3. gimnasio.setNomMedalla(...) / setTipoMedalla(...) → actualiza campos
     *   4. controller.update(gimnasio) → merge() + commit → UPDATE en BD
     */
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
/**
     * eliminarGimnasio(): Elimina el Gimnasio indicado.
     *
     * VERIFICACIÓN DE INTEGRIDAD:
     *   Si el Gimnasio tiene Medallas registradas (historial de victorias de entrenadores),
     *   no se puede borrar. Se consulta gimnasio.getMedallas() para comprobarlo.
     *   Si no está vacía, muestra aviso y cancela la operación.
     *
     *   Nota: aunque la entidad Gimnasio tiene cascade=REMOVE sobre Medallas,
     *   la vista hace esta verificación explícita para que el usuario sea
     *   consciente de la acción destructiva.
     */
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
 /**
     * limpiarFormulario(): Resetea el formulario al estado inicial.
     *
     * comboRegion.setSelectedIndex(-1) → deselecciona la región (sin selección)
     */
    private void limpiarFormulario() {
        txtId.setText("");
        comboRegion.setSelectedIndex(-1);
        txtNomMedalla.setText("");
        txtTipoMedalla.setText("");
        table.clearSelection();
    }
}