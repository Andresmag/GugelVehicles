package practica3.reset;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import practica3.Mensajes;

public class Reset {

    public static void main(String[] args) {

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                "test", Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        System.out.println("HOLAAAA al crear el agente");

        try {
            CualquierAgente ca = new CualquierAgente(new AgentID("HolaGG"));
            ca.start();

            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error al crear el agente");
        }

    }

}