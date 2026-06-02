package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Clase que representa la clave compuesta de la tabla DetallePokemon
 * Esta tabla de unión tiene una clave primaria formada por:
 * - numpokedex (referencia a Pokemon)
 * - codtipo (referencia a Tipo)
 * 
 * Esto representa qué tipos tiene cada Pokémon (un Pokémon puede tener varios tipos)
 */
@Embeddable
public class DetallePokemonPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Primer componente: número de Pokédex
    private int numpokedex;
    
    // Segundo componente: código de tipo
    private int codtipo;

    // Constructor vacío (requerido por JPA)
    public DetallePokemonPK() {
    }

    // Constructor completo
    public DetallePokemonPK(int numpokedex, int codtipo) {
        this.numpokedex = numpokedex;
        this.codtipo = codtipo;
    }

    // Getters y Setters
    public int getNumpokedex() {
        return numpokedex;
    }

    public void setNumpokedex(int numpokedex) {
        this.numpokedex = numpokedex;
    }

    public int getCodtipo() {
        return codtipo;
    }

    public void setCodtipo(int codtipo) {
        this.codtipo = codtipo;
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
