package controladores;

import entidades.DetallePokemon;
import entidades.DetallePokemonPK;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad DetallePokemon.
 * Usa clave primaria compuesta para la relación Pokémon-Tipo.
 */
public class DetallePokemonController {

    public DetallePokemon create(DetallePokemon detallePokemon) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(detallePokemon);
            tx.commit();
            return detallePokemon;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public DetallePokemon findById(DetallePokemonPK id) {
        // EntityManager.find recupera la entidad DetallePokemon por su clave compuesta.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DetallePokemon.class, id);
        } finally {
            em.close();
        }
    }

    public List<DetallePokemon> findAll() {
        // Creamos una consulta JPQL usando EntityManager para recuperar todos los detalles.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<DetallePokemon> query = em.createQuery("SELECT d FROM DetallePokemon d", DetallePokemon.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public DetallePokemon update(DetallePokemon detallePokemon) {
        // EntityManager.merge actualiza el estado de la entidad en el contexto.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DetallePokemon merged = em.merge(detallePokemon);
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

    public void delete(DetallePokemonPK id) {
        // Para eliminar primero se busca la entidad gestionada y luego se remueve.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DetallePokemon detallePokemon = em.find(DetallePokemon.class, id);
            if (detallePokemon != null) {
                em.remove(detallePokemon);
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
