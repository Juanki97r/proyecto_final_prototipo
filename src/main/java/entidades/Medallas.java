package entidades;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa una Medalla obtenida por un Entrenador en un Gimnasio
 * Mapea la tabla MEDALLAS de la base de datos
 * Esta es una tabla de unión que registra cuando un entrenador ganó una medalla específica
 */
@Entity
@Table(name = "Medallas")
public class Medallas {
    
    // CLAVE PRIMARIA COMPUESTA
    // Usamos @EmbeddedId para indicar que la clave está compuesta por múltiples atributos
    // La clase MedallasPK contiene los dos componentes de la clave: identrenador y codgym
    @EmbeddedId
    private MedallasPK id;
    
    // Fecha en que se obtuvo la medalla (DATE, puede ser nula)
    @Column(name = "fecha")
    private LocalDate fecha;
    
    // RELACIÓN MANY-TO-ONE con ENTRENADOR
    // Muchos registros de Medallas pertenecen a UN Entrenador
    // @JoinColumn("identrenador") especifica que este atributo de la clave foránea 
    // viene del id embebido MedallasPK
    // insertable=false, updatable=false indica que no actualicemos este campo manualmente,
    // sino que se sincroniza automáticamente con id.identrenador
    @ManyToOne
    @JoinColumn(name = "identrenador", insertable = false, updatable = false)
    private Entrenador entrenador;
    
    // RELACIÓN MANY-TO-ONE con GIMNASIO
    // Muchos registros de Medallas pertenecen a UN Gimnasio
    // @JoinColumn("codgym") especifica que este atributo de la clave foránea 
    // viene del id embebido MedallasPK
    // insertable=false, updatable=false: JPA sincronizará automáticamente con id.codgym
    @ManyToOne
    @JoinColumn(name = "codgym", insertable = false, updatable = false)
    private Gimnasio gimnasio;

    // Constructor vacío (requerido por JPA)
    public Medallas() {
    }

    // Constructor con parámetros
    public Medallas(MedallasPK id, LocalDate fecha, Entrenador entrenador, Gimnasio gimnasio) {
        this.id = id;
        this.fecha = fecha;
        this.entrenador = entrenador;
        this.gimnasio = gimnasio;
    }

    // Getters y Setters
    public MedallasPK getId() {
        return id;
    }

    public void setId(MedallasPK id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    public Gimnasio getGimnasio() {
        return gimnasio;
    }

    public void setGimnasio(Gimnasio gimnasio) {
        this.gimnasio = gimnasio;
    }
}
