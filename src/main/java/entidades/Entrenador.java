package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║                        CLASE: ENTRENADOR                                   ║
 * ║  Representa un Entrenador Pokémon que captura y entrena Pokémons.          ║
 * ║  Cada entrenador tiene UN equipo de hasta 6 Pokémons y puede tener         ║
 * ║  múltiples medallas obtenidas en diferentes gimnasios.                     ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 * 
 * MAPEO A BASE DE DATOS:
 *   - Mapea la tabla "ENTRENADOR" de la base de datos
 *   - @Entity: Indica que esta clase es una entidad JPA que se persiste
 *   - @Table(name = "Entrenador"): Especifica el nombre exacto de la tabla
 * 
 * CONTEXTO EN LA APLICACIÓN:
 *   - Es una entidad central (junto con Pokémon, Equipo, etc.)
 *   - Se gestiona a través de EntrenadorView (interfaz gráfica)
 *   - Las operaciones CRUD (Create, Read, Update, Delete) se realizan mediante EntrenadorController
 * 
 * PATRÓN DE RELACIONES:
 *   - Un Entrenador tiene 1 Equipo → Relación ONE-TO-ONE
 *   - Un Entrenador tiene múltiples Medallas → Relación ONE-TO-MANY
 *   - El Equipo tiene múltiples Pokémons a través de DetalleEquipo
 */
@Entity
@Table(name = "Entrenador")
public class Entrenador {
    
    /**
     * IDENTRENADOR: Clave primaria (Primary Key)
     * - Es un identificador único para cada entrenador
     * - @Id: Marca este atributo como clave primaria
     * - Se asigna manualmente (no es auto-incremental)
     * - Tipo: INT en la base de datos
     */
    @Id
    @Column(name = "identrenador")
    private int identrenador;
    
    /**
     * NOMBRE: Nombre del entrenador
     * - @Column(name = "nombre", length = 20, nullable = false)
     *   * name: Nombre exacto de la columna en la BD
     *   * length = 20: VARCHAR(20) - máximo 20 caracteres
     *   * nullable = false: Campo obligatorio (NOT NULL)
     * - Ejemplos: "Ash", "Misty", "Brock", etc.
     */
    @Column(name = "nombre", length = 20, nullable = false)
    private String nombre;
    
    /**
     * EDAD: Edad del entrenador
     * - @Column(name = "edad"): Sin restricciones adicionales
     * - Tipo Integer (no int): Permite valores nulos (NULL)
     * - Útil para entrenadores sin edad definida
     */
    @Column(name = "edad")
    private Integer edad;
    
