package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * CLASE GIMNASIO
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa un Gimnasio Pokémon donde los Entrenadores van a batallar para ganar Medallas.
 * Mapea la tabla GIMNASIO de la base de datos.
 * 
 * ESTRUCTURA:
 *   • Cada Gimnasio está ubicado en UNA Región
 *   • Cada Gimnasio otorga UNA Medalla (de un tipo específico)
 *   • Múltiples Entrenadores pueden ganar la misma Medalla en diferentes fechas
 * 
 * EJEMPLO:
 *   Gimnasio Pewter City
 *   - Región: Kanto
 *   - Medalla: "Boulder Badge"
 *   - Líder: Brock
 *   - Tipo: Roca
 *   - Entrenadores que ganaron aquí: {Ash (2023), Misty (2023), ...}
 */
@Entity
@Table(name = "Gimnasio")
public class Gimnasio {
    
    /**
     * CODGYM: Código del Gimnasio (clave primaria)
     * Identificador único para cada gimnasio
     */
    @Id
    @Column(name = "codgym")
    private int codgym;
    
    /**
     * NOMMEDALLA: Nombre de la Medalla
     * VARCHAR(20), puede ser nula
     * Ejemplos: "Boulder Badge", "Thunder Badge", "Soul Badge"
     */
    @Column(name = "nomMedalla", length = 20)
    private String nomMedalla;
    
    /**
     * TIPOMEDALLA: Tipo asociado a la Medalla
     * VARCHAR(20), NOT NULL
     * Ejemplos: "Roca", "Eléctrico", "Psíquico"
     */
    @Column(name = "tipoMedalla", length = 20, nullable = false)
    private String tipoMedalla;
    
    /**
     * REGION: Relación MUCHOS-A-UNO con Region
     * 
     * Muchos Gimnasios pertenecen a UNA Región
     * Ejemplo: {Gym Pewter City, Gym Cerulean City, Gym Vermilion City} ──> Región Kanto
     * 
     * @ManyToOne: Una relación donde muchos Gimnasios señalan a 1 Región
     * @JoinColumn(name = "codregion"): La columna FK en la tabla GIMNASIO
     * 
     * Esta es la relación INVERSA de Region.gimnasios (OneToMany)
     */
    @ManyToOne
    @JoinColumn(name = "codregion", nullable = false)
    private Region region;
    
    /**
     * MEDALLAS: Historial de Medallas otorgadas (1:M)
     * 
     * Un Gimnasio → MUCHOS registros en la tabla MEDALLAS
     * Cada registro representa: {Entrenador X ganó Medalla en Gimnasio Y en fecha Z}
     * 
     * Ejemplo:
     *   Gimnasio Pewter City tiene registros:
     *   - {Ash, Pewter City, 2023-05-15}
     *   - {Misty, Pewter City, 2023-06-20}
     *   - {Brock, Pewter City, 2023-07-10}
     * 
     * cascade = CascadeType.REMOVE: Si borro el Gimnasio → borro historial de medallas
     * (aunque en la práctica esto podría considerarse destructivo para datos históricos)
     */
    @OneToMany(mappedBy = "gimnasio", cascade = CascadeType.REMOVE)
    private Collection<Medallas> medallas;

    /** Constructor sin parámetros (requerido por JPA) */
    public Gimnasio() {
    }

    /** Constructor completo */
    public Gimnasio(String nomMedalla, String tipoMedalla, Region region) {
        this.nomMedalla = nomMedalla;
        this.tipoMedalla = tipoMedalla;
        this.region = region;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Código del Gimnasio */
    public int getCodgym() {
        return codgym;
    }

    /** SETTER: Asigna código del Gimnasio */
    public void setCodgym(int codgym) {
        this.codgym = codgym;
    }

    /** GETTER: Nombre de la Medalla */
    public String getNomMedalla() {
        return nomMedalla;
    }

    /** SETTER: Actualiza el nombre de la Medalla */
    public void setNomMedalla(String nomMedalla) {
        this.nomMedalla = nomMedalla;
    }

    /** GETTER: Tipo de la Medalla */
    public String getTipoMedalla() {
        return tipoMedalla;
    }

    /** SETTER: Actualiza el tipo de la Medalla */
    public void setTipoMedalla(String tipoMedalla) {
        this.tipoMedalla = tipoMedalla;
    }

    /** GETTER: Devuelve la Región donde está este Gimnasio */
    public Region getRegion() {
        return region;
    }

    /** SETTER: Asigna la Región del Gimnasio */
    public void setRegion(Region region) {
        this.region = region;
    }

    /** GETTER: Devuelve el historial de Medallas otorgadas aquí */
    public Collection<Medallas> getMedallas() {
        return medallas;
    }

    /** SETTER: Reemplaza el historial de Medallas */
    public void setMedallas(Collection<Medallas> medallas) {
        this.medallas = medallas;
    }

    /**
     * Representación en texto
     * @return "Nombre_Región - Nombre_Medalla" o variantes
     */
    @Override
    public String toString() {
        if (region != null && region.getNomregion() != null) {
            return region.getNomregion() + " - " + (nomMedalla != null ? nomMedalla : "Sin medalla");
        }
        return nomMedalla != null ? nomMedalla : "Gimnasio sin nombre";
    }
}
