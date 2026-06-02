package controladores;

import entidades.Region;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Region.
 */
public class RegionController {

    public Region create(Region region) {
        // Creamos un EntityManager para ejecutar la operación de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        // Obtenemos la transacción del EntityManager para ejecutar write.
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(region);
            tx.commit();
            return region;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Region findById(Integer id) {
        // EntityManager.find busca la entidad gestionada por su clave primaria.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Region.class, id);
        } finally {
            em.close();
        }
    }

    public List<Region> findAll() {
        // EntityManager crea consultas JPQL para recuperar colecciones.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery("SELECT r FROM Region r", Region.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Region update(Region region) {
        // EntityManager.merge sincroniza los cambios de la entidad con la base de datos.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Region merged = em.merge(region);
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
        // Para borrar, primero obtenemos la entidad gestionada con find y luego la removemos.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Region region = em.find(Region.class, id);
            if (region != null) {
                em.remove(region);
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
