package entidades;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * CLASE MEDALLAS
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa el registro histórico de una Medalla obtenida por un Entrenador en un Gimnasio.
 * Mapea la tabla MEDALLAS de la base de datos.
 * 
 * PROPÓSITO:
 *   Esta es una TABLA DE UNIÓN que registra: {Entrenador X ganó Medalla en Gimnasio Y en fecha Z}
 *   Permite que múltiples Entrenadores ganen la misma Medalla
 *   Registra CUÁNDO se ganó cada medalla
 * 
 * ESTRUCTURA:
 *   • Clave primaria compuesta: {identrenador, codgym} → Un Entrenador solo puede ganar
 *     UNA medalla por Gimnasio (aunque puede tener varias en diferentes gimnasios)
 *   • Campo adicional: fecha (cuándo se ganó)
 * 
 * EJEMPLO DE DATOS:
 *   - {Entrenador: Ash, Gimnasio: Pewter City, Fecha: 2023-05-15}
 *   - {Entrenador: Ash, Gimnasio: Cerulean City, Fecha: 2023-05-20}
 *   - {Entrenador: Misty, Gimnasio: Pewter City, Fecha: 2023-06-20}
 */
@Entity
@Table(name = "Medallas")
public class Medallas {
    
    /**
     * ID: Clave primaria compuesta
     * 
     * Usamos @EmbeddedId para indicar que la clave está embebida en otra clase.
     * La clase MedallasPK contiene los componentes: {identrenador, codgym}
     * 
     * PATRÓN JPA: Cuando tienes clave primaria compuesta:
     *   1. Creas una clase anotada con @Embeddable (MedallasPK)
     *   2. Colocas un campo de tipo MedallasPK anotado con @EmbeddedId
     *   3. Creas un getter/setter para cada componente usando @Column
     */
    @EmbeddedId
    private MedallasPK id;
    
    /**
     * FECHA: Cuándo se ganó la Medalla
     * 
     * LocalDate: Tipo específico para fechas (sin hora)
     * @Column(name = "fecha"): Mapea a la columna DATE en la BD
     * Puede ser nula (nullable por defecto)
     * 
     * PATRÓN: Registra datos históricos/auditables
     */
    @Column(name = "fecha")
    private LocalDate fecha;
    
    /**
     * ENTRENADOR: Relación MUCHOS-A-UNO con Entrenador
     * 
     * Muchos registros de Medallas pertenecen a UN Entrenador
     * 
     * @ManyToOne: Un Entrenador puede tener múltiples Medallas
     * @JoinColumn(name = "identrenador", insertable=false, updatable=false)
     *   - insertable=false, updatable=false: Esta FK viene del id embebido (MedallasPK)
     *   - JPA sincronizará automáticamente este campo con id.identrenador
     * 
     * RELACIÓN INVERSA: En Entrenador.medallas (OneToMany mappedBy="entrenador")
     */
    @ManyToOne
    @JoinColumn(name = "identrenador", insertable = false, updatable = false)
    private Entrenador entrenador;
    
    /**
     * GIMNASIO: Relación MUCHOS-A-UNO con Gimnasio
     * 
     * Muchos registros de Medallas pertenecen a UN Gimnasio
     * 
     * @ManyToOne: Un Gimnasio puede tener múltiples registros de Medallas
     * @JoinColumn(name = "codgym", insertable=false, updatable=false)
     *   - insertable=false, updatable=false: Esta FK viene del id embebido (MedallasPK)
     *   - JPA sincronizará automáticamente este campo con id.codgym
     * 
     * RELACIÓN INVERSA: En Gimnasio.medallas (OneToMany mappedBy="gimnasio")
     */
    @ManyToOne
    @JoinColumn(name = "codgym", insertable = false, updatable = false)
    private Gimnasio gimnasio;

    /** Constructor sin parámetros (requerido por JPA) */
    public Medallas() {
    }

    /** Constructor completo */
    public Medallas(MedallasPK id, LocalDate fecha, Entrenador entrenador, Gimnasio gimnasio) {
        this.id = id;
        this.fecha = fecha;
        this.entrenador = entrenador;
        this.gimnasio = gimnasio;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Devuelve la clave primaria compuesta */
    public MedallasPK getId() {
        return id;
    }

    /** SETTER: Asigna la clave primaria compuesta */
    public void setId(MedallasPK id) {
        this.id = id;
    }

    /** GETTER: Devuelve la fecha de obtención de la Medalla */
    public LocalDate getFecha() {
        return fecha;
    }

    /** SETTER: Actualiza la fecha */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /** GETTER: Devuelve el Entrenador que ganó la Medalla */
    public Entrenador getEntrenador() {
        return entrenador;
    }

    /** SETTER: Asigna el Entrenador */
    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    /** GETTER: Devuelve el Gimnasio donde se ganó la Medalla */
    public Gimnasio getGimnasio() {
        return gimnasio;
    }

    /** SETTER: Asigna el Gimnasio */
    public void setGimnasio(Gimnasio gimnasio) {
        this.gimnasio = gimnasio;
    }
}
