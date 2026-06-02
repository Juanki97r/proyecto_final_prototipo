package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa un Tipo de Pokémon
 * Mapea la tabla TIPO de la base de datos
 * Los tipos en Pokémon son: Planta, Fuego, Agua, Veneno, Volador, etc.
 */
@Entity
@Table(name = "Tipo")
public class Tipo {
    
    // Clave primaria manual
    @Id
    @Column(name = "codtipo")
    private int codtipo;
    
    // Nombre del tipo (VARCHAR(10), NOT NULL)
    @Column(name = "nomtipo", length = 10, nullable = false)
    private String nomtipo;
    
    // RELACIÓN BIDIRECCIONAL
    // Un Tipo puede estar asociado a VARIOS Pokémons a través de DetallePokemon (OneToMany)
    // mappedBy indica que DetallePokemon es el dueño de la relación mediante su atributo "tipo"
    @OneToMany(mappedBy = "tipo")
    private Collection<DetallePokemon> detallesPokemon;

    // Constructor vacío (requerido por JPA)
    public Tipo() {
    }

    // Constructor con parámetros
    public Tipo(String nomtipo) {
        this.nomtipo = nomtipo;
    }

    // Getters y Setters
    public int getCodtipo() {
        return codtipo;
    }

    public void setCodtipo(int codtipo) {
        this.codtipo = codtipo;
    }

    public String getNomtipo() {
        return nomtipo;
    }

    public void setNomtipo(String nomtipo) {
        this.nomtipo = nomtipo;
    }

    public Collection<DetallePokemon> getDetallesPokemon() {
        return detallesPokemon;
    }

    public void setDetallesPokemon(Collection<DetallePokemon> detallesPokemon) {
        this.detallesPokemon = detallesPokemon;
    }

    @Override
    public String toString() {
        return nomtipo != null ? nomtipo : "Tipo sin nombre";
    }
}
