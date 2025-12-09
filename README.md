Documentación 

1. Generar resumen ejecutivo que cuente con los siguientes elementos en el archivo README.md dentro del repositorio:

a.	Descripción, problema identificado, solución, arquitectura

I. Descripción del Sistema

El sistema de control de solicitudes Internas es una aplicación de consola desarrollada en Java diseñada para operar dentro de la Comisión Federal de Electricidad. Su propósito es digitalizar y centralizar la gestión del ciclo de vida de las solicitudes internas, desde su creación por el solicitante hasta su resolución por el responsable, asegurando el cumplimiento de la normativa de trazabilidad y gestión de recursos.

II. Problema identificado

El proceso actual de gestión de solicitudes se caracteriza por:

•	Falta de trazabilidad (RF7): Dependencia de documentos físicos o sistemas de correo electrónico dispersos, lo que impide un seguimiento preciso del estado, el responsable actual y las observaciones históricas.
•	Ineficiencia operacional: Los responsables de área no pueden identificar rápidamente la carga de trabajo pendiente (RF8), lo que resulta en tiempos de respuesta lentos.
•	Falta de seguridad (RF19): Carencia de un control de acceso basado en roles que restrinja las acciones solo a personal autorizado, por ejemplo, solo el responsable puede cambiar el estado.

III. Solución propuesta

La solución es un sistema robusto, aunque simple en su despliegue, que resuelve los problemas mediante las siguientes funcionalidades esenciales:

•	Persistencia en CSV (RF5): Los datos de usuarios, solicitudes e historial se almacenan en archivos planos (.csv), simulando la capa de datos y permitiendo la ejecución portable en un único JAR.
•	Control de Roles (RF19): Implementación de roles ADMIN, RESPONSABLE, y SOLICITANTE, con menús y permisos diferenciados.
•	Trazabilidad Completa (RF3, RF7, RF10): Asignación de folios únicos a cada solicitud y registro automático de todo cambio de estado, comentarios u observaciones en un archivo de historial.
•	Reportes y Métricas (RF15, RF16): Generación de indicadores básicos (pendientes, completadas) y la capacidad de exportar reportes detallados en formato PDF para auditoría.

IV. Arquitectura de la solución

La aplicación utiliza un modelo de 3 capas simulado para asegurar la separación lógica de las responsabilidades, lo cual facilita el mantenimiento y una futura migración a un entorno web/base de datos relacional.

•	Para la 1er capa; Presentación, se tiene el componente de la Interfaz de consola, que abarca la tecnología de Java Scanner y salidas de información para mostrar los datos de la aplicación, el cual tiene el rol principal de recibir la entrada del usuario y mostrar los resultados.
•	Para la 2da capa; Lógica de negocio, se tiene el componente de la clase principal Main del código fuente, que abarca la tecnología de Java/Lógica orientada a objetos, la cual tiene el rol principal de contener los servicios, controladores y modelos que implementan los requerimientos funcionales 1, 6, 15 y 19.
•	Para la 3er capa; Acceso a datos, se tiene el componente de Archivos de persistencia, que abarca la tecnología de archivos del tipo CSV, la cual tiene el rol principal de almacenar de forma estructurada los objetos Usuario, Solicitud e Historial, enfocados al requerimiento funcional 5.

b.	Tabla de contenidos (ToC) con enlaces o a la sección wiki dentro del repositorio o algún medio externo como ReadTheDocs.io

