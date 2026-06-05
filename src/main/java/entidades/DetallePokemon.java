package entidades;

import javax.persistence.*;

/**
 * CLASE DETALLEPOKEMON
 * ═════════════════════════════════════════════════════════════════════════════════
 * Tabla de unión que especifica qué TIPOS tiene cada POKÉMON.
 * Mapea la tabla DETALLEPOKEMON de la base de datos.
 * 
 * PROPÓSITO:
 *   Implementar relación MUCHOS-A-MUCHOS entre Pokémons y Tipos
 *   Un Pokémon puede tener 1 o 2 tipos (raramente más)
 *   Un Tipo está asociado a múltiples Pokémons
 * 
 * EJEMPLO:
 *   - Charizard (numpokedex: 6) tiene Fuego (codtipo: 10) Y Volador (codtipo: 3)
 *   - Registros en DETALLEPOKEMON:
 *     * {numpokedex: 6, codtipo: 10}
 *     * {numpokedex: 6, codtipo: 3}
 * 
 * CLAVE PRIMARIA COMPUESTA: {numpokedex, codtipo}
 *   - Un Pokémon no puede tener el MISMO tipo dos veces
 */
@Entity
@Table(name = "DetallePokemon")
public class DetallePokemon {
    
    /**
     * ID: Clave primaria compuesta (embebida)
     * 
     * @EmbeddedId: Indica que la clave está en otra clase (DetallePokemonPK)
     * La clase DetallePokemonPK contiene {numpokedex, codtipo}
     */
    @EmbeddedId
    private DetallePokemonPK id;
    
    /**
     * POKEMON: Relación MUCHOS-A-UNO con Pokemon
     * 
     * Muchos detalles apuntan al MISMO Pokémon
     * (cada detalle es un tipo del Pokémon)
     * 
     * @ManyToOne: Un Pokémon tiene múltiples tipos
     * @JoinColumn(name = "numpokedex", insertable = false, updatable = false)
     *   - insertable/updatable = false: La FK viene del id embebido (DetallePokemonPK)
     *   - JPA sincroniza automáticamente con id.numpokedex
     * 
     * RELACIÓN INVERSA: Pokemon.detallesPokemon (OneToMany mappedBy="pokemon")
     */
    @ManyToOne
    @JoinColumn(name = "numpokedex", insertable = false, updatable = false)
    private Pokemon pokemon;
    
    /**
     * TIPO: Relación MUCHOS-A-UNO con Tipo
     * 
     * Muchos detalles apuntan al MISMO Tipo
     * (múltiples Pokémons comparten el mismo tipo)
     * 
     * @ManyToOne: Un Tipo está en múltiples Pokémons
     * @JoinColumn(name = "codtipo", insertable = false, updatable = false)
     *   - insertable/updatable = false: La FK viene del id embebido (DetallePokemonPK)
     *   - JPA sincroniza automáticamente con id.codtipo
     * 
     * RELACIÓN INVERSA: Tipo.detallesPokemon (OneToMany mappedBy="tipo")
     */
    @ManyToOne
    @JoinColumn(name = "codtipo", insertable = false, updatable = false)
    private Tipo tipo;

    /** Constructor sin parámetros (requerido por JPA) */
    public DetallePokemon() {
    }

    /** Constructor completo */
    public DetallePokemon(DetallePokemonPK id, Pokemon pokemon, Tipo tipo) {
        this.id = id;
        this.pokemon = pokemon;
        this.tipo = tipo;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Devuelve la clave primaria compuesta */
    public DetallePokemonPK getId() {
        return id;
    }

    /** SETTER: Asigna la clave primaria compuesta */
    public void setId(DetallePokemonPK id) {
        this.id = id;
    }

    /** GETTER: Devuelve el Pokémon */
    public Pokemon getPokemon() {
        return pokemon;
    }

    /** SETTER: Asigna el Pokémon */
    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    /** GETTER: Devuelve el Tipo */
    public Tipo getTipo() {
        return tipo;
    }

    /** SETTER: Asigna el Tipo */
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}
