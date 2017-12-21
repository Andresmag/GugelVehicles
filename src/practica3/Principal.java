package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class Principal {

    public static void main(String[] args) {
        // Ventana de captura de nombre y mapa
      /*    PARTE FINAL DE LA P2
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        GugelCarView gcv = new GugelCarView(newCapture.getMapaSeleccionado(),newCapture.getNombreAgente());

        gcv.setVisible(true);

      */

        /**
         * PARTE PARA TEST Y PRUEBAS INTERNAS
         * hasta que se añada la GUI
         */
        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                Mensajes.AGENT_HOST, Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        try {
            SuperMente einstein = new SuperMente("map1", new AgentID("Supermente"));

            //Vehiculo vehiculo = new Vehiculo("map1", new AgentID("coche1")/*GUI,this*/);

            System.out.println("\n\n-------------------------------\n");

            einstein.start();
            //vehiculo.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
