# Documentación técnica del proyecto

## 1. Descripción general

Este proyecto es una aplicación de escritorio Java para gestionar una base de datos MySQL de tipo Pokémon. Utiliza:

- Java 21
- Maven como gestor de dependencias y compilación
- JPA con EclipseLink para persistencia de datos
- MySQL como base de datos relacional
- Swing para la interfaz gráfica de usuario
- Backup/restore con archivos CSV para copia de seguridad manual


## 2. Estructura del proyecto

Carpetas principales:

- `pom.xml` - configuración del proyecto Maven y dependencias.
- `src/main/java` - código fuente Java.
  - `daw` - clase `Main` que inicia la aplicación.
  - `controladores` - manejadores de persistencia JPA y controladores CRUD.
  - `entidades` - clases JPA que representan las tablas de la base de datos.
  - `vistas` - ventanas Swing y componentes de la interfaz.
  - `backup` - funcionalidad de copia de seguridad y restauración.
- `src/main/resources/META-INF/persistence.xml` - configuración de la unidad de persistencia JPA.
- `backups` - carpeta donde se guardan las copias de seguridad generadas.


## 3. Configuración y dependencias importantes

### `pom.xml`

El archivo declara las dependencias principales:

- `mysql-connector-java` para la conexión JDBC a MySQL.
- `org.eclipse.persistence.*` para JPA y EclipseLink.
- `jakarta.persistence` para las anotaciones JPA.

También configura el compilador Maven para Java 21.


## 4. Persistencia JPA

### `src/main/resources/META-INF/persistence.xml`

Define la unidad de persistencia `pokemonPU`:

- `provider`: `org.eclipse.persistence.jpa.PersistenceProvider`
- `transaction-type`: `RESOURCE_LOCAL`
- Clases JPA registradas: `Entrenador`, `Region`, `Tipo`, `Pokemon`, `Gimnasio`, `Equipo`, `Medallas`, `DetallePokemon`, `DetalleEquipo`
- Propiedades JDBC:
  - URL: `jdbc:mysql://localhost:3306/proyectopokemon?serverTimezone=UTC`
  - Usuario: `root`
  - Password: `1234`
  - Driver: `com.mysql.cj.jdbc.Driver`


## 5. Punto de entrada de la aplicación

### `src/main/java/daw/Main.java`

El flujo de inicio es:

1. Configura un manejador global de excepciones para el hilo Swing.
2. Ejecuta la interfaz gráfica en el hilo de eventos de Swing con `SwingUtilities.invokeLater(...)`.
3. Crea y muestra `MainWindow`.
4. Registra un `WindowListener` para cerrar `JPAUtil` y finalizar la aplicación cuando se cierra la ventana.

Este diseño asegura que la UI se ejecute de forma segura en el hilo correcto.


## 6. Gestión de EntityManager

### `src/main/java/controladores/JPAUtil.java`

Esta clase centraliza la creación de `EntityManagerFactory` y de los `EntityManager`:

- `ENTITY_MANAGER_FACTORY` se inicializa una sola vez.
- `getEntityManager()` entrega un `EntityManager` nuevo para cada operación.
- `close()` cierra la fábrica cuando finaliza la aplicación.

La lógica asegura que no se cree una nueva fábrica en cada consulta, lo que mejora rendimiento y evita fugas de recursos.


## 7. Entidades JPA

Las clases dentro de `src/main/java/entidades` representan las tablas de la base de datos y sus relaciones.

Cada entidad incluye:

- Anotaciones JPA como `@Entity`, `@Table`, `@Id`, `@GeneratedValue`.
- Relaciones entre entidades con `@OneToMany`, `@ManyToOne`, `@OneToOne` o `@ManyToMany` según la tabla.
- Campos que reflejan columnas de la tabla.
- Constructores, getters y setters.

Esto permite trabajar con objetos Java en lugar de SQL directo en la mayoría de las operaciones.


## 8. Controladores CRUD

El paquete `controladores` contiene los servicios que realizan operaciones CRUD sobre las entidades.

Principales responsabilidades:

- Abrir un `EntityManager` con `JPAUtil.getEntityManager()`.
- Iniciar transacciones con `EntityTransaction`.
- Realizar operaciones de persistencia como `persist`, `find`, `merge` y `remove`.
- Cerrar el `EntityManager` al finalizar.

