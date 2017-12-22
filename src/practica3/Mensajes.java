package practica3;
/**
 * Enum que contiene los mensajes de la aplicación
 * incluyendo los mensajes del agente.
 *
 * @author Jose Luis Martínez Ortiz, Andrés Molina López
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
    public static final String AGENT_COM_LOGIN = "login";

    public static final String AGENT_COM_RESULT = "result";

    public static final String AGENT_COM_COMMAND = "command";
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

    // ESTADOS DE GUGEL CAR
    public static final int VEHICLE_STATUS_ESCUCHANDO = 0;
    public static final int VEHICLE_STATUS_CONECTADO = 1;
    public static final int VEHICLE_STATUS_ACTUANDO = 2;

}