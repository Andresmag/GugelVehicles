package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.sun.xml.internal.ws.resources.SenderMessages;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import org.codehaus.jettison.json.JSONObject;
import practica3.GUI.GugelCarView;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Supermente, clase controladora de los vehículos zombies.
 *
 *
 */

public class SuperMente extends SingleAgent {

    // DATOS MIEMBROS
    private AgentID controllerID;
    private String mapa;                // Si va a loguearse tiene que saber a que mapa
    private String conversationID;
    private String replyWith;
    private GugelCarView view;
    private int status;
    /////////////////////////////////////////////

    // Datos que nos indican los estados de los vehiculos (posicion, bateria, tipo, ...)
    private ArrayList<EstadoVehiculo> vehiculos;

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    private int [][] mapaMundo = new int[1000][1000];

    // Batería total en el mundo. Se actualiza cada vez que se procesa la percepción de un vehículo.
    private int bateriaTotal;

    // Memoria interna con las direcciones
    //private final ArrayList<String> direcciones;


    /**
     * Constructor para la view
     * @autor Diego Iáñez Ávila, Andrés Molina López
     */
    public SuperMente(String map, AgentID aid, GugelCarView v) throws Exception {
        super(aid);

        mapa = map;
        controllerID = new AgentID("Girtab");
        view = v;
        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
    }

    // He tenido que crear este segundo constructor mientras la view no funciona para que no
    // de fallos y deje compilar
    /**
     * Constructor
     * @autor Diego Iáñez Ávila, Andrés Molina López
     */
    SuperMente(String map, AgentID aid) throws Exception {
        super(aid);

        mapa = map;
        controllerID = new AgentID("Girtab");
    }

    /**
     * Método para la inicialización del agente
     *
     * @author Andrés Molina López, Ángel Píñar Rivas
     */
    @Override
    public void init(){
        /*FUTURO INIT*/

        // Inicializar algunas estructuras
        vehiculos = new ArrayList<>();

        for(int i=0; i<4; i++){
            vehiculos.add(new EstadoVehiculo(new AgentID("coche" + i)));
        }

        //reiniciarSesion();

        /*INIT DEPRECATED, ACTUALMENTE PARA PRUEBAS* /
        JsonObject jsonLogin = Json.object();
        jsonLogin.add(Mensajes.AGENT_COM_WORLD, mapa);

        sendMessageController(ACLMessage.SUBSCRIBE, jsonLogin.toString());

        // Recibir y guardar el conversation-ID
        ACLMessage answer = receiveMessage();

        if (answer.getPerformativeInt() == ACLMessage.INFORM){
            conversationID = answer.getConversationId();
            System.out.println(conversationID);
        }
        else{
            System.out.println(answer.getContent().toString());
        }

        sendMessageController(ACLMessage.CANCEL, "");
        /**/
    }

    /**
     * Método de ejecución de la supermente
     *
     * @author Ángel Píñar Rivas, Jose Luis Martínez Ortiz
     */
    @Override
    public void execute(){
        boolean salir=false;
        boolean exploracion_exitosa=false;
        boolean tenemos_dron = false;

        while(!salir) {
            switch (status) {
                case Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO:
                    comenzarSesion();
                    status = Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS;
                    break;
                case Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS:

                    for(EstadoVehiculo vehiculo: vehiculos){
                        registrarVehiculo(vehiculo);

                        //Cuando tengamos el drón
                        if (vehiculo.tipoVehiculo == TipoVehiculo.DRON){
                            tenemos_dron = true;
                            // dejamos de registrar más vehículos si vamos a explorar
                            //if(!exploracion_exitosa)
                            //    break;
                        }
                    }

                    if(!exploracion_exitosa) {
                        if(tenemos_dron) {
                            status = Mensajes.SUPERMENTE_STATUS_EXPLORACION;
                        }else{
                            reiniciarSesion();
                            status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
                        }
                    } else {
                        status = Mensajes.SUPERMENTE_STATUS_YENDO_OBJ;
                    }

                    break;
                case Mensajes.SUPERMENTE_STATUS_EXPLORACION:
                    exploracion_exitosa = explorarMapa();

                    //Cuando termino de explorar vuelvo a empezar según la exploración
                    reiniciarSesion();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;

                    break;
                case Mensajes.SUPERMENTE_STATUS_YENDO_OBJ:
                    salir = irAlObjetivo();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO; //Si salir es true esto da igual, pero si es false toca volver a intentarlo.

                    //Si por algún motivo terminamos y no completamos el mapa, reiniciamos
                    if(!salir){
                        reiniciarSesion();
                    }

                    break;
            }
        }
        finalizarSesion();
    }

