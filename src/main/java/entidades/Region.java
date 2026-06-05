package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * CLASE REGION
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa una Región geográfica en el mundo Pokémon (Kanto, Johto, Hoenn, etc.).
 * Mapea la tabla REGION de la base de datos.
 * 
 * PROPÓSITO: Organizar Gimnasios geográficamente.
 * RELACIÓN: Una Región contiene MÚLTIPLES Gimnasios.
 * 
 * EJEMPLO DE DATOS:
 *   - Región "Kanto" contiene Gimnasios: {Brock, Misty, Blaine, Sabrina, ...}
 *   - Región "Johto" contiene Gimnasios: {Falkner, Bugsy, Whitney, ...}
 */
@Entity
@Table(name = "Region")
public class Region {
    
    /**
     * CODREGION: Código de región (clave primaria)
     * Identificador único para cada región
     */
    @Id
    @Column(name = "codregion")
    private int codregion;
    
    /**
     * NOMREGION: Nombre de la región
     * VARCHAR(20), NOT NULL
     * Ejemplos: "Kanto", "Johto", "Hoenn"
     */
    @Column(name = "nomregion", length = 20, nullable = false)
    private String nomregion;
    
    /**
     * GIMNASIOS: Colección de Gimnasios en esta Región (1:M)
     * 
     * Una Región → MUCHOS Gimnasios
     * Ejemplo: Kanto tiene {Gym de Brock, Gym de Misty, Gym de Blaine, ...}
     * 
     * cascade = CascadeType.REMOVE: Si borro una región → borro todos sus gimnasios
     * (aunque en la práctica esto es arriesgado para datos históricos)
     */
    @OneToMany(mappedBy = "region", cascade = CascadeType.REMOVE)
    private Collection<Gimnasio> gimnasios;

    /**
     * Constructor sin parámetros (requerido por JPA)
     */
    public Region() {
    }

    /**
     * Constructor con nombre de región
     */
    public Region(String nomregion) {
        this.nomregion = nomregion;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Devuelve el código de región */
    public int getCodregion() {
        return codregion;
    }

    /** SETTER: Asigna el código de región */
    public void setCodregion(int codregion) {
        this.codregion = codregion;
    }

    /** GETTER: Devuelve el nombre de la región */
    public String getNomregion() {
        return nomregion;
    }

    /** SETTER: Actualiza el nombre de la región */
    public void setNomregion(String nomregion) {
        this.nomregion = nomregion;
    }

    /** GETTER: Devuelve la colección de Gimnasios en esta región */
    public Collection<Gimnasio> getGimnasios() {
        return gimnasios;
    }

    /** SETTER: Reemplaza la colección de Gimnasios */
    public void setGimnasios(Collection<Gimnasio> gimnasios) {
        this.gimnasios = gimnasios;
    }

    /**
     * Representación en texto
     * @return El nombre de la región
     */
    @Override
    public String toString() {
        return nomregion != null ? nomregion : "Región sin nombre";
    }
}
