package controladores;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Clase auxiliar para centralizar la creación del EntityManager.
 *
 * EntityManagerFactory es una fábrica pesada que solo debe crearse una vez
 * por aplicación. A partir de ella se crea un EntityManager por operación.
 */
public class JPAUtil {
    private static EntityManagerFactory ENTITY_MANAGER_FACTORY;
    private static Exception initializationError;

    static {
        try {
            ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("pokemonPU");
        } catch (Exception ex) {
            initializationError = ex;
            System.err.println("Error al inicializar el EntityManagerFactory:");
            ex.printStackTrace();
        }
    }

    /**
     * Devuelve un nuevo EntityManager para ejecutar operaciones JPA.
     * Cada método CRUD debe usar su propio EntityManager y cerrarlo al terminar.
     * @throws RuntimeException si hay error de inicialización de la BD
     */
    public static EntityManager getEntityManager() {
        if (ENTITY_MANAGER_FACTORY == null) {
            throw new RuntimeException("Error: No se pudo conectar a la base de datos. " +
                "Verifica que MySQL esté corriendo y la configuración de persistence.xml sea correcta.\n" +
                "Detalles: " + (initializationError != null ? initializationError.getMessage() : "Unknown error"));
        }
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    /**
     * Cierra la fábrica de EntityManager cuando la aplicación finaliza.
     */
    public static void close() {
        if (ENTITY_MANAGER_FACTORY != null && ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }
}
