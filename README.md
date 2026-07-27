# WSR Biblioteca

Sistema de gestión de biblioteca desarrollado con **Spring Boot** y **Oracle Database**.

## Tecnologías

- Java 21
- Spring Boot 4.1.0
- Spring Data JDBC
- Spring MVC
- Spring Validation
- Lombok
- Oracle JDBC Driver (`ojdbc11`)
- Maven

## Requisitos previos

- JDK 21
- Oracle Database (probado con **Oracle Database Free**, PDB `FREEPDB1`)
- Maven (o usar el wrapper incluido `./mvnw`)

## Configuración de la base de datos

Conéctate a la base de datos como un usuario con privilegios (`SYSTEM` o `SYS AS SYSDBA`) sobre el PDB `FREEPDB1`:

```bash
sqlplus system/<password>@//localhost:1521/FREEPDB1
```

Y ejecuta el siguiente script para crear el usuario/esquema de la aplicación:

```sql
CREATE USER BD_BIBLIOTECA IDENTIFIED BY Rmi11dp009;

GRANT CREATE SESSION   TO BD_BIBLIOTECA;
GRANT CREATE TABLE     TO BD_BIBLIOTECA;
GRANT CREATE VIEW      TO BD_BIBLIOTECA;
GRANT CREATE SEQUENCE  TO BD_BIBLIOTECA;
GRANT CREATE PROCEDURE TO BD_BIBLIOTECA;
GRANT CREATE TRIGGER   TO BD_BIBLIOTECA;

GRANT CONNECT, RESOURCE TO BD_BIBLIOTECA;

-- Verificación: debe devolver una fila con el usuario creado
SELECT username
FROM dba_users
WHERE username = 'BD_BIBLIOTECA';
```

Esto crea el usuario/esquema con los siguientes datos:

| Dato     | Valor           |
|----------|-----------------|
| Usuario  | `BD_BIBLIOTECA` |
| Password | `Rmi11dp009`    |
| Servidor | `FREEPDB1`      |

> ⚠️ Estas credenciales son para entorno local de desarrollo. No las reutilices en entornos compartidos o productivos; usa variables de entorno o un gestor de secretos.

## Configuración de la aplicación

Configura la conexión en `src/main/resources/application.properties` (o mediante variables de entorno):

```properties
spring.application.name=wsr_biblioteca

spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=BD_BIBLIOTECA
spring.datasource.password=Rmi11dp009
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

## Ejecución

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

## Compilar

```bash
./mvnw clean package
```

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/jhontruse/biblioteca/
│   │   └── WsrBibliotecaApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/jhontruse/biblioteca/
```
