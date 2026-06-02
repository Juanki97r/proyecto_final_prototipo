package controladores;

import entidades.Entrenador;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Entrenador.
 *
 * Aquí definimos manualmente cada método para comprender cómo funciona
 * cada operación JPA en lugar de delegar en una clase genérica.
 */
public class EntrenadorController {

    /**
     * Crea un nuevo entrenador en la base de datos.
     * Se usa EntityManager.persist dentro de una transacción.
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
     * Busca un entrenador por su clave primaria.
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
     * Devuelve todos los entrenadores de la base de datos.
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
     * Actualiza un entrenador existente.
     * Se usa merge para sincronizar el estado de la entidad con el contexto de persistencia.
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
     * Elimina un entrenador por su clave primaria.
     * Primero se busca la entidad dentro del contexto y luego se elimina.
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
