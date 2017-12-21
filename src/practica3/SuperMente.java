package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
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
    private GugelCarView view;
    /////////////////////////////////////////////

    // Dato que nos indica si el agente a alcanzado el objetivo
    private boolean reachedGoal;

    // Elementos para la percepcion inmediata del agente
    private ArrayList<Integer> radarCar;    // Matriz que representa la percepcion del sensor radar
    private ArrayList<Integer> completeRadar;    // Matriz que representa la percepcion del sensor radar
    private ArrayList<Float> scannerCar;    // Matriz que representa la percepcion del sensor scanner
    private int bateriaCar;                 // Porcentaje de carga de la bateria

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    private int [][] mapaPulgarcito;
    private int pos_fila_mapa;
    private int pos_col_mapa;

    // Memoria interna con las direcciones
    //private final ArrayList<String> direcciones;

    // Atributos propios de Fantasmita(TM)
    private boolean fantasma_activo;
    private int [][] radarFantasmita;
    private int [][] mapaMundo;
    private int fantasmita_x;       // Variable X de origen del algoritmo
    private int fantasmita_y;       // Variable Y de origen del algoritmo
    private int objetivoX;
    private int objetivoY;

    /**
     * Constructor para la view
     * @autor Diego Iáñez Ávila, Andrés Molina López
     */
    public SuperMente(String map, AgentID aid, GugelCarView v) throws Exception {
        super(aid);

        mapa = map;
        reachedGoal = false;
        controllerID = new AgentID("Girtab");
        view = v;
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
        reachedGoal = false;
        controllerID = new AgentID("Girtab");
    }

    /**
     * Método para la inicialización del agente
     *
     * @author Andrés Molina López
     */
    @Override
    public void init(){
        JsonObject jsonLogin = Json.object();
        jsonLogin.add(Mensajes.AGENT_COM_WORLD, mapa);

        sendMessage(ACLMessage.SUBSCRIBE, jsonLogin.toString());

        // Recibir y guardar el conversation-ID
        ACLMessage answer = null;

        try {
            answer = receiveACLMessage();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (answer.getPerformativeInt() == ACLMessage.INFORM){
            conversationID = answer.getConversationId();
            System.out.println(conversationID);
        }
        else{
            System.out.println(answer.getContent().toString());
        }

        sendMessage(ACLMessage.CANCEL, "");
    }

    public boolean hasReachedGoal() {
        return false;
    }

    public String nextAction() {
        return "";
    }

    public void refreshMemory(boolean resultadoMovimiento, String nextMove) {
    }

    public void refreshBatery() {
    }

    public void processPerception(ArrayList<JsonObject> messages) {
    }

    /**
     * Método para crear mensajes con distintas performativas
     * @param performativa tipo de performativa que va a tener el mensaje
     * @param contenido mensaje que se va a mandar
     * @author Andrés Molina López
     */
    private void sendMessage(int performativa, String contenido){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(controllerID);
        outbox.setContent(contenido);
        outbox.setPerformative(performativa); // int
        this.send(outbox);
    }

}
