package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa una Región del mundo Pokémon
 * Mapea la tabla REGION de la base de datos
 */
@Entity
@Table(name = "Region")
public class Region {
    
    // Clave primaria manual
    @Id
    @Column(name = "codregion")
    private int codregion;
    
    // Nombre de la región (VARCHAR(20), NOT NULL)
    @Column(name = "nomregion", length = 20, nullable = false)
    private String nomregion;
    
    // RELACIÓN BIDIRECCIONAL
    // Una Región contiene VARIOS Gimnasios (OneToMany)
    // mappedBy indica que Gimnasio es el dueño de la relación mediante su atributo "region"
    // cascade = CascadeType.REMOVE: si se borra una región, se borran todos sus gimnasios
    @OneToMany(mappedBy = "region", cascade = CascadeType.REMOVE)
    private Collection<Gimnasio> gimnasios;

    // Constructor vacío (requerido por JPA)
    public Region() {
    }

    // Constructor con parámetros
    public Region(String nomregion) {
        this.nomregion = nomregion;
    }

    // Getters y Setters
    public int getCodregion() {
        return codregion;
    }

    public void setCodregion(int codregion) {
        this.codregion = codregion;
    }

    public String getNomregion() {
        return nomregion;
    }

    public void setNomregion(String nomregion) {
        this.nomregion = nomregion;
    }

    public Collection<Gimnasio> getGimnasios() {
        return gimnasios;
    }

    public void setGimnasios(Collection<Gimnasio> gimnasios) {
        this.gimnasios = gimnasios;
    }

    @Override
    public String toString() {
        return nomregion != null ? nomregion : "Región sin nombre";
    }
}
