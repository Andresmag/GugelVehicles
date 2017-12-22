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
        status = Mensajes.VEHICLE_STATUS_ESCUCHANDO;
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
                case Mensajes.VEHICLE_STATUS_ESCUCHANDO:
                    String command = procesarOrden();
                    sendMessageController(ACLMessage.REQUEST, command);


                    String answer = procesarOrden();
                    sendMessageSupermente(ACLMessage.INFORM, answer);

                    break;
                case Mensajes.VEHICLE_STATUS_CONECTADO:

                    break;
                case Mensajes.VEHICLE_STATUS_ACTUANDO:
                    salir = true;
                    break;
            }
        }

        endSession();
    }

    /**
     * Metodo para escuchar el mensaje que manda Supermente
     *
     * @author Andrés Molina López
     */
    private String procesarOrden(){
        ACLMessage inbox = receiveMessage();
        JsonObject contenido = Json.parse(inbox.getContent()).asObject();

        switch (inbox.getPerformativeInt()){
            case ACLMessage.REQUEST:
                String comando = contenido.get(Mensajes.AGENT_COM_COMMAND).asString();
                conversationID = inbox.getConversationId();
                return (comando);
                break;
            case ACLMessage.QUERY_REF:
                break;
            case ACLMessage.CANCEL:
                break;
            case ACLMessage.INFORM:
                String respuesta = contenido.get(Mensajes.AGENT_COM_RESULT).asString();
                if (respuesta.equals("OK")){
                    JsonObject capabilities = contenido.get("capabilities").asObject();
                    int fuel = capabilities.get("fuelrate").asInt();
                    int range = capabilities.get("range").asInt();
                    boolean fly = capabilities.get("fly").asBoolean();
                    if(fly){
                        return ()
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Le manda al servidor el comando con el movimiento del coche
     *
     * @author Andrés Molina López
     * @param nextMove indica cual es el string que se va a mandar al servidor
     */
 /*   private void makeMove(String nextMove) {
        if(!nextMove.isEmpty()) {
            boolean resultadoMovimiento = sendCommand(nextMove);
            superMente.refreshMemory(resultadoMovimiento, nextMove);
        }
    }

    /**
     * Recarga la bateria del coche
     *
     * @author Andrés Molina López
     */
 /*   private void refuel(){
        sendCommand(Mensajes.AGENT_COM_ACCION_REFUEL);
        superMente.refreshBatery();
    }

    /**
     * Finaliza la sesión con el controlador
     *
     * @author Diego Iáñez Ávila
     */
    private void endSession(){
        // Desloguearse
        System.out.println("Terminando sesión");
        //GUI view.printToGeneralMsg("Terminando sesión");

        sendCommand(Mensajes.AGENT_COM_LOGOUT);
        //processPerception();

        try{
            System.out.println("Recibiendo traza");
    //GUI        view.printToGeneralMsg("Recibiendo traza");
            JsonObject injson = receiveJson();
            JsonArray ja = injson.get(Mensajes.AGENT_COM_TRACE).asArray();

            byte data[] = new byte[ja.size()];

            for (int i = 0; i < data.length; ++i){
                data[i] = (byte) ja.get(i).asInt();
            }

            FileOutputStream fos = new FileOutputStream("traza_" + password + ".png");
            fos.write(data);
            fos.close();
            System.out.println("Traza guardada en " + "traza_" + password + ".png");
          //GUI  view.printToGeneralMsg("Traza guardada en \" + \"traza_\" + password + \".png");

        } catch (InterruptedException | IOException ex){
            System.err.println("Error procesando traza");
            //GUI view.printToGeneralMsg("Error procesando traza");
        }

        //GUI view.enableEjecutar();
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

            replyWith = inbox.getReplyWith();

        } catch (InterruptedException e) {
            System.err.println("Error al recibir mensaje en receiveMessage");
         }

        return inbox;
    }

    /**
     * Iniciar el procesamiento de la percepción
     *
     * @author Diego Iáñez Ávila
     */
  /*  private void processPerception(){
        try {
            // Recibimos los mensajes del servidor en orden
            ArrayList<JsonObject> messages = new ArrayList<>();

            for (int i = 0; i < numSensores; ++i) {
                JsonObject msg = receiveJson();
                messages.add(msg);
            }

            superMente.processPerception(messages);

        } catch (Exception e){
            e.printStackTrace();
        }

   //GUI     view.printToScanner(cerebro.getScannerCar());
       //GUI view.printToRadar(cerebro.getRadarCar());

        // Pintar el contenido del radar completo en el mapa
        //GUI  view.updateMap(cerebro.getPosX(), cerebro.getPosY(), cerebro.getCompleteRadar());
    }

*/
}
