package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.sun.xml.internal.ws.resources.SenderMessages;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import practica3.GUI.GugelCarView;

import java.util.ArrayList;

/**
 * Supermente, clase controladora de los vehículos zombies.
 *
 *
 */

public class SuperMente extends SingleAgent {


    //DATOS MIEMBROS
    private AgentID controllerID;
    private String mapa; // Si va a logearse tiene que saber a que mapa
    private String conversationID;
    private String replyWith;
    private GugelCarView view;
    private int status;
    /////////////////////////////////////////////

    // Datos que nos indican los estados de los vehiculos (posicion, bateria, tipo, ...)
    private ArrayList<EstadoVehiculo> vehiculos;

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    private int [][] mapaMundo;

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
     * @author Andrés Molina López
     */
    @Override
    public void init(){
        /*FUTURO INIT* /
        comenzarSesion();
        /*INIT DEPRECATED, ACTUALMENTE PARA PRUEBAS*/
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
     * @author Ángel Píñar Rivas
     */
    @Override
    public void execute(){
        boolean salir=false;
        boolean exploracion_exitosa=false;
        boolean vehiculos_ir_obj=false; // Si está a true, tenemos los vehiculos que queremos para empezar a ir al objetivo
        boolean preparados_ir_obj=true; // Si está a true, pueden ir al objetivo ya que es un nuevo subscribe y la bateria esta llena

        while(!salir) {
            switch (status) {
                case Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO:
                    comenzarSesion();
                    preparados_ir_obj = true;
                    status = Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS;
                    break;
                case Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS:

                    for(EstadoVehiculo vehiculo: vehiculos){
                        registrarVehiculo(vehiculo.id);
                    }

                    if(!exploracion_exitosa) {
                        status = Mensajes.SUPERMENTE_STATUS_EXPLORACION;
                    } else {
                        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO_OBJ;
                    }
                    break;
                case Mensajes.SUPERMENTE_STATUS_EXPLORACION:
                    exploracion_exitosa = explorarMapa();
                    preparados_ir_obj = false;

                    if (exploracion_exitosa) {
                        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO_OBJ;
                    } else {
                        reiniciarSesion();
                        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
                    }
                    break;
                case Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO_OBJ:
                    if(preparados_ir_obj) {
                        for (EstadoVehiculo vehiculo : vehiculos) {
                            //Criterio de aceptación para ir al objetivo, provisionalmente minimo 1 dron
                            if (!vehiculos_ir_obj) {
                                vehiculos_ir_obj = vehiculo.tipoVehiculo == TipoVehiculo.dron;
                            }
                        }
                    }

                    if(vehiculos_ir_obj){
                        status = Mensajes.SUPERMENTE_STATUS_YENDO_OBJ;
                    } else {
                        reiniciarSesion();
                        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
                    }
                    break;
                case Mensajes.SUPERMENTE_STATUS_YENDO_OBJ:
                    salir = irAlObjetivo();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO; //Si salir es true esto da igual, pero si es false toca volver a intentarlo.
                    break;
            }
        }
        finalizarSesion();
    }

    /** Manda un cancel al controlador para así poder iniciar una nueva sesión posteriormente
     *
     * @author Ángel Píñar Rivas
     */
    private void reiniciarSesion(){
        sendMessageController(ACLMessage.CANCEL, "");
    }

    /** Manda cancel al controlador y a los vehículos para finalizar la sesión
     *
     * @author Ángel Píñar Rivas
     */
    private void finalizarSesion(){
        for(EstadoVehiculo vehiculo: vehiculos){
            sendMessageVehiculo(ACLMessage.CANCEL, "", vehiculo.id);
        }
        sendMessageController(ACLMessage.CANCEL, "");
    }

    /** Manda subscribe al servidor y recibe la respuesta
     *
     * @author Ángel Píñar Rivas
     */
    private void comenzarSesion(){
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
    }

    /** Ordena al vehiculo que haga checkin y recibe la respuesta
     *
     * @author
     */
    private void registrarVehiculo(AgentID idVehiculo){

    }

    /**
     * Envía a los vehículos hacia el objetivo
     *
     * @author Ángel Píñar Rivas
     * @return True si estamos conformes con la cantidad de vehículos que han llegado al objetivo
     */
    private boolean irAlObjetivo(){
        boolean conformes = false;

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
        }

        return exito;
    }


    /**
     * Método para crear mensajes con distintas performativas
     * @param performativa tipo de performativa que va a tener el mensaje
     * @param message mensaje que se va a mandar
     * @author Andrés Molina López
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
            System.out.println(inbox.getContent());
            /**/

            if (inbox.getSender() == controllerID) {
                replyWith = inbox.getReplyWith();
            }

        } catch (InterruptedException e) {
            System.err.println("Error al recibir mensaje en receiveMessage de supermente");
        }

        return inbox;
    }

}
