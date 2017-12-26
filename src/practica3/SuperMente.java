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
        status = 0;
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
                    //Suscribe al servidor
                    //Recibe inform OK, coge conversation ID
                    preparados_ir_obj = true;
                    status = Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS;
                    break;
                case Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS:
                    //Pide checkin a vehiculo 0 mandando conversation id
                    //Pide checkin a vehiculo 1 mandando conversation id
                    //Pide checkin a vehiculo 2 mandando conversation id
                    //Pide checkin a vehiculo 3 mandando conversation id

                    //Recibe inform tipovehiculo de vehiculo 0
                    //Recibe inform tipovehiculo de vehiculo 1
                    //Recibe inform tipovehiculo de vehiculo 2
                    //Recibe inform tipovehiculo de vehiculo 3
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
                        //hace un cancel para empezar de nuevo
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
                        //Hace cancel
                        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
                    }
                    break;
                case Mensajes.SUPERMENTE_STATUS_YENDO_OBJ:
                    salir = irAlObjetivo();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO; //Si salir es true esto da igual, pero si es false toca volver a intentarlo.
                    break;
            }
        }
        //Hacer un cancel
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
