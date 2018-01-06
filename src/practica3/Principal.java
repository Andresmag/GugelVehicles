package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import practica3.GUI.AgentNameCapture;
import practica3.GUI.GugelCarView;

public class Principal {

    public static void main(String[] args) {

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                Mensajes.AGENT_HOST, Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        // Creación e inicialización de los vehículos
        try {
            for(int i=0; i<4; i++){
                Vehiculo vehiculo = new Vehiculo(new AgentID("vehiculo" + i));
                vehiculo.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Captura del nombre Supermente e inicialización de la GUI
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        GugelCarView gcv = new GugelCarView(newCapture.getMapaSeleccionado(),newCapture.getNombreAgente());

        gcv.setVisible(true);

    }
}
