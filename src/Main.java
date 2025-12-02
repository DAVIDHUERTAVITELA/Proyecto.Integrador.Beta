import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // --- 1. CONFIGURACIÓN Y CONSTANTES ---
    private static final String ARCHIVO_USUARIOS = "usuarios.csv";
    private static final String ARCHIVO_SOLICITUDES = "solicitudes.csv";
    private static final String CSV_DELIMITER = ";";
    private static final Scanner scanner = new Scanner(System.in);

    // Almacenamiento In-Memory (Simula el caché de la aplicación)
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Solicitud> solicitudes = new ArrayList<>();
    private static Usuario usuarioActual = null;

    // --- 2. ENUMS Y CLASES DE MODELO (RF5, RF19) ---

    enum Rol { SOLICITANTE, RESPONSABLE, ADMIN }
    enum Estado { RECIBIDA, EN_PROCESO, COMPLETADA, CANCELADA }

    static class Solicitud {
        public String folio;
        public String titulo;
        public String descripcion;
        public String solicitanteUsername;
        public Estado estado;

        public Solicitud(String folio, String titulo, String descripcion, String solicitanteUsername, Estado estado) {
            this.folio = folio;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.solicitanteUsername = solicitanteUsername;
            this.estado = estado;
        }

        // Método para CSV (RF5)
        public String toCSV() {
            return String.join(CSV_DELIMITER,
                    folio, titulo, descripcion.replace("\n", " "), solicitanteUsername, estado.name());
        }

        @Override
        public String toString() {
            return String.format("| Folio: %-10s | Solicitante: %-15s | Estado: %-12s | Título: %s",
                    folio, solicitanteUsername, estado, titulo.substring(0, Math.min(titulo.length(), 20)) + "...");
        }
    }

    static class Usuario {
        public String username;
        public String password; // NOTA: En versión final, usaríamos hash
        public Rol rol;

        public Usuario(String username, String password, Rol rol) {
            this.username = username;
            this.password = password;
            this.rol = rol;
        }

        public String toCSV() {
            return String.join(CSV_DELIMITER, username, password, rol.name());
        }
    }

    // --- 3. GESTIÓN DE CSV (RF5 - Persistencia) ---

    private static void asegurarArchivos() {
        try {
            File usersFile = new File(ARCHIVO_USUARIOS);
            File requestsFile = new File(ARCHIVO_SOLICITUDES);

            // Inicializar usuarios si el archivo no existe
            if (!usersFile.exists()) {
                usersFile.createNewFile();
                // Usuario Admin por defecto y usuario de prueba
                usuarios.add(new Usuario("admin", "0000", Rol.ADMIN)); // Requisito Admin
                usuarios.add(new Usuario("dpto_ti", "1234", Rol.RESPONSABLE));
                usuarios.add(new Usuario("dpto_compras", "1234", Rol.SOLICITANTE));
                guardarUsuarios();
            }
            if (!requestsFile.exists()) {
                requestsFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error al crear archivos iniciales: " + e.getMessage());
        }
    }

    // Carga la lista de usuarios desde el CSV
    private static void cargarUsuarios() {
        usuarios.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_USUARIOS))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_DELIMITER);
                if (values.length == 3) {
                    usuarios.add(new Usuario(values[0], values[1], Rol.valueOf(values[2])));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar la lista de usuarios. Inicializando por defecto.");
            asegurarArchivos(); // Recargar usuarios por defecto si hay error.
        }
    }

    // Guarda la lista de usuarios al CSV
    private static void guardarUsuarios() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_USUARIOS))) {
            for (Usuario u : usuarios) {
                pw.println(u.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    // Carga la lista de solicitudes desde el CSV
    private static void cargarSolicitudes() {
        solicitudes.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO_SOLICITUDES))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_DELIMITER);
                if (values.length == 5) {
                    solicitudes.add(new Solicitud(values[0], values[1], values[2], values[3], Estado.valueOf(values[4])));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar la lista de solicitudes. Empezando vacío.");
        }
    }

    // Guarda la lista de solicitudes al CSV
    private static void guardarSolicitudes() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_SOLICITUDES))) {
            for (Solicitud s : solicitudes) {
                pw.println(s.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar solicitudes: " + e.getMessage());
        }
    }

    // --- 4. GESTIÓN DE USUARIOS (RF19, ADMIN) ---

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

    // --- 5. LÓGICA DE SOLICITUDES (RF1, RF3, RF6, RF8, RF12) ---

    private static void crearSolicitud() {
        System.out.println("\n--- REGISTRO DE NUEVA SOLICITUD (RF1) ---");

        System.out.print("Ingrese el Título de la Solicitud (Obligatorio - RF2): ");
        String titulo = scanner.nextLine().trim();

        if (titulo.isEmpty()) {
            System.err.println("❌ ERROR: El Título es obligatorio.");
            return;
        }

        System.out.print("Ingrese la Descripción detallada: ");
        String descripcion = scanner.nextLine().trim();

        String folio = generarFolio(); // RF3

        Solicitud nueva = new Solicitud(folio, titulo, descripcion, usuarioActual.username, Estado.RECIBIDA);
        solicitudes.add(nueva);
        guardarSolicitudes(); // Persistencia CSV

        System.out.println("\n✅ SOLICITUD REGISTRADA CON ÉXITO.");
        System.out.println("   Folio Asignado (RF3): " + nueva.folio);
    }

    private static void gestionarSolicitudes() {
        System.out.println("\n--- VISTA DE SOLICITUDES PENDIENTES (RF8) ---");

        // RF8: Filtrar solo las solicitudes no completadas o canceladas
        List<Solicitud> pendientes = solicitudes.stream()
                .filter(s -> s.estado != Estado.COMPLETADA && s.estado != Estado.CANCELADA)
                .collect(Collectors.toList());

        if (pendientes.isEmpty()) {
            System.out.println("➡️ No hay solicitudes pendientes de gestión.");
            return;
        }

        // Mostrar listado
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (int i = 0; i < pendientes.size(); i++) {
            System.out.println((i + 1) + ". " + pendientes.get(i));
        }
        System.out.println("--------------------------------------------------------------------------------------------------");

        System.out.print("\nIngrese el número de la solicitud a modificar (o 0 para salir): ");
        String seleccionStr = scanner.nextLine();
        int seleccion;

        try {
            seleccion = Integer.parseInt(seleccionStr);
        } catch (NumberFormatException e) {
            return;
        }

        if (seleccion > 0 && seleccion <= pendientes.size()) {
            Solicitud s = pendientes.get(seleccion - 1);
            System.out.println("\n--- GESTIÓN DEL FOLIO " + s.folio + " (RF6) ---");
            System.out.println("1. EN_PROCESO | 2. COMPLETADA | 3. CANCELADA | 4. Volver");
            System.out.print("Ingrese la nueva opción de estado: ");
            String opcionEstadoStr = scanner.nextLine();

            try {
                int opcionEstado = Integer.parseInt(opcionEstadoStr);
                Estado nuevoEstado = s.estado;

                switch (opcionEstado) {
                    case 1: nuevoEstado = Estado.EN_PROCESO; break;
                    case 2: nuevoEstado = Estado.COMPLETADA; break;
                    case 3: nuevoEstado = Estado.CANCELADA; break;
                    case 4: return;
                    default: System.err.println("❌ Opción no válida."); return;
                }

                // RF6: Actualizar estado y guardar Historial (Simulado)
                s.estado = nuevoEstado;
                guardarSolicitudes(); // Persistencia CSV
                System.out.println("✅ Estado del Folio " + s.folio + " actualizado a: " + nuevoEstado);

            } catch (NumberFormatException e) {
                System.err.println("❌ Entrada inválida.");
            }
        }
    }

    private static void consultarEstado() {
        System.out.println("\n--- ESTADO DE MIS SOLICITUDES (RF12) ---");

        List<Solicitud> misSolicitudes = solicitudes.stream()
                .filter(s -> s.solicitanteUsername.equals(usuarioActual.username))
                .collect(Collectors.toList());

        if (misSolicitudes.isEmpty()) {
            System.out.println("➡️ No ha registrado ninguna solicitud todavía.");
            return;
        }

        for (Solicitud s : misSolicitudes) {
            System.out.println(s);
        }
    }

    private static void consultarBaseDatosCSV() {
        System.out.println("\n--- ACCESO DIRECTO A DATOS CSV ---");
        System.out.println("1. Mostrar Solicitudes (Desde " + ARCHIVO_SOLICITUDES + ")");
        System.out.println("2. Mostrar Usuarios (Desde " + ARCHIVO_USUARIOS + ")");
        System.out.print("Seleccione una opción: ");
        String choice = scanner.nextLine();

        try {
            if (choice.equals("1")) {
                cargarSolicitudes();
                System.out.println("\n* Contenido del archivo de solicitudes:");
                solicitudes.forEach(s -> System.out.println(s.toCSV()));
            } else if (choice.equals("2")) {
                cargarUsuarios();
                System.out.println("\n* Contenido del archivo de usuarios:");
                usuarios.forEach(u -> System.out.println(u.toCSV()));
            } else {
                System.err.println("Opción no válida.");
            }
        } catch (Exception e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }


    // --- 6. MENÚS Y FLUJO PRINCIPAL ---

    private static void menuAdmin() {
        while (true) {
            System.out.println("\n--- MENÚ ADMINISTRADOR ---");
            System.out.println("1. Gestionar Usuarios (Crear, Eliminar, Listar)");
            System.out.println("2. Acceso a Base de Datos (CSV)");
            System.out.println("3. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": gestionarUsuarios(); break;
                case "2": consultarBaseDatosCSV(); break;
                case "3": System.out.println("Cerrando sesión de Administrador..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void gestionarUsuarios() {
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

    private static void menuResponsable() {
        while (true) {
            System.out.println("\n--- MENÚ RESPONSABLE ---");
            System.out.println("1. Gestionar Solicitudes Pendientes (RF8, RF6)");
            System.out.println("2. Acceso a Base de Datos (CSV)");
            System.out.println("3. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": gestionarSolicitudes(); break;
                case "2": consultarBaseDatosCSV(); break;
                case "3": System.out.println("Cerrando sesión de Responsable..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void menuSolicitante() {
        while (true) {
            System.out.println("\n--- MENÚ SOLICITANTE ---");
            System.out.println("1. Registrar Nueva Solicitud (RF1, RF2)");
            System.out.println("2. Consultar Estado de Solicitudes Propias (RF12)");
            System.out.println("3. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1": crearSolicitud(); break;
                case "2": consultarEstado(); break;
                case "3": System.out.println("Cerrando sesión de Solicitante..."); return;
                default: System.err.println("Opción no válida.");
            }
        }
    }

    private static void login() {
        cargarUsuarios();
        cargarSolicitudes();
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
                usuarioActual = null; // Limpiar al cerrar sesión
                return;
            } else {
                intentos++;
                System.err.println("❌ Credenciales incorrectas. Intento " + intentos + " de " + MAX_INTENTOS);
            }
        }
        System.out.println("Máximo de intentos alcanzado. Aplicación terminada.");
    }

    private static String generarFolio() {
        Random rand = new Random();
        return "CFE-" + (10000 + rand.nextInt(90000));
    }

    public static void main(String[] args) {
        asegurarArchivos(); // Asegura la existencia de los archivos CSV iniciales
        System.out.println("=====================================================================");
        System.out.println("  SISTEMA DE CONTROL DE SOLICITUDES CFE - PROTOTIPO CONSOLE + CSV");
        System.out.println("=====================================================================");

        login();

        System.out.println("\nPrograma Finalizado.");
    }
}