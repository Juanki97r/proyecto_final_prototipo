package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * CLASE TIPO
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa un Tipo de Pokémon (Fuego, Agua, Planta, Eléctrico, etc.).
 * Mapea la tabla TIPO de la base de datos.
 * 
 * PROPÓSITO: Clasificar Pokémons y determinar ventajas/desventajas en combate.
 * RELACIÓN: Un Tipo está asociado a MÚLTIPLES Pokémons.
 * 
 * EJEMPLO: El tipo "Fuego" está asociado a {Charizard, Arcanine, Rapidash, ...}
 */
@Entity
@Table(name = "Tipo")
public class Tipo {
    
    /**
     * CODTIPO: Código de Tipo (clave primaria)
     * Identificador único para cada tipo
     */
    @Id
    @Column(name = "codtipo")
    private int codtipo;
    
    /**
     * NOMTIPO: Nombre del Tipo
     * VARCHAR(10), NOT NULL
     * Ejemplos: "Fuego", "Agua", "Planta", "Eléctrico", "Hielo"
     */
    @Column(name = "nomtipo", length = 10, nullable = false)
    private String nomtipo;
    
    /**
     * DETALLES POKEMON: Pokémons que tienen este Tipo (1:M)
     * 
     * Un Tipo → MUCHOS Pokémons
     * Ejemplo: El tipo "Fuego" está en {Charizard, Arcanine, Rapidash, ...}
     * 
     * La tabla DETALLEPOKEMON es la tabla de unión que mapea:
     *   DETALLEPOKEMON.codtipo ──> TIPO.codtipo
     *   DETALLEPOKEMON.numpokedex ──> POKEMON.numpokedex
     */
    @OneToMany(mappedBy = "tipo")
    private Collection<DetallePokemon> detallesPokemon;

    /** Constructor sin parámetros (requerido por JPA) */
    public Tipo() {
    }

    /** Constructor con nombre de tipo */
    public Tipo(String nomtipo) {
        this.nomtipo = nomtipo;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Código del Tipo */
    public int getCodtipo() {
        return codtipo;
    }

    /** SETTER: Asigna el código del Tipo */
    public void setCodtipo(int codtipo) {
        this.codtipo = codtipo;
    }

    /** GETTER: Nombre del Tipo */
    public String getNomtipo() {
        return nomtipo;
    }

    /** SETTER: Actualiza el nombre del Tipo */
    public void setNomtipo(String nomtipo) {
        this.nomtipo = nomtipo;
    }

    /** GETTER: Devuelve todos los Pokémons que tienen este Tipo */
    public Collection<DetallePokemon> getDetallesPokemon() {
        return detallesPokemon;
    }

    /** SETTER: Reemplaza la colección de detalles */
    public void setDetallesPokemon(Collection<DetallePokemon> detallesPokemon) {
        this.detallesPokemon = detallesPokemon;
    }

    /**
     * Representación en texto
     * @return El nombre del Tipo
     */
    @Override
    public String toString() {
        return nomtipo != null ? nomtipo : "Tipo sin nombre";
    }
}
