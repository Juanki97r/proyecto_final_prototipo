package controladores;

import entidades.DetalleEquipo;
import entidades.DetalleEquipoPK;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad DetalleEquipo.
 * Usa clave primaria compuesta para identificar cada slot en un equipo.
 */
public class DetalleEquipoController {

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

    public DetalleEquipo findById(DetalleEquipoPK id) {
        // EntityManager.find busca la entidad por clave compuesta.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DetalleEquipo.class, id);
        } finally {
            em.close();
        }
    }

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