    /**
     * EQUIPO: Relación UNO-A-UNO con Equipo
     * 
     * EXPLICACIÓN DE LA RELACIÓN:
     *   - Un Entrenador tiene exactamente UN Equipo
     *   - Un Equipo pertenece a exactamente UN Entrenador
     *   - Esta es una relación BIDIRECCIONAL:
     *     * El lado "propietario" es Equipo (tiene la columna FK)
     *     * Este lado es "mappedBy" (lado inverso)
     * 
     * @OneToOne(mappedBy = "entrenador", cascade = CascadeType.REMOVE, orphanRemoval = true)
     *   - mappedBy = "entrenador": La relación se define en Equipo.entrenador
     *   - cascade = CascadeType.REMOVE: Si borro el Entrenador → borro su Equipo automáticamente
     *   - orphanRemoval = true: Si desvinculo el Equipo → lo borro de la BD
     * 
     * PATRÓN: Cuando cargamos un Entrenador, Hibernate carga automáticamente su Equipo
     */
    @OneToOne(mappedBy = "entrenador", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Equipo equipo;
    
    /**
     * MEDALLAS: Relación UNO-A-MUCHOS con Medallas
     * 
     * EXPLICACIÓN:
     *   - Un Entrenador puede tener MUCHOS registros de medallas
     *   - Cada medalla está registrada en la tabla MEDALLAS
     *   - Medallas es una tabla de unión que vincula Entrenadores con Gimnasios
     * 
     * @OneToMany(mappedBy = "entrenador")
     *   - mappedBy = "entrenador": La relación se define en Medallas.entrenador
     *   - NO tiene cascade REMOVE: Queremos preservar el historial de medallas
     *   - Cuando borramos un entrenador, sus medallas permanecen (registro histórico)
     * 
     * EJEMPLO: El Entrenador "Ash" puede tener 8 registros en la tabla MEDALLAS
     *   (una por cada gimnasio donde ganó una medalla)
     */
    @OneToMany(mappedBy = "entrenador")
    private Collection<Medallas> medallas;


    // ════════════════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ════════════════════════════════════════════════════════════════════════════════

    /**
     * Constructor vacío (sin parámetros)
     * OBLIGATORIO en JPA: El framework necesita poder instanciar entidades sin argumentos.
     * Este constructor se usa internamente cuando Hibernate recupera datos de la BD.
     */
    public Entrenador() {
    }

    /**
     * Constructor con parámetros (usa frecuentemente en la aplicación)
     * Facilita la creación de nuevos Entrenador desde la interfaz gráfica.
     * NO incluye 'identrenador' porque se espera que el usuario lo introduzca manualmente.
     */
    public Entrenador(String nombre, Integer edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    // ════════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS (Accesores)
    // ════════════════════════════════════════════════════════════════════════════════

    /**
     * GETTER para identrenador
     * Devuelve el ID único del entrenador.
     * @return El identificador del entrenador
     */
    public int getIdentrenador() {
        return identrenador;
    }

    /**
     * SETTER para identrenador
     * Asigna el ID del entrenador. Generalmente se llama UNA SOLA VEZ (en creación).
     * @param identrenador El nuevo ID del entrenador
     */
    public void setIdentrenador(int identrenador) {
        this.identrenador = identrenador;
    }

    /**
     * GETTER para nombre
     * @return El nombre del entrenador
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * SETTER para nombre
     * Permite cambiar el nombre del entrenador (operación UPDATE).
     * @param nombre El nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * GETTER para edad
     * @return La edad del entrenador (puede ser null si no se especificó)
     */
    public Integer getEdad() {
        return edad;
    }

    /**
     * SETTER para edad
     * @param edad La nueva edad del entrenador
     */
    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    /**
     * GETTER para equipo
     * Devuelve el Equipo de Pokémons asociado a este entrenador.
     * IMPORTANTE: Esta es una relación lazy-loaded (se carga cuando se accede).
     * @return El Equipo del entrenador, o null si no tiene equipo
     */
    public Equipo getEquipo() {
        return equipo;
    }

    /**
     * SETTER para equipo
     * Asigna un Equipo al entrenador.
     * @param equipo El nuevo Equipo
     */
    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    /**
     * GETTER para medallas
     * Devuelve la colección de medallas obtenidas por el entrenador.
     * @return Collection de Medallas (puede ser null o vacía)
     */
    public Collection<Medallas> getMedallas() {
        return medallas;
    }

    /**
     * SETTER para medallas
     * Reemplaza toda la colección de medallas.
     * @param medallas La nueva colección de medallas
     */
    public void setMedallas(Collection<Medallas> medallas) {
        this.medallas = medallas;
    }

    // ════════════════════════════════════════════════════════════════════════════════
    // MÉTODO toString()
    // ════════════════════════════════════════════════════════════════════════════════

    /**
     * Devuelve una representación en texto del Entrenador.
     * Se usa en:
     *   - JComboBox y JList (mostrar datos en listas desplegables)
     *   - Depuración y logs
     *   - Interfaz gráfica
     * @return El nombre del entrenador, o "Entrenador sin nombre" si es nulo
     */
    @Override
    public String toString() {
        return nombre != null ? nombre : "Entrenador sin nombre";
    }
}
