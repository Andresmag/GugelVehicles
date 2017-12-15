package practica3;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

import java.util.ArrayList;

/**
 * Supermente, clase controladora de los vehículos zombies.
 *
 *
 */

public class SuperMente extends SingleAgent {


    //DATOS MIEMBROS
    private AgentID controllerID;
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
     * Constructor
     * @autor Diego Iáñez Ávila
     */
    SuperMente(AgentID aid) throws Exception {
        super(aid);

        controllerID = new AgentID("Girtab");
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
}