    /** Manda un cancel al controlador para así poder iniciar una nueva sesión posteriormente
     *
     * @author Ángel Píñar Rivas, Diego Iáñez Ávila
     * @return El mensaje con la traza
     */
    private ACLMessage reiniciarSesion(){
        sendMessageController(ACLMessage.CANCEL, "");

        // Esperar al agree
        ACLMessage inbox;
        do {
            inbox = receiveMessage();
        } while (inbox.getPerformativeInt() != ACLMessage.AGREE);

        // Recibir la traza
        inbox = receiveMessage();

        return inbox;
    }

    /** Manda cancel al controlador y a los vehículos para finalizar la sesión
     *
     * @author Ángel Píñar Rivas
     */
    private void finalizarSesion(){
        for(EstadoVehiculo vehiculo: vehiculos){
            sendMessageVehiculo(ACLMessage.CANCEL, "", vehiculo.id);
        }

        ACLMessage mensajeTraza = reiniciarSesion();
        guardarTraza(mensajeTraza);
    }

    /**
     * Guardar la traza recibida del servidor
     * @author Diego Iáñez Ávila
     */
    private void guardarTraza(ACLMessage mensajeTraza){
        if (mensajeTraza.getPerformativeInt() == ACLMessage.INFORM && status != Mensajes.SUPERMENTE_STATUS_EXPLORACION){
            try {
                JsonObject injson = Json.parse(mensajeTraza.getContent()).asObject();
                JsonArray ja = injson.get(Mensajes.AGENT_COM_TRACE).asArray();

                byte data[] = new byte[ja.size()];

                for (int i = 0; i < data.length; ++i){
                    data[i] = (byte) ja.get(i).asInt();
                }

                String nombre = "traza_" + conversationID + "_vehiculos_";

                for (EstadoVehiculo vehiculo : vehiculos){
                    switch (vehiculo.tipoVehiculo){
                        case DRON:
                            nombre += "dron_";
                            break;
                        case COCHE:
                            nombre += "coche_";
                            break;
                        case CAMION:
                            nombre += "camion_";
                            break;
                    }
                }

                nombre += ".png";

                FileOutputStream fos = new FileOutputStream(nombre);
                fos.write(data);
                fos.close();
                System.out.println("Traza guardada en " + nombre);

            } catch (IOException ex){
                System.err.println("Error procesando traza");
            }
        }
    }

    /** Manda subscribe al servidor y recibe la respuesta
     *
     * @author Ángel Píñar Rivas, Andrés Molina López
     */
    private void comenzarSesion(){
        JsonObject jsonLogin = Json.object();
        jsonLogin.add(Mensajes.AGENT_COM_WORLD, mapa);

        ACLMessage answer;

        do {
            sendMessageController(ACLMessage.SUBSCRIBE, jsonLogin.toString());

            // Recibir y guardar el conversation-ID
            answer = receiveMessage();

            if (answer.getPerformativeInt() == ACLMessage.INFORM) {
                conversationID = answer.getConversationId();
                System.out.println(conversationID);
            } else {
                System.out.println(answer.getContent().toString());
            }
        } while (answer.getPerformativeInt() != ACLMessage.INFORM || !answer.getContent().equals("{\"result\":\"OK\"}"));
    }