Esto separa la lógica de datos de la lógica de la interfaz.


## 9. Interfaz gráfica

### `src/main/java/vistas/MainWindow.java`

`MainWindow` es la ventana principal que muestra botones de gestión para cada entidad y funciones adicionales:

- Botones para gestionar: Entrenadores, Regiones, Tipos, Pokémon, Gimnasios, Equipos, Medallas, Detalles Pokémon y Detalles Equipo.
- Botón para crear copia de seguridad.
- Botón para restaurar copia de seguridad.
- Botón para mostrar el estado del backup.

Los botones abren ventanas específicas de gestión (por ejemplo, `EntrenadorView`, `RegionView`, etc.).

### Interacciones principales

- `btnBackup`: llama a `BackupManager.crearCopiaDeSeguridad()`.
- `btnRestore`: permite seleccionar una copia disponible y restaura esa versión.
- `btnEstadoBackup`: muestra información de la última copia y de la última restauración.


## 10. Copias de seguridad y restauración

### `src/main/java/backup/BackupManager.java`

Este componente implementa el backup en CSV y restauración en MySQL.

#### Copia de seguridad

1. Crea la carpeta `backups/` si no existe.
2. Genera un subdirectorio con la fecha y hora actual (`yyyyMMdd_HHmmss`).
3. Para cada tabla listada en `BACKUP_TABLES`, ejecuta `SELECT * FROM <tabla>`.
4. Genera un archivo `<tabla>.csv` con cabecera y datos.

#### Restauración

1. Lista las copias disponibles con `listarCopias()`.
2. Permite restaurar la más reciente con `restaurarUltimaCopia()` o una copia específica con `restaurarCopia(Path copia)`.
3. Abre conexión JDBC directa a la base de datos.
4. Desactiva autocommit y ejecuta:
   - `DELETE FROM <tabla>` en orden de dependencias (`DELETE_TABLES`) para vaciar las tablas hijas primero.
   - Lee cada CSV y ejecuta `INSERT INTO <tabla>(...) VALUES (...)`.
5. Si ocurre un error, hace rollback para evitar estados inconsistentes.
6. Registra la restauración en `backups/last_restore.txt`.

#### CSV y parsing

- `escribirCsv(...)`: escribe columnas con comillas dobles escapadas.
- `parseCsvLine(...)`: lee líneas CSV respetando comillas internas.
- `rellenarParametros(...)`: convierte valores vacíos a `NULL` y detecta fechas en formato `YYYY-MM-DD`.


## 11. Flujo técnico de ejecución

1. El profesor ejecuta la aplicación con `mvn compile exec:java` o creando el JAR.
2. `Main.main(...)` inicia Swing.
3. Se muestra `MainWindow`.
4. El usuario usa botones para gestionar datos o para crear/recuperar backups.
5. Las ventanas de gestión usan controladores CRUD para leer/escribir en la base de datos.
6. `BackupManager` crea/restaura los datos en archivos CSV cuando se solicita.


## 12. Notas para la presentación

- Explica que el proyecto separa claramente:
  - Capa de persistencia (`entidades`, `controladores`, `persistence.xml`).
  - Capa de interfaz (`vistas`).
  - Capa de respaldo (`backup`).
- Menciona que JPA es usado con EclipseLink y que `JPAUtil` es la puerta de acceso a la base de datos.
- Señala que el backup no es un dump completo de MySQL, sino un export/import de CSV con orden de dependencias.
- Indica que la restauración seleccionable permite volver a versiones anteriores, no solo a la última copia.


## 13. Cómo ejecutar el proyecto

1. Asegurarse de que MySQL esté ejecutándose.
2. La base de datos `proyectopokemon` debe existir y tener la estructura esperada.
3. Ejecutar desde la raíz del proyecto:

```bash
mvn compile
```

4. Ejecutar la aplicación con la clase `daw.Main` o usando un IDE.
5. Usar la interfaz para crear backups, restaurarlos y gestionar registros.


## 14. Recomendaciones de estudio

- Repasa el ciclo de vida de un `EntityManager`.
- Estudia cómo Swing maneja la UI en el hilo de eventos.
- Analiza el orden de `DELETE` y `INSERT` para mantener integridad referencial.
- Comprende la funcionalidad de `BackupManager` como una alternativa de copia de seguridad fuera de JPA.
