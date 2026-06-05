package entidades;

import javax.persistence.*;
import java.util.Collection;

/**
 * CLASE POKEMON
 * ═════════════════════════════════════════════════════════════════════════════════
 * Representa una especie Pokémon (ej: Pikachu, Charizard).
 * Mapea la tabla POKEMON de la base de datos.
 * 
 * PUNTOS CLAVE:
 * • Cada Pokémon es una ESPECIE (no un individuo específico)
 * • Puede tener MÚLTIPLES TIPOS (ej: Charizard = Fuego + Volador)
 * • Puede estar en MÚLTIPLES EQUIPOS de diferentes entrenadores
 * • Gestión: PokemonView (interfaz) + PokemonController (CRUD)
 * 
 * RELACIONES EN LA APLICACIÓN:
 *   Pokemon ──1:M──> DetallePokemon ──M:1──> Tipo
 *   Pokemon ──1:M──> DetalleEquipo ──M:1──> Equipo
 */
@Entity
@Table(name = "Pokemon")
public class Pokemon {
    
    /**
     * NUMPOKEDEX: Número de Pokédex (clave primaria)
     * Identificador único. En datos reales: 1=Bulbasaur, 25=Pikachu, 6=Charizard
     */
    @Id
    @Column(name = "numpokedex")
    private int numpokedex;
    
    /**
     * NOMPOKEMON: Nombre de la especie
     * VARCHAR(20), NOT NULL
     * Ejemplos: "Pikachu", "Charizard", "Blastoise"
     */
    @Column(name = "nompokemon", length = 20, nullable = false)
    private String nompokemon;
    
    /**
     * DETALLES POKEMON: Sus tipos (1:M a través de tabla de unión)
     * 
     * Un Pokémon → MUCHOS tipos
     * Ejemplo: Charizard tiene {Fuego, Volador}
     * 
     * La tabla DETALLEPOKEMON es una tabla de unión que mapea:
     *   DETALLEPOKEMON.numpokedex ──> POKEMON.numpokedex
     *   DETALLEPOKEMON.codtipo ──> TIPO.codtipo
     * 
     * mappedBy = "pokemon": La FK y la relación se definen en DetallePokemon.pokemon
     */
    @OneToMany(mappedBy = "pokemon")
    private Collection<DetallePokemon> detallesPokemon;
    
    /**
     * DETALLES EQUIPO: Equipos que contienen este Pokémon (1:M)
     * 
     * Un Pokémon → MUCHOS equipos
     * Ejemplo: Pikachu (especie) está en equipo de Ash Y en equipo de Misty
     * 
     * La tabla DETALLEEQUIPO especifica:
     *   {codequipo, numslot (1-6)} → numpokedex
     * 
     * mappedBy = "pokemon": La FK y relación se definen en DetalleEquipo.pokemon
     */
    @OneToMany(mappedBy = "pokemon")
    private Collection<DetalleEquipo> detallesEquipo;

    /**
     * Constructor sin parámetros
     * Requerido por JPA para instancias creadas por Hibernate desde la BD
     */
    public Pokemon() {
    }

    /**
     * Constructor con nombre
     * Utilizado en la interfaz gráfica cuando se crea un nuevo Pokémon
     */
    public Pokemon(String nompokemon) {
        this.nompokemon = nompokemon;
    }

    /**
     * Constructor completo
     * Incluye número de Pokédex (ID) y nombre
     */
    public Pokemon(int numpokedex, String nompokemon) {
        this.numpokedex = numpokedex;
        this.nompokemon = nompokemon;
    }


    // ═══════════════════════════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════════════════════════

    /** GETTER: Devuelve el número de Pokédex (identificador único) */
    public int getNumpokedex() {
        return numpokedex;
    }

    /** SETTER: Asigna el número de Pokédex (generalmente UNA sola vez en creación) */
    public void setNumpokedex(int numpokedex) {
        this.numpokedex = numpokedex;
    }

    /** GETTER: Devuelve el nombre de la especie Pokémon */
    public String getNompokemon() {
        return nompokemon;
    }

    /** SETTER: Actualiza el nombre del Pokémon */
    public void setNompokemon(String nompokemon) {
        this.nompokemon = nompokemon;
    }

    /** GETTER: Devuelve la colección de tipos que tiene este Pokémon */
    public Collection<DetallePokemon> getDetallesPokemon() {
        return detallesPokemon;
    }

    /** SETTER: Reemplaza la colección de tipos */
    public void setDetallesPokemon(Collection<DetallePokemon> detallesPokemon) {
        this.detallesPokemon = detallesPokemon;
    }

    /** GETTER: Devuelve todos los equipos en los que participa este Pokémon */
    public Collection<DetalleEquipo> getDetallesEquipo() {
        return detallesEquipo;
    }

    /** SETTER: Reemplaza la colección de equipos */
    public void setDetallesEquipo(Collection<DetalleEquipo> detallesEquipo) {
        this.detallesEquipo = detallesEquipo;
    }

    /**
     * Representación en texto del Pokémon
     * Se usa en listas, combos desplegables, y logs
     * @return El nombre del Pokémon
     */
    @Override
    public String toString() {
        return nompokemon != null ? nompokemon : "Pokémon sin nombre";
    }
}
