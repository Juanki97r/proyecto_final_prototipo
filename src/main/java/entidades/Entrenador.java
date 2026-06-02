package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa un Entrenador de Pokémon
 * Mapea la tabla ENTRENADOR de la base de datos
 */
@Entity
@Table(name = "Entrenador")
public class Entrenador {
    
    // Clave primaria manual
    @Id
    @Column(name = "identrenador")
    private int identrenador;
    
    // Campo de nombre (VARCHAR(20), NOT NULL)
    @Column(name = "nombre", length = 20, nullable = false)
    private String nombre;
    
    // Campo de edad (INT, puede ser nulo)
    @Column(name = "edad")
    private Integer edad;
    
    // RELACIONES BIDIRECCIONALES
    // Un Entrenador puede tener VARIOS Equipos (OneToMany)
    // mappedBy indica que la relación es manejada desde el lado de Equipo mediante su atributo "entrenador"
    // cascade = CascadeType.REMOVE hace que si se borra un entrenador, se borren todos sus equipos
    @OneToMany(mappedBy = "entrenador", cascade = CascadeType.REMOVE)
    private Collection<Equipo> equipos;
    
    // Un Entrenador puede tener VARIOS registros de Medallas (OneToMany)
    // Esta es una relación a través de una tabla de unión (Medallas)
    // No usamos cascade REMOVE porque no queremos perder el historial de medallas al borrar un entrenador
    @OneToMany(mappedBy = "entrenador")
    private Collection<Medallas> medallas;

    // Constructor vacío (requerido por JPA)
    public Entrenador() {
    }

    // Constructor con parámetros básicos
    public Entrenador(String nombre, Integer edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    // Getters y Setters
    public int getIdentrenador() {
        return identrenador;
    }

    public void setIdentrenador(int identrenador) {
        this.identrenador = identrenador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public Collection<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(Collection<Equipo> equipos) {
        this.equipos = equipos;
    }

    public Collection<Medallas> getMedallas() {
        return medallas;
    }

    public void setMedallas(Collection<Medallas> medallas) {
        this.medallas = medallas;
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Entrenador sin nombre";
    }
}