    /** Ordena al vehiculo que haga checkin y recibe la respuesta
     *
     * @author Diego Iáñez Ávila
     */
    private void registrarVehiculo(EstadoVehiculo vehiculo){
        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_CHECKIN), vehiculo.id);

        ACLMessage inbox = receiveMessage();
        String tipoVehiculo = inbox.getContent();

        if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_HELICOPTERO)){
            vehiculo.tipoVehiculo = TipoVehiculo.DRON;
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_CAMION)){
            vehiculo.tipoVehiculo = TipoVehiculo.CAMION;
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_COCHE)){
            vehiculo.tipoVehiculo = TipoVehiculo.COCHE;
        }

        System.out.println("Vehículo registrado.");
    }

    /**
     * Crear un JSON para mandar comandos
     * @author Diego Iáñez Ávila
     */
    private String jsonComando(String comando){
        JsonObject json = Json.object();
        json.add(Mensajes.AGENT_COM_COMMAND, comando);

        return json.toString();
    }

    /**
     * Envía a los vehículos hacia el objetivo
     *
     * @author Ángel Píñar Rivas
     * @return True si estamos conformes con la cantidad de vehículos que han llegado al objetivo
     */
    private boolean irAlObjetivo(){
        boolean conformes = false;

        System.out.println("El método irAlObjetivo no está hecho.");
        conformes = true;

        return conformes;
    }

    /**
     * Gestiona y ordena a los vehiculos para que exploren el mapa.
     * Termina cuando se ha explorado el mapa completo TODO ¿O cuando encuentran el objetivo?
     *
     * @author Ángel Píñar Rivas
     * @return True si se ha terminado la exploración con éxito antes de quedarse sin batería.
     */
    private boolean explorarMapa(){
        boolean exploracion_terminada = false;
        boolean exito = false;

        while(!exploracion_terminada){
            System.out.println("El método explorarMapa no está hecho.");
            exploracion_terminada = true;
            exito = true;
        }

        /* Pruebas */
        for (EstadoVehiculo vehiculo : vehiculos){
            sendMessageVehiculo(ACLMessage.QUERY_REF, "", vehiculo.id);
            receiveMessage();
        }
        /* Fin de prueba */

        return exito;
    }

    /**
     * Procesar la percepción recibida de un vehículo
     * @author Diego Iáñez Ávila
     */
    private void procesarPercepcion(EstadoVehiculo vehiculo, String jsonPercepcion){
        JsonObject percepcion = Json.parse(jsonPercepcion).asObject();
        JsonObject resultado = percepcion.get(Mensajes.AGENT_COM_RESULT).asObject();

        vehiculo.battery = resultado.getInt(Mensajes.AGENT_COM_SENSOR_BATTERY, 0);
        vehiculo.coor_x = resultado.getInt(Mensajes.AGENT_COM_SENSOR_GPS_X, 0);
        vehiculo.coor_y = resultado.getInt(Mensajes.AGENT_COM_SENSOR_GPS_Y, 0);
        vehiculo.objetivoAlcanzado = resultado.getBoolean(Mensajes.AGENT_COM_SENSOR_REACHED_GOAL, false);

        bateriaTotal = resultado.getInt(Mensajes.AGENT_COM_SENSOR_GLOBAL_ENERGY, 0);

        // Procesar el sensor
        JsonArray sensor = resultado.get(Mensajes.AGENT_COM_SENSOR_SENSOR).asArray();

        int inicio = 0, ancho = 0;

        switch (vehiculo.tipoVehiculo){
            case DRON:
                inicio = 1;
                ancho = 3;
                break;

            case COCHE:
                inicio = 2;
                ancho = 5;
                break;

            case CAMION:
                inicio = 5;
                ancho = 11;
                break;
        }

        int x = vehiculo.coor_x - inicio;
        int y = vehiculo.coor_y - inicio;
        int xmapa, ymapa;

        for (int fila = 0; fila < ancho; ++fila){
            for (int columna = 0; columna < ancho; ++columna){
                xmapa = x + columna;
                ymapa = y + fila;

                if (xmapa >= 0 && ymapa >= 0){
                    mapaMundo[xmapa][ymapa] = sensor.get(fila * ancho + columna).asInt();
                }
            }
        }
    }


    /**
     * Método para crear mensajes con distintas performativas
     * @param performativa tipo de performativa que va a tener el mensaje
     * @param message mensaje que se va a mandar
     * @author Andrés Molina López
     * Está en amarillo porque se repite con el de Vehiculo.java,
     * pero como no hay herencia múltiple pues es un mal menor
     */
    private void sendMessageController(int performativa, String message){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(controllerID);
        outbox.setContent(message);
        outbox.setConversationId(conversationID);

        if (replyWith != null)
            outbox.setInReplyTo(replyWith);

        outbox.setPerformative(performativa);

        System.out.println("Supermente envia a controlador: " + message);

        send(outbox);
    }

    /**
     * Enviar un mensaje a Supermente
     *
     * @author Andrés Molina López
     * @param message Mensaje a enviar
     */
    private void sendMessageVehiculo(int performativa, String message, AgentID aid){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(aid);
        outbox.setContent(message);
        outbox.setConversationId(conversationID);
        outbox.setPerformative(performativa);

        System.out.println("Supermente envía a vehículo " + aid.toString() + ": " + message);

        send(outbox);
    }

    /**
     * Recibir un mensaje ACL
     *
     * @author Diego Iáñez Ávila
     * @return El mensaje recibido
     */
    private ACLMessage receiveMessage(){
        ACLMessage inbox = null;

        try {
            inbox = receiveACLMessage();
            /* Imprimir para debug */
            System.out.println("Supermente recibe " + inbox.getContent());
            /**/

            if (inbox.getSender().toString().equals(controllerID.toString())) {
                replyWith = inbox.getReplyWith();
            }

        } catch (InterruptedException e) {
            System.err.println("Error al recibir mensaje en receiveMessage de supermente");
        }

        return inbox;
    }

}
