package backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * CLASE BACKUPMANAGER
 * ═════════════════════════════════════════════════════════════════════════════════
 * Gestor completo de copias de seguridad en formato CSV para la BD Pokémon.
 * 
 * PROPÓSITO:
 *   • Crear copias de seguridad (backups) de toda la base de datos
 *   • Restaurar desde una copia de seguridad anterior
 *   • Gestionar la integridad referencial durante operaciones de backup/restore
 * 
 * FUNCIONAMIENTO:
 * 
 * BACKUP (Crear copia):
 *   1. Crea carpeta: backups/YYYYMMDD_HHMMSS/
 *   2. Exporta cada tabla a un archivo CSV
 *   3. Cada CSV tiene: Cabecera (nombres de columnas) + Datos
 *   4. Retorna el Path de la carpeta creada
 * 
 * RESTORE (Restaurar copia):
 *   1. Borra TODOS los datos en orden correcto (respeta FK)
 *   2. Lee cada archivo CSV
 *   3. Inserta datos en el mismo orden
 *   4. Registra la restauración en last_restore.txt
 * 
 * ¿POR QUÉ CSV?
 *   • Formato portable (editable, entendible)
 *   • No requiere SQL dump (más seguro)
 *   • Fácil de revisar manualmente
 *   • Compatible con Excel/Calc
 * 
 * INTEGRIDAD REFERENCIAL:
 *   • DELETE_TABLES: Tablas hijas primero, padres después
 *   • RESTORE_TABLES: Tablas padres primero, hijas después
 *   • Ejemplo: Borra Medallas antes que Gimnasio (FK)
 */
public class BackupManager {

    /**
     * CONSTANTES DE CONFIGURACIÓN
     * ════════════════════════════════════════════════════════════════════════════
     */

    /** Carpeta raíz donde se guardan todas las copias de seguridad */
    private static final String BACKUP_ROOT = "backups";

    /**
     * CREDENCIALES JDBC
     * 
     * IMPORTANTE: Deben coincidir con persistence.xml
     * • JDBC_URL: Dirección de la BD
     *   - 127.0.0.1:3306 → MySQL local, puerto 3306
     *   - proyectopokemon → nombre de la BD
     *   - serverTimezone=UTC → Zona horaria (evita warnings)
     * • JDBC_USER / PASSWORD: Usuario y contraseña MySQL
     */
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/proyectopokemon?serverTimezone=UTC";
    private static final String JDBC_USER = "juancarlos";
    private static final String JDBC_PASSWORD = "1234";

    /**
     * BACKUP_TABLES: Orden de exportación (no crítico)
     * El orden de exportación no afecta la integridad (no hay transacción)
     */
    private static final String[] BACKUP_TABLES = {
            "Region",
            "Entrenador",
            "Tipo",
            "Pokemon",
            "Gimnasio",
            "Equipo",
            "Medallas",
            "DetallePokemon",
            "DetalleEquipo"
    };

    /**
     * RESTORE_TABLES: Orden de RESTAURACIÓN (CRÍTICO)
     * 
     * Restauramos en este orden PORQUE:
     *   1. Region → Base de datos (no tiene FK)
     *   2. Entrenador → Base de datos (no tiene FK)
     *   3. Tipo → Base de datos (no tiene FK)
     *   4. Pokemon → Base de datos (no tiene FK)
     *   5. Gimnasio → Refiere a Region (Region ya existe)
     *   6. Equipo → Refiere a Entrenador (Entrenador ya existe)
     *   7. Medallas → Refiere a Entrenador y Gimnasio (ambos existen)
     *   8. DetallePokemon → Refiere a Pokemon y Tipo (ambos existen)
     *   9. DetalleEquipo → Refiere a Equipo y Pokemon (ambos existen)
     * 
     * Si restauramos en orden incorrecto:
     *   • Gymnasio se inserta ANTES de Region → ERROR FK
     *   • Datos no se restauran correctamente
     */
    private static final String[] RESTORE_TABLES = {
            "Region",
            "Entrenador",
            "Tipo",
            "Pokemon",
            "Gimnasio",
            "Equipo",
            "Medallas",
            "DetallePokemon",
            "DetalleEquipo"
    };

