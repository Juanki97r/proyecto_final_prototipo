package controladores;

import entidades.Equipo;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Equipo.
 */
public class EquipoController {

    public Equipo create(Equipo equipo) {
        // Usamos EntityManager para iniciar el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(equipo);
            tx.commit();
            return equipo;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Equipo findById(Integer id) {
        // EntityManager.find recupera la entidad gestionada por el ID.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Equipo.class, id);
        } finally {
            em.close();
        }
    }

    public List<Equipo> findAll() {
        // Las consultas JPQL se crean a través del EntityManager.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Equipo> query = em.createQuery("SELECT e FROM Equipo e", Equipo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Equipo update(Equipo equipo) {
        // merge hace que el EntityManager mezcle el estado del objeto con la BD.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo merged = em.merge(equipo);
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

    public void delete(Integer id) {
        // Para eliminar usamos find para obtener la entidad gestionada y luego remove.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo equipo = em.find(Equipo.class, id);
            if (equipo != null) {
                em.remove(equipo);
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
