package controladores;

import entidades.Pokemon;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Controlador de operaciones CRUD para la entidad Pokemon.
 */
public class PokemonController {

    public Pokemon create(Pokemon pokemon) {
        // Creamos un EntityManager que representa el contexto de persistencia.
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Persist añade la entidad al contexto. Al hacer commit se guarda en la base.
            em.persist(pokemon);
            tx.commit();
            return pokemon;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Pokemon findById(Integer id) {
        // EntityManager.find busca una entidad gestionada por su ID en el contexto.
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Pokemon.class, id);
        } finally {
            em.close();
        }
    }

    public List<Pokemon> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Pokemon> query = em.createQuery("SELECT p FROM Pokemon p", Pokemon.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Pokemon update(Pokemon pokemon) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // merge sincroniza la entidad con la base de datos y devuelve la instancia gestionada.
            Pokemon merged = em.merge(pokemon);
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
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Buscar la entidad gestionada antes de eliminarla es necesario para remove.
            Pokemon pokemon = em.find(Pokemon.class, id);
            if (pokemon != null) {
                em.remove(pokemon);
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