I. Introducción y contexto
I.I. Resumen ejecutivo
I.II. Objetivos del proyecto
I.III. Justificación del problema en CFE
II. Análisis de requerimientos
II.I. Documento ERS (IEEE 830)
II.II. Requerimientos funcionales
II.III. Requerimientos no funcionales
III. Diseño de la solución
III.I. Arquitectura del sistema
III.I.I. Diagrama de 3 Capas
III.I.II. Justificación de componentes
III.II. Diseño de Persistencia (RF5)
III.II.I. Modelo lógico de datos
III.II.II. Estructura de archivos CSV
III.III. Diseño de interfaz
III.III.I. Diagramas de flujo de menús
IV. Desarrollo e implementación
IV.I. Tecnología seleccionada 
IV.I.I. Java, Maven/Gradle 
IV.I.II. Librería iText/PDFBox 
IV.II. Flujo de integración continua 
IV.II.I. Pasos de conexión Drone CI y archivo .drone.yml para pruebas Junit
IV.III. Detalle de Issues/tareas (P0, P1)
IV.III.I. Mapeo de RFs a Issues de desarrollo
V. Resultados y pruebas
V.I. Casos de uso
V.I.I. Demostración del flujo de trabajo 
V.II. Pruebas unitarias
V.II.I. Resultados de las pruebas JUnit 
V.III. Reportes generados
V.III.I. Ejemplos de formato PDF
VI. Conclusiones y recomendaciones
VI.I. Cumplimiento de RF´s
VI.II. Limitaciones del entorno JAR
VI.III. Propuesta de Roadmap a Versión 2.0 (migración a Web/BD Relacional).


2. Requerimientos:

a.	Servidores de aplicación, web, bases de datos, etc.

La funcionalidad de la aplicación no involucra servidores web o de aplicación y no incorpora tecnologías de almacenamiento de datos en bases de datos, ya que estas herramientas no son aplicables dentro de la organización debido a su arquitectura ya establecida y que el presente es una propuesta de mejor a las condiciones internas de la empresa, pues esta aplicación será albergada dentro de un solo computador, pudiendo acceder a él y la información que contenga de forma local de forma centralizada, o a través de un acceso remoto por medio de herramientas administrativas instaladas en diversos equipos fuera del alcance de los usuarios.

b.	Paquetes adicionales.

Para que el proyecto compile y ejecute todas las funcionalidades, especialmente la generación de PDFs y el manejo de archivos CSV, se requiere el uso de librerías de terceros. Estas dependencias deben incluirse en el archivo de configuración del proyecto para ser empaquetadas dentro del JAR final e incluyen los siguientes elementos:

•	iText: Con el propósito de la generación de documentos PDF. Permite crear reportes estructurados de solicitudes y métricas para su exportación y auditoría. Cumpliendo con el requerimiento RF16.
•	Junit: Con el propósito de pruebas unitarias. Es utilizado por el sistema de Integración Continua (Drone CI) para verificar la correcta implementación de la lógica de negocio. Cumpliendo con el requerimiento de las tareas de calidad.
•	Log4j: Con el propósito de la gestión de Logging. Aunque no se implementó explícitamente en el código de consola, es esencial para registrar errores en un entorno de producción o auditar transacciones. Cumpliendo con el requerimiento funcional de la trazabilidad y mantenimiento.
•	CSV Handling: Con el propósito de manejo de archivos planos. Aunque este no requiere librería si se usa java.io nativo, como se hizo. Si se necesitara un manejo más robusto, se usaría Apache Commons CSV. Cumpliendo con el requerimiento RF5 para almacenamiento estructurado.

c.	Versión de Java, etc.

El proyecto ha sido desarrollado utilizando versiones modernas del ecosistema Java, lo que asegura compatibilidad y el uso de features recientes del lenguaje. Elementos tales como:

•	Lenguaje de programación: Específicamente JavaSE. Es un lenguaje robusto, multiplataforma y requerido para generar un archivo JAR ejecutable.
•	Version de Java: Específicamente Java 17 o superior. Se recomienda una versión LTS (Long-Term Support) para estabilidad y acceso a características modernas del lenguaje, optimizando el rendimiento.
•	Entorno de desarrollo: Específicamente IntelliJ IDEA. Es recomendado por el usuario y estándar de la industria para el desarrollo Java, facilitando la gestión de dependencias y la compilación a JAR.
•	Ssistema de construcción: Específicamente Maven o Gradle. Es necesario para automatizar la compilación, gestionar las dependencias externas (iText, JUnit) y generar el archivo final .jar ejecutable (incluyendo todas las librerías).
•	Sistema operativo: Cualquiera de los más comunes (Windows, macOS y Linux). Al ser un JAR de Java, la portabilidad es intrínseca, cumpliendo con el requisito de ser ejecutado en la máquina local.


