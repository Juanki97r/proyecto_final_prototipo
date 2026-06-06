package controladores;

import entidades.DetalleEquipo;
import entidades.DetalleEquipoPK;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║              CLASE: DETALLEEQUIPOCONTROLLER                                ║
 * ║   Controlador CRUD para la entidad DetalleEquipo                           ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 *
 * RESPONSABILIDAD:
 *   Gestiona las operaciones de base de datos sobre la tabla DETALLEEQUIPO.
 *   Esta tabla es la implementación de la relación MUCHOS-A-MUCHOS entre
 *   Equipos y Pokémons, con un campo adicional (numslot = posición 1-6).
 *
 * PARTICULARIDAD DE ESTA ENTIDAD:
 *   Usa CLAVE PRIMARIA COMPUESTA (codequipo, numslot).
 *   Por eso el tipo de clave en findById() y delete() es DetalleEquipoPK,
 *   no un simple Integer como en los otros controladores.
 *
 * MÉTODO ESPECIAL:
 *   findByEquipoId() → Consulta JPQL con filtro (no genérica como findAll)
 *   Permite obtener todos los slots de un Equipo concreto, ordenados por slot.
 *
 * USO EN LA APLICACIÓN:
 *   DetalleEquipoView llama a este controlador para:
 *     • Mostrar los 6 slots de un equipo
 *     • Añadir un Pokémon a un slot
 *     • Eliminar un Pokémon de un slot
 */
public class DetalleEquipoController {
 /**
     * CREATE: Añade un Pokémon a un slot de un Equipo
     *
     * OPERACIÓN SQL equivalente:
     *   INSERT INTO DetalleEquipo (codequipo, numslot, numpokedex) VALUES (?, ?, ?)
     *
     * PRE-CONDICIONES que valida la vista antes de llamar aquí:
     *   • El slot (numslot) debe estar entre 1 y 6
     *   • El Equipo debe existir
     *   • El Pokémon debe existir
     *
     * @param detalleEquipo El detalle a crear (con id={codequipo, numslot} y pokemon)
     * @return El DetalleEquipo creado
     */
    public DetalleEquipo create(DetalleEquipo detalleEquipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(detalleEquipo);
            tx.commit();
            return detalleEquipo;
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
     * READ: Busca un DetalleEquipo por su clave primaria compuesta
     *
     * OPERACIÓN SQL equivalente:
     *   SELECT * FROM DetalleEquipo WHERE codequipo = ? AND numslot = ?
     *
     * USO TÍPICO:
     *   DetalleEquipoPK pk = new DetalleEquipoPK(codequipo, numslot);
     *   DetalleEquipo detalle = controller.findById(pk);
     *
     * @param id Clave primaria compuesta {codequipo, numslot}
     * @return El DetalleEquipo encontrado, o null si ese slot está vacío
     */
    public DetalleEquipo findById(DetalleEquipoPK id) {
        // EntityManager.find busca la entidad por clave compuesta.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DetalleEquipo.class, id);
        } finally {
            em.close();
        }
    }
 /**
     * READ ALL: Obtiene todos los DetallesEquipo (todos los equipos, todos los slots)
     *
     * OPERACIÓN SQL equivalente: SELECT * FROM DetalleEquipo
     *
     * NOTA: Este método devuelve TODOS los slots de TODOS los equipos.
     * Para filtrar por un equipo concreto, usa findByEquipoId().
     *
     * @return Lista de todos los DetallesEquipo
     */
    public List<DetalleEquipo> findAll() {
        // Query JPQL creada por el EntityManager para recuperar todos los detalles.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<DetalleEquipo> query = em.createQuery("SELECT d FROM DetalleEquipo d", DetalleEquipo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
 /**
     * READ BY EQUIPO: Obtiene todos los Pokémons de un Equipo concreto
     *
     * OPERACIÓN SQL equivalente:
     *   SELECT * FROM DetalleEquipo WHERE codequipo = ? ORDER BY numslot ASC
     *
     * JPQL CON PARÁMETROS:
     *   • "d.equipo.codequipo": Navega la relación JPA (no usa JOIN explícito)
     *     Equivale a: DetalleEquipo JOIN Equipo ON DetalleEquipo.codequipo = Equipo.codequipo
     *   • ":codequipo": Parámetro nombrado en JPQL
     *   • query.setParameter("codequipo", codequipo): Asigna el valor al parámetro
     *   • ORDER BY d.id.numslot: Ordena por número de slot (1, 2, 3, 4, 5, 6)
     *
     * USO EN LA APLICACIÓN:
     *   Se usa en EquipoView para mostrar los 6 Pokémons de un equipo seleccionado.
     *
     * DIFERENCIA CON findAll():
     *   findAll()        → todos los equipos mezclados
     *   findByEquipoId() → solo un equipo específico, ordenado por slot
     *
     * @param codequipo El código del Equipo cuyos slots queremos obtener
     * @return Lista de DetallesEquipo del equipo, ordenada por numslot (1→6)
     */
    public List<DetalleEquipo> findByEquipoId(int codequipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<DetalleEquipo> query = em.createQuery(
                "SELECT d FROM DetalleEquipo d WHERE d.equipo.codequipo = :codequipo ORDER BY d.id.numslot",
                DetalleEquipo.class
            );
            query.setParameter("codequipo", codequipo);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
 /**
     * UPDATE: Actualiza un slot del equipo (cambia el Pokémon de esa posición)
     *
     * OPERACIÓN SQL equivalente:
     *   UPDATE DetalleEquipo SET numpokedex=? WHERE codequipo=? AND numslot=?
     *
     * CUÁNDO SE USA:
     *   Cuando el usuario quiere cambiar el Pokémon en una posición existente
     *   sin eliminar y volver a crear el registro.
     *
     * @param detalleEquipo El detalle con los cambios aplicados
     * @return El DetalleEquipo actualizado (instancia managed)
     */
    public DetalleEquipo update(DetalleEquipo detalleEquipo) {
        // merge sincroniza la entidad con la base de datos en el contexto actual.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DetalleEquipo merged = em.merge(detalleEquipo);
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
     * DELETE: Elimina un Pokémon de un slot del equipo
     *
     * OPERACIÓN SQL equivalente:
     *   DELETE FROM DetalleEquipo WHERE codequipo=? AND numslot=?
     *
     * USO:
     *   Cuando el usuario quiere "vaciar" un slot del equipo.
     *   El Pokémon en sí no se borra, solo el vínculo entre el Pokémon y ese slot.
     *
     * @param id Clave primaria compuesta {codequipo, numslot} del slot a vaciar
     */
    public void delete(DetalleEquipoPK id) {
        // Para eliminar primero localizamos la entidad gestionada y luego la removemos.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DetalleEquipo detalleEquipo = em.find(DetalleEquipo.class, id);
            if (detalleEquipo != null) {
                em.remove(detalleEquipo);
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
