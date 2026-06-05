package vistas;

/**
 * CLASE VIEWUTILS
 * ═════════════════════════════════════════════════════════════════════════════════
 * Utilidad para detectar y manejar errores específicos de la interfaz gráfica.
 * 
 * PATRÓN: UTILITY CLASS (clase de utilidad)
 *   • No tiene estado (sin atributos de instancia)
 *   • Solo métodos static (estáticos)
 *   • Constructor privado → No se puede instanciar
 * 
 * CASOS DE USO:
 *   • Detectar si el error es por clave primaria duplicada
 *   • Mostrar mensajes de error personalizados al usuario
 *   • Reutilizar este código en múltiples vistas
 * 
 * ¿POR QUÉ AQUÍ?
 *   • Las vistas (JFrame, JDialog) necesitan manejar errores de BD
 *   • Centralizamos la lógica de detección de errores
 *   • Si cambia el mensaje, lo cambios en UN lugar
 */
public final class ViewUtils {

    /**
     * Constructor privado
     * Impide que se instancie esta clase de utilidad
     * 
     * Comentario: "Helper class, no instances."
     * (Clase auxiliar, no se crean instancias)
     */
    private ViewUtils() {
        // Helper class, no instances.
    }

    /**
     * Detecta si un error es por clave primaria duplicada
     * 
     * ¿POR QUÉ NECESITAMOS ESTO?
     *   • El usuario intenta crear un Entrenador con ID que ya existe
     *   • La BD lanza una excepción (EntityExistsException o similar)
     *   • Queremos mostrar un mensaje amigable al usuario
     *   • En lugar de: "Duplicate entry '1' for key 'PRIMARY'"
     *   • Mostramos: "No se puede crear Entrenador porque la clave primaria ya existe"
     * 
     * PATRÓN: Exception Chaining
     *   • Una excepción puede contener otra excepción (getCause())
     *   • Recorremos toda la cadena de excepciones
     *   • Buscamos palabras clave: "duplicate", "unique", "primary key"
     * 
     * EJEMPLO:
     *   try {
     *       controller.create(entrenador);
     *   } catch (Exception ex) {
     *       if (ViewUtils.esClavePrimariaDuplicada(ex)) {
     *           JOptionPane.showMessageDialog(..., ViewUtils.mensajeClaveDuplicada("Entrenador"));
     *       }
     *   }
     * 
     * @param ex La excepción capturada desde la BD
     * @return true si es un error de clave primaria duplicada, false en otro caso
     */
    public static boolean esClavePrimariaDuplicada(Throwable ex) {
        // Recorremos la cadena de excepciones (exception chain)
        while (ex != null) {
            // Obtenemos el mensaje de la excepción actual
            String mensaje = ex.getMessage();
            
            // Comprobamos si es un error específico de clave primaria
            if (ex instanceof javax.persistence.EntityExistsException ||
                (mensaje != null && mensaje.toLowerCase().contains("duplicate")) ||
                (mensaje != null && mensaje.toLowerCase().contains("unique")) ||
                (mensaje != null && mensaje.toLowerCase().contains("primary key")) ||
                (mensaje != null && mensaje.toLowerCase().contains("clave primaria"))) {
                return true;
            }
            
            // Pasamos a la siguiente excepción en la cadena (getCause)
            ex = ex.getCause();
        }
        // Si llegamos aquí, no es un error de clave duplicada
        return false;
    }

    /**
     * Genera el mensaje de error para clave primaria duplicada
     * 
     * @param entidad El nombre de la entidad (ej: "Entrenador", "Pokémon")
     * @return El mensaje personalizado para mostrar al usuario
     * 
     * EJEMPLO:
     *   mensajeClaveDuplicada("Entrenador")
     *   → "No se puede crear Entrenador porque la clave primaria ya existe"
     */
    public static String mensajeClaveDuplicada(String entidad) {
        return "No se puede crear " + entidad + " porque la clave primaria ya existe";
    }
}
