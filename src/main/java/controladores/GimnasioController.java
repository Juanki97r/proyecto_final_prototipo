package controladores;

import entidades.Gimnasio;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Gimnasio.
 */
public class GimnasioController {

    public Gimnasio create(Gimnasio gimnasio) {
        // Obtenemos un EntityManager para hacer la operación de persistencia.
        // Este objeto gestiona el ciclo de vida de la entidad en el contexto JPA.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(gimnasio);
            tx.commit();
            return gimnasio;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Gimnasio findById(Integer id) {
        // Usamos EntityManager.find para recuperar la entidad a partir de su clave primaria.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Gimnasio.class, id);
        } finally {
            em.close();
        }
    }

    public List<Gimnasio> findAll() {
        // EntityManager también crea consultas JPQL para obtener colecciones de entidades.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Gimnasio> query = em.createQuery("SELECT g FROM Gimnasio g", Gimnasio.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Gimnasio update(Gimnasio gimnasio) {
        // Para modificar una entidad usamos merge, que sincroniza los cambios con el contexto.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Gimnasio merged = em.merge(gimnasio);
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
        // Para eliminar una entidad, primero la recuperamos con EntityManager.find.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Gimnasio gimnasio = em.find(Gimnasio.class, id);
            if (gimnasio != null) {
                em.remove(gimnasio);
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
