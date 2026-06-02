package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Clase que representa la clave compuesta de la tabla DetalleEquipo
 * Esta tabla de unión relaciona Equipos con Pokémons, permitiendo que
 * cada equipo tenga múltiples Pokémons en diferentes slots (posiciones)
 * 
 * La clave primaria está formada por:
 * - codequipo (código del equipo)
 * - numslot (posición/slot del Pokémon en el equipo, 1-6)
 */
@Embeddable
public class DetalleEquipoPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Primer componente: código del equipo
    private int codequipo;
    
    // Segundo componente: número de slot (posición en el equipo 1-6)
    private int numslot;

    // Constructor vacío (requerido por JPA)
    public DetalleEquipoPK() {
    }

    // Constructor completo
    public DetalleEquipoPK(int codequipo, int numslot) {
        this.codequipo = codequipo;
        this.numslot = numslot;
    }

    // Getters y Setters
    public int getCodequipo() {
        return codequipo;
    }

    public void setCodequipo(int codequipo) {
        this.codequipo = codequipo;
    }

    public int getNumslot() {
        return numslot;
    }

    public void setNumslot(int numslot) {
        this.numslot = numslot;
    }

    // equals() y hashCode() OBLIGATORIOS para claves embebidas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetalleEquipoPK that = (DetalleEquipoPK) o;

        if (codequipo != that.codequipo) return false;
        return numslot == that.numslot;
    }

    @Override
    public int hashCode() {
        int result = codequipo;
        result = 31 * result + numslot;
        return result;
    }
}
