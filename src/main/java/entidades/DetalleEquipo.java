package entidades;

import javax.persistence.*;

/**
 * Entidad que representa la relación entre un Equipo y los Pokémons que contiene
 * Mapea la tabla DETALLEEQUIPO de la base de datos
 * Esta es una tabla de unión que especifica qué Pokémons tiene cada equipo
 * y en qué posición/slot están (del 1 al 6)
 */
@Entity
@Table(name = "DetalleEquipo")
public class DetalleEquipo {
    
    // CLAVE PRIMARIA COMPUESTA
    // Usamos @EmbeddedId para indicar que la clave está compuesta por múltiples atributos
    // La clase DetalleEquipoPK contiene los dos componentes: codequipo y numslot
    @EmbeddedId
    private DetalleEquipoPK id;
    
    // RELACIÓN MANY-TO-ONE con EQUIPO
    // Muchos detalles pertenecen a UN Equipo
    // @JoinColumn("codequipo") especifica que este atributo de la clave foránea 
    // viene del id embebido DetalleEquipoPK
    // insertable=false, updatable=false: JPA sincronizará automáticamente con id.codequipo
    @ManyToOne
    @JoinColumn(name = "codequipo", insertable = false, updatable = false)
    private Equipo equipo;
    
    // RELACIÓN MANY-TO-ONE con POKEMON
    // Muchos detalles pertenecen a UN Pokémon
    // @JoinColumn("numpokedex") hace referencia a la columna en la tabla de base de datos
    // Esta columna NO está en la clave compuesta, es solo una referencia foránea adicional
    @ManyToOne
    @JoinColumn(name = "numpokedex", nullable = false)
    private Pokemon pokemon;

    // Constructor vacío (requerido por JPA)
    public DetalleEquipo() {
    }

    // Constructor con parámetros
    public DetalleEquipo(DetalleEquipoPK id, Equipo equipo, Pokemon pokemon) {
        this.id = id;
        this.equipo = equipo;
        this.pokemon = pokemon;
    }

    // Getters y Setters
    public DetalleEquipoPK getId() {
        return id;
    }

    public void setId(DetalleEquipoPK id) {
        this.id = id;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }
}