3. Instalación:

a.	¿Cómo instalar el ambiente de desarrollo?

Para trabajar con el código fuente y generar el archivo final .jar, se requieren las siguientes herramientas o componentes:

•	Java Development Kit (JDK): Java 17 o superior.
•	IDE: IntelliJ IDEA.
•	Sistema de construcción: Macen o Gradle.

Pasos para la configuración del entorno y su ejecución local a través del JAR:

I. Instalar el JDK (Java 17+): Descargue e instale la versión Long-Term Support (LTS) de Java. Asegúrese de que la variable de entorno JAVA_HOME apunte correctamente al directorio de instalación del JDK.

II. Instalar y Configurar Maven/Gradle: Instale el sistema de construcción de su elección y configure su IDE para usarlo. Este es crucial, ya que se encarga de:

•	Gestionar y descargar las dependencias externas (Ej: la librería iText para PDF).
•	Compilar el código fuente.
•	Empaquetar el proyecto en el JAR ejecutable final.

III. Configurar las Dependencias (Ej. Maven): Dentro del archivo de configuración de su proyecto (pom.xml si usa Maven), asegúrese de que la dependencia para la generación de PDFs (e.g., iText) esté incluida y que la configuración del plugin de empaquetado (ej. maven-jar-plugin) esté lista para crear un JAR ejecutable con la clase Main como punto de entrada principal.
IV. Abrir el Proyecto en IntelliJ IDEA: Abra la carpeta del proyecto en IntelliJ. El IDE debe detectar automáticamente el archivo de configuración (pom.xml o build.gradle) y descargar todas las dependencias necesarias.
 
b.	¿Cómo ejecutar pruebas manualmente?

Las pruebas del proyecto se basan en la librería JUnit y se ejecutan a través de su sistema de construcción (Maven o Gradle).

I. Ejecución desde la consola

Abra una terminal en la raíz del proyecto, ya sea en:

•	Maven (mvn clean test): Limpia compilaciones anteriores, compila el código fuente y ejecuta todos los tests JUnit definidos en la carpeta src/test/java.
•	Gradle (gradle clean test): Realiza la misma operación de limpieza, compilación y ejecución de pruebas.

Obtendrá como resultado que la consola mostrará un resumen del número de pruebas ejecutadas, fallidas y omitidas. Un resultado exitoso indica que la lógica central del sistema (Folios, Roles, Persistencia simulada) funciona según lo esperado.

II. Ejecucion desde IntelliJ IDEA

•	Navegue a la carpeta de pruebas (src/test/java).
•	Localice los archivos de prueba (ej. SolicitudTest.java).
•	Haga clic derecho sobre el archivo o método de prueba y seleccione "Run 'NombreDelTest'".


c.	¿Cómo implementar la solución en producción en un ambiente local o en la nube como Heroku?

El proceso de implementación en un entorno local de "producción" (es decir, el entorno donde el usuario final ejecutará la aplicación) consta de dos fases: la Generación del JAR y la Ejecución.

Fase 1: Generación del Archivo JAR Final

I. Construcción del proyecto: Abra la terminal en la raíz del proyecto y ejecute el comando de empaquetado:
•	Maven: mvn clean package
•	Gradle: gradle clean build

II. Ubicación del archivo: Una vez finalizado el proceso, el archivo JAR ejecutable se encontrará en el directorio target/ (Maven) o build/libs/ (Gradle). El nombre será similar a cfe-solicitudes-1.0-SNAPSHOT.jar.

III. Distribución: Este archivo JAR es el único componente que debe distribuirse a los usuarios finales (junto con los archivos CSV vacíos o inicializados si es la primera vez).

Fase 2: Ejecución para el Usuario Final

Para ejecutar la aplicación, el usuario final solo necesita tener instalado el JRE (Java Runtime Environment) versión 17 o superior.

