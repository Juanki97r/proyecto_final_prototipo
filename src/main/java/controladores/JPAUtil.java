package controladores;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * CLASE JPAUtil
 * ═════════════════════════════════════════════════════════════════════════════════
 * Utilidad SINGLETON para centralizar la creación del EntityManager.
 * 
 * PATRÓN: SINGLETON
 *   - Una única instancia de EntityManagerFactory por aplicación (creada en static {})
 *   - Cada operación CRUD obtiene su propio EntityManager de la fábrica
 *   - Se cierra la fábrica al terminar la aplicación
 * 
 * ¿QUÉ ES EntityManagerFactory?
 *   • Es una "fábrica" que crea EntityManagers
 *   • Configuración pesada: Lee persistence.xml una sola vez
 *   • DEBE crearse una única vez en toda la aplicación
 *   • Mantiene pools de conexión a la base de datos
 *   • Es thread-safe (seguro en multi-threading)
 * 
 * ¿QUÉ ES EntityManager?
 *   • Es un "contexto de persistencia"
 *   • Representa una conexión a la base de datos (para una operación)
 *   • Gestiona la vida útil de las entidades (managed, detached, etc.)
 *   • DEBE cerrarse después de usarlo
 *   • NO es thread-safe (no debe compartirse entre threads)
 * 
 * FLUJO EN LA APLICACIÓN:
 *   1. Aplicación inicia → static {} crea EntityManagerFactory
 *   2. Cada CRUD → Obtiene EntityManager de la fábrica
 *   3. Cada operación usa su propio EntityManager
 *   4. EntityManager se cierra al terminar la operación
 *   5. Aplicación termina → JPAUtil.close() cierra la fábrica
 */
public class JPAUtil {
    
    /**
     * ENTITY_MANAGER_FACTORY: La fábrica singleton
     * 
     * - static: Una única instancia compartida por toda la aplicación
     * - private: Solo accesible dentro de esta clase
     * - Se inicializa en el bloque static {}, no en el constructor
     */
    private static EntityManagerFactory ENTITY_MANAGER_FACTORY;
    
    /**
     * initializationError: Guarda excepciones durante la inicialización
     * 
     * Útil para depuración: Si falla la conexión, guardamos el error
     * para poder reportarlo al usuario más tarde
     */
    private static Exception initializationError;

    /**
     * BLOQUE STATIC: Se ejecuta una sola vez cuando se carga la clase
     * 
     * Aquí inicializamos la EntityManagerFactory.
     * Si hay error (BD no accesible, persistence.xml mal formado, etc.)
     * lo capturamos y lo guardamos.
     */
    static {
        try {
            // Persistence.createEntityManagerFactory(nombre)
            //   • Lee el archivo persistence.xml
            //   • Busca la unidad de persistencia con ese nombre ("pokemonPU")
            //   • Crea la EntityManagerFactory con esa configuración
            //   • Establece la conexión a la BD (pool inicial)
            ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("pokemonPU");
        } catch (Exception ex) {
            // Si algo falla (MySQL no corriendo, credenciales mal, etc.)
            // lo guardamos para reportarlo luego
            initializationError = ex;
            System.err.println("Error al inicializar el EntityManagerFactory:");
            ex.printStackTrace();
        }
    }

    /**
     * Devuelve un nuevo EntityManager para ejecutar operaciones JPA
     * 
     * PATRÓN IMPORTANTE:
     *   • Cada método que acceda a la BD DEBE:
     *     1. Obtener un EntityManager con este método
     *     2. Usarlo para la operación
     *     3. Cerrarlo en un bloque finally
     * 
     * EJEMPLO CORRECTO:
     *   EntityManager em = JPAUtil.getEntityManager();
     *   try {
     *       // hacer operación
     *   } finally {
     *       em.close();  // SIEMPRE cerrar
     *   }
     * 
     * @return Un nuevo EntityManager
     * @throws RuntimeException Si la BD no fue inicializada correctamente
     */
    public static EntityManager getEntityManager() {
        if (ENTITY_MANAGER_FACTORY == null) {
            // Si la fábrica es null, significa que falló la inicialización
            throw new RuntimeException("Error: No se pudo conectar a la base de datos. " +
                "Verifica que MySQL esté corriendo y la configuración de persistence.xml sea correcta.\n" +
                "Detalles: " + (initializationError != null ? initializationError.getMessage() : "Unknown error"));
        }
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    /**
     * Cierra la fábrica de EntityManager cuando la aplicación finaliza
     * 
     * IMPORTANTE: Llamar esto UNA SOLA VEZ al cerrar la aplicación
     * 
     * En Main.java, se llama así:
     *   window.addWindowListener(new WindowAdapter() {
     *       public void windowClosed(WindowEvent e) {
     *           JPAUtil.close();  // ← Aquí se cierra la fábrica
     *           System.exit(0);
     *       }
     *   });
     * 
     * ¿QUÉ HACE?
     *   • Cierra la EntityManagerFactory
     *   • Cierra todos los EntityManagers activos
     *   • Cierra el pool de conexiones a la BD
     *   • Libera recursos (conexiones, memoria, etc.)
     */
    public static void close() {
        if (ENTITY_MANAGER_FACTORY != null && ENTITY_MANAGER_FACTORY.isOpen()) {
            ENTITY_MANAGER_FACTORY.close();
        }
    }
}
