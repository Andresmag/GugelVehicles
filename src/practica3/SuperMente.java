package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import practica3.GUI.GugelCarView;

import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Supermente, clase controladora de los vehículos zombies.
 */

public class SuperMente extends SingleAgent {

    // DATOS MIEMBROS
    private AgentID controllerID;
    private String mapa;
    private String conversationID;
    private String replyWith;
    private GugelCarView view;
    private int status;

    // Datos que nos indican los estados de los vehiculos (posicion, bateria, tipo, ...)
    private ArrayList<EstadoVehiculo> vehiculos;

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    final private int DIMENSIONES = 1000;
    private int [][] mapaMundo = new int[DIMENSIONES][DIMENSIONES];
    private int goalLeft,goalRight,goalTop,goalBottom;

    // Batería total en el mundo. Se actualiza cada vez que se procesa la percepción de un vehículo.
    private int bateriaTotal;
    private int contador = 0; //TODO Revisión si ruta.size esta bien

    // Memoria interna con las direcciones
    //private final ArrayList<String> direcciones;

    // Datos empleados para exploración del mapa
    private boolean exploracionIniciada;
    private boolean comienzaArriba;
    private int explorandoX;
    private int explorandoY;
    private boolean explorandoIzquierda;
    private boolean explorandoUltimaFila;

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila, Andrés Molina López
     */
    public SuperMente(String map, AgentID aid,GugelCarView v) throws Exception {
        super(aid);

        mapa = map;
        controllerID = new AgentID("Girtab");
        view = v;
        vehiculos = new ArrayList<>();
        goalLeft = 1000;
        goalRight = 0;
        goalTop = 1000;
        goalBottom = 0;
        exploracionIniciada = false;
        explorandoIzquierda = true;
        explorandoUltimaFila = false;
    }

