package controladores;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * CLASE GENERICSERVICE
 * ═════════════════════════════════════════════════════════════════════════════════
 * Servicio genérico que implementa operaciones CRUD básicas para CUALQUIER entidad.
 * 
 * PATRÓN: TEMPLATE METHOD (plantilla genérica)
 *   • T = Tipo de entidad (ej: Entrenador, Pokemon, etc.)
 *   • K = Tipo de clave primaria (ej: Integer, String, etc.)
 * 
 * ¿POR QUÉ GENÉRICO?
 *   • Evita duplicar código CRUD en cada controlador
 *   • Centraliza la lógica de transacciones
 *   • Asegura consistencia en manejo de EntityManager
 * 
 * OPERACIONES IMPLEMENTADAS:
 *   • CREATE: Crear una nueva entidad
 *   • READ: Buscar por ID o listar todas
 *   • UPDATE: Actualizar una entidad existente
 *   • DELETE: Eliminar por ID
 * 
 * NOTA: Esta clase es ABSTRACTA (abstract)
 *   • No se instancia directamente
 *   • Se heredan controladores concretos:
 *     EntrenadorController, PokemonController, etc.
 */
public abstract class GenericService<T, K> {

    /**
     * entityClass: La clase de la entidad (ej: Entrenador.class)
     * 
     * Se necesita porque al usar genéricos, la información de tipos
     * se pierde en tiempo de ejecución (type erasure en Java).
     * Almacenarla permite hacer operaciones como:
     *   em.find(entityClass, id)
     *   em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + "...")
     */
    private final Class<T> entityClass;

    /**
     * Constructor protegido
     * 
     * Debe ser llamado por subclases como:
     *   public PokemonController() {
     *       super(Pokemon.class);
     *   }
     * 
     * @param entityClass La clase de la entidad a gestionar
     */
    protected GenericService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * CREATE: Crea una entidad en la base de datos
     * 
     * PATRÓN DE TRANSACCIÓN:
     *   1. Obtener EntityManager
     *   2. Obtener EntityTransaction
     *   3. tx.begin() → Inicia transacción
     *   4. em.persist(entidad) → Marca para insertar
     *   5. tx.commit() → Ejecuta en BD
     *   6. Catch: Si hay error → tx.rollback() (deshacer cambios)
     *   7. Finally: em.close() (liberar recursos)
     * 
     * ¿QUÉ ES persist()?
     *   • Marca la entidad como "managed" (gestionada)
     *   • Cuando hace commit, Hibernate genera INSERT
     *   • Si ya existe la clave primaria → excepción
     * 
     * @param entity La entidad a crear
     * @return La entidad creada (con ID asignado si es auto-incremental)
     */
    public T create(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // persist(): Inserta la entidad en la BD cuando hace commit
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (RuntimeException ex) {
            // Si hay error durante la inserción
            if (tx.isActive()) {
                // Si la transacción está activa, la deshacemos
                tx.rollback();
            }
            throw ex; // Relanzamos la excepción para que la vista la maneje
        } finally {
            // SIEMPRE cerrar EntityManager para liberar conexión
            em.close();
        }
    }

    /**
     * READ: Busca una entidad por su clave primaria
     * 
     * ¿QUÉ OCURRE?
     *   • EntityManager.find(Class<T>, Object pk)
     *   • Busca EN LA BASE DE DATOS la entidad con esa clave
     *   • Si existe, la retorna (managed)
     *   • Si no existe, retorna null
     * 
     * EJEMPLO:
     *   Entrenador ash = entrenadorController.findById(1);
     *   // Busca el Entrenador con ID 1
     * 
     * @param id La clave primaria a buscar
     * @return La entidad encontrada, o null si no existe
     */
    public T findById(K id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // find(): Busca por clave primaria en la BD
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * READ ALL: Obtiene TODAS las entidades de la tabla
     * 
     * PATRÓN JPQL (JPA Query Language):
     *   • Es SQL orientado a objetos (usa nombres de clases, no tablas)
     *   • TypedQuery<T>: Query con tipo genérico (type-safe)
     *   • "SELECT e FROM " + entityClass.getSimpleName() + " e"
     *     ej: SELECT e FROM Pokemon e
     * 
     * @return Lista de todas las entidades (puede estar vacía)
     */
    public List<T> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // TypedQuery: Query con tipo genérico (seguro de tipos)
            // getResultList(): Retorna lista de resultados
            TypedQuery<T> query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * UPDATE: Actualiza una entidad existente
     * 
     * PATRÓN merge():
     *   • La entidad que pasas puede venir de FUERA del contexto (detached)
     *   • merge() la sincroniza con el contexto actual
     *   • Retorna la instancia "managed" (gestionada)
     *   • Cuando hace commit, Hibernate genera UPDATE
     * 
     * DIFERENCIA persist() vs merge():
     *   • persist(): Para entidades NUEVAS (INSERT)
     *   • merge(): Para entidades EXISTENTES (UPDATE)
     * 
     * EJEMPLO:
     *   Entrenador ash = new Entrenador();
     *   ash.setIdentrenador(1);
     *   ash.setNombre("Ash Nuevo");
     *   entrenadorController.update(ash);  // Actualiza nombre en BD
     * 
     * @param entity La entidad con cambios
     * @return La entidad actualizada
     */
    public T update(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // merge(): Sincroniza la entidad con el contexto y la BD
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
     * DELETE: Elimina una entidad por su clave primaria
     * 
     * PASOS:
     *   1. Obtener EntityManager con transacción
     *   2. BUSCAR la entidad con find() (debe estar managed)
     *   3. ELIMINAR con remove()
     *   4. HACER COMMIT (Hibernate genera DELETE)
     * 
     * ¿POR QUÉ DEBE ESTAR MANAGED?
     *   • EntityManager.remove() solo funciona con entidades managed
     *   • Si pasas una entidad detached, falla
     *   • Por eso primero hacemos find()
     * 
     * @param id La clave primaria a eliminar
     */
    public void delete(K id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Primero buscamos la entidad (debe estar managed para remove)
            T entity = em.find(entityClass, id);
            if (entity != null) {
                // remove(): Marca para eliminar. En commit → DELETE en BD
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
