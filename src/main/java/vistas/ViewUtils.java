package vistas;

public final class ViewUtils {

    private ViewUtils() {
        // Helper class, no instances.
    }

    public static boolean esClavePrimariaDuplicada(Throwable ex) {
        while (ex != null) {
            String mensaje = ex.getMessage();
            if (ex instanceof javax.persistence.EntityExistsException ||
                (mensaje != null && mensaje.toLowerCase().contains("duplicate")) ||
                (mensaje != null && mensaje.toLowerCase().contains("unique")) ||
                (mensaje != null && mensaje.toLowerCase().contains("primary key")) ||
                (mensaje != null && mensaje.toLowerCase().contains("clave primaria"))) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    public static String mensajeClaveDuplicada(String entidad) {
        return "No se puede crear " + entidad + " porque la clave primaria ya existe";
    }
}
