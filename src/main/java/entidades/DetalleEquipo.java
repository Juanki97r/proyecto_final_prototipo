package entidades;

import javax.persistence.*;

/**
 * CLASE DETALLEEQUIPO
 * ═════════════════════════════════════════════════════════════════════════════════
 * Tabla de unión que especifica qué POKÉMONS tiene cada EQUIPO y EN QUÉ SLOT (posición).
 * Mapea la tabla DETALLEEQUIPO de la base de datos.
 * 
 * PROPÓSITO:
 *   Implementar relación MUCHOS-A-MUCHOS entre Equipos y Pokémons
 *   Un Equipo tiene de 0 a 6 Pokémons (cada uno en un slot diferente)
 *   Un Pokémon puede estar en múltiples equipos de diferentes entrenadores
 * 
 * EJEMPLO:
 *   Equipo de Ash (codequipo: 1):
 *   - Slot 1: Pikachu (numpokedex: 25)
 *   - Slot 2: Charizard (numpokedex: 6)
 *   - Slot 3: Venusaur (numpokedex: 3)
 * 
 * CLAVE PRIMARIA COMPUESTA: {codequipo, numslot}
 *   - Un Equipo no puede tener TWO Pokémons en el mismo slot
 *   - numslot va de 1 a 6 (posiciones del equipo)
 */
@Entity
@Table(name = "DetalleEquipo")
public class DetalleEquipo {
    
    /**
     * ID: Clave primaria compuesta (embebida)
     * 
     * @EmbeddedId: Indica que la clave está en otra clase (DetalleEquipoPK)
     * La clase DetalleEquipoPK contiene {codequipo, numslot}
     */
    @EmbeddedId
    private DetalleEquipoPK id;
    
    /**
     * EQUIPO: Relación MUCHOS-A-UNO con Equipo
     * 
     * Muchos detalles pertenecen al MISMO Equipo
     * (cada detalle es un Pokémon del equipo)
     * 
     * @ManyToOne: Un Equipo tiene múltiples Pokémons
     * @JoinColumn(name = "codequipo", insertable = false, updatable = false)
     *   - insertable/updatable = false: La FK viene del id embebido (DetalleEquipoPK)
     *   - JPA sincroniza automáticamente con id.codequipo
     * 
     * RELACIÓN INVERSA: Equipo.detallesEquipo (OneToMany mappedBy="equipo")
     */
    @ManyToOne
    @JoinColumn(name = "codequipo", insertable = false, updatable = false)
    private Equipo equipo;
    
    /**
     * POKEMON: Relación MUCHOS-A-UNO con Pokemon
     * 
     * Muchos detalles apuntan a diferentes Pokémons
     * (múltiples equipos pueden tener el mismo Pokémon)
     * 
     * @ManyToOne: Un Pokémon está en múltiples equipos
     * @JoinColumn(name = "numpokedex", nullable = false)
     *   - nullable = false: Cada slot DEBE tener un Pokémon
     *   - numpokedex NO está en la clave primaria (es solo una FK adicional)
     * 
     * RELACIÓN INVERSA: Pokemon.detallesEquipo (OneToMany mappedBy="pokemon")
     */
    @ManyToOne
    @JoinColumn(name = "numpokedex", nullable = false)
    private Pokemon pokemon;

    /** Constructor sin parámetros (requerido por JPA) */
    public DetalleEquipo() {
    }

    /** Constructor completo */
    public DetalleEquipo(DetalleEquipoPK id, Equipo equipo, Pokemon pokemon) {
        this.id = id;
        this.equipo = equipo;
        this.pokemon = pokemon;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Devuelve la clave primaria compuesta */
    public DetalleEquipoPK getId() {
        return id;
    }

    /** SETTER: Asigna la clave primaria compuesta */
    public void setId(DetalleEquipoPK id) {
        this.id = id;
    }

    /** GETTER: Devuelve el Equipo */
    public Equipo getEquipo() {
        return equipo;
    }

    /** SETTER: Asigna el Equipo */
    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    /** GETTER: Devuelve el Pokémon */
    public Pokemon getPokemon() {
        return pokemon;
    }

    /** SETTER: Asigna el Pokémon */
    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }
}
