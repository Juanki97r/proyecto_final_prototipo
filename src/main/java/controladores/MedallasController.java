package controladores;

import entidades.Medallas;
import entidades.MedallasPK;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Medallas.
 * Esta entidad usa una clave primaria compuesta, por eso la clave es MedallasPK.
 */
public class MedallasController {

    public Medallas create(Medallas medalla) {
        // Abrimos un EntityManager para obtener el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(medalla);
            tx.commit();
            return medalla;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Medallas findById(MedallasPK id) {
        // EntityManager.find recupera una entidad por su clave primaria compuesta.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Medallas.class, id);
        } finally {
            em.close();
        }
    }

    public List<Medallas> findAll() {
        // EntityManager crea la consulta JPQL para recuperar todas las medallas.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Medallas> query = em.createQuery("SELECT m FROM Medallas m", Medallas.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Medallas update(Medallas medalla) {
        // merge sincroniza los cambios de la entidad con la base de datos.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Medallas merged = em.merge(medalla);
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

    public void delete(MedallasPK id) {
        // Primero buscamos la entidad gestionada y luego la eliminamos con remove.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Medallas medalla = em.find(Medallas.class, id);
            if (medalla != null) {
                em.remove(medalla);
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