    /** Archivo que almacena info de la última restauración */
    private static final Path LAST_RESTORE_FILE = Paths.get(BACKUP_ROOT).resolve("last_restore.txt");

    /**
     * DELETE_TABLES: Orden de BORRADO (CRÍTICO)
     * 
     * Borramos en orden INVERSO a la creación:
     *   • Primero: Tablas hijas (DetalleEquipo, DetallePokemon, Medallas)
     *   • Después: Tablas intermedias (Equipo, Gimnasio, Pokemon, Tipo)
     *   • Finalmente: Tablas padres (Entrenador, Region)
     * 
     * Si borramos en orden incorrecto:
     *   • Intenta borrar Entrenador pero Equipo refiere a él → ERROR FK
     *   • La base de datos impide el borrado
     */
    private static final String[] DELETE_TABLES = {
            "DetalleEquipo",
            "DetallePokemon",
            "Medallas",
            "Equipo",
            "Gimnasio",
            "Pokemon",
            "Tipo",
            "Entrenador",
            "Region"
    };

    /**
     * CREAR COPIA DE SEGURIDAD
     * ════════════════════════════════════════════════════════════════════════════
     * 
     * Crea una carpeta con timestamp y exporta todas las tablas a CSV.
     * 
     * OPERACIONES:
     *   1. Crear carpeta: backups/YYYYMMDD_HHMMSS/
     *   2. Para cada tabla en BACKUP_TABLES:
     *      a. Ejecutar: SELECT * FROM tabla
     *      b. Escribir resultado a: tabla.csv
     *      c. Primera línea: nombres de columnas
     *      d. Siguientes líneas: datos (uno por fila)
     * 
     * EJEMPLO DE CSV:
     *   ──────────────────────────────────────────
     *   "identrenador","nombre","edad"
     *   "1","Ash Ketchum","10"
     *   "2","Misty","10"
     *   ──────────────────────────────────────────
     * 
     * MANEJO DE ERRORES:
     *   • SQLException: Si MySQL no responde
     *   • IOException: Si no puedo escribir archivos (permisos)
     * 
     * @return Path de la carpeta creada (ej: backups/20260604_153021/)
     * @throws SQLException si hay error en SELECT de la BD
     * @throws IOException  si hay error escribiendo archivos CSV
     */
    public Path crearCopiaDeSeguridad() throws SQLException, IOException {
        // Crear carpeta raíz si no existe
        Path backupsRoot = Paths.get(BACKUP_ROOT);
        if (Files.notExists(backupsRoot)) {
            Files.createDirectories(backupsRoot);
        }

        // Generar timestamp: YYYYMMDD_HHMMSS
        // Ejemplo: 20260604_153021 (4 junio 2026, 15:30:21)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFolder = backupsRoot.resolve(timestamp);
        Files.createDirectories(backupFolder);

        // Conectar a BD y exportar cada tabla
        try (Connection connection = obtenerConexion()) {
            for (String tabla : BACKUP_TABLES) {
                exportarTablaACsv(connection, tabla, backupFolder);
            }
        }

        return backupFolder;
    }

    /**
     * RESTAURAR ÚLTIMA COPIA
     * ════════════════════════════════════════════════════════════════════════════
     * 
     * Restaura la copia de seguridad más reciente encontrada.
     * Busca la carpeta más nueva en backups/ (orden alfabético por timestamp).
     * 
     * FLUJO:
     *   1. Buscar última copia: obtenerUltimaCopia()
     *   2. Si no existe: lanzar IOException
     *   3. Si existe: llamar a restaurarCopia(path)
     * 
     * @return Path de la carpeta restaurada
     * @throws SQLException si error en DELETE/INSERT a BD
     * @throws IOException  si falta archivo CSV o no se encuentra copia
     */
    public Path restaurarUltimaCopia() throws SQLException, IOException {
        Path ultimaCopia = obtenerUltimaCopia();
        if (ultimaCopia == null) {
            throw new IOException("No se encontró ninguna copia de seguridad para restaurar.");
        }
        return restaurarCopia(ultimaCopia);
    }

