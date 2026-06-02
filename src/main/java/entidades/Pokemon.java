package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa un Pokémon
 * Mapea la tabla POKEMON de la base de datos
 */
@Entity
@Table(name = "Pokemon")
public class Pokemon {
    
    // Clave primaria manual. numpokedex es el número único de cada Pokémon en la Pokédex
    @Id
    @Column(name = "numpokedex")
    private int numpokedex;
    
    // Nombre del Pokémon (VARCHAR(20), NOT NULL)
    @Column(name = "nompokemon", length = 20, nullable = false)
    private String nompokemon;
    
    // RELACIONES BIDIRECCIONALES
    // Un Pokémon puede tener VARIOS tipos a través de DetallePokemon (OneToMany)
    // mappedBy indica que DetallePokemon es el dueño de la relación mediante su atributo "pokemon"
    @OneToMany(mappedBy = "pokemon")
    private Collection<DetallePokemon> detallesPokemon;
    
    // Un Pokémon puede estar en VARIOS equipos a través de DetalleEquipo (OneToMany)
    // mappedBy indica que DetalleEquipo es el dueño de la relación mediante su atributo "pokemon"
    @OneToMany(mappedBy = "pokemon")
    private Collection<DetalleEquipo> detallesEquipo;

    // Constructor vacío (requerido por JPA)
    public Pokemon() {
    }

    // Constructor con parámetros
    public Pokemon(String nompokemon) {
        this.nompokemon = nompokemon;
    }

    public Pokemon(int numpokedex, String nompokemon) {
        this.numpokedex = numpokedex;
        this.nompokemon = nompokemon;
    }

    // Getters y Setters
    public int getNumpokedex() {
        return numpokedex;
    }

    public void setNumpokedex(int numpokedex) {
        this.numpokedex = numpokedex;
    }

    public String getNompokemon() {
        return nompokemon;
    }

    public void setNompokemon(String nompokemon) {
        this.nompokemon = nompokemon;
    }

    public Collection<DetallePokemon> getDetallesPokemon() {
        return detallesPokemon;
    }

    public void setDetallesPokemon(Collection<DetallePokemon> detallesPokemon) {
        this.detallesPokemon = detallesPokemon;
    }

    public Collection<DetalleEquipo> getDetallesEquipo() {
        return detallesEquipo;
    }

    public void setDetallesEquipo(Collection<DetalleEquipo> detallesEquipo) {
        this.detallesEquipo = detallesEquipo;
    }

    @Override
    public String toString() {
        return nompokemon != null ? nompokemon : "Pokémon sin nombre";
    }
}
