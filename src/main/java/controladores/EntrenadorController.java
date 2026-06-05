package controladores;

import entidades.Entrenador;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * CLASE ENTRENADORCONTROLLER
 * ═════════════════════════════════════════════════════════════════════════════════
 * Controlador de operaciones CRUD para la entidad Entrenador.
 * 
 * NOTA IMPORTANTE: Este controlador NO hereda de GenericService
 * porque queremos mostrar explícitamente cómo funciona JPA
 * en la aplicación educativa.
 * 
 * PATRÓN MVC (Model-View-Controller):
 *   • Model: Entidad Entrenador
 *   • View: EntrenadorView (interfaz gráfica Swing)
 *   • Controller: Este archivo (lógica de negocio)
 * 
 * FLUJO DE DATOS:
 *   Usuario hace clic en botón → EntrenadorView.crearEntrenador()
 *   → EntrenadorController.create(entrenador)
 *   → Base de datos recibe INSERT
 *   → EntrenadorView actualiza la tabla
 * 
 * CONCEPTOS CLAVE EN CADA MÉTODO:
 *   • EntityManager: Contexto de persistencia
 *   • EntityTransaction: Transacción (ACID)
 *   • JPAUtil.getEntityManager(): Obtiene EntityManager
 *   • persist() / merge() / remove(): Operaciones JPA
 *   • finally { em.close() }: Liberar conexión
 */
public class EntrenadorController {

    /**
     * CREATE: Crea un nuevo Entrenador
     * 
     * OPERACIÓN: INSERT en la tabla ENTRENADOR
     * 
     * FLUJO:
     *   1. Obtener EntityManager (conexión a BD)
     *   2. Obtener transacción
     *   3. Iniciar transacción (tx.begin)
     *   4. Persistir la entidad (em.persist) - MARCADA para insertar
     *   5. Hacer commit (tx.commit) - EJECUTA el INSERT
     *   6. Si error: rollback (deshacer cambios)
     *   7. Siempre: cerrar EntityManager
     * 
     * @param entrenador El nuevo Entrenador a crear
     * @return El Entrenador creado
     */
    public Entrenador create(Entrenador entrenador) {
        // Obtenemos un EntityManager desde nuestra utilidad.
        // El EntityManager representa el contexto de persistencia,
        // nos permite gestionar entidades y ejecutar operaciones JPA.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Persist guarda la entidad en la base de datos cuando se hace commit.
            em.persist(entrenador);
            tx.commit();
            return entrenador;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            // Cerramos el EntityManager para liberar recursos.
            em.close();
        }
    }

    /**
     * READ: Busca un Entrenador por su ID (clave primaria)
     * 
     * OPERACIÓN: SELECT WHERE identrenador = ?
     * 
     * @param id El identrenador a buscar
     * @return El Entrenador encontrado, o null si no existe
     */
    public Entrenador findById(Integer id) {
        // Usamos EntityManager para realizar una consulta de entidad por su ID.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Entrenador.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * READ ALL: Obtiene TODOS los Entrenadores
     * 
     * OPERACIÓN: SELECT * FROM ENTRENADOR
     * 
     * JPQL (JPA Query Language):
     *   • "SELECT e FROM Entrenador e"
     *   • Es SQL orientado a objetos (no SQL directo)
     *   • Em translada a SQL automáticamente
     *   • TypedQuery<T> asegura que el resultado es una List<Entrenador>
     * 
     * @return Lista de todos los Entrenadores en la BD
     */
    public List<Entrenador> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Creamos una consulta JPQL para obtener todas las entidades Entrenador.
            TypedQuery<Entrenador> query = em.createQuery("SELECT e FROM Entrenador e", Entrenador.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * UPDATE: Actualiza un Entrenador existente
     * 
     * OPERACIÓN: UPDATE ENTRENADOR SET nombre=?, edad=? WHERE identrenador=?
     * 
     * PATRÓN merge():
     *   • La entidad que pasas puede venir DETACHED (desvinculada del contexto)
     *   • merge() la revincula al EntityManager actual
     *   • Retorna la entidad MANAGED (gestionada)
     *   • En commit, Hibernate genera el UPDATE
     * 
     * EJEMPLO:
     *   Entrenador ash = new Entrenador();
     *   ash.setIdentrenador(1);
     *   ash.setNombre("Ash Ketchum");
     *   entrenadorController.update(ash);  // Actualiza en BD
     * 
     * @param entrenador El Entrenador con cambios
     * @return El Entrenador actualizado
     */
    public Entrenador update(Entrenador entrenador) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // merge devuelve una instancia gestionada que está sincronizada con la BD.
            Entrenador merged = em.merge(entrenador);
            tx.commit();
            return merged;
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
     * DELETE: Elimina un Entrenador por su ID
     * 
     * OPERACIÓN: DELETE FROM ENTRENADOR WHERE identrenador=?
     * 
     * PASOS IMPORTANTES:
     *   1. find(): Obtener la entidad MANAGED (necesario para remove)
     *   2. remove(): Marcar para eliminar
     *   3. commit(): EJECUTA el DELETE en BD
     * 
     * CASCADA:
     *   • Si el Entrenador tiene un Equipo (cascade = REMOVE)
     *   • El Equipo también se elimina
     *   • Las Medallas NO se eliminan (solo referencia)
     * 
     * @param id El identrenador a eliminar
     */
    public void delete(Integer id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Para eliminar primero debemos encontrar la entidad gestionada por el EntityManager.
            Entrenador entrenador = em.find(Entrenador.class, id);
            if (entrenador != null) {
                em.remove(entrenador);
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