I. Copiar Archivos: Coloque el archivo .jar en una carpeta dedicada (ej: C:\CFE_Sistema_Solicitudes\). Los archivos de persistencia (usuarios.csv, solicitudes.csv, historial.csv) se crearán automáticamente en esta misma carpeta la primera vez que se ejecute la aplicación.

II. Ejecución del JAR: Abra la línea de comandos (CMD o PowerShell) en esa carpeta y ejecute el siguiente comando: java -jar cfe-solicitudes-1.0-SNAPSHOT.jar

III. Inicio: El sistema se inicializará, cargará los datos de los CSV y solicitará al usuario iniciar sesión.

•	ADMIN: admin / 0000
•	RESPONSABLE: dpto_ti / 1234
•	SOLICITANTE: dpto_compras / 1234


4. Configuración:

Esta sección describe los elementos de configuración que rigen el comportamiento del sistema, tanto a nivel de producto final (archivos de configuración) como a nivel de requerimientos operacionales.

a.	Configuración del producto (archivos de configuración).

Dado que el entorno de la aplicación está restringido a un único archivo JAR ejecutable y a la persistencia en archivos planos, los "archivos de configuración" son los propios archivos de datos CSV y la configuración inicial inmutable codificada en Java.

I. Archivos de persistencia CSV

Estos archivos son cruciales, ya que no solo almacenan los datos transaccionales, sino que también definen el estado de la aplicación. Su modificación directa por un administrador (fuera de la aplicación) puede alterar el comportamiento del sistema.

•	Usuarios.csv: Para seguridad (RF18, RF19): Define las credenciales de acceso y los permisos de cada usuario.
•	Solicitudes.csv: Para la funcionalidad principal: (RF1, RF6): Almacena el estado actual y los datos de cada solicitud.
•	Historial.csv: Para la trazabilidad (RF7): Registra cada evento importante. Es fundamental para una posible auditoria o consulta de estados.

Parámetros Sensibles: La contraseña del usuario admin (0000) debe ser modificada inmediatamente por el administrador de TI tras la primera ejecución, si bien en este prototipo está codificada como default.

II. Configuración estática

Estos parámetros están fijos en la clase Main.java y solo pueden ser modificados recompilando el archivo JAR.

•	CSV_DELIMITER (;): Define el carácter utilizado para separar los campos en los archivos CSV.
•	DATE_FORMAT (yyyy-MM-dd HH:mm:ss): Trazabilidad (RF7): Estándar de formato de tiempo para todos los registros de historial y creación.
•	Roles (ADMIN, RESPONSABLE, SOLICITANTE): Seguridad (RF19): Define los tipos de acceso y las restricciones del sistema.
•	Estados (RECIBIDA, EN PROCESO, COMPLETADA, CANCELADA): Lógica de Negocio (RF6): Define el ciclo de vida de una solicitud.


b.	Configuración de los requerimientos.

Esta sección aborda la configuración necesaria en el entorno de "producción" (el equipo del usuario final o de soporte) para garantizar la ejecución de la aplicación.


I. Requerimientos de software obligatorios

•	Java Runtime Environment (JRE): Entorno para ejecutar el JAR. Versión 17 (LTS) o superior instalada en el sistema operativo.
•	Librerías externas: Generación de PDFs (RF16). La librería iText/PDFBox debe estar empaquetada dentro del JAR final (tarea del build system).

II. Requerimientos de hardware recomendados

•	Sistema operativo: Windows 7, macOS, o Linux (64-bit).	Versión moderna con soporte actualizado.
•	Memoria RAM:	2 GB. 4 GB o más, para estabilidad de la JRE.
•	Espacio en disco: 50 MB. Requiere espacio mínimo para el JAR y el crecimiento de los archivos CSV (persistencia).

III. Requerimientos opcionales

