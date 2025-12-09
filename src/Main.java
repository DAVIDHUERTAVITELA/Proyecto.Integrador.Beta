import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// NOTA: Esta clase requiere la dependencia de iText

class ReportePDFGenerator {

    public static void generarPDF(String nombreArchivo, String contenido) {

        try (PrintWriter out = new PrintWriter(nombreArchivo)) {

            out.println("--- INICIO REPORTE CFE (PDF GENERADOR) ---");
            out.println(contenido);
            out.println("--- FIN REPORTE CFE ---");
            System.out.println("✅ Reporte generado y guardado como texto en: " + nombreArchivo);

        } catch (IOException e) {

            System.err.println("❌ Error al simular la generación de PDF: " + e.getMessage());

        }
    }
}

public class Main {

    private static final String CSV_DELIMITER = ";";
    private static final Scanner scanner = new Scanner(System.in);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String ARCHIVO_USUARIOS = "usuarios.csv";
    private static final String ARCHIVO_SOLICITUDES = "solicitudes.csv";
    private static final String ARCHIVO_HISTORIAL = "historial.csv";

    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Solicitud> solicitudes = new ArrayList<>();
    private static List<Historial> historial = new ArrayList<>();
    private static Usuario usuarioActual = null;

    enum Rol { SOLICITANTE, RESPONSABLE, ADMIN }
    enum Estado { RECIBIDA, EN_PROCESO, COMPLETADA, CANCELADA }
    enum TipoEvento { CAMBIO_ESTADO, COMENTARIO, MODIFICACION_DATOS, CREACION }

    static class Usuario {

        public String username;
        public String password;
        public Rol rol;

        public Usuario(String username, String password, Rol rol) {
            this.username = username; this.password = password; this.rol = rol;
        }
        public String toCSV() {
            return String.join(CSV_DELIMITER, username, password, rol.name());
        }
    }

    static class Solicitud {

        public String folio;
        public String titulo;
        public String descripcion;
        public String solicitanteUsername;
        public Estado estado;
        public String fechaCreacion;

        public Solicitud(String folio, String titulo, String descripcion, String solicitanteUsername, Estado estado, String fechaCreacion) {
            this.folio = folio; this.titulo = titulo; this.descripcion = descripcion;
            this.solicitanteUsername = solicitanteUsername; this.estado = estado;
            this.fechaCreacion = fechaCreacion;
        }

        public String toCSV() {

            return String.join(CSV_DELIMITER, folio, titulo.replace(CSV_DELIMITER, ","),
                    descripcion.replace(CSV_DELIMITER, ",").replace("\n", " "),
                    solicitanteUsername, estado.name(), fechaCreacion);
        }

        @Override
        public String toString() {

            return String.format("| Folio: %-10s | Solicitante: %-15s | Estado: %-12s | Título: %s",
                    folio, solicitanteUsername, estado, titulo.substring(0, Math.min(titulo.length(), 30)) + (titulo.length() > 30 ? "..." : ""));
        }
    }

    static class Historial {

        public String folio;
        public String usuarioCambio;
        public TipoEvento tipo;
        public String detalle;
        public String fecha;

        public Historial(String folio, String usuarioCambio, TipoEvento tipo, String detalle, String fecha) {

            this.folio = folio; this.usuarioCambio = usuarioCambio; this.tipo = tipo;
            this.detalle = detalle; this.fecha = fecha;
        }

        public String toCSV() {
            return String.join(CSV_DELIMITER, folio, usuarioCambio, tipo.name(), detalle.replace(CSV_DELIMITER, ","), fecha);
        }

        public String toReportString() {
            return String.format("[%s] [%-15s] [%-20s] Detalle: %s", fecha, usuarioCambio, tipo.name(), detalle);
        }
    }

