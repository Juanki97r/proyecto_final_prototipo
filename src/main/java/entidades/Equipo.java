package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * CLASE EQUIPO
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa el Equipo de Pokémons de un Entrenador.
 * Mapea la tabla EQUIPO de la base de datos.
 * 
 * IMPORTANTE:
 *   • Cada Entrenador tiene EXACTAMENTE 1 Equipo (relación 1:1)
 *   • Un Equipo contiene HASTA 6 Pokémons (especificado en DetalleEquipo.numslot)
 *   • El propietario de la relación 1:1 es esta clase (contiene la FK a Entrenador)
 * 
 * FLUJO EN LA APLICACIÓN:
 *   1. Creas un Entrenador
 *   2. Se crea automáticamente un Equipo para ese Entrenador
 *   3. Añades Pokémons al Equipo a través de DetalleEquipo (hasta 6)
 */
@Entity
@Table(name = "Equipo")
public class Equipo {
    
    /**
     * CODEQUIPO: Código del Equipo (clave primaria)
     * Identificador único para cada equipo
     */
    @Id
    @Column(name = "codequipo")
    private int codequipo;
    
    /**
     * ENTRENADOR: Relación UNO-A-UNO con Entrenador
     * 
     * EXPLICACIÓN:
     *   • Cada Equipo pertenece a EXACTAMENTE 1 Entrenador
     *   • Cada Entrenador tiene EXACTAMENTE 1 Equipo
     *   • Este lado CONTIENE la columna de clave foránea (identrenador)
     * 
     * @OneToOne(optional = false)
     *   - optional = false: Un Equipo DEBE estar vinculado a un Entrenador
     * 
     * @JoinColumn(name = "identrenador", nullable = false, unique = true)
     *   - name = "identrenador": Nombre de la columna FK en la tabla EQUIPO
     *   - nullable = false: Campo obligatorio
     *   - unique = true: Solo UN Equipo por Entrenador (refuerza la relación 1:1)
     * 
     * RELACIÓN INVERSA: En Entrenador.equipo (mappedBy = "entrenador")
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "identrenador", nullable = false, unique = true)
    private Entrenador entrenador;
    
    /**
     * DETALLES EQUIPO: Los Pokémons en este Equipo (1:M)
     * 
     * Un Equipo → MÚLTIPLES Pokémons (hasta 6)
     * 
     * La tabla DETALLEEQUIPO especifica:
     *   {codequipo, numslot (1-6)} ──> numpokedex
     * 
     * cascade = CascadeType.REMOVE: Si borro el Equipo → borro todos los detalles
     * (es decir, se varía el equipo pero preservamos los Pokémons)
     */
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.REMOVE)
    private Collection<DetalleEquipo> detallesEquipo;

    /** Constructor sin parámetros (requerido por JPA) */
    public Equipo() {
    }

    /** Constructor con Entrenador */
    public Equipo(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Código del Equipo */
    public int getCodequipo() {
        return codequipo;
    }

    /** SETTER: Asigna código del Equipo */
    public void setCodequipo(int codequipo) {
        this.codequipo = codequipo;
    }

    /** GETTER: Devuelve el Entrenador propietario del Equipo */
    public Entrenador getEntrenador() {
        return entrenador;
    }

    /** SETTER: Asigna un Entrenador al Equipo */
    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    /** GETTER: Devuelve los Pokémons en este Equipo */
    public Collection<DetalleEquipo> getDetallesEquipo() {
        return detallesEquipo;
    }

    /** SETTER: Reemplaza la colección de Pokémons */
    public void setDetallesEquipo(Collection<DetalleEquipo> detallesEquipo) {
        this.detallesEquipo = detallesEquipo;
    }

    /**
     * Representación en texto
     * @return "Equipo X - Nombre_Entrenador" o "Equipo X"
     */
    @Override
    public String toString() {
        return "Equipo " + codequipo + (entrenador != null && entrenador.getNombre() != null ? " - " + entrenador.getNombre() : "");
    }
}
