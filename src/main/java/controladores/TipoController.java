package controladores;

import entidades.Tipo;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Tipo.
 */
public class TipoController {

    public Tipo create(Tipo tipo) {
        // Abrimos un EntityManager para trabajar con el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(tipo);
            tx.commit();
            return tipo;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Tipo findById(Integer id) {
        // Usamos EntityManager.find para recuperar la entidad por su clave primaria.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Tipo.class, id);
        } finally {
            em.close();
        }
    }

    public List<Tipo> findAll() {
        // EntityManager crea un TypedQuery JPQL para obtener todas las entidades Tipo.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Tipo> query = em.createQuery("SELECT t FROM Tipo t", Tipo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Tipo update(Tipo tipo) {
        // merge sincroniza los cambios del objeto con el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Tipo merged = em.merge(tipo);
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
        // Primero obtenemos la entidad gestionada y después la eliminamos.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Tipo tipo = em.find(Tipo.class, id);
            if (tipo != null) {
                em.remove(tipo);
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
