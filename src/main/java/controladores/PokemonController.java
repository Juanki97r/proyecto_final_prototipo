package controladores;

import entidades.Pokemon;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              CLASE: POKEMONCONTROLLER                                      ║
 * ║   Controlador CRUD para la entidad Pokémon                                 ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * RESPONSABILIDAD:
 *   Centraliza todas las operaciones de base de datos (CRUD) sobre la tabla POKEMON.
 *   La vista (PokemonView) llama a este controlador; nunca accede a JPA directamente.
 *
 * PATRÓN MVC (Model-View-Controller):
 *   • Model     → Entidad Pokemon (entidades/Pokemon.java)
 *   • View      → PokemonView (interfaz gráfica Swing)
 *   • Controller→ Esta clase (lógica de negocio + acceso a BD)
 *
 * OPERACIONES DISPONIBLES:
 *   create()   → INSERT en POKEMON
 *   findById() → SELECT WHERE numpokedex = ?
 *   findAll()  → SELECT * FROM POKEMON
 *   update()   → UPDATE POKEMON SET ... WHERE numpokedex = ?
 *   delete()   → DELETE FROM POKEMON WHERE numpokedex = ?
 *
 * PATRÓN DE TRANSACCIÓN usado en todos los métodos de escritura:
 *   1. EntityManager em = JPAUtil.getEntityManager()   → abre contexto
 *   2. EntityTransaction tx = em.getTransaction()       → obtiene transacción
 *   3. tx.begin()                                        → inicia transacción
 *   4. operación JPA (persist/merge/remove)              → registra cambio
 *   5. tx.commit()                                       → ejecuta en BD
 *   6. catch: tx.rollback()                              → deshace si error
 *   7. finally: em.close()                               → libera conexión
 */
public class PokemonController {
 /**
     * CREATE: Crea un nuevo Pokémon en la base de datos
     *
     * OPERACIÓN SQL equivalente: INSERT INTO Pokemon (numpokedex, nompokemon) VALUES (?, ?)
     *
     * PASOS JPA:
     *   1. persist(pokemon): Pone el objeto en estado "managed" (gestionado)
     *      Hibernate lo marca para insertar pero NO ejecuta SQL todavía
     *   2. commit(): Ejecuta el INSERT en la BD
     *   3. Si hay error (ej: clave duplicada) → rollback() cancela todo
     *
     * ERRORES POSIBLES:
     *   • EntityExistsException: Si el numpokedex ya existe en la BD
     *   • ConstraintViolationException: Si viola alguna restricción
     *
     * @param pokemon El Pokémon a crear (debe tener numpokedex y nompokemon)
     * @return El Pokémon creado (mismo objeto, ahora persistido)
     */
    public Pokemon create(Pokemon pokemon) {
        // Creamos un EntityManager que representa el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Persist añade la entidad al contexto. Al hacer commit se guarda en la base.
            em.persist(pokemon);
            tx.commit();
            return pokemon;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            // Si hay error (clave duplicada, violación de constraint, etc.)
            // deshacemos todos los cambios de esta transacción
            throw ex; // Relanzamos para que la vista la maneje y muestre el error al usuario
        } finally {
            em.close(); // SIEMPRE cerrar el EntityManager para liberar la conexión a la BD
        }
    }
  /**
     * READ: Busca un Pokémon por su número de Pokédex (clave primaria)
     *
     * OPERACIÓN SQL equivalente: SELECT * FROM Pokemon WHERE numpokedex = ?
     *
     * COMPORTAMIENTO de em.find():
     *   • Primero busca en el caché del EntityManager (primer nivel)
     *   • Si no está en caché, hace SELECT a la BD
     *   • Retorna null si no existe ningún Pokémon con ese ID
     *
     * NOTA: No necesita transacción porque es una operación de solo lectura.
     *
     * @param id El numpokedex del Pokémon (ej: 25 para Pikachu)
     * @return El Pokémon encontrado, o null si no existe
     */
    public Pokemon findById(Integer id) {
        // EntityManager.find busca una entidad gestionada por su ID en el contexto.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Pokemon.class, id);
        } finally {
            em.close();
        }
    }
 /**
     * READ ALL: Obtiene todos los Pokémons de la base de datos
     *
     * OPERACIÓN SQL equivalente: SELECT * FROM Pokemon
     *
     * JPQL (JPA Query Language):
     *   • "SELECT p FROM Pokemon p" es JPQL, NO SQL
     *   • Usa el nombre de la CLASE Java (Pokemon), no el de la tabla
     *   • "p" es un alias (como en SQL)
     *   • Hibernate lo traduce automáticamente a SQL para MySQL
     *
     * TypedQuery<Pokemon>:
     *   • Query con tipo seguro (evita casting manual)
     *   • getResultList() devuelve List<Pokemon> directamente
     *
     * @return Lista de todos los Pokémons (puede ser vacía si no hay datos)
     */
    public List<Pokemon> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Pokemon> query = em.createQuery("SELECT p FROM Pokemon p", Pokemon.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
  /**
     * UPDATE: Actualiza un Pokémon existente
     *
     * OPERACIÓN SQL equivalente: UPDATE Pokemon SET nompokemon=? WHERE numpokedex=?
     *
     * DIFERENCIA entre persist() y merge():
     *   • persist(): Para entidades NUEVAS (INSERT). Falla si ya existe.
     *   • merge():   Para entidades EXISTENTES (UPDATE). Sincroniza cambios.
     *
     * FLUJO típico de uso:
     *   1. Obtienes el Pokémon: Pokemon p = controller.findById(25)
     *   2. Cambias el nombre: p.setNompokemon("Pikachu Nuevo")
     *   3. Actualizas: controller.update(p)
     *
     * @param pokemon El Pokémon con los cambios aplicados
     * @return El Pokémon actualizado (instancia managed del contexto)
     */
    public Pokemon update(Pokemon pokemon) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // merge sincroniza la entidad con la base de datos y devuelve la instancia gestionada.
            Pokemon merged = em.merge(pokemon);
            tx.commit();
            return merged;
             // merge() sincroniza la entidad con la BD y devuelve la instancia gestionada.
            // La entidad "pokemon" que pasas puede estar DETACHED (desvinculada del contexto).
            // merge() la re-vincula y registra los cambios para el próximo commit.
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
/**
     * DELETE: Elimina un Pokémon por su número de Pokédex
     *
     * OPERACIÓN SQL equivalente: DELETE FROM Pokemon WHERE numpokedex = ?
     *
     * ¿POR QUÉ PRIMERO find() Y LUEGO remove()?
     *   • em.remove() solo funciona con entidades en estado "managed"
     *   • Si pasamos un objeto "detached" directamente, JPA lanza excepción
     *   • Solución: primero find() para obtener la versión "managed", luego remove()
     *
     * INTEGRIDAD REFERENCIAL:
     *   • Si el Pokémon tiene tipos asociados (DetallePokemon), la BD lanzará error
     *   • PokemonView verifica esto antes de llamar a delete()
     *   • También se valida con constraint check en la BD (MySQL)
     *
     * @param id El numpokedex del Pokémon a eliminar
     */
    public void delete(Integer id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Buscar la entidad gestionada antes de eliminarla es necesario para remove.
            Pokemon pokemon = em.find(Pokemon.class, id);
            if (pokemon != null) {
                em.remove(pokemon);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