Permisos de archivo: El usuario que ejecute el JAR debe tener permisos de lectura/escritura sobre la carpeta donde se aloja el JAR. Esencial para la persistencia (RF5), ya que el sistema crea y actualiza los archivos CSV en ese directorio.
Uso de consola: Se requiere una interfaz de línea de comandos (CMD, PowerShell, Terminal, etc.) para iniciar la aplicación mediante el comando java -jar. Limitación del entorno de consola.



5. Uso:

Esta sección proporciona la guía detallada para la operación del Sistema de Control de Solicitudes Internas CFE, dividida según el perfil de acceso (Rol) del usuario.

Instrucciones generales de inicio:

•	Ejecución: Abra la terminal o CMD en el directorio donde se encuentra el archivo .jar.
•	Comando: Ejecute el sistema con el comando: java -jar cfe-solicitudes-1.0-SNAPSHOT.jar
•	Login: Ingrese su usuario y contraseña. El sistema validará sus credenciales y lo dirigirá automáticamente a su menú de rol.

a.	Sección de referencia para usuario final. Manual que se hará referencia para usuarios finales.

Este manual cubre las operaciones diarias esenciales para la creación, seguimiento y gestión de solicitudes, que son los pilares del flujo de trabajo del sistema.

I. Manual para el rol: SOLICITANTE

El rol SOLICITANTE está limitado a la creación de solicitudes y la consulta de su progreso (RF19).

•	Registrar nueva solicitud: Creación (RF1, RF2). Ingrese el título (obligatorio) y la descripción detallada. El sistema asignará y mostrará el folio único (RF3).
•	Consultar solicitudes propias: Listado (RF12). Muestra un listado simple de todas las solicitudes creadas por su usuario, indicando el folio, solicitante y estado actual.
•	Búsqueda por folio (Solo consulta): Consulta Detallada (RF14)Ingrese el Folio de la solicitud. El sistema mostrará: folio, título, descripción, estado y el historial de eventos (RF7). Nota: No se permite ninguna modificación.

II. Manual para el rol: RESPONSABLE

El rol RESPONSABLE se enfoca en la gestión, procesamiento y resolución de solicitudes (RF6, RF8).

•	Búsqueda y modificación detallada de solicitudes: Gestión total (RF6, RF8). Ingrese el Folio de la solicitud que desea procesar. Y accederá al menú de Acciones de Gestión.
•	Sub-Opción 1: Modificar título/descripción. Ajuste de datos. Permite actualizar el título o la descripción. Todo cambio se registra en el historial (RF7).
•	Sub-Opción 2: Cambiar estado. Resolución (RF6). Permite cambiar el estado EN_PROCESO (para iniciar el trabajo), COMPLETADA (para finalizar) o CANCELADA. El cambio se registra en el historial (RF7).
•	Sub-Opción 3: Agregar comentario: Trazabilidad (RF10). Registra un comentario u observación en el historial de la solicitud sin afectar el estado principal.
•	Sub-Opción 4: Generar reporte PDF: Documentación (RF16). Genera un archivo PDF (simulado en el entorno actual) que contiene el detalle y el historial completo de la solicitud para archivo o envío.
•	Visualizar reporte de métricas: Indicadores (RF15). Muestra en consola un resumen de los indicadores clave: Total de solicitudes registradas, pendientes/En proceso, y completadas.

b.	Sección de referencia para usuario administrador.

El rol ADMIN tiene permisos de superusuario para mantener la seguridad, gestionar el acceso y realizar auditorías completas del sistema (RF19).

•	Gestionar usuarios. Seguridad/acceso (RF19). Accede al submenú para: Crear usuario: Define username, contraseña y rol (SOLICITANTE, RESPONSABLE, ADMIN); Eliminar usuario: Elimina una cuenta existente (excepto a sí mismo, "admin"). Los cambios se persisten en usuarios.csv.
•	Búsqueda y modificación detallada de solicitudes: Auditoría/corrección (RF6). Idéntico al menú de Responsable, pero con la potestad de modificar cualquier dato o estado, incluyendo las solicitudes de otros responsables.
•	Generar reporte de métricas (PDF): Auditoría (RF15, RF16). Genera el reporte de métricas del sistema y lo exporta a un archivo PDF (simulado) para documentación oficial.
•	Acceso a datos CSV (Auditoría): Acceso directo (RF5). Permite visualizar el contenido crudo de los archivos de persistencia (usuarios.csv, solicitudes.csv, historial.csv) para fines de verificación directa de la integridad de los datos.


