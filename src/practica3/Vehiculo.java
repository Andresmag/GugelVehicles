package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Vehículo zombie que se mueve por el mundo.
 */
public class Vehiculo extends SingleAgent {


    private String password;
    private AgentID controllerID, supermenteID;
    private String conversationID;
    private String replyWith = null;
    private int status;

   // private GugelCarView view;

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila, Andrés Molina López, Jose Luis Martínez Ortiz
     * @param aid ID del agente
     * @throws Exception si no puede crear el agente
     */
    public Vehiculo(AgentID aid) throws Exception {
        super(aid);
        controllerID = new AgentID("Girtab");
        supermenteID = new AgentID("Supermente");
        conversationID = null;
    }

    /**
     * Método de inicialización del agente
     *
     * @author Andrés Molina López ft. Diego, Jose y Ángel
     */
    @Override
    public void init(){
        status = Mensajes.VEHICLE_STATUS_CHECKIN;
    }

    /**
     * Cuerpo del agente
     *
     * @author Diego Iáñez Ávila, Andrés Molina López, Jose Luis Martínez Ortiz, Ángel Píñar Rivas
     */
    @Override
    public void execute(){
        boolean salir=false;

        while(!salir){
            switch (status){
                case Mensajes.VEHICLE_STATUS_CHECKIN:
                    checkin();

                    break;
                case Mensajes.VEHICLE_STATUS_CONFIRMANDO_TIPO:
                    tipoVehiculo();

                    break;
                case Mensajes.VEHICLE_STATUS_PERCIBIENDO:
                    informarPercepcion();
                    break;
                case Mensajes.VEHICLE_STATUS_ESCUCHANDO_ORDEN:
                    escucharOrden();
                    break;

                case Mensajes.VEHICLE_STATUS_EJECUTANDO_ORDEN:
                    ejecutarOrden();
                    break;

                case Mensajes.VEHICLE_STATUS_TERMINAR:
                    salir = true;
                    break;
            }
        }
    }

    /**
     * Estado Haciendo checkin
     * @author Diego Iáñez Ávila
     */
    private void checkin(){
        ACLMessage inbox = receiveMessage();

        if (inbox.getPerformativeInt() == ACLMessage.REQUEST) {
            hacerCheckin(inbox);
        }
    }

    /**
     * Hace checking y resetea las cosas.
     * Hay que pasarle el mensaje recibido de supermente con la petición de checkin
     * @author Diego Iáñez Ávila
     */
    private void hacerCheckin(ACLMessage peticionSupermente){
        conversationID = peticionSupermente.getConversationId();
        replyWith = null;
        sendMessageController(ACLMessage.REQUEST, peticionSupermente.getContent());

        status = Mensajes.VEHICLE_STATUS_CONFIRMANDO_TIPO;
    }

    /**
     * Estado Confirmando tipo de vehículo
     * @author Diego Iañez Ávila, Andrés Molina López
     */
    private void tipoVehiculo(){
        ACLMessage inbox = receiveMessage();
        JsonObject contenido = Json.parse(inbox.getContent()).asObject();

        if (inbox.getSender() == controllerID){
            String tipo = "";

            // Mensaje de controlador: dice nuestras capacidades
            if (inbox.getPerformativeInt() == ACLMessage.INFORM){
                String respuesta = contenido.get(Mensajes.AGENT_COM_RESULT).asString();
                if (respuesta.equals("OK")){
                    JsonObject capabilities = contenido.get("capabilities").asObject();
                    int fuel = capabilities.get("fuelrate").asInt();
                    int range = capabilities.get("range").asInt();
                    boolean fly = capabilities.get("fly").asBoolean();
                    if(fly){
                        tipo = ("helicoptero");
                    } else {
                        if (range > 5)
                            tipo = ("camion");
                        else
                            tipo = ("coche");
                    }
                }
            }

            sendMessageSupermente(ACLMessage.INFORM, tipo);
        }
        else{
            // Mensaje de supermente: pide percepción o pide que nos volvamos a suscribir
            if (inbox.getPerformativeInt() == ACLMessage.QUERY_REF){
                // Pedir percepción a controlador y cambiar de estado
                sendMessageController(ACLMessage.QUERY_REF, "");

                status = Mensajes.VEHICLE_STATUS_PERCIBIENDO;
            }
            else if (inbox.getPerformativeInt() == ACLMessage.REQUEST){
                hacerCheckin(inbox);
            }
        }
    }

    /**
     * Estado informando percepción a supermente
     * @author Diego Iáñez Ávila
     */
    private void informarPercepcion(){
        ACLMessage inbox = receiveMessage();

        // Recibir percepción de controlador y reenviar a supermente
        if (inbox.getReceiver() == controllerID && inbox.getPerformativeInt() == ACLMessage.INFORM){
            sendMessageSupermente(ACLMessage.INFORM, inbox.getContent());

            status = Mensajes.VEHICLE_STATUS_ESCUCHANDO_ORDEN;
        }
    }

    /**
     * Estado Escuchando orden de supermente
     * @author Diego Iáñez Ávila
     */
    private void escucharOrden(){
        ACLMessage inbox = receiveMessage();

        if (inbox.getReceiver() == controllerID){
            if (inbox.getPerformativeInt() == ACLMessage.REQUEST){
                // Supermente pide que ejecutemos un comando
                sendMessageController(ACLMessage.REQUEST, inbox.getContent());

                status = Mensajes.VEHICLE_STATUS_EJECUTANDO_ORDEN;
            }
            else if (inbox.getPerformativeInt() == ACLMessage.CANCEL){
                // Supermente pide que terminemos la ejecución
                status = Mensajes.VEHICLE_STATUS_TERMINAR;
            }
        }
    }

    /**
     * Estado Ejecutando orden
     * @author Diego Iáñez Ávila
     */
    private void ejecutarOrden(){
        ACLMessage inbox = receiveMessage();

        // Recibir confirmación de la ejecución del controlador y pedirle la percepción
        if (inbox.getReceiver() == controllerID && inbox.getPerformativeInt() == ACLMessage.INFORM){
            sendMessageController(ACLMessage.QUERY_REF, "");

            status = Mensajes.VEHICLE_STATUS_PERCIBIENDO;
        }
    }

    /**
     * Enviar un mensaje al controlador
     *
     * @author Diego Iáñez Ávila
     * @param message Mensaje a enviar
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
    private void sendMessageSupermente(int performativa, String message){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(supermenteID);
        outbox.setContent(message);
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
            System.err.println("Error al recibir mensaje en receiveMessage");
         }

        return inbox;
    }
}
