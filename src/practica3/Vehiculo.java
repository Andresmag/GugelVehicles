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
    private AgentID controllerID;
    private int numSensores;
    private String mapa;
    private int status;

   // private GugelCarView view;

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila, Andrés Molina López, Jose Luis Martínez Ortiz
     * @param aid ID del agente
     * @throws Exception si no puede crear el agente
     */
    public Vehiculo(String map, AgentID aid /* GUI, GugelCarView v*/) throws Exception {
        super(aid);
        controllerID = new AgentID("Girtab");
        superMente = new SuperMente();
        mapa = map;
       // view = v;
    }

    /**
     * Método de inicialización del agente
     *
     * @author Diego Iáñez Ávila, Jose Luis Martínez Ortiz, Ángel Píñar Rivas
     */
    @Override
    public void init(){
        // Loguearse en el mapa
        JsonValue agentID = Json.value(getAid().toString());

        JsonObject jsonLogin = Json.object();
        jsonLogin.add(Mensajes.AGENT_COM_COMMAND, Mensajes.AGENT_COM_LOGIN);
        jsonLogin.add(Mensajes.AGENT_COM_WORLD, mapa);
        jsonLogin.add(Mensajes.AGENT_COM_SENSOR_RADAR, agentID);
        jsonLogin.add(Mensajes.AGENT_COM_SENSOR_SCANNER, agentID);

        numSensores = 2;

        sendMessage(jsonLogin.toString());

        // Recibir y guardar la contraseña
        try {
            password = null;
            while(password == null) {
                JsonObject answer = receiveJson();
                password = answer.getString(Mensajes.AGENT_COM_RESULT, null);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = Mensajes.AGENT_STATUS_PERCIBIENDO;
    }

    /**
     * Cuerpo del agente
     *
     * @author Diego Iáñez Ávila, Andrés Molina López, Jose Luis Martínez Ortiz, Ángel Píñar Rivas
     */
    @Override
    public void execute(){
        int it=0;
        boolean salir=false;
        boolean objetivo_bloqueado=false;

        while(!salir){
            switch (status){
                case Mensajes.AGENT_STATUS_PERCIBIENDO:
                    processPerception();


                    if(superMente.hasReachedGoal() || it>1500 || objetivo_bloqueado){
                        status = Mensajes.AGENT_STATUS_FINALIZADO;
                    } else {
                        status = Mensajes.AGENT_STATUS_ACTUANDO;
                    }
                    break;
                case Mensajes.AGENT_STATUS_ACTUANDO:
                    String nextAction = superMente.nextAction();
                    System.out.println(nextAction);

                    if (nextAction.equals(Mensajes.AGENT_COM_ACCION_REFUEL))
                        refuel();
                    else
                        makeMove(nextAction);

                    status = Mensajes.AGENT_STATUS_PERCIBIENDO;
                    //Aumenta pasos cuando actúa
                    it++;

                    break;
                case Mensajes.AGENT_STATUS_FINALIZADO:
                    salir = true;
                    break;
            }
        }

        endSession();
    }

    /**
     * Le manda al servidor el comando con el movimiento del coche
     *
     * @author Andrés Molina López
     * @param nextMove indica cual es el string que se va a mandar al servidor
     */
    private void makeMove(String nextMove) {
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
    private void refuel(){
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
        processPerception();

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
    private void sendMessage(String message){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(controllerID);
        outbox.setContent(message);

        send(outbox);
    }

    /**
     * Envía un comando al controlador
     *
     * @author Diego Iáñez Ávila, Jose Luis Martínez Ortiz
     * @param command Comando a enviar
     * @return true si el controlador respondió con OK al comando
     */
    private boolean sendCommand(String command){
        boolean success = true;

        JsonObject jsonCommand = Json.object();
        jsonCommand.add(Mensajes.AGENT_COM_COMMAND, command);
        jsonCommand.add(Mensajes.AGENT_COM_KEY, password);

        sendMessage(jsonCommand.toString());

        try{
            JsonObject answer = receiveJson();
            String result = answer.getString(Mensajes.AGENT_COM_RESULT, Mensajes.AGENT_COM_BADMESSAGE);

            if (!result.equals(Mensajes.AGENT_COM_OK))
                success = false;

        } catch (InterruptedException e){
            success = false;
        }

        return success;
    }

    /**
     * Recibir un mensaje del controlador en formato JSON
     *
     * @author Diego Iáñez Ávila
     * @return El JSON recibido
     * @throws InterruptedException Si hay error al recibir el mensaje
     */
    private JsonObject receiveJson() throws InterruptedException {
        ACLMessage inbox = receiveACLMessage();
        System.out.println("Recibido mensaje " + inbox.getContent());
        //GUI view.printToGeneralMsg("Recibido mensaje "+ inbox.getContent());

        return Json.parse(inbox.getContent()).asObject();
    }

    /**
     * Iniciar el procesamiento de la percepción
     *
     * @author Diego Iáñez Ávila
     */
    private void processPerception(){
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


}
