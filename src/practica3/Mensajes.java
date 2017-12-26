package practica3;
/**
 * Enum que contiene los mensajes de la aplicación
 * incluyendo los mensajes del agente.
 *
 * @author Jose Luis Martínez Ortiz, Andrés Molina López, Diego Iáñez Ávila
 *
 *
 */
public enum Mensajes {

    INSTANCE;

    // Configuración del servidor
    public static final String AGENT_USER = "Eridano";
    public static final String AGENT_HOST = "Girtab";
    public static final String AGENT_PASS = "Esquivel";


    // MENSAJES DEL AGENTE
    public static final String AGENT_COM_OK = "OK";
    public static final String AGENT_COM_BADMESSAGE = "BAD_MESSAGE";

    public static final String AGENT_COM_LOGOUT = "logout";

    public static final String AGENT_COM_COMMAND = "command";
    public static final String AGENT_COM_RESULT = "result";
    public static final String AGENT_COM_KEY = "key";
    public static final String AGENT_COM_WORLD = "world";
    public static final String AGENT_COM_SENSOR_SCANNER = "scanner";
    public static final String AGENT_COM_SENSOR_RADAR = "radar";

    public static final String AGENT_COM_TRACE = "trace";

    // ACCIONES DEL COCHE
    public static final String AGENT_COM_ACCION_REFUEL = "refuel";
    public static final String AGENT_COM_ACCION_MV_NW = "moveNW";
    public static final String AGENT_COM_ACCION_MV_N = "moveN";
    public static final String AGENT_COM_ACCION_MV_NE = "moveNE";
    public static final String AGENT_COM_ACCION_MV_W = "moveW";
    public static final String AGENT_COM_ACCION_MV_E = "moveE";
    public static final String AGENT_COM_ACCION_MV_SW = "moveSW";
    public static final String AGENT_COM_ACCION_MV_S = "moveS";
    public static final String AGENT_COM_ACCION_MV_SE = "moveSE";
    public static final String AGENT_COM_CHECKIN = "checkin";

    // ESTADOS DE GUGEL CAR
    public static final int VEHICLE_STATUS_CHECKIN = 0;
    public static final int VEHICLE_STATUS_CONFIRMANDO_TIPO = 1;
    public static final int VEHICLE_STATUS_PERCIBIENDO = 2;
    //public static final int VEHICLE_STATUS_INFORMANDO_PERCEPCION = 3;
    public static final int VEHICLE_STATUS_ESCUCHANDO_ORDEN = 4;
    public static final int VEHICLE_STATUS_EJECUTANDO_ORDEN = 5;
    public static final int VEHICLE_STATUS_TERMINAR = 6;

    // ESTADOS DE SUPERMENTE
    public static final int SUPERMENTE_STATUS_SUSCRIBIENDO = 0;
    public static final int SUPERMENTE_STATUS_CONTANDOVEHICULOS = 1;
    public static final int SUPERMENTE_STATUS_EXPLORACION = 2;
    public static final int SUPERMENTE_STATUS_SUSCRIBIENDO_OBJ = 3;
    public static final int SUPERMENTE_STATUS_YENDO_OBJ = 4;
}