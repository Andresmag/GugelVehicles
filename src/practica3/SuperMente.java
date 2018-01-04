package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.sun.mail.imap.ACL;
import com.sun.xml.internal.ws.resources.SenderMessages;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import org.codehaus.jettison.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import practica3.GUI.GugelCarView;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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
    private int goalLeft,goalRight,goalTop,goalBottom; //TODO rellenar en la exploración

    // Batería total en el mundo. Se actualiza cada vez que se procesa la percepción de un vehículo.
    private int bateriaTotal;

    // Memoria interna con las direcciones
    //private final ArrayList<String> direcciones;


    /**
     * Constructor para la view
     * @author Diego Iáñez Ávila, Andrés Molina López
     * /
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
     * @author Diego Iáñez Ávila, Andrés Molina López
     */
    public SuperMente(String map, AgentID aid,GugelCarView v) throws Exception {
        super(aid);

        mapa = map;
        controllerID = new AgentID("Girtab");
        view = v;
        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;
        goalLeft = 1000;
        goalRight = 0;
        goalTop = 1000;
        goalBottom = 0;
    }

    /**
     * Método para la inicialización del agente
     *
     * @author Andrés Molina López, Ángel Píñar Rivas
     */
    @Override
    public void init(){
        /*FUTURO INIT*/

        System.out.println("Iniciando...");

        // Inicializar algunas estructuras
        vehiculos = new ArrayList<>();

        for(int i=0; i<4; i++){
            vehiculos.add(new EstadoVehiculo(new AgentID("coche" + i)));
        }

        System.out.println("Iniciado");

        reiniciarSesion();
    }

    /**
     * Método de ejecución de la supermente
     *
     * @author Ángel Píñar Rivas, Jose Luis Martínez Ortiz, David Vargas Carrillo
     */

    @Override
    public void execute(){
        boolean finalizar = false;
        boolean exploracionFinalizada = false;
        boolean tenemos_dron = false;

        while(!finalizar) {
            switch (status) {
                case Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO:
                    comenzarSesion();
                    //finalizarSesion(); // -> Descomentar en caso de que la ejecución se quede colgada y comentar en main el inicio de vehiculos
                    status = Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS;
                    break;
                case Mensajes.SUPERMENTE_STATUS_CONTANDOVEHICULOS:

                    for(EstadoVehiculo vehiculo: vehiculos){
                        registrarVehiculo(vehiculo);

                        if (vehiculo.getTipoVehiculo() == TipoVehiculo.DRON){
                            tenemos_dron = true;

                        }
                    }

                    if(!exploracionFinalizada) {
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
                    exploracionFinalizada = explorarMapa();

                    System.out.println("Fin explorar mapa");

                    //Cuando termino de explorar vuelvo a empezar según la exploración
                    if(exploracionFinalizada){
                        reiniciarSesion();
                    } else{
                        System.out.println("No se ha podido explorar el mapa completo");
                        reiniciarSesion();
                    }
                    //reiniciarSesion();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;

                    break;
                case Mensajes.SUPERMENTE_STATUS_YENDO_OBJ:
                    finalizar = irAlObjetivo();
                    status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;

                    // Si no se consigue llegar al objetivo, se reinicia la sesion
                    if(!finalizar){
                        System.out.println("No se ha podido alcanzar el objetivo");
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
                    switch (vehiculo.getTipoVehiculo()){
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
            vehiculo.setTipoVehiculo(TipoVehiculo.DRON);
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_CAMION)){
            vehiculo.setTipoVehiculo(TipoVehiculo.CAMION);
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_COCHE)){
            vehiculo.setTipoVehiculo(TipoVehiculo.COCHE);
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
     * Una vez localizado el objetivo, envia a los vehiculos hacia el mismo
     *
     * @author David Vargas Carrillo, Ángel Píñar Rivas
     * @return true si todos los vehiculos han alcanzado el objetivo, false en caso contrario
     */
    private boolean irAlObjetivo() {
        // Declarar las 4 rutas
        Stack<String> rutav0, rutav1, rutav2, rutav3;

        // todo Ordenar vehiculos por gasto de bateria, o mejor, dron>coche>camion DEBATIR SOBRE ESTO PLS
        // todo Este es un algoritmo de ordenado de mierda, no me apetece mucho pensar hoy
        ArrayList<EstadoVehiculo> vordenados = new ArrayList<>();
        for (int i=0 ; i < vehiculos.size() ; i++){
            if(vehiculos.get(i).getTipoVehiculo() == TipoVehiculo.DRON){
                vordenados.add(vehiculos.get(i));
            }
        }

        for (int i=0 ; i < vehiculos.size() ; i++){
            if(vehiculos.get(i).getTipoVehiculo() == TipoVehiculo.COCHE){
                vordenados.add(vehiculos.get(i));
            }
        }

        for (int i=0 ; i < vehiculos.size() ; i++){
            if(vehiculos.get(i).getTipoVehiculo() == TipoVehiculo.CAMION){
                vordenados.add(vehiculos.get(i));
            }
        }

        //Escoger los goalX y goalY más cercano a cada vehiculo
        int goalX[] = new int[4];
        int goalY[] = new int[4];

        for(int i=0 ; i<4 ; i++){
            //0,0 arriba a la izquierda, pero bajar es positivo
            if(vehiculos.get(i).coor_x < goalLeft){
                goalX[i] = goalLeft;
            } else if (vehiculos.get(i).coor_x > goalRight){
                goalX[i] = goalRight;
            } else {
                goalX[i] = vehiculos.get(i).coor_x;
            }

            if(vehiculos.get(i).coor_y < goalBottom){
                goalY[i] = goalBottom;
            } else if (vehiculos.get(i).coor_y > goalTop){
                goalY[i] = goalTop;
            } else {
                goalY[i] = vehiculos.get(i).coor_y;
            }
        }

        // En caso de conflicto, el de menor combustible, si es igual, el primero del array ordenado,
        // dará un paso mas para no quedarse en la periferia del objetivo
        //todo He rehecho esto de 4234 formas y tengo la cabeza como un bombo, revisadlo pls
        /** /
         La idea es: revisa los goals de los 4 coches en orden de menos consumo a mas.
         Si encuentra que tienen el mismo goal, el de menos consumo se moverá a una casilla adyacente que sea objetivo
         y que esté libre.
         **/
        for (int i = 0; i < 4; i++) {
            if(comprobarCoincidenciaObjetivo(i, goalX, goalY)){ // Si la casilla objetivo está ocupada por otro vehículo
                // Iteramos sobre las casillas adyacentes
                for(int y=goalY[i]-1 ; y<=goalY[i]+1 ; y++){
                    for(int x=goalX[i]-1 ; x<=goalX[i]+1 ; x++){
                        if(!(y==goalY[i] && x==goalX[i])){ //exceptuando el centro
                            if(esObjetivo(x,y) && !comprobarPosicionOcupada(x,y,goalX,goalY)){ //si es objetivo y no está ocupada
                                //Todo salirse del bucle
                                //Establecemos nuevo goal
                                goalX[i] = x;
                                goalY[i] = y;
                            }
                        }
                    }
                }
            }
        }

        // Ahora que tenemos los objetivos, obtenemos secuencia y ejecutamos
        // así, para que tengan en cuenta la posición de los vehiculos en el objetivo y no se choquen con ellos
        rutav0 = encontrarRuta(goalX[0], goalY[0], vordenados.get(0));
        guiarVehiculo(rutav0, vordenados.get(0));
        //rutav1 = encontrarRuta(goalX[1], goalY[1], vordenados.get(1));
        //guiarVehiculo(rutav1, vordenados.get(1));
        //rutav2 = encontrarRuta(goalX[2], goalY[2], vordenados.get(2));
        //guiarVehiculo(rutav2, vordenados.get(2));
        //rutav3 = encontrarRuta(goalX[3], goalY[3], vordenados.get(3));
        //guiarVehiculo(rutav3, vordenados.get(3));


        // Todo revisar criterio de aceptacion de cantidad de vehiculos que llegan
        return true;
    }


    /**Ejecuta una serie de acciones para un vehículo. Se usa para guiarlo hasta el objetivo
     *
     * @author Ángel Píñar Rivas
     * @param ruta Conjunto de movimientos que debe realizar para ir a un sitio
     * @param vehiculo El vehiculo que deseamos mover
     */
    private void guiarVehiculo(Stack<String> ruta, EstadoVehiculo vehiculo){
        //todo revisar si ruta.size da la cantidad de elementos dentro del stack o el tamaño del stack, no estoy seguro
        String nextMove;
        boolean hayBateriaMundo = bateriaTotal > 0;
        // Va hacia el objetivo recargando cada vez que sea estrictamente necesario.
        while(ruta.size()*vehiculo.consumo < 100 && hayBateriaMundo){
            if(vehiculo.battery <= vehiculo.consumo){
                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);

            } else {
                nextMove = ruta.pop();
                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(nextMove), vehiculo.id);

            }

            /*Si esto devuelve false, es que no ha podido repostar porque no hay bateria
                en el mundo, por lo que aborta
            */
            if(!recogerPercepcion(vehiculo)){
                hayBateriaMundo = false;
            }
        }

        //TODO si cuenta la cercania de los vehiculos al objetivo, revisar esta parte
        if(bateriaTotal+vehiculo.battery >= 100) {
            // Sale del while porque ya le va a costar menos de 100 de batería llegar al objetivo, así que recarga una última vez
            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
            recogerPercepcion(vehiculo);

            // Realiza las acciones que le quedan sin recargar
            while (!ruta.isEmpty()) {
                nextMove = ruta.pop();
                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(nextMove), vehiculo.id);
                recogerPercepcion(vehiculo); //?? TODO revisar si es conveniente o no por lo que sea recoger la percepcion
            }
        }
    }


    /** Comprueba si una posición es parte del objetivo
     *
     * @author Ángel Píñar Rivas
     * @param x Coordenada X a comprobar
     * @param y Coordenada Y a comprobar
     * @return True si (x,y) es un objetivo
     */
    private boolean esObjetivo(int x, int y){
        return mapaMundo[y][x] == 3;
    }

    /** En el contexto de posiciones objetivo de los vehiculos, donde el indice representa el vehiculo, comprueba
     * si un vehiculo de indice index tiene la misma posicion objetivo que otro.
     *
     * @author Ángel Píñar Rivas
     * @param index Indice del vehiculo del que se quiere buscar una coincidencia
     * @param goalX Array de coordenadas objetivo X de todos los vehículos
     * @param goalY Array de coordenadas objetivo Y de todos los vehículos
     * @return True si hay coincidencia, false en caso contrario
     */
    private boolean comprobarCoincidenciaObjetivo(int index, int goalX[], int goalY[]){
        boolean coincide = false;
        for(int i=0 ; i<goalX.length ; i++){
            if(i!=index){
                if(goalX[i] == goalX[index] && goalY[i] == goalY[index]){
                    coincide = true;
                }
            }
        }

        return coincide;
    }

    /** Comprueba si una posicion X, Y se encuentra en los arrays de coordenadas X y coordenadas Y
     *
     * @author Ángel Píñar Rivas
     * @param posX Coordenada de posición X que se quiere comprobar
     * @param posY Coordenada de posición Y que se quiere comprobar
     * @param goalX Array de coordenadas objetivo X de todos los vehículos
     * @param goalY Array de coordenadas objetivo Y de todos los vehículos
     * @return True si hay coincidencia, false en caso contrario
     */
    private boolean comprobarPosicionOcupada(int posX, int posY, int goalX[], int goalY[]){
        boolean ocupada = false;

        for(int i=0 ; i<goalX.length ; i++){
            if(posX == goalX[i] && posY == goalY[i]){
                ocupada=true;
            }
        }

        return ocupada;
    }


    /**
     * @author Ángel Píñar Rivas, Jose Luis Martínez Ortiz
     * @param goalX Coordenada X del objetivo.
     * @param goalY Coordenada y del objetivo.
     * @param vehiculo vehiculo para buscar su ruta al objetivo.
     *
     */
    private Stack<String> encontrarRuta(int goalX, int goalY, EstadoVehiculo vehiculo){
        Stack<String> acciones = new Stack<String>();
        Point2D.Double goal = new Point2D.Double(goalX,goalY);

        LinkedList<Nodo> abiertos = new LinkedList<Nodo>();
        LinkedList<Nodo> cerrados = new LinkedList<Nodo>();

        abiertos.add(new Nodo(new Point2D.Double(vehiculo.coor_x, vehiculo.coor_y),0,goal,"",null));

        Nodo actual;

        while(!abiertos.isEmpty()){
            actual = posCosteMasBajo(abiertos);

            /** /
            System.out.println("Método encontrarRuta: el equals y tal");
            System.out.println("Actual: x->" + actual.punto.x + " y->" + actual.punto.y);
            System.out.println("Goal: x->" + goal.x + " y->" + goal.y);
             /**/
            if(actual.punto.equals(goal)){
                System.out.println("Son iguales");
                //Sacar la lista de acciones
                return reconstruirRuta(actual);
            }

            cerrados.add(actual);
            abiertos.remove(actual);

            // Miramos los posibles movimientos
            ArrayList<Nodo> nodosVecinos = new ArrayList<>();
            //NOROESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()-1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_NW, actual));

            //NORTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x(),actual.y()-1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_N,actual ));

            //NORESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()-1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_NE,actual ));

            //OESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_W,actual ));

            //OESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_E,actual ));

            //SUROESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()+1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_SW,actual ));

            //SUR
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x(),actual.y()+1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_S,actual ));


            //SURESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()+1),actual.g_coste + 1,
                    goal,Mensajes.AGENT_COM_ACCION_MV_NE,actual ));

            //Si el vecino es válido lo añado a abiertos

            System.out.println("Método encontrarRuta: el contains y tal");
            for (Nodo nodoVecino:nodosVecinos) {
                System.out.println("Cerrados: x->" + cerrados.contains(nodoVecino) );
                //TODO a partir de aqui peta (INDICES NEGATIVOS EN nodosVecinos???)
                if(!cerrados.contains(nodoVecino) &&
                        mapaMundo[(int)nodoVecino.y()][(int)nodoVecino.x()] != 2 &&
                        mapaMundo[(int)nodoVecino.y()][(int)nodoVecino.x()] != 4){
                    if(vehiculo.getTipoVehiculo() == TipoVehiculo.DRON ||
                            mapaMundo[(int)nodoVecino.y()][(int)nodoVecino.x()] != 1) {
                        abiertos.add(nodoVecino);
                    }
                }
                //TODO a partir de aqui ?? porque lo de antes peta.

            }
            System.out.println("Método encontrarRuta: Terminado bucle del contains y tal");

        } // Fin while
        System.out.println("Metodo encontrarRuta: No hay solución, algo falla");
        // Si se devuelve este acciones (vacio) es que no hay solucion
        return acciones;
    }


    /** Calcula la posición con menor valor heuristico de una lista
     *
     * @author Ángel Píñar Rivas, José Luis Martínez Ortiz
     *
     * @param listaPos Lista de posiciones sobre las que realizar la comprobación
     * @return La posición con menor valor heuristico
     */
    private Nodo posCosteMasBajo(LinkedList<Nodo> listaPos){
        double costeMinimo = Double.MAX_VALUE;
        Nodo nodo_resultado = new Nodo();
        for(Nodo nodo: listaPos){
            if(nodo.f_coste < costeMinimo){
                costeMinimo = nodo.f_coste;
                nodo_resultado = nodo;
            }
        }
        return nodo_resultado;
    }


    /**
     * Reconstruye la ruta hasta el origen.
     * @author Jose Luis Martínez Ortiz
     * @param nodo nodo final de una ruta.
     * @return una secuencia de acciones para llegar al nodo.
     */
    private Stack<String> reconstruirRuta(Nodo nodo){
        Stack<String> acciones = new Stack<String>();

        while (nodo.anterior != null){
            acciones.push(nodo.accion);
            nodo = nodo.anterior;
        }

        return acciones;
    }

    /**
     * Explora el mapa completamente utilizando el vehículo de tipo DRON
     *
     * @author David Vargas Carrillo, Andrés Molina López
     * @return estado final de la exploracion (true si se ha explorado el mapa completo)
     */
    private boolean explorarMapa(){
        boolean arriba = false;                             // Movimiento hacia arriba o hacia abajo
        int esquinasExploradas = 0;                         // Numero de esquinas exploradas (cuando sea 4, terminar)

        // Propiedades del dron
        EstadoVehiculo dron = null;

        // Obtencion del estado del dron
        for (EstadoVehiculo vehiculo : vehiculos) {
            if (vehiculo.getTipoVehiculo() == TipoVehiculo.DRON)
                dron = vehiculo;

            // Obtenemos la percepción de todos los vehiculos
            sendMessageVehiculo(ACLMessage.QUERY_REF, jsonComando("percepcion"), vehiculo.id);
            recogerPercepcion(vehiculo);
        }

        // Determinación de la posicion inicial en el mapa
        if (dron.coor_y == 0)
            arriba = true;        // Empezamos en la parte superior, de otro modo, en la inferior

        // Movimiento a la izquierda, hasta la columna 0
        while(dron.coor_x > 0) {
            if (dron.battery > dron.consumo) {
                if(mapaMundo[dron.coor_y][dron.coor_x - 1] != 4) { // Se comprueba que la casilla a la que nos vamos a mover no tenga un vehiculo
                    sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), dron.id);
                    recogerPercepcion(dron);
                }
                else{   // En caso de que haya un vehiculo en la casilla lo que hacemos es esquivarlo en zigzag
                    if (arriba) {  // Comprobamos si empieza arriba del mapa
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SW), dron.id);
                        recogerPercepcion(dron);
                        if(!evitarVehiculo(dron, arriba, true)) return false;
                    }
                    else{
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NW), dron.id);
                        recogerPercepcion(dron);
                        if(!evitarVehiculo(dron, arriba, true)) return false;
                    }
                }
                //if (!recogerPercepcion(dron)) return false;
            } else {
                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                if (!recogerPercepcion(dron)) return false;
            }
        }
        esquinasExploradas++;     // Se ha llegado a la esquina izquierda inferior o superior
        System.out.println("Conseguido llegar a la primera esquina");

        boolean mov_derecha = true;
        // Bucle de exploracion
        while (esquinasExploradas < 4) {
            if (mov_derecha) {
                while(mapaMundo[dron.coor_y][dron.coor_x + 1] != 2) {
                    if (dron.battery > dron.consumo) {
                        if(mapaMundo[dron.coor_y][dron.coor_x+1] != 4) {    // Se comprueba que la casilla a la que nos vamos a mover no tenga un vehiculo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_E), dron.id);
                            recogerPercepcion(dron);
                        }
                        else{   // En caso de que haya un vehiculo en la casilla lo que hacemos es esquivarlo en zigzag
                            if (arriba) {  // Comprobamos si empieza arriba del mapa
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SE), dron.id);
                                recogerPercepcion(dron);
                                if(!evitarVehiculo(dron, arriba, false)) return false; // Se manda el arriba al reves porque estamos en la fila opuesta a la que empezamos
                            }
                            else{
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NE), dron.id);
                                recogerPercepcion(dron);
                                if(!evitarVehiculo(dron, arriba, false)) return false; // Se manda el arriba al reves porque estamos en la fila opuesta a la que empezamos
                            }
                        }
                    } else {
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                        if (!recogerPercepcion(dron)) return false;
                    }
                }
                // Se comprueba si estamos en una esquina
                if ((dron.coor_y == 0 || mapaMundo[dron.coor_y - 1][dron.coor_x] == 2) && (mapaMundo[dron.coor_y][dron.coor_x + 1] == 2)) {
                    esquinasExploradas++;
                }
                mov_derecha = false;
            }
            else {
                while(dron.coor_x != 0 && mapaMundo[dron.coor_y][dron.coor_x - 1] != 2) {
                    if (dron.battery > dron.consumo) {
                        if (mapaMundo[dron.coor_y][dron.coor_x-1] != 4) {   // Se comprueba que la casilla a la que nos vamos a mover no tenga un vehiculo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), dron.id);
                            recogerPercepcion(dron);
                        }
                        else{   // En caso de que haya un vehiculo en la casilla lo que hacemos es esquivarlo en zigzag
                            if (arriba) {  // Comprobamos si empieza arriba del mapa
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NW), dron.id);
                                recogerPercepcion(dron);
                                if(!evitarVehiculo(dron, !arriba, true)) return false; // Se manda el arriba al reves porque estamos en la fila opuesta a la que empezamos
                            }
                            else{
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SW), dron.id);
                                recogerPercepcion(dron);
                                if(!evitarVehiculo(dron, !arriba, true)) return false; // Se manda el arriba al reves porque estamos en la fila opuesta a la que empezamos
                            }
                        }
                    } else {
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                        if (!recogerPercepcion(dron)) return false;
                    }
                }
                // Se comprueba si estamos en una esquina
                if ((dron.coor_y == 0 || mapaMundo[dron.coor_y - 1][dron.coor_x] == 2) && (dron.coor_x == 0 || mapaMundo[dron.coor_y][dron.coor_x - 1] == 2)) {
                    esquinasExploradas++;
                }
                mov_derecha = true;
            }

            if (esquinasExploradas < 4) {
                boolean seguir_mov = true;
                boolean esquivado_vehiculo = false;
                int movs = 0;
                if (arriba) {
                    // Moverse hacia abajo
                    do {
                        if (mapaMundo[dron.coor_y + 1][dron.coor_x] != 2 && mapaMundo[dron.coor_y + 1][dron.coor_x] != 4) {
                            if (dron.battery > dron.consumo) {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_S), dron.id);
                                recogerPercepcion(dron);
                                movs++;
                            } else {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                                if (!recogerPercepcion(dron)) return false;
                            }
                        }
                        else if (mapaMundo[dron.coor_y + 1][dron.coor_x] == 4){
                            if (dron.battery > dron.consumo) {
                                if (mov_derecha)
                                    sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SW), dron.id);
                                else
                                    sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SE), dron.id);

                                recogerPercepcion(dron);
                                movs++;
                                seguir_mov = false;
                                esquivado_vehiculo = true;
                            } else {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                                if (!recogerPercepcion(dron)) return false;
                            }
                        }
                        else if(mapaMundo[dron.coor_y + 1][dron.coor_x] == 2)
                            seguir_mov = false;
                    } while (seguir_mov && movs < 3);

                    // Se comprueba si estamos en una esquina
                    if (mapaMundo[dron.coor_y + 1][dron.coor_x] == 2 || esquivado_vehiculo) { // Si hemos esquivado un vehiculo mientras bajabamos es que estaba situado en una extremo
                        esquinasExploradas++;
                    }
                } else {
                    // Moverse hacia arriba
                    do {
                        if (mapaMundo[dron.coor_y - 1][dron.coor_x] != 2 && mapaMundo[dron.coor_y - 1][dron.coor_x] != 4) {
                            if (dron.battery > dron.consumo) {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_N), dron.id);
                                recogerPercepcion(dron);
                                movs++;
                            } else {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                                if (!recogerPercepcion(dron)) return false;
                            }
                        }
                        else if (mapaMundo[dron.coor_y - 1][dron.coor_x] == 4){
                            if (dron.battery > dron.consumo) {
                                if (mov_derecha)
                                    sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NW), dron.id);
                                else
                                    sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NE), dron.id);

                                recogerPercepcion(dron);
                                movs++;
                                seguir_mov = false;
                                esquivado_vehiculo = true;
                            } else {
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), dron.id);
                                if (!recogerPercepcion(dron)) return false;
                            }
                        }
                        else if(mapaMundo[dron.coor_y - 1][dron.coor_x] == 2)
                            seguir_mov = false;
                    } while (seguir_mov && movs < 3);

                    // Se comprueba si estamos en una esquina
                    if (dron.coor_y == 0 || mapaMundo[dron.coor_y - 1][dron.coor_x] == 2 || esquivado_vehiculo) { // Si hemos esquivado un vehiculo mientras subiamos es que estaba situado en una extremo
                        esquinasExploradas++;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Metodo para bordear a los vehiculos haciendo zigzag
     *
     * @author Andrés Molina López
     * @param vehiculo datos del vehículo que se está moviendo
     * @param posicion_inicial si el vehículo ha aparecido en la fila de arriba o de abajo del mapa
     * @param mov_izquierda si el vehiculo se está moviendo a la izquierda o a la derecha
     * @return si se ha conseguido bordear con exito al otro vehiculo o no
     */
    private boolean evitarVehiculo(EstadoVehiculo vehiculo, boolean posicion_inicial, boolean mov_izquierda){
        System.out.println("Intentando esquivar vehículo");

        boolean exito = false;
        if (posicion_inicial && mov_izquierda){
            switch (mapaMundo[vehiculo.coor_y-1][vehiculo.coor_x-1]){
                case 2:
                    exito = true; // El coche a bordear estaba en la posicion limite del mapa
                    break;
                case 4:
                    if (mapaMundo[vehiculo.coor_y][vehiculo.coor_x-1] == 2){
                        exito = true; // habia dos coches consecutivos puestos al lado del borde
                    }
                    else{
                        if (vehiculo.battery > vehiculo.consumo){
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            if(evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                exito = true;
                        }
                        else{
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                            if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), vehiculo.id);
                                recogerPercepcion(vehiculo);
                                if (evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                    exito = true;
                            }
                        }
                    }
                    break;
                default:
                    if (vehiculo.battery > vehiculo.consumo){
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NW), vehiculo.id);
                        recogerPercepcion(vehiculo);
                        exito = true;
                    }
                    else{
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                        if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NW), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            exito = true;
                        }
                    }
                    break;
            }
        }
        else if(posicion_inicial && !mov_izquierda){
            switch (mapaMundo[vehiculo.coor_y-1][vehiculo.coor_x+1]){
                case 2:
                    exito = true; // El coche a bordear estaba en la posicion limite del mapa
                    break;
                case 4:
                    if (mapaMundo[vehiculo.coor_y][vehiculo.coor_x+1] == 2){
                        exito = true; // habia dos coches consecutivos puestos al lado del borde
                    }
                    else{
                        if (vehiculo.battery > vehiculo.consumo){
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_E), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            if(evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                exito = true;
                        }
                        else{
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                            if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_E), vehiculo.id);
                                recogerPercepcion(vehiculo);
                                if (evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                    exito = true;
                            }
                        }
                    }
                    break;
                default:
                    if (vehiculo.battery > vehiculo.consumo){
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NE), vehiculo.id);
                        recogerPercepcion(vehiculo);
                        exito = true;
                    }
                    else{
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                        if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_NE), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            exito = true;
                        }
                    }
                    break;
            }
        }
        else if(!posicion_inicial && mov_izquierda){
            switch (mapaMundo[vehiculo.coor_y+1][vehiculo.coor_x-1]){
                case 2:
                    exito = true; // El coche a bordear estaba en la posicion limite del mapa
                    break;
                case 4:
                    if (mapaMundo[vehiculo.coor_y][vehiculo.coor_x-1] == 2){
                        exito = true; // habia dos coches consecutivos puestos al lado del borde
                    }
                    else{
                        if (vehiculo.battery > vehiculo.consumo){
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            if(evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                exito = true;
                        }
                        else{
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                            if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_W), vehiculo.id);
                                recogerPercepcion(vehiculo);
                                if (evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                    exito = true;
                            }
                        }
                    }
                    break;
                default:
                    if (vehiculo.battery > vehiculo.consumo){
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SW), vehiculo.id);
                        recogerPercepcion(vehiculo);
                        exito = true;
                    }
                    else{
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                        if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SW), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            exito = true;
                        }
                    }
                    break;
            }
        }
        else if(!posicion_inicial && !mov_izquierda){
            switch (mapaMundo[vehiculo.coor_y+1][vehiculo.coor_x+1]){
                case 2:
                    exito = true; // El coche a bordear estaba en la posicion limite del mapa
                    break;
                case 4:
                    if (mapaMundo[vehiculo.coor_y][vehiculo.coor_x+1] == 2){
                        exito = true; // habia dos coches consecutivos puestos al lado del borde
                    }
                    else{
                        if (vehiculo.battery > vehiculo.consumo){
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_E), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            if(evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                exito = true;
                        }
                        else{
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                            if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_E), vehiculo.id);
                                recogerPercepcion(vehiculo);
                                if (evitarVehiculo(vehiculo, posicion_inicial, mov_izquierda))
                                    exito = true;
                            }
                        }
                    }
                    break;
                default:
                    if (vehiculo.battery > vehiculo.consumo){
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SE), vehiculo.id);
                        recogerPercepcion(vehiculo);
                        exito = true;
                    }
                    else{
                        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
                        if(recogerPercepcion(vehiculo)){ // si tiene gasolina completa el bordeo
                            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_MV_SE), vehiculo.id);
                            recogerPercepcion(vehiculo);
                            exito = true;
                        }
                    }
                    break;
            }
        }

        return exito;
    }

    /**
     * Recibe y procesa la percepcion del vehiculo indicado
     * @author David Vargas Carrillo, Andrés Molina López
     *
     * @param vehiculo objeto de EstadoVehiculo
     * @return si el estado del vehiculo ha sido actualizado
     */
    private boolean recogerPercepcion(EstadoVehiculo vehiculo) {
        System.out.println("Recogiendo percepción");
        boolean actualizado = true;

        // Recibir percepción
        ACLMessage respuesta = receiveMessage();

        if (respuesta.getPerformativeInt() == ACLMessage.INFORM) {
            // Actualizar percepción
            procesarPercepcion(vehiculo, respuesta.getContent());
        } else if (respuesta.getPerformativeInt() == ACLMessage.REFUSE) {
            actualizado = false;
        }

        System.out.println("Percepción recogida y procesada");

        return actualizado;
    }

    /**
     * Procesar la percepción recibida de un vehículo
     * @author Diego Iáñez Ávila, Ángel Píñar Rivas, Jose Luis Martínez Ortiz
     */
    private void procesarPercepcion(EstadoVehiculo vehiculo, String jsonPercepcion){
        System.out.println("Procesando percepción");
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

        switch (vehiculo.getTipoVehiculo()){
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

        ArrayList<Integer> radar = new ArrayList<>();
        int valorSensor = -1;
        for (int fila = 0; fila < ancho; ++fila){
            for (int columna = 0; columna < ancho; ++columna){
                xmapa = x + columna;
                ymapa = y + fila;

                valorSensor = sensor.get(fila*ancho + columna).asInt();

                radar.add(valorSensor);

                if (xmapa >= 0 && ymapa >= 0){
                    mapaMundo[ymapa][xmapa] = valorSensor;

                    if(valorSensor == 3){
                        if(ymapa < goalTop){
                            goalTop = ymapa;
                        }
                        if(ymapa > goalBottom){
                            goalBottom = ymapa;
                        }
                        if(xmapa < goalLeft){
                            goalLeft = xmapa;
                        }
                        if(xmapa > goalRight){
                            goalRight = xmapa;
                        }
                    }
                }
            }
        }

        view.updateMap(vehiculo.coor_x, vehiculo.coor_y, radar, ancho, inicio);
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
     * Enviar un mensaje a Vehiculo
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