    /**
     * RESTAURAR COPIA ESPECÍFICA
     * ════════════════════════════════════════════════════════════════════════════
     * 
     * Restaura una copia seleccionada por el usuario.
     * 
     * PROCESO (TRANSACCIONAL):
     *   1. Validar que la carpeta existe
     *   2. Obtener conexión JDBC (setAutoCommit false)
     *   3. try:
     *      a. borrarTodosLosDatos() → DELETE en orden correcto (respeta FK)
     *      b. restaurarDesdeCsv() → INSERT desde CSVs
     *      c. connection.commit() → Ejecuta ambas operaciones
     *   4. catch: connection.rollback() → Deshace cambios si hay error
     *   5. finally: connection.setAutoCommit(true) → Vuelve a modo automático
     * 
     * TRANSACCIÓN ATOMICIDAD (ACID):
     *   • Atómica: O todo se ejecuta o nada
     *   • Consistente: Integridad referencial siempre respetada
     *   • Aislada: No interfiere con otras conexiones
     *   • Durable: Una vez committed, persiste
     * 
     * EJEMPLO:
     *   Entrenador con Equipo:
     *   • Si falla después de borrar Entrenador pero antes de insertar:
     *   • rollback() rehace todo: Entrenador vuelve a estar
     *   • Datos nunca se quedan en estado incompleto
     * 
     * @param copia Path de la carpeta backup a restaurar
     * @return Path de la carpeta restaurada
     * @throws SQLException si error en operaciones BD
     * @throws IOException  si falta archivo CSV o permisos
     */
    public Path restaurarCopia(Path copia) throws SQLException, IOException {
        // Validar que la carpeta existe y es un directorio
        if (copia == null || Files.notExists(copia) || !Files.isDirectory(copia)) {
            throw new IOException("La copia de seguridad seleccionada no existe: " + copia);
        }

        // Obtener conexión e iniciar transacción manual
        try (Connection connection = obtenerConexion()) {
            // setAutoCommit(false): No commitea automáticamente cada SQL
            // Esperamos a commit() o rollback() manual
            connection.setAutoCommit(false);
            try {
                // PASO 1: Borrar todos los datos en orden correcto
                borrarTodosLosDatos(connection);
                
                // PASO 2: Restaurar desde CSVs
                restaurarDesdeCsv(connection, copia);
                
                // PASO 3: Ejecutar ambas operaciones (commit de la transacción)
                connection.commit();
                
                // PASO 4: Registrar en archivo la restauración que se hizo
                registrarRestauracion(copia);
            } catch (SQLException | IOException ex) {
                // Si algo falla: rollback (deshacer cambios)
                connection.rollback();
                throw ex;
            } finally {
                // Volver a autocommit automático
                connection.setAutoCommit(true);
            }
        }

        return copia;
    }