    /**
     * Método para la inicialización del agente
     *
     * @author Andrés Molina López, Ángel Píñar Rivas
     */
    @Override
    public void init(){

        System.out.println("Iniciando...");

        // Inicializar vector con los estados de los vehiculos
        for(int i=0; i<4; i++){
            vehiculos.add(new EstadoVehiculo(new AgentID("coche" + i)));
        }

        // Pasando al primer estado
        status = Mensajes.SUPERMENTE_STATUS_SUSCRIBIENDO;

        System.out.println("Iniciado");

       // reiniciarSesion();
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
                    /**
                    // Engañar para pruebas
                    exploracionFinalizada = true;

                    //Puestas a ojo para el mapa 3
                    goalLeft = 64;
                    goalRight = 67;
                    goalTop = 35;
                    goalBottom = 37;

                    for (int x = goalLeft; x <= goalRight; ++x){
                        for (int y = goalTop; y <= goalBottom; ++y){
                            mapaMundo[y][x] = 3;
                        }
                    }
                    rellenarFilaDePared(100);
                    for (int i = 0; i < DIMENSIONES; ++i){
                        mapaMundo[i][100] = 2;
                    }
                    **/

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


    /**
     * Manda un cancel al controlador para así poder iniciar una nueva sesión posteriormente
     *
     * @author Ángel Píñar Rivas, Diego Iáñez Ávila
     * @return El mensaje con la traza
     */
    public ACLMessage reiniciarSesion(){
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

                for (int i = 0; i < vehiculos.size(); ++i){
                    switch (vehiculos.get(i).getTipoVehiculo()){
                        case DRON:
                            nombre += "dron" + i + "_";
                            break;
                        case COCHE:
                            nombre += "coche" + i + "_";
                            break;
                        case CAMION:
                            nombre += "camion" + i + "_";
                            break;
                    }
                }

                nombre += "llegaron_";

                for (int i = 0; i < vehiculos.size(); ++i){
                    if (vehiculos.get(i).objetivoAlcanzado)
                        nombre += i + "_";
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
                System.out.println(answer.getContent());
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

        switch (tipoVehiculo){
            case Mensajes.VEHICLE_TYPE_HELICOPTERO:
                vehiculo.setTipoVehiculo(TipoVehiculo.DRON);
                break;
            case Mensajes.VEHICLE_TYPE_CAMION:
                vehiculo.setTipoVehiculo(TipoVehiculo.CAMION);
                break;
            case Mensajes.VEHICLE_TYPE_COCHE:
                vehiculo.setTipoVehiculo(TipoVehiculo.COCHE);
                break;
        }

        /*
        if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_HELICOPTERO)){
            vehiculo.setTipoVehiculo(TipoVehiculo.DRON);
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_CAMION)){
            vehiculo.setTipoVehiculo(TipoVehiculo.CAMION);
        }
        else if (tipoVehiculo.equals(Mensajes.VEHICLE_TYPE_COCHE)){
            vehiculo.setTipoVehiculo(TipoVehiculo.COCHE);
        }
        */

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
     * @author David Vargas Carrillo, Ángel Píñar Rivas, Diego Iáñez Ávila, Jose Luis Martínez Ortiz
     * @return true si todos los vehiculos han alcanzado el objetivo, false en caso contrario
     */
    private boolean irAlObjetivo() {
        for (EstadoVehiculo vehiculo : vehiculos){
            sendMessageVehiculo(ACLMessage.QUERY_REF, "", vehiculo.id);
            recogerPercepcion(vehiculo);
        }

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
            if(vordenados.get(i).coor_x < goalLeft){
                goalX[i] = goalLeft;
            } else if (vordenados.get(i).coor_x > goalRight){
                goalX[i] = goalRight;
            } else {
                goalX[i] = vordenados.get(i).coor_x;
            }

            if(vordenados.get(i).coor_y > goalBottom){
                goalY[i] = goalBottom;
            } else if (vordenados.get(i).coor_y < goalTop){
                goalY[i] = goalTop;
            } else {
                goalY[i] = vordenados.get(i).coor_y;
            }
        }

        int objInicialX[] = goalX.clone();
        int objInicialY[] = goalY.clone();


        // En caso de conflicto, el de menor combustible, si es igual, el primero del array ordenado,
        // dará un paso mas para no quedarse en la periferia del objetivo
        //todo He rehecho esto de 4234 formas y tengo la cabeza como un bombo, revisadlo pls
        /** /
         La idea es: revisa los goals de los 4 coches en orden de menos consumo a mas.
         Si encuentra que tienen el mismo goal, el de menos consumo se moverá a una casilla adyacente que sea objetivo
         y que esté libre.
         **/
        boolean salir = false;
        for (int i = 0; i < 4; i++) {
            if(comprobarCoincidenciaObjetivo(i, goalX, goalY)){ // Si la casilla objetivo está ocupada por otro vehículo
                // Iteramos sobre las casillas adyacentes
                for(int y=goalY[i]-1 ; y<=goalY[i]+1 && !salir; y++){
                    for(int x=goalX[i]-1 ; x<=goalX[i]+1 && !salir; x++){
                        if(!(y==goalY[i] && x==goalX[i])){ //exceptuando el centro
                            if(esObjetivo(x,y) && !comprobarPosicionOcupada(x,y,goalX,goalY)){ //si es objetivo y no está ocupada
                                salir = true;
                                //Establecemos nuevo goal
                                goalX[i] = x;
                                goalY[i] = y;
                            }
                        }
                    }
                }
            }
            salir = false;
        }

        boolean terminar;
        for(int i=0 ; i<4 ;i++){
            if(comprobarCoincidenciaObjetivo(i, goalX, goalY)){
                terminar = false;

                for(int y=goalTop; y<= goalBottom && !terminar ; y++) {
                    for (int x = goalLeft; x <= goalRight && !terminar ; x++) {
                        if(esObjetivo(x,y) && !comprobarPosicionOcupada(x,y,goalX,goalY)){
                            terminar = true;
                            goalX[i] = x;
                            goalY[i] = y;
                        }
                    }
                }


                System.out.println("ERROR LOCALIZADO (irAlObjetivo) Hay dos vehiculos con el mismo objetivo");
                try {
                    Thread.sleep(10000);
                } catch(Exception e){
                    System.out.println("EXCEPCION DE ERROR LOCALIZADO");
                }

            }


        }


        for (int i=0 ; i<4 ; ++i) {
            System.out.println("Objetivos iniciales vehiculo ordenado "+i+": ");
            System.out.println("x:" + objInicialX[i] + " y: " + objInicialY[i]);
            System.out.println("Objetivos alterados vehiculo ordenado "+i+": ");
            System.out.println("x:" + goalX[i] + " y: " + goalY[i]);
        }

        // Ahora que tenemos los objetivos, obtenemos secuencia y ejecutamos
        // así, para que tengan en cuenta la posición de los vehiculos en el objetivo y no se choquen con ellos
        for (int i=0 ; i<4 ; ++i) {
            rutav0 = encontrarRuta(goalX[i], goalY[i], vordenados.get(i));
            guiarVehiculo(rutav0, vordenados.get(i));
            mapaMundo[goalY[i]][goalX[i]] = 4;

        }



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
        System.out.println("Debug guiarVehiculo: "+ ruta.size() +"="+contador);
        /**/
        Stack<String> copia = (Stack<String>) ruta.clone();
        System.out.println("Vehiculo x:" + vehiculo.coor_x + " y:" + vehiculo.coor_y);
        while(!copia.isEmpty()){
            System.out.println(copia.pop());
        }
        /**/

        String nextMove;
        boolean hayBateriaMundo = bateriaTotal > 0;
        // Va hacia el objetivo recargando cada vez que sea estrictamente necesario.
        System.out.println("Inicio While "+ vehiculo.battery+ " - condicion="+ruta.size()*vehiculo.consumo+ " Bateria mundo="+bateriaTotal);

        while(ruta.size()*vehiculo.consumo >= 100 && hayBateriaMundo){
            nextMove = ruta.pop();
            System.out.println("Cojo movimiento :"+nextMove);
            moverseConBateria(vehiculo, nextMove);
            System.out.println("GV --- Enviado");

            /*Si esto devuelve false, es que no ha podido repostar porque no hay bateria
                en el mundo, por lo que aborta
            */
            if(bateriaTotal <= vehiculo.consumo){//TODO puede pasarle lo mismo que abajo, todavia no me ha salido el camion
                System.out.println("Sin bateria- total:"+bateriaTotal+", battery:"+vehiculo.battery);
                hayBateriaMundo = false;
            }
        }

        //TODO si cuenta la cercania de los vehiculos al objetivo, revisar esta parte
        if(bateriaTotal+vehiculo.battery >= 100) {
            System.out.println("VG - if ultimo repostage ="+bateriaTotal+" - "+vehiculo.battery);
            // Sale del while porque ya le va a costar menos de 100 de batería llegar al objetivo, así que recarga una última vez
            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
            System.out.println("VG - enviado ultimo repostage");

            recogerPercepcion(vehiculo);
            System.out.println("VG - recibido ultimo repostage");

            // Realiza las acciones que le quedan sin recargar
            while (!ruta.isEmpty() && vehiculo.battery > vehiculo.consumo) {
                System.out.println("VG - Mov restantes "+ruta.size()+", bateria restante:"+vehiculo.battery);
                nextMove = ruta.pop();
                System.out.println("VG - next move "+nextMove);
                sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(nextMove), vehiculo.id);
                System.out.println("VG - enviado movimiento ");
                //Parche para arreglar el error de percepción
                vehiculo.battery -= vehiculo.consumo;


                //TODO lo mismo que el de arriba (linea 460)
                recogerPercepcion(vehiculo); //?? TODO revisar si es conveniente o no por lo que sea recoger la percepcion
                System.out.println("VG - actualizo percepcioon ");
            }
        }

        //Esto solo sirve las coordenada x e y si se espera a la percepción del vehículo, si no dice su posición original
        //TODO arreglar, si termina el método antes de que el vehiculo ejecute todos los movimientos, se realiza el cancel del
        // reinicio de que hemos terminado y vehiculo todavía le quedan por enviar movimientos y se vuevle loco el controlador.
        System.out.println(vehiculo.id.name+ " ha llegado a su destino ("+vehiculo.coor_x+","+vehiculo.coor_y+ ") -> es goal= "+esObjetivo(vehiculo.coor_x,vehiculo.coor_y));
    }


    /** Comprueba si una posición es parte del objetivo
     *
     * @author Ángel Píñar Rivas
     * @param x Coordenada X a comprobar
     * @param y Coordenada Y a comprobar
     * @return True si (x,y) es un objetivo
     */
    private boolean esObjetivo(int x, int y){
        boolean esObjetivo = false;
        if(x>0 && y>0){
            esObjetivo = mapaMundo[y][x] == 3;
        }
        return esObjetivo;
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

        System.out.print("Método encontrarRuta: buscando");


        float costemovimiento = 1;

        while(!abiertos.isEmpty()){
            //System.out.print(". ");
            actual = posCosteMasBajo(abiertos);
            cerrados.add(actual);
            //Borra todas las ocurrencias de abiertos
            while(abiertos.remove(actual)){
                System.out.println("borro");

            };


            System.out.println("Método encontrarRuta: el equals y tal: "+abiertos.contains(actual));
            System.out.println("Actual: x->" + actual.punto.x + " y->" + actual.punto.y);
            System.out.println("Goal: x->" + goal.x + " y->" + goal.y);

            if(actual.punto.equals(goal)){
                System.out.println("Ruta encontrada para el vehículo "+vehiculo.id.name);
                //Sacar la lista de acciones
                return reconstruirRuta(actual);
            }


            // Miramos los posibles movimientos
            ArrayList<Nodo> nodosVecinos = new ArrayList<>();
            //NOROESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()-1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_NW, actual));

            //NORTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x(),actual.y()-1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_N,actual ));

            //NORESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()-1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_NE,actual ));

            //OESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_W,actual ));

            //OESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_E,actual ));

            //SUROESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()-1,actual.y()+1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_SW,actual ));

            //SUR
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x(),actual.y()+1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_S,actual ));


            //SURESTE
            nodosVecinos.add(new Nodo(new Point2D.Double(actual.x()+1,actual.y()+1),actual.g_coste + costemovimiento,
                    goal,Mensajes.AGENT_COM_ACCION_MV_SE,actual ));

            //Si el vecino es válido lo añado a abiertos

            //System.out.println("Método encontrarRuta: el contains y tal");
            for (Nodo nodoVecino:nodosVecinos) {
                System.out.println("Cerrados: x->" + cerrados.contains(nodoVecino) );
                System.out.print("NodoVecino: " + nodoVecino.accion + " - X="+(int)nodoVecino.x()+", Y="+(int)nodoVecino.y());
                if((int)nodoVecino.y() >= 0 && (int)nodoVecino.y() < 1000 &&
                   (int)nodoVecino.x() >= 0 && (int)nodoVecino.x() < 1000)
                    if(!cerrados.contains(nodoVecino)) {
                        if (mapaMundo[(int) nodoVecino.y()][(int) nodoVecino.x()] != 2 &&
                            mapaMundo[(int) nodoVecino.y()][(int) nodoVecino.x()] != 4) {
                            if (vehiculo.getTipoVehiculo() == TipoVehiculo.DRON ||
                                    mapaMundo[(int) nodoVecino.y()][(int) nodoVecino.x()] != 1) {
                                abiertos.add(nodoVecino);
                                System.out.println("");
                            } else {
                                System.out.println(" XX obstáculo");
                            }
                        } else {
                            System.out.println(" XX coche/bordemundo");
                        }
                    }else{
                        System.out.println(" XX en cerrados");
                    }

                //Revisado hasta aquí, funciona y encuentra el objetivo con el retuns adecuado.

            }
            //System.out.println("Método encontrarRuta: Terminado bucle del contains y tal");

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
            contador++;
        }

