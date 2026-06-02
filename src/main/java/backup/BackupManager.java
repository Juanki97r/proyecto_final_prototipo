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
 * Gestor de copias de seguridad en CSV para la base de datos Pokémon.
 *
 * Esta clase crea una carpeta de backup con la fecha y hora de ejecución,
 * genera un archivo CSV por cada tabla y permite restaurar desde la última copia.
 *
 * La restauración borra primero los datos existentes en el orden correcto
 * para preservar la integridad referencial y después inserta los datos desde CSV.
 */
public class BackupManager {

    // Carpeta raíz donde se guardan todas las copias de seguridad
    private static final String BACKUP_ROOT = "backups";

    // Conexión JDBC a la base de datos. Debe coincidir con la configuración de persistence.xml.
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/proyectopokemon?serverTimezone=UTC";
    private static final String JDBC_USER = "juancarlos";
    private static final String JDBC_PASSWORD = "1234";

    // Orden de exportación / respaldo de tablas. Este orden no necesita ser estrictamente inverso,
    // solo se usa para crear los archivos. La restauración usa otro orden que respeta dependencias.
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

    // Orden de restauración: primero tablas padre, luego tablas hijas.
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

    // Archivo que almacena la fecha de la última restauración realizada.
    private static final Path LAST_RESTORE_FILE = Paths.get(BACKUP_ROOT).resolve("last_restore.txt");

    // Orden de borrado: primero tablas hijas, luego tablas padre.
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
     * Crea una copia de seguridad completa de todas las tablas en formato CSV.
     *
     * @return Path de la carpeta creada con los CSV.
     * @throws SQLException si hay un error de consulta a la base de datos.
     * @throws IOException  si hay un error al escribir los archivos.
     */
    public Path crearCopiaDeSeguridad() throws SQLException, IOException {
        Path backupsRoot = Paths.get(BACKUP_ROOT);
        if (Files.notExists(backupsRoot)) {
            Files.createDirectories(backupsRoot);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFolder = backupsRoot.resolve(timestamp);
        Files.createDirectories(backupFolder);

        try (Connection connection = obtenerConexion()) {
            for (String tabla : BACKUP_TABLES) {
                exportarTablaACsv(connection, tabla, backupFolder);
            }
        }

        return backupFolder;
    }

    /**
     * Restaura la última copia de seguridad encontrada en la carpeta de backups.
     *
     * @return Path de la carpeta restaurada.
     * @throws SQLException si hay un error en la base de datos.
     * @throws IOException  si falta un archivo CSV o hay problema al leerlo.
     */
    public Path restaurarUltimaCopia() throws SQLException, IOException {
        Path ultimaCopia = obtenerUltimaCopia();
        if (ultimaCopia == null) {
            throw new IOException("No se encontró ninguna copia de seguridad para restaurar.");
        }
        return restaurarCopia(ultimaCopia);
    }

    /**
     * Restaura una copia de seguridad específica seleccionada por el usuario.
     *
     * @param copia Path de la carpeta de backup a restaurar.
     * @return Path de la carpeta restaurada.
     * @throws SQLException si hay un error en la base de datos.
     * @throws IOException  si falta un archivo CSV o hay problema al leerlo.
     */
    public Path restaurarCopia(Path copia) throws SQLException, IOException {
        if (copia == null || Files.notExists(copia) || !Files.isDirectory(copia)) {
            throw new IOException("La copia de seguridad seleccionada no existe: " + copia);
        }

        try (Connection connection = obtenerConexion()) {
            connection.setAutoCommit(false);
            try {
                borrarTodosLosDatos(connection);
                restaurarDesdeCsv(connection, copia);
                connection.commit();
                registrarRestauracion(copia);
            } catch (SQLException | IOException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        return copia;
    }

    /**
     * Lista todas las copias de seguridad disponibles ordenadas desde la más antigua a la más reciente.
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
     * Devuelve información del estado de backup y restauración.
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
