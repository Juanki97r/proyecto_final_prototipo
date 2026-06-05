package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * CLASE DETALLEPOKEMONPK (Clave Primaria Compuesta para DetallePokemon)
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa la clave primaria compuesta de la tabla DETALLEPOKEMON.
 * 
 * ESTRUCTURA:
 *   PRIMARY KEY (numpokedex, codtipo)
 *   Significa: Un Pokémon tiene UNA sola entrada por Tipo
 *   Un Pokémon puede tener múltiples tipos, pero no duplicados
 * 
 * EJEMPLO DE DATOS:
 *   - {numpokedex: 6, codtipo: 10} → Charizard tiene Fuego
 *   - {numpokedex: 6, codtipo: 3}  → Charizard tiene Volador
 *   - {numpokedex: 25, codtipo: 13} → Pikachu tiene Eléctrico
 * 
 * PATRÓN @Embeddable:
 *   Esta clase se embeberá en DetallePokemon mediante @EmbeddedId
 *   @Embeddable: Indica que se puede embeberI en otra entidad
 *   Implements Serializable: Requerido por JPA
 */
@Embeddable
public class DetallePokemonPK implements Serializable {
    
    /**
     * serialVersionUID: ID para serialización
     * Requerido cuando implements Serializable
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * NUMPOKEDEX: Número de Pokédex (primer componente)
     * Referencia a POKEMON.numpokedex
     * Especifica QUÉ Pokémon tiene el tipo
     */
    private int numpokedex;
    
    /**
     * CODTIPO: Código de Tipo (segundo componente)
     * Referencia a TIPO.codtipo
     * Especifica QUÉ tipo tiene el Pokémon
     */
    private int codtipo;

    /** Constructor sin parámetros (requerido por JPA) */
    public DetallePokemonPK() {
    }

    /**
     * Constructor completo
     * Facilita la creación:
     *   new DetallePokemonPK(6, 10) → Charizard tiene Fuego
     */
    public DetallePokemonPK(int numpokedex, int codtipo) {
        this.numpokedex = numpokedex;
        this.codtipo = codtipo;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Número de Pokédex */
    public int getNumpokedex() {
        return numpokedex;
    }

    /** SETTER: Asigna número de Pokédex */
    public void setNumpokedex(int numpokedex) {
        this.numpokedex = numpokedex;
    }

    /** GETTER: Código de Tipo */
    public int getCodtipo() {
        return codtipo;
    }

    /** SETTER: Asigna código de Tipo */
    public void setCodtipo(int codtipo) {
        this.codtipo = codtipo;
    }
}

    // equals() y hashCode() OBLIGATORIOS para claves embebidas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetallePokemonPK that = (DetallePokemonPK) o;

        if (numpokedex != that.numpokedex) return false;
        return codtipo == that.codtipo;
    }

    @Override
    public int hashCode() {
        int result = numpokedex;
        result = 31 * result + codtipo;
        return result;
    }
}
