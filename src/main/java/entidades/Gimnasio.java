package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * Entidad que representa un Gimnasio Pokémon
 * Mapea la tabla GIMNASIO de la base de datos
 * Cada gimnasio está ubicado en una región y otorga una medalla de un tipo específico
 */
@Entity
@Table(name = "Gimnasio")
public class Gimnasio {
    
    // Clave primaria manual
    @Id
    @Column(name = "codgym")
    private int codgym;
    
    // Nombre de la medalla que otorga (VARCHAR(20), puede ser nulo)
    @Column(name = "nomMedalla", length = 20)
    private String nomMedalla;
    
    // Tipo de medalla (VARCHAR(20), NOT NULL)
    @Column(name = "tipoMedalla", length = 20, nullable = false)
    private String tipoMedalla;
    
    // RELACIÓN MANY-TO-ONE con REGION
    // Muchos Gimnasios pertenecen a UNA Region
    // @JoinColumn especifica que la columna "codregion" es la clave foránea
    // Esta columna está en la tabla GIMNASIO y hace referencia a la tabla REGION
    @ManyToOne
    @JoinColumn(name = "codregion", nullable = false)
    private Region region;
    
    // RELACIÓN BIDIRECCIONAL ONE-TO-MANY
    // Un Gimnasio puede tener VARIOS registros en Medallas (historial de entrenadores que ganaron medallas aquí)
    // mappedBy indica que Medallas es el dueño de esta relación mediante su atributo "gimnasio"
    // cascade = CascadeType.REMOVE: si se borra un gimnasio, se borran todas las medallas asociadas
    @OneToMany(mappedBy = "gimnasio", cascade = CascadeType.REMOVE)
    private Collection<Medallas> medallas;

    // Constructor vacío (requerido por JPA)
    public Gimnasio() {
    }

    // Constructor con parámetros
    public Gimnasio(String nomMedalla, String tipoMedalla, Region region) {
        this.nomMedalla = nomMedalla;
        this.tipoMedalla = tipoMedalla;
        this.region = region;
    }

    // Getters y Setters
    public int getCodgym() {
        return codgym;
    }

    public void setCodgym(int codgym) {
        this.codgym = codgym;
    }

    public String getNomMedalla() {
        return nomMedalla;
    }

    public void setNomMedalla(String nomMedalla) {
        this.nomMedalla = nomMedalla;
    }

    public String getTipoMedalla() {
        return tipoMedalla;
    }

    public void setTipoMedalla(String tipoMedalla) {
        this.tipoMedalla = tipoMedalla;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Collection<Medallas> getMedallas() {
        return medallas;
    }

    public void setMedallas(Collection<Medallas> medallas) {
        this.medallas = medallas;
    }

    @Override
    public String toString() {
        if (region != null && region.getNomregion() != null) {
            return region.getNomregion() + " - " + (nomMedalla != null ? nomMedalla : "Sin medalla");
        }
        return nomMedalla != null ? nomMedalla : "Gimnasio sin nombre";
    }
}