    private static void asegurarArchivosYDatos() {

        try {

            File usersFile = new File(ARCHIVO_USUARIOS);

            if (!usersFile.exists() || usersFile.length() == 0) {
                // Usuarios por defecto, incluyendo ADMIN
                usuarios.add(new Usuario("admin", "0000", Rol.ADMIN));
                usuarios.add(new Usuario("dpto_ti", "1234", Rol.RESPONSABLE));
                usuarios.add(new Usuario("dpto_compras", "1234", Rol.SOLICITANTE));
                guardarUsuarios();
            }

            new File(ARCHIVO_SOLICITUDES).createNewFile();
            new File(ARCHIVO_HISTORIAL).createNewFile();

        } catch (IOException e) {
            System.err.println("Error al asegurar archivos iniciales.");
        }
    }

    private static <T> List<T> cargarDesdeCSV(String archivo, Class<T> clazz) {

        List<T> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] values = line.split(CSV_DELIMITER);
                if (clazz.equals(Usuario.class) && values.length == 3) {
                    lista.add(clazz.cast(new Usuario(values[0], values[1], Rol.valueOf(values[2]))));
                } else if (clazz.equals(Solicitud.class) && values.length == 6) {
                    lista.add(clazz.cast(new Solicitud(values[0], values[1], values[2], values[3], Estado.valueOf(values[4]), values[5])));
                } else if (clazz.equals(Historial.class) && values.length == 5) {
                    lista.add(clazz.cast(new Historial(values[0], values[1], TipoEvento.valueOf(values[2]), values[3], values[4])));
                }
            }

        } catch (IOException | IllegalArgumentException e) {
            // Manejo silencioso: si el archivo no existe o está mal, devuelve lista vacía
        }
        return lista;
    }

    private static void guardarLista(String archivo, List<?> lista) {

        try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {

            for (Object obj : lista) {

                if (obj instanceof Usuario) pw.println(((Usuario) obj).toCSV());
                else if (obj instanceof Solicitud) pw.println(((Solicitud) obj).toCSV());
                else if (obj instanceof Historial) pw.println(((Historial) obj).toCSV());
            }

        } catch (IOException e) {
            System.err.println("❌ Error de persistencia en " + archivo + ": " + e.getMessage());
        }
    }

    private static void cargarTodo() {
        usuarios = cargarDesdeCSV(ARCHIVO_USUARIOS, Usuario.class);
        solicitudes = cargarDesdeCSV(ARCHIVO_SOLICITUDES, Solicitud.class);
        historial = cargarDesdeCSV(ARCHIVO_HISTORIAL, Historial.class);
    }

    private static void guardarTodo() {
        guardarLista(ARCHIVO_USUARIOS, usuarios);
        guardarLista(ARCHIVO_SOLICITUDES, solicitudes);
        guardarLista(ARCHIVO_HISTORIAL, historial);
    }

    private static void registrarHistorial(String folio, TipoEvento tipo, String detalle) {
        Historial h = new Historial(folio, usuarioActual.username, tipo, detalle, DATE_FORMAT.format(new Date()));
        historial.add(h);
        guardarTodo();
    }

    private static void crearSolicitud() {

        if (usuarioActual.rol != Rol.SOLICITANTE) {
            System.err.println("❌ ERROR: Solo los solicitantes pueden crear nuevas solicitudes.");
            return;
        }

        System.out.println("\n--- REGISTRO DE NUEVA SOLICITUD (RF1) ---");
        System.out.print("Ingrese el Título (Obligatorio - RF2): ");
        String titulo = scanner.nextLine().trim();

        if (titulo.isEmpty()) {
            System.err.println("❌ ERROR: El Título es obligatorio.");
            return;
        }

        System.out.print("Ingrese la Descripción detallada: ");
        String descripcion = scanner.nextLine().trim();

        String folio = generarFolio(); // RF3
        String fecha = DATE_FORMAT.format(new Date());

        Solicitud nueva = new Solicitud(folio, titulo, descripcion, usuarioActual.username, Estado.RECIBIDA, fecha);
        solicitudes.add(nueva);

        registrarHistorial(folio, TipoEvento.CREACION, "Solicitud creada. Estado inicial: RECIBIDA."); // RF7
        guardarTodo();

        System.out.println("\n✅ SOLICITUD REGISTRADA CON ÉXITO.");
        System.out.println("   Folio Asignado (RF3): " + nueva.folio);
    }

    private static void modificarSolicitudDetallada(Solicitud s) {

        System.out.println("\n** MODIFICAR TÍTULO/DESCRIPCIÓN DE FOLIO " + s.folio + " **");

        System.out.print("Nuevo Título (Actual: " + s.titulo + " - Dejar vacío para no cambiar): ");
        String nuevoTitulo = scanner.nextLine().trim();

        if (!nuevoTitulo.isEmpty() && !s.titulo.equals(nuevoTitulo)) {
            registrarHistorial(s.folio, TipoEvento.MODIFICACION_DATOS, "Título cambiado de '" + s.titulo + "' a '" + nuevoTitulo + "'");
            s.titulo = nuevoTitulo;
        }

        System.out.print("Nueva Descripción (Dejar vacío para no cambiar): ");
        String nuevaDescripcion = scanner.nextLine().trim();

        if (!nuevaDescripcion.isEmpty() && !s.descripcion.equals(nuevaDescripcion)) {
            registrarHistorial(s.folio, TipoEvento.MODIFICACION_DATOS, "Descripción actualizada.");
            s.descripcion = nuevaDescripcion;
        }

        guardarTodo();
        System.out.println("✅ Solicitud " + s.folio + " actualizada.");
    }

    private static void cambiarEstadoSolicitud(Solicitud s) {

        System.out.println("\n** CAMBIAR ESTADO **");
        Estado estadoAnterior = s.estado;
        System.out.println("Estado actual: " + estadoAnterior);
        System.out.println("1. RECIBIDA | 2. EN_PROCESO | 3. COMPLETADA | 4. CANCELADA");
        System.out.print("Ingrese el número del nuevo estado: ");
        String opcionEstadoStr = scanner.nextLine();

        try {

            int opcionEstado = Integer.parseInt(opcionEstadoStr);
            Estado nuevoEstado = s.estado;

            switch (opcionEstado) {

                case 1: nuevoEstado = Estado.RECIBIDA; break;
                case 2: nuevoEstado = Estado.EN_PROCESO; break;
                case 3: nuevoEstado = Estado.COMPLETADA; break;
                case 4: nuevoEstado = Estado.CANCELADA; break;
                default: System.err.println("❌ Opción no válida."); return;

            }

            if (estadoAnterior != nuevoEstado) {

                s.estado = nuevoEstado;
                registrarHistorial(s.folio, TipoEvento.CAMBIO_ESTADO, "Estado cambiado de " + estadoAnterior + " a " + nuevoEstado); // RF7
                guardarTodo();
                System.out.println("✅ Estado del Folio " + s.folio + " actualizado a: " + nuevoEstado);

            } else {
                System.out.println("El estado no ha cambiado.");
            }

        } catch (NumberFormatException e) {
            System.err.println("❌ Entrada inválida.");
        }
    }

    private static void agregarComentario(Solicitud s) {

        System.out.println("\n** AÑADIR COMENTARIO (RF10) **");
        System.out.print("Ingrese el comentario/observación: ");
        String comentario = scanner.nextLine().trim();

        if (!comentario.isEmpty()) {
            registrarHistorial(s.folio, TipoEvento.COMENTARIO, comentario); // RF10, RF7
            System.out.println("✅ Comentario añadido al historial.");

        } else {
            System.out.println("Comentario vacío. Cancelado.");
        }
    }

    private static void buscarGestionarSolicitud(boolean puedeEditar) {

        System.out.println("\n--- BÚSQUEDA Y GESTIÓN DE SOLICITUDES POR FOLIO ---");
        System.out.print("Ingrese el Folio de la Solicitud (Ej: CFE-12345): ");
        String folioBuscado = scanner.nextLine().trim().toUpperCase();

        Solicitud s = solicitudes.stream()
                .filter(sol -> sol.folio.equals(folioBuscado))
                .findFirst()
                .orElse(null);

        if (s == null) {
            System.err.println("❌ ERROR: Folio '" + folioBuscado + "' no encontrado.");
            return;
        }

        if (usuarioActual.rol == Rol.SOLICITANTE && !s.solicitanteUsername.equals(usuarioActual.username) && !puedeEditar) {
            System.err.println("❌ ACCESO DENEGADO: Solo puede consultar sus propias solicitudes.");
            return;
        }

        System.out.println("\n✅ DETALLE DEL FOLIO " + s.folio + ":");
        System.out.println("  Folio: " + s.folio);
        System.out.println("  Solicitante: " + s.solicitanteUsername);
        System.out.println("  Fecha Creación: " + s.fechaCreacion);
        System.out.println("  Título: " + s.titulo);
        System.out.println("  Descripción: " + s.descripcion);
        System.out.println("  Estado Actual: " + s.estado.name());

        mostrarHistorial(s.folio); // RF14

        if (!puedeEditar) return;

        while (true) {

            System.out.println("\n--- ACCIONES DE GESTIÓN (RESPONSABLE/ADMIN) ---");
            System.out.println("1. Modificar Título/Descripción");
            System.out.println("2. Cambiar Estado (RF6)");
            System.out.println("3. Agregar Comentario/Observación (RF10)");
            System.out.println("4. Generar Reporte PDF de Solicitud (RF16)");
            System.out.println("5. Volver");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": modificarSolicitudDetallada(s); break;
                case "2": cambiarEstadoSolicitud(s); break;
                case "3": agregarComentario(s); break;
                case "4": generarReporteDetalladoPDF(s); break;
                case "5": return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void mostrarHistorial(String folio) {

        System.out.println("\n--- HISTORIAL DE CAMBIOS (RF7, RF14) ---");
        List<Historial> hList = historial.stream()
                .filter(h -> h.folio.equals(folio))
                .sorted(Comparator.comparing(h -> h.fecha))
                .collect(Collectors.toList());

        if (hList.isEmpty()) {
            System.out.println("➡️ Sin registros en el historial.");
            return;
        }

        hList.forEach(h -> System.out.println(h.toReportString()));
        System.out.println("----------------------------------------");
    }

    private static void generarReporteDetalladoPDF(Solicitud s) {

        System.out.println("\n** GENERANDO REPORTE DETALLADO (PDF) **");
        StringBuilder sb = new StringBuilder();

        sb.append("REPORTE DETALLADO DE SOLICITUD\n");
        sb.append("FOLIO: ").append(s.folio).append("\n");
        sb.append("SOLICITANTE: ").append(s.solicitanteUsername).append("\n");
        sb.append("ESTADO: ").append(s.estado).append("\n");
        sb.append("TÍTULO: ").append(s.titulo).append("\n");
        sb.append("DESCRIPCIÓN:\n").append(s.descripcion).append("\n\n");

        sb.append("HISTORIAL DE EVENTOS:\n");
        historial.stream()
                .filter(h -> h.folio.equals(s.folio))
                .sorted(Comparator.comparing(h -> h.fecha))
                .forEach(h -> sb.append(h.toReportString()).append("\n"));

        String nombreArchivo = "Reporte_" + s.folio + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf";
        ReportePDFGenerator.generarPDF(nombreArchivo, sb.toString());
    }

    private static void generarReporteMetricas(boolean exportarPDF) {

        long totalPendientes = solicitudes.stream().filter(s -> s.estado != Estado.COMPLETADA && s.estado != Estado.CANCELADA).count();
        long totalCompletadas = solicitudes.stream().filter(s -> s.estado == Estado.COMPLETADA).count();

        StringBuilder sb = new StringBuilder();
        sb.append("--- REPORTE DE MÉTRICAS (RF15) ---\n");
        sb.append("Total Solicitudes Registradas: ").append(solicitudes.size()).append("\n");
        sb.append("Solicitudes Pendientes/En Proceso: ").append(totalPendientes).append("\n");
        sb.append("Solicitudes Completadas: ").append(totalCompletadas).append("\n");
        sb.append("---------------------------------\n");

        if (exportarPDF) {
            String nombreArchivo = "ReporteMetricas_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".pdf";
            ReportePDFGenerator.generarPDF(nombreArchivo, sb.toString());
        } else {
            System.out.println(sb.toString());
        }
    }

    private static void menuGestionUsuarios() {

        while (true) {

            System.out.println("\n--- GESTIÓN DE USUARIOS ---");
            listarUsuarios();
            System.out.println("1. Crear Usuario");
            System.out.println("2. Eliminar Usuario");
            System.out.println("3. Volver al menú principal");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": crearUsuario(); break;
                case "2": eliminarUsuario(); break;
                case "3": return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void crearUsuario() {

        System.out.println("\n--- CREAR NUEVO USUARIO ---");
        System.out.print("Username (único): ");
        String user = scanner.nextLine().trim().toLowerCase();

        if (usuarios.stream().anyMatch(u -> u.username.equals(user))) {
            System.err.println("❌ ERROR: El nombre de usuario ya existe.");
            return;
        }

        System.out.print("Contraseña (temporal): ");
        String pass = scanner.nextLine().trim();

        System.out.println("Rol [1: SOLICITANTE, 2: RESPONSABLE, 3: ADMIN]: ");
        String rolChoice = scanner.nextLine().trim();
        Rol rol;

        switch (rolChoice) {
            case "1": rol = Rol.SOLICITANTE; break;
            case "2": rol = Rol.RESPONSABLE; break;
            case "3": rol = Rol.ADMIN; break;
            default: System.err.println("❌ Opción de rol inválida."); return;
        }

        usuarios.add(new Usuario(user, pass, rol));
        guardarUsuarios(); // Persistencia
        System.out.println("✅ Usuario '" + user + "' creado como " + rol.name() + ".");
    }

    private static void eliminarUsuario() {

        System.out.println("\n--- ELIMINAR USUARIO ---");
        System.out.print("Username a eliminar: ");
        String user = scanner.nextLine().trim().toLowerCase();

        if (user.equals("admin")) {
            System.err.println("❌ ERROR: El usuario 'admin' no puede ser eliminado.");
            return;
        }

        // RemoveIf simplifica la eliminación en listas
        boolean eliminado = usuarios.removeIf(u -> u.username.equals(user));

        if (eliminado) {
            guardarUsuarios(); // Persistencia
            System.out.println("✅ Usuario '" + user + "' eliminado.");

        } else {
            System.err.println("❌ ERROR: Usuario no encontrado.");
        }
    }

    private static void listarUsuarios() {

        System.out.println("\n--- LISTA DE USUARIOS ACTIVOS ---");
        System.out.printf("%-15s %-12s\n", "USERNAME", "ROL");
        System.out.println("---------------------------");

        for (Usuario u : usuarios) {
            System.out.printf("%-15s %-12s\n", u.username, u.rol.name());
        }

    }

    private static void guardarUsuarios() {

        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_USUARIOS))) {
            for (Usuario u : usuarios) {
                pw.println(u.toCSV());
            }
        }
        catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    // --- 8. MENÚS Y FLUJO PRINCIPAL ---

    private static void menuAdmin() {

        while (true) {

            System.out.println("\n--- MENÚ ADMINISTRADOR (RF19) ---");
            System.out.println("1. Gestionar Usuarios (Crear, Eliminar, Listar)");
            System.out.println("2. Búsqueda y Modificación Detallada de Solicitudes");
            System.out.println("3. Generar Reporte de Métricas (RF15/RF16)");
            System.out.println("4. Acceso a Datos CSV (Auditoría)");
            System.out.println("5. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": menuGestionUsuarios(); break;
                case "2": buscarGestionarSolicitud(true); break;
                case "3": generarReporteMetricas(true); break;
                case "4": /* consultarBaseDatosCSV(); */ System.out.println("➡️ Implementar consulta CSV..."); break;
                case "5": System.out.println("Cerrando sesión de Administrador..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void menuResponsable() {

        while (true) {

            System.out.println("\n--- MENÚ RESPONSABLE (RF19) ---");
            System.out.println("1. Búsqueda y Modificación Detallada de Solicitudes (RF6, RF8, RF10)");
            System.out.println("2. Visualizar Reporte de Métricas (RF15)");
            System.out.println("3. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": buscarGestionarSolicitud(true); break;
                case "2": generarReporteMetricas(false); break; // Solo muestra en consola
                case "3": System.out.println("Cerrando sesión de Responsable..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void menuSolicitante() {

        while (true) {

            System.out.println("\n--- MENÚ SOLICITANTE (RF19) ---");
            System.out.println("1. Registrar Nueva Solicitud (RF1, RF2)");
            System.out.println("2. Consultar Solicitudes Propias (RF12)");
            System.out.println("3. Búsqueda por Folio (Solo Consulta)");
            System.out.println("4. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": crearSolicitud(); break;
                case "2": consultarSolicitudesPropias(); break;
                case "3": buscarGestionarSolicitud(false); break; // RF19: No puede editar
                case "4": System.out.println("Cerrando sesión de Solicitante..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void consultarSolicitudesPropias() {

        System.out.println("\n--- ESTADO DE MIS SOLICITUDES (RF12) ---");
        List<Solicitud> misSolicitudes = solicitudes.stream()
                .filter(s -> s.solicitanteUsername.equals(usuarioActual.username))
                .collect(Collectors.toList());

        if (misSolicitudes.isEmpty()) {
            System.out.println("➡️ No ha registrado ninguna solicitud todavía.");
            return;
        }
        misSolicitudes.forEach(System.out::println);
    }

    private static void login() {

        cargarTodo();

        int intentos = 0;
        final int MAX_INTENTOS = 3;

        while (intentos < MAX_INTENTOS) {

            System.out.println("\n--- INICIO DE SESIÓN CFE (RF18) ---");
            System.out.print("Usuario: ");
            String user = scanner.nextLine().trim().toLowerCase();
            System.out.print("Contraseña: ");
            String pass = scanner.nextLine().trim();

            usuarioActual = usuarios.stream()
                    .filter(u -> u.username.equals(user) && u.password.equals(pass))
                    .findFirst()
                    .orElse(null);

            if (usuarioActual != null) {
                System.out.println("\n*** Bienvenido, " + usuarioActual.rol.name() + " (" + usuarioActual.username.toUpperCase() + "). ***");
                switch (usuarioActual.rol) {
                    case ADMIN: menuAdmin(); break;
                    case RESPONSABLE: menuResponsable(); break;
                    case SOLICITANTE: menuSolicitante(); break;
                }
                usuarioActual = null;
                return;
            }
            else {
                intentos++;
                System.err.println("❌ Credenciales incorrectas. Intento " + intentos + " de " + MAX_INTENTOS);
            }
        }
        System.out.println("Máximo de intentos alcanzado. Aplicación terminada.");
    }

    private static String generarFolio() {
        return "CFE-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + (1000 + new Random().nextInt(9000)); // RF3
    }

    public static void main(String[] args) {

        asegurarArchivosYDatos();
        System.out.println("=====================================================================");
        System.out.println("  SISTEMA DE CONTROL DE SOLICITUDES CFE - PROYECTO INTEGRADOR");
        System.out.println("=====================================================================");

        login();

        System.out.println("\nPrograma finalizado.");
    }
}