6. Contribución:

Esta guía está dirigida a desarrolladores y personal técnico que desee contribuir directamente al código fuente del Sistema de Control de Solicitudes Internas CFE, siguiendo las mejores prácticas de Gitflow y Desarrollo Basado en Ramas (Branching).

a.	Guía de contribución para usuarios.

El repositorio principal del proyecto es la fuente única de verdad. Todo el trabajo nuevo (corrección de errores, nuevas funcionalidades) debe realizarse en una rama separada para garantizar la estabilidad de la rama principal (main o develop).

Requisitos previos

•	Tener instalado Git en su máquina local.
•	Tener una cuenta de GitHub con permisos de acceso al repositorio del proyecto.
•	Tener configurado el Ambiente de Desarrollo (JDK 17+, Maven/Gradle, IntelliJ IDEA).

Pasos específicos para la contribución

I. Clonar el repositorio: Obtenga una copia local del código fuente del proyecto desde GitHub (git clone [URL_DEL_REPOSITORIO]). Establece la conexión entre su entorno local y el repositorio remoto.
II. Preparar el entorno: Navegue a la carpeta del proyecto y sincronice con la rama de desarrollo principal. (cd cfe-solicitudes-repo $\rightarrow$ git checkout develop $\rightarrow$ git pull origin develop). Asegura que está trabajando con la versión más reciente del código base.
III. Crear una nueva rama. Nunca trabaje directamente en develop. Cree una rama descriptiva para su trabajo (Ej: feature/rf21-reporte-avanzado o bugfix/fix-login-admin). (git checkout -b feature/su-nueva-funcionalidad). Aísla sus cambios para evitar introducir fallos en la rama de desarrollo.
IV. Implementar y ComprometerRealice los cambios en el código (nuevas funciones, correcciones, etc.). Asegúrese de que las pruebas JUnit (simuladas) pasen localmente.git add . ($\rightarrow$ git commit -m "feat: implementacion de RF X"). Documenta los cambios realizados. Los commits deben ser atómicos y descriptivos.
V. Subir la rama: Envíe la nueva rama y sus commits al repositorio remoto de GitHub. (git push origin feature/su-nueva-funcionalidad). Hace que sus cambios sean visibles en GitHub para iniciar el proceso de revisión.
VI. Enviar el pull request: Vaya a la interfaz web de GitHub y cree un pull request. solicitando fusionar su nueva rama (feature/...) con la rama develop. Señaliza oficialmente que su trabajo está listo para revisión y fusión.
VII. Esperar verificación CI: Drone CI detectará automáticamente el nuevo PR y ejecutará el pipeline definido en el archivo .drone.yml (Incluyendo las pruebas Junit). Verificación automática: Si las pruebas fallan (estado X), el PR será bloqueado hasta que se corrijan los errores. Si pasan (estado OK), el PR es apto para revisión.
VIII. Revisión y merge: El líder técnico revisará su código. Una vez que el estado de Drone CI sea éxito y el código sea aprobado por el revisor, la rama será fusionada (merge) a develop. Sus cambios se integran al proyecto principal, y su rama de trabajo puede ser eliminada.
IX. Sincronizar: Una vez fusionado, regrese a la rama develop y actualice su copia local. (git checkout develop $\rightarrow$ git pull origin develop). Prepara su entorno local para comenzar la próxima tarea con el código más actualizado.

b.	Debe contar con pasos específicos para clonar repositorio, crear un nuevo branch, enviar el pull request, esperar a hacer el merge.

El sistema de Integración Continua (Drone CI), configurado en pasos anteriores, es un componente crítico de este flujo.

