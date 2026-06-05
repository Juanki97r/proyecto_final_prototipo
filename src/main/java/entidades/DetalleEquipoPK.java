package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * CLASE DETALLEEQUIPOPK (Clave Primaria Compuesta para DetalleEquipo)
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa la clave primaria compuesta de la tabla DETALLEEQUIPO.
 * 
 * ESTRUCTURA:
 *   PRIMARY KEY (codequipo, numslot)
 *   Significa: Un Equipo tiene UN Pokémon en cada slot (1-6)
 *   Un Equipo puede tener de 0 a 6 Pokémons
 * 
 * NUMSLOT: Rango de valores 1-6
 *   - Slot 1 a 6: Las 6 posiciones del equipo
 *   - Cada slot contiene exactamente UN Pokémon
 * 
 * EJEMPLO:
 *   Equipo de Ash (codequipo: 1)
 *   - {1, 1} → Slot 1 (Pikachu)
 *   - {1, 2} → Slot 2 (Charizard)
 *   - {1, 3} → Slot 3 (Venusaur)
 *   - {1, 4} → Slot 4 (vacío)
 *   - {1, 5} → Slot 5 (vacío)
 *   - {1, 6} → Slot 6 (vacío)
 */
@Embeddable
public class DetalleEquipoPK implements Serializable {
    
    /** serialVersionUID: ID para serialización Java */
    private static final long serialVersionUID = 1L;
    
    /**
     * CODEQUIPO: Código del Equipo (primer componente)
     * Referencia a EQUIPO.codequipo
     * Especifica A QUÉ Equipo pertenece el Pokémon
     */
    private int codequipo;
    
    /**
     * NUMSLOT: Número de Slot/Posición (segundo componente)
     * Rango: 1 a 6
     * Especifica EN QUÉ POSICIÓN está el Pokémon en el equipo
     */
    private int numslot;

    /** Constructor sin parámetros (requerido por JPA) */
    public DetalleEquipoPK() {
    }

    /**
     * Constructor completo
     * Facilita la creación:
     *   new DetalleEquipoPK(1, 3) → Equipo 1, Slot 3
     */
    public DetalleEquipoPK(int codequipo, int numslot) {
        this.codequipo = codequipo;
        this.numslot = numslot;
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

    /** GETTER: Número de Slot (1-6) */
    public int getNumslot() {
        return numslot;
    }

    /** SETTER: Asigna número de Slot */
    public void setNumslot(int numslot) {
        this.numslot = numslot;
    }
}
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
