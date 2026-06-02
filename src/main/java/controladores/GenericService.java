package controladores;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Servicio genérico que implementa las operaciones CRUD básicas para cualquier entidad.
 *
 * Usamos una clase base genérica para evitar duplicar el código de transacciones
 * y manejo de EntityManager en cada servicio concreto.
 */
public abstract class GenericService<T, K> {

    private final Class<T> entityClass;

    protected GenericService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Crea una entidad en la base de datos.
     * Persistimos la entidad en una transacción porque JPA requiere
     * un contexto transaccional para las operaciones de escritura.
     */
    public T create(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
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
     * Busca una entidad por su clave primaria.
     * Esta es la operación READ de CRUD.
     */
    public T findById(K id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene todos los registros de la entidad.
     * Usamos TypedQuery para tener seguridad de tipos en el resultado.
     */
    public List<T> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<T> query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza una entidad existente en la base de datos.
     * En JPA usamos merge para sincronizar el estado de la entidad con el contexto.
     */
    public T update(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
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
     * Elimina una entidad por su clave primaria.
     * Primero buscamos la entidad dentro del contexto para poder borrarla.
     */
    public void delete(K id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
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