        return acciones;
    }

    /**
     * Explora el mapa completamente utilizando el vehículo de tipo DRON
     * o hasta que se acabe la batería. Si se llama por segunda vez, retoma la exploración
     * anterior por donde se hubiera quedado.
     *
     * @author Diego Iáñez Ávila, David Vargas Carrillo, Andrés Molina López
     * @return estado final de la exploracion (true si se ha explorado el mapa completo)
     */
    private boolean explorarMapa(){
        boolean terminado = false;

        EstadoVehiculo dron = null;

        for (EstadoVehiculo vehiculo : vehiculos){
            if (vehiculo.getTipoVehiculo() == TipoVehiculo.DRON)
                dron = vehiculo;
        }

        // Obtener la percepción del dron
        sendMessageVehiculo(ACLMessage.QUERY_REF, jsonComando("percepcion"), dron.id);
        recogerPercepcion(dron);

        if (!exploracionIniciada){
            // Comenzar la exploración
            comienzaArriba = dron.coor_y == 0;

            if (!comienzaArriba){
                // Sabemos que la fila de abajo es pared
                rellenarFilaDePared(dron.coor_y + 1);

                // Avanzamos una casilla hacia arriba para no chocarnos con los demás vehículos
                moverseConBateria(dron, Mensajes.AGENT_COM_ACCION_MV_N);
                guardarPosicionExploracion(dron);
            }
            else {
                // Avanzamos una casilla hacia abajo para no chocarnos con los demás vehículos
                moverseConBateria(dron, Mensajes.AGENT_COM_ACCION_MV_S);
                guardarPosicionExploracion(dron);
            }
        }
        else{
            // Ir hacia donde estábamos antes
            // todo Suponemos que hay energía en el mapa suficiente al menos para esto

            String direccion;

            // Ir a la coordenada Y
            if (dron.coor_y < explorandoY)
                direccion = Mensajes.AGENT_COM_ACCION_MV_S;
            else
                direccion = Mensajes.AGENT_COM_ACCION_MV_N;

            while (dron.coor_y != explorandoY)
                moverseConBateria(dron, direccion);

            // Ir a la coordenada X
            if (dron.coor_x < explorandoX)
                direccion = Mensajes.AGENT_COM_ACCION_MV_E;
            else
                direccion = Mensajes.AGENT_COM_ACCION_MV_W;

            while (dron.coor_x != explorandoX)
                moverseConBateria(dron, direccion);
        }

        // Mientras no hayamos terminado y podamos recargar con cierto margen
        while (!terminado && bateriaTotal > 20){
            if (explorandoIzquierda){
                if (dron.coor_x == 1){
                    // Chocando con borde izquierdo

                    if (explorandoUltimaFila) {
                        terminado = true;
                    }
                    else {
                        // No se puede avanzar a la izquierda
                        explorandoIzquierda = false;

                        if (!exploracionIniciada) {
                            // Si acabamos de empezar, simplemente nos vamos para la derecha
                            exploracionIniciada = true;
                        } else {
                            // Si ya habíamos empezado, nos vamos a la siguiente fila
                            explorarSiguienteFila(dron);
                        }
                    }
                }
                else {
                    moverseConBateria(dron, Mensajes.AGENT_COM_ACCION_MV_W);
                    guardarPosicionExploracion(dron);
                }
            }
            else{
                if (mapaMundo[dron.coor_y][dron.coor_x + 1] == 2){
                    // Chocando con borde derecho

                    if (explorandoUltimaFila){
                        terminado = true;
                    }
                    else {
                        // No se puede avanzar a la derecha
                        explorandoIzquierda = true;
                        explorarSiguienteFila(dron);
                    }
                }
                else {
                    moverseConBateria(dron, Mensajes.AGENT_COM_ACCION_MV_E);
                    guardarPosicionExploracion(dron);
                }
            }
        }

        return terminado;
    }

    /**
     * Pasa a la siguiente fila para explorar
     *
     * @author Diego Iáñez Ávila, David Vargas Castillo, Andrés Molina López
     * @return True si es la última fila a explorar
     */
    private void explorarSiguienteFila(EstadoVehiculo dron){
        System.out.println("Explorando siguiente fila");

        String direccion;

        if (comienzaArriba)
            direccion = Mensajes.AGENT_COM_ACCION_MV_S;
        else
            direccion = Mensajes.AGENT_COM_ACCION_MV_N;

        for (int i = 0; i < 3 && ((comienzaArriba && mapaMundo[dron.coor_y + 1][dron.coor_x] != 2 && mapaMundo[dron.coor_y + 1][dron.coor_x] != 4) || (!comienzaArriba && dron.coor_y > 1)); ++i){
            // Nos tenemos que mover arriba o abajo y el hueco está libre
            moverseConBateria(dron, direccion);
            guardarPosicionExploracion(dron);
        }

        if (!comienzaArriba && dron.coor_y == 1){
            // Empezamos abajo y hemos llegado arriba, esta es la última fila a explorar
            explorandoUltimaFila = true;
        }

        if (comienzaArriba && mapaMundo[dron.coor_y + 1][dron.coor_x] == 2){
            // Empezamos arriba y hemos llegado abajo, esta es la última fila a explorar
            explorandoUltimaFila = true;

            // Sabemos que debajo hay pared
            rellenarFilaDePared(dron.coor_y + 1);

            // Nos movemos hacia arriba para no chocar con otros vehículos
            moverseConBateria(dron, Mensajes.AGENT_COM_ACCION_MV_N);
            guardarPosicionExploracion(dron);
        }
        else if (comienzaArriba && mapaMundo[dron.coor_y + 1][dron.coor_x] == 4){
            // Empezamos arriba y hemos llegado abajo pero hay un coche, esta es la última fila a explorar
            explorandoUltimaFila = true;

            // Sabemos que dos filas abajo hay pared
            rellenarFilaDePared(dron.coor_y + 2);
        }
    }

    /**
     * Moverse en una dirección recargando si es necesario y recogiendo la percepción
     *
     * @author Diego Iáñez Ávila
     * @param direccion Por ejemplo, Mensajes.AGENT_COM_ACCION_MV_N para moverse al norte
     */
    private void moverseConBateria(EstadoVehiculo vehiculo, String direccion){
        if (vehiculo.battery <= vehiculo.consumo){
            // Recargar
            sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(Mensajes.AGENT_COM_ACCION_REFUEL), vehiculo.id);
            recogerPercepcion(vehiculo);
        }

        sendMessageVehiculo(ACLMessage.REQUEST, jsonComando(direccion), vehiculo.id);
        recogerPercepcion(vehiculo);
    }

    /**
     * Guarda la posición actual de la exploración para retomarla más adelante
     *
     * @author Diego Iáñez Ávila
     * @param vehiculo
     */
    private void guardarPosicionExploracion(EstadoVehiculo vehiculo){
        // Guardar la posición
        explorandoX = vehiculo.coor_x;
        explorandoY = vehiculo.coor_y;
    }

    /**
     * Rellena una fila de mapaMundo con borde de mundo
     *
     * @author Diego Iáñez Ávila
     * @param fila
     */
    private void rellenarFilaDePared(int fila){
        for (int x = 0; x < DIMENSIONES; ++x){
            mapaMundo[fila][x] = 2;
        }
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
