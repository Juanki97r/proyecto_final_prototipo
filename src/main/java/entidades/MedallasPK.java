package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * CLASE MEDALLASP (Clave Primaria Compuesta para Medallas)
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa la clave primaria compuesta de la tabla MEDALLAS.
 * 
 * EN BASES DE DATOS:
 *   PRIMARY KEY (identrenador, codgym)
 *   Significa: Un Entrenador solo puede ganar UNA medalla en cada Gimnasio
 *   Pero un Entrenador puede tener medallas en múltiples Gimnasios
 * 
 * EN JPA:
 *   • @Embeddable: Indica que esta clase se "embeberá" en otra entidad
 *   • Implements Serializable: Requerido por JPA para claves embebidas
 *   • serialVersionUID: Identificador de versión para serialización
 * 
 * PATRÓN JPA - CLAVES PRIMARIAS COMPUESTAS:
 *   1. Crear clase con @Embeddable
 *   2. Implementar Serializable
 *   3. Incluir los componentes de la clave como atributos
 *   4. En la entidad principal, crear un campo con @EmbeddedId
 *   5. Crear getters/setters para cada componente (usados por @JoinColumn)
 * 
 * ALTERNATIVA: @IdClass (menos común en este proyecto)
 */
@Embeddable
public class MedallasPK implements Serializable {
    
    /**
     * serialVersionUID: ID para serialización Java
     * Requerido porque implementamos Serializable
     * Cambiar este número si modificas la estructura de la clase
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * IDENTRENADOR: Primer componente de la clave
     * Referencia a ENTRENADOR.identrenador
     * Especifica QUÉ Entrenador ganó la medalla
     */
    private int identrenador;
    
    /**
     * CODGYM: Segundo componente de la clave
     * Referencia a GIMNASIO.codgym
     * Especifica EN QUÉ Gimnasio se ganó la medalla
     */
    private int codgym;

    /**
     * Constructor sin parámetros
     * Requerido por JPA
     */
    public MedallasPK() {
    }

    /**
     * Constructor completo
     * Facilita la creación de instancias:
     *   new MedallasPK(1, 5) → Entrenador 1 ganó medalla en Gimnasio 5
     */
    public MedallasPK(int identrenador, int codgym) {
        this.identrenador = identrenador;
        this.codgym = codgym;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GETTER: Devuelve el ID del Entrenador
     * @return Identificador del Entrenador (primer componente de la clave)
     */
    public int getIdentrenador() {
        return identrenador;
    }

    /**
     * SETTER: Asigna el ID del Entrenador
     * @param identrenador El nuevo ID del Entrenador
     */
    public void setIdentrenador(int identrenador) {
        this.identrenador = identrenador;
    }

    /**
     * GETTER: Devuelve el código del Gimnasio
     * @return Código del Gimnasio (segundo componente de la clave)
     */
    public int getCodgym() {
        return codgym;
    }

    /**
     * SETTER: Asigna el código del Gimnasio
     * @param codgym El nuevo código del Gimnasio
     */
    public void setCodgym(int codgym) {
        this.codgym = codgym;
    }
}
    }

    // equals() y hashCode() son OBLIGATORIOS para claves embebidas
    // JPA usa estos métodos para comparar y gestionar las claves
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedallasPK that = (MedallasPK) o;

        if (identrenador != that.identrenador) return false;
        return codgym == that.codgym;
    }

    @Override
    public int hashCode() {
        int result = identrenador;
        result = 31 * result + codgym;
        return result;
    }
}