•	Archivo Clave: .drone.yml
•	Función: Ejecuta automáticamente el comando mvn clean install al recibir un Pull Request. Este comando compila el código y corre todas las pruebas JUnit, asegurando que la contribución no rompa funcionalidades existentes.
•	Regla: Ningún Pull Request podrá ser fusionado a la rama develop si el status check de Drone CI muestra un fallo de compilación o de pruebas.


7. Roadmap:

El estado actual del proyecto es un producto mínimo viable funcional que demuestra el ciclo de vida de las solicitudes, la trazabilidad y la seguridad por roles, cumpliendo con la mayoría de los requisitos funcionales dentro de las limitaciones de un entorno de consola y persistencia CSV.

El siguiente Roadmap se enfoca en superar las limitaciones del entorno actual (consola y CSV) y en implementar los requerimientos que fueron clasificados como imposibles o limitados en el entorno JAR.

a.	Requerimientos que se implementarán en un futuro.

El plan de trabajo se estructura en dos fases principales, priorizando la migración a una plataforma que soporte los requisitos de seguridad y escalabilidad que una empresa como CFE realmente necesita.

Fase 1: Robustez y Migración (Corto plazo a 3 meses)

El objetivo principal es migrar la Capa de Datos y la Interfaz para eliminar las restricciones de la consola y la dependencia de los archivos CSV.

•	RF-D1: Migración a base de datos relacional. Solución al RF5 (limitado): Reemplazar los archivos CSV (usuarios.csv, solicitudes.csv, historial.csv) por una base de datos robusta (MySQL o PostgreSQL) para garantizar la integridad, concurrencia y escalabilidad de los datos.
•	RF-D2: Manejo de sesiones automático. Solución al RF20 (imposible): Implementar la lógica para el cierre automático de sesión por inactividad. Esto requiere migrar a un entorno multi-hilo o web (como un servidor de aplicaciones) que pueda gestionar timers y la actividad del usuario.
•	RF-D3: Notificaciones por email/sistema. Solución al RF4: Integrar un servicio de envío de correos (SMTP) o un sistema de mensajería interno para notificar a los Solicitantes cuando su petición cambia de estado (RF6) y a los Responsables cuando reciben una nueva solicitud.
•	RF-D4: Interfaz gráfica de usuario (GUI/Web). Mejora de UX: Reemplazar la interfaz de consola por una interfaz web responsiva (utilizando un framework como Spring Boot + HTML/CSS/JS) o una aplicación de escritorio. Esto facilitará la interacción y permitirá la visualización de reportes gráficos.


Fase 2: Escalabilidad y funcionalidad avanzada (Mediano plazo de 6 a 12 meses)

Una vez que el sistema se ejecute sobre una base de datos y una interfaz robusta, se añadirán funcionalidades avanzadas para optimizar el flujo de trabajo interno.

•	RF-A1: Gestión de acuerdos de nivel de Servicio (SLA). Capacidad de definir tiempos límite (Ej: 48 horas) para la resolución de ciertos tipos de solicitudes y alertar a los responsables cuando una solicitud está a punto de vencer el SLA.
•	RF-A2: Asignación específica de responsables. Permitir que los Administradores o Líderes asignen solicitudes directamente a un usuario responsable específico de la lista, en lugar de que la gestión sea genérica.
•	RF-A3: Soporte para documentos adjuntos. Integrar la capacidad de que los Solicitantes adjunten evidencia (imágenes, contratos, archivos de Excel) a su solicitud. Esto requiere almacenamiento de archivos (S3, disco duro o BD).
•	RF-A4: Reportes gráficos y dashboard. Creación de un dashboard visual que muestre el rendimiento del sistema, tiempos promedio de resolución y distribución de la carga de trabajo por responsable (RF15).
•	RF-A5: Flujos de aprobación jerárquica. Implementar un flujo donde ciertas solicitudes críticas requieran la aprobación de un usuario de nivel superior (como gerencia) antes de pasar al estado "EN_PROCESO" o "COMPLETADA".





Producto

1. Video de demostración con los requerimientos cumplidos.

https://youtu.be/6AnUVvbTnWQ

