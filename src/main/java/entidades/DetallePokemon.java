package entidades;

import javax.persistence.*;

/**
 * Entidad que representa la relación entre un Pokémon y sus Tipos
 * Mapea la tabla DETALLEPOKEMON de la base de datos
 * Esta es una tabla de unión que especifica qué tipos tiene cada Pokémon
 * (un Pokémon puede tener 1 o 2 tipos)
 */
@Entity
@Table(name = "DetallePokemon")
public class DetallePokemon {
    
    // CLAVE PRIMARIA COMPUESTA
    // Usamos @EmbeddedId para indicar que la clave está compuesta por múltiples atributos
    // La clase DetallePokemonPK contiene los dos componentes: numpokedex y codtipo
    @EmbeddedId
    private DetallePokemonPK id;
    
    // RELACIÓN MANY-TO-ONE con POKEMON
    // Muchos detalles pertenecen a UN Pokémon
    // @JoinColumn("numpokedex") especifica que este atributo de la clave foránea 
    // viene del id embebido DetallePokemonPK
    // insertable=false, updatable=false: JPA sincronizará automáticamente con id.numpokedex
    @ManyToOne
    @JoinColumn(name = "numpokedex", insertable = false, updatable = false)
    private Pokemon pokemon;
    
    // RELACIÓN MANY-TO-ONE con TIPO
    // Muchos detalles pertenecen a UN Tipo
    // @JoinColumn("codtipo") especifica que este atributo de la clave foránea 
    // viene del id embebido DetallePokemonPK
    // insertable=false, updatable=false: JPA sincronizará automáticamente con id.codtipo
    @ManyToOne
    @JoinColumn(name = "codtipo", insertable = false, updatable = false)
    private Tipo tipo;

    // Constructor vacío (requerido por JPA)
    public DetallePokemon() {
    }

    // Constructor con parámetros
    public DetallePokemon(DetallePokemonPK id, Pokemon pokemon, Tipo tipo) {
        this.id = id;
        this.pokemon = pokemon;
        this.tipo = tipo;
    }

    // Getters y Setters
    public DetallePokemonPK getId() {
        return id;
    }

    public void setId(DetallePokemonPK id) {
        this.id = id;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}