    /**
     * LISTAR TODAS LAS COPIAS DISPONIBLES
     * ════════════════════════════════════════════════════════════════════════════
     * 
     * @return Lista de Paths ordenadas de antigua a nueva
     * @throws IOException si no se puede leer la carpeta backups/
     */
    public List<Path> listarCopias() throws IOException {
        Path backupsRoot = Paths.get(BACKUP_ROOT);
        if (Files.notExists(backupsRoot) || !Files.isDirectory(backupsRoot)) {
            return Collections.emptyList();
        }

        List<Path> carpetas = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupsRoot)) {
            for (Path archivo : stream) {
                if (Files.isDirectory(archivo)) {
                    carpetas.add(archivo);
                }
            }
        }

        carpetas.sort(Comparator.comparing(Path::getFileName));
        return carpetas;
    }

    /**
     * OBTENER INFORMACIÓN DE BACKUP
     * ════════════════════════════════════════════════════════════════════════════
     * 
     * Muestra al usuario:
     *   • Cuál fue la última copia de seguridad realizada
     *   • Cuándo fue la última restauración
     *   • Desde qué copia se restauró
     * 
     * Se usa en MainWindow → Botón "Estado de Backup"
     * 
     * @return String con información formateada
     * @throws IOException si no se pueden leer archivos
     */
    public String obtenerInformacionBackup() throws IOException {
        StringBuilder info = new StringBuilder();

        Path ultimaCopia = obtenerUltimaCopia();
        if (ultimaCopia != null) {
            info.append("Última copia de seguridad: ").append(ultimaCopia.toAbsolutePath()).append("\n");
        } else {
            info.append("No se han encontrado copias de seguridad.\n");
        }

        if (Files.exists(LAST_RESTORE_FILE)) {
            List<String> lineas = Files.readAllLines(LAST_RESTORE_FILE, StandardCharsets.UTF_8);
            if (!lineas.isEmpty()) {
                info.append("Última restauración: ").append(lineas.get(0)).append("\n");
                if (lineas.size() > 1) {
                    info.append("Carpeta restaurada: ").append(lineas.get(1)).append("\n");
                }
            }
        } else {
            info.append("No se ha registrado ninguna restauración.");
        }

        return info.toString();
    }

    /**
     * Obtiene una conexión JDBC a la base de datos.
     */
    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    /**
     * Exporta una tabla a un archivo CSV dentro de la carpeta de backup.
     */
    private void exportarTablaACsv(Connection connection, String tabla, Path carpetaBackup)
            throws SQLException, IOException {
        String consulta = "SELECT * FROM " + tabla;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(consulta)) {

            Path archivoCsv = carpetaBackup.resolve(tabla + ".csv");
            try (BufferedWriter writer = Files.newBufferedWriter(archivoCsv, StandardCharsets.UTF_8)) {
                escribirCsv(resultSet, writer);
            }
        }
    }

    /**
     * Escribe el contenido de un ResultSet en formato CSV.
     *
     * Cada fila se guarda como una línea con todas las columnas.
     * La primera línea es la cabecera con los nombres de columnas.
     */
    private void escribirCsv(ResultSet resultSet, BufferedWriter writer) throws SQLException, IOException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnas = metaData.getColumnCount();

        // Primera línea con nombres de columnas
        for (int i = 1; i <= columnas; i++) {
            writer.write(escapeCsv(metaData.getColumnLabel(i)));
            if (i < columnas) {
                writer.write(',');
            }
        }
        writer.newLine();

        // Filas de datos
        while (resultSet.next()) {
            for (int i = 1; i <= columnas; i++) {
                Object valor = resultSet.getObject(i);
                writer.write(escapeCsv(valor != null ? valor.toString() : ""));
                if (i < columnas) {
                    writer.write(',');
                }
            }
            writer.newLine();
        }
    }

    /**
     * Convierte un texto en una celda CSV válida, escapando comillas dobles.
     */
    private String escapeCsv(String texto) {
        if (texto == null) {
            return "";
        }
        String escaped = texto.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Busca la última carpeta de backup creada ordenando por nombre.
     */
    private Path obtenerUltimaCopia() throws IOException {
        Path backupsRoot = Paths.get(BACKUP_ROOT);
        if (Files.notExists(backupsRoot) || !Files.isDirectory(backupsRoot)) {
            return null;
        }

        List<Path> carpetas = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupsRoot)) {
            for (Path archivo : stream) {
                if (Files.isDirectory(archivo)) {
                    carpetas.add(archivo);
                }
            }
        }

        if (carpetas.isEmpty()) {
            return null;
        }

        carpetas.sort(Comparator.comparing(Path::getFileName));
        return carpetas.get(carpetas.size() - 1);
    }

    /**
     * Borra todos los datos de la base de datos en el orden correcto.
     *
     * Es importante borrar primero las tablas hijas (que tienen claves foráneas)
     * y después las tablas padre.
     */
    private void borrarTodosLosDatos(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String tabla : DELETE_TABLES) {
                statement.executeUpdate("DELETE FROM " + tabla);
            }
        }
    }

    /**
     * Registra la fecha y carpeta de la última restauración para mostrarla en la interfaz.
     */
    private void registrarRestauracion(Path carpetaBackup) throws IOException {
        Path backupsRoot = Paths.get(BACKUP_ROOT);
        if (Files.notExists(backupsRoot)) {
            Files.createDirectories(backupsRoot);
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String contenido = timestamp + System.lineSeparator() + carpetaBackup.toAbsolutePath().toString();
        Files.writeString(LAST_RESTORE_FILE, contenido, StandardCharsets.UTF_8);
    }

    /**
     * Restaura los datos desde los archivos CSV.
     *
     * Inserta primero las tablas padre y después las tablas hijas.
     */
    private void restaurarDesdeCsv(Connection connection, Path carpetaBackup) throws SQLException, IOException {
        for (String tabla : RESTORE_TABLES) {
            Path archivoCsv = carpetaBackup.resolve(tabla + ".csv");
            if (Files.exists(archivoCsv)) {
                insertarDatosDesdeCsv(connection, tabla, archivoCsv);
            } else {
                throw new IOException("Archivo de copia no encontrado: " + archivoCsv);
            }
        }
    }

    /**
     * Inserta las filas de un CSV en la tabla indicada.
     */
    private void insertarDatosDesdeCsv(Connection connection, String tabla, Path archivoCsv)
            throws SQLException, IOException {
        try (BufferedReader reader = Files.newBufferedReader(archivoCsv, StandardCharsets.UTF_8)) {
            String linea = reader.readLine();
            if (linea == null) {
                return;
            }

            String[] columnas = parseCsvLine(linea);
            String sql = construirSqlInsert(tabla, columnas);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                while ((linea = reader.readLine()) != null) {
                    String[] valores = parseCsvLine(linea);
                    rellenarParametros(statement, valores);
                    statement.executeUpdate();
                }
            }
        }
    }

    /**
     * Construye la sentencia SQL INSERT con tantos placeholders como columnas haya.
     */
    private String construirSqlInsert(String tabla, String[] columnas) {
        String columnasCsv = String.join(",", columnas);
        String placeholders = String.join(",", Collections.nCopies(columnas.length, "?"));
        return "INSERT INTO " + tabla + " (" + columnasCsv + ") VALUES (" + placeholders + ")";
    }

    /**
     * Rellena los parámetros de un PreparedStatement a partir de los valores CSV.
     */
    private void rellenarParametros(PreparedStatement statement, String[] valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i];
            if (valor == null || valor.isEmpty()) {
                statement.setNull(i + 1, java.sql.Types.VARCHAR);
            } else if (esFecha(valor)) {
                statement.setDate(i + 1, Date.valueOf(valor));
            } else {
                statement.setString(i + 1, valor);
            }
        }
    }

    /**
     * Detecta si un texto está en formato de fecha ISO (YYYY-MM-DD).
     * Esto se usa solo para convertir la columna fecha de Medallas correctamente.
     */
    private boolean esFecha(String texto) {
        return texto.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Parsea una línea CSV respetando comillas dobles y comas internas.
     */
    private String[] parseCsvLine(String linea) {
        List<String> valores = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroDeComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (dentroDeComillas) {
                if (c == '"') {
                    if (i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                        actual.append('"');
                        i++;
                    } else {
                        dentroDeComillas = false;
                    }
                } else {
                    actual.append(c);
                }
            } else {
                if (c == '"') {
                    dentroDeComillas = true;
                } else if (c == ',') {
                    valores.add(actual.toString());
                    actual.setLength(0);
                } else {
                    actual.append(c);
                }
            }
        }

        valores.add(actual.toString());
        return valores.toArray(new String[0]);
    }
}
