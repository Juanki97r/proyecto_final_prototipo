package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa un Equipo de Pokémons
 * Mapea la tabla EQUIPO de la base de datos
 * Cada entrenador puede tener un equipo con hasta 6 Pokémons
 */
@Entity
@Table(name = "Equipo")
public class Equipo {
    
    // Clave primaria manual
    @Id
    @Column(name = "codequipo")
    private int codequipo;
    
    // RELACIÓN MANY-TO-ONE con ENTRENADOR
    // Muchos Equipos pertenecen a UN Entrenador
    // @JoinColumn especifica que la columna "identrenador" es la clave foránea
    // Esta columna está en la tabla EQUIPO y hace referencia a la tabla ENTRENADOR
    // La relación inversa está definida en Entrenador.equipos
    @ManyToOne
    @JoinColumn(name = "identrenador", nullable = false)
    private Entrenador entrenador;
    
    // RELACIÓN BIDIRECCIONAL ONE-TO-MANY
    // Un Equipo puede tener VARIOS Pokémons en diferentes slots a través de DetalleEquipo
    // mappedBy indica que DetalleEquipo es el dueño de esta relación mediante su atributo "equipo"
    // cascade = CascadeType.REMOVE: si se borra un equipo, se borran todos los detalles (Pokémons del equipo)
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.REMOVE)
    private Collection<DetalleEquipo> detallesEquipo;

    // Constructor vacío (requerido por JPA)
    public Equipo() {
    }

    // Constructor con parámetros
    public Equipo(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    // Getters y Setters
    public int getCodequipo() {
        return codequipo;
    }

    public void setCodequipo(int codequipo) {
        this.codequipo = codequipo;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    public Collection<DetalleEquipo> getDetallesEquipo() {
        return detallesEquipo;
    }

    public void setDetallesEquipo(Collection<DetalleEquipo> detallesEquipo) {
        this.detallesEquipo = detallesEquipo;
    }

    @Override
    public String toString() {
        return "Equipo " + codequipo + (entrenador != null && entrenador.getNombre() != null ? " - " + entrenador.getNombre() : "");
    }
}
