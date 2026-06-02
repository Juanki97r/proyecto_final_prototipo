package entidades;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Clase que representa la clave compuesta (primary key) de la tabla Medallas
 * Esta tabla tiene una clave primaria formada por dos campos:
 * - identrenador (referencia a Entrenador)
 * - codgym (referencia a Gimnasio)
 * 
 * Implementamos Serializable porque JPA requiere que las claves embebidas 
 * lo implementen para poder ser persistidas correctamente
 */
@Embeddable
public class MedallasPK implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Primer componente de la clave: identificador del entrenador
    private int identrenador;
    
    // Segundo componente de la clave: código del gimnasio
    private int codgym;

    // Constructor vacío (requerido por JPA)
    public MedallasPK() {
    }

    // Constructor completo para facilitar la creación de instancias
    public MedallasPK(int identrenador, int codgym) {
        this.identrenador = identrenador;
        this.codgym = codgym;
    }

    // Getters y Setters
    public int getIdentrenador() {
        return identrenador;
    }

    public void setIdentrenador(int identrenador) {
        this.identrenador = identrenador;
    }

    public int getCodgym() {
        return codgym;
    }

    public void setCodgym(int codgym) {
        this.codgym = codgym;
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
