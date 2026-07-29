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
| Servidor | `FREE`      |

> ⚠️ Estas credenciales son para entorno local de desarrollo. No las reutilices en entornos compartidos o productivos; usa variables de entorno o un gestor de secretos.

### Tabla MENU

Jerarquía autorreferenciada de menús de navegación. Ejecuta el siguiente script sobre el esquema `BD_BIBLIOTECA`:

```sql
-- ============================================================
-- Tabla MENU  (jerarquia autorreferenciada)
-- Esquema: BD_BIBLIOTECA
-- ============================================================

CREATE TABLE MENU (
    ID_MENU              VARCHAR2(36 CHAR)  NOT NULL,
    ID_MENU_PADRE        VARCHAR2(36 CHAR),                      -- NULL = menu raiz
    COD_MENU             VARCHAR2(40 CHAR)  NOT NULL,
    NOMBRE_MENU          VARCHAR2(100 CHAR) NOT NULL,
    DESCRIPCION_MENU     VARCHAR2(200 CHAR),
    URL_MENU             VARCHAR2(200 CHAR),                     -- los nodos padre no navegan
    ORDEN_MENU           NUMBER(3,0)        NOT NULL,
    ICONO_MENU           VARCHAR2(100 CHAR),
    DESTINO_MENU         VARCHAR2(20 CHAR),
    MCA_NUEVA_VENTANA    CHAR(1 CHAR)       DEFAULT 'N' NOT NULL,
    MCA_INH              CHAR(1 CHAR)       DEFAULT 'N' NOT NULL,
    FECHA_CREACION       TIMESTAMP(6)       DEFAULT SYSTIMESTAMP NOT NULL,
    USUARIO_CREACION     VARCHAR2(36 CHAR)  NOT NULL,
    FECHA_MODIFICACION   TIMESTAMP(6),
    USUARIO_MODIFICACION VARCHAR2(36 CHAR),          -- <-- la coma que faltaba va aqui

    CONSTRAINT PK_MENU PRIMARY KEY (ID_MENU)
        USING INDEX TABLESPACE USERS,

    CONSTRAINT UK_MENU_COD_MENU UNIQUE (COD_MENU)
        USING INDEX TABLESPACE USERS,

    -- Autorreferencia: apunta a la PK (ID_MENU), no a ID_MENU_PADRE
    CONSTRAINT FK_MENU_ID_MENU_PADRE
        FOREIGN KEY (ID_MENU_PADRE)
        REFERENCES MENU (ID_MENU),

    CONSTRAINT CK_MENU_NUEVA_VENTANA CHECK (MCA_NUEVA_VENTANA IN ('S','N')),
    CONSTRAINT CK_MENU_INH           CHECK (MCA_INH           IN ('S','N')),

    -- Evita que un menu sea su propio padre (ciclo de longitud 1)
    CONSTRAINT CK_MENU_NO_AUTOPADRE  CHECK (ID_MENU <> ID_MENU_PADRE)
);


-- ------------------------------------------------------------
-- Indices
-- ------------------------------------------------------------
-- Oracle NO indexa automaticamente las columnas FK. Sin este indice,
-- borrar o actualizar un menu padre bloquea toda la tabla hija.
CREATE INDEX IX_MENU_ID_MENU_PADRE ON MENU (ID_MENU_PADRE) TABLESPACE USERS;

CREATE INDEX IX_MENU_NOMBRE_MENU   ON MENU (NOMBRE_MENU)   TABLESPACE USERS;

-- NOTA: no crear indice sobre COD_MENU. La constraint UK_MENU_COD_MENU ya
-- genera uno; un segundo indice sobre la misma columna falla con ORA-01408.


-- ------------------------------------------------------------
-- Comentarios
-- ------------------------------------------------------------
COMMENT ON TABLE  MENU                    IS 'Menus de navegacion, jerarquia autorreferenciada';
COMMENT ON COLUMN MENU.ID_MENU_PADRE      IS 'FK a MENU.ID_MENU. NULL indica menu de primer nivel';
COMMENT ON COLUMN MENU.ORDEN_MENU         IS 'Orden de despliegue entre hermanos';
COMMENT ON COLUMN MENU.MCA_NUEVA_VENTANA  IS 'S/N: abrir el enlace en pestana nueva';
COMMENT ON COLUMN MENU.MCA_INH            IS 'S/N: marca de inhabilitado (borrado logico)';


-- ------------------------------------------------------------
-- Consulta del arbol completo
-- ------------------------------------------------------------
SELECT LPAD(' ', (LEVEL - 1) * 4) || NOMBRE_MENU AS JERARQUIA,
       LEVEL AS NIVEL,
       COD_MENU,
       URL_MENU
FROM   MENU
WHERE  MCA_INH = 'N'
START WITH ID_MENU_PADRE IS NULL
CONNECT BY NOCYCLE PRIOR ID_MENU = ID_MENU_PADRE
ORDER SIBLINGS BY ORDEN_MENU;
```

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
