package practica3.reset;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import practica3.Mensajes;

public class CualquierAgente  extends SingleAgent {


    public CualquierAgente(AgentID aid) throws Exception {
        super(aid);
    }

    /**
     * Método de inicialización del agente
     *
     * @author Andrés Molina López ft. Diego, Jose y Ángel
     */
    @Override
    public void init(){

        System.out.println("Reset " + getAid().toString() + " activo");
    }

    /**
     * Cuerpo del agente para resetear Shenron
     *
     * @author Jose Luis Martínez Ortiz, Ángel Píñar Rivas
     */
    @Override
    public void execute(){


        System.out.println("Enviando");

        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());

        JsonObject json = Json.object();
        json.add("user",Mensajes.AGENT_USER);
        json.add("password",Mensajes.AGENT_PASS);

        outbox.setContent(json.toString());
        System.out.println(json.toString());
        outbox.setPerformative(ACLMessage.REQUEST);

        send(outbox);

        System.out.println("Enviado");

        ACLMessage inbox = null;

        try {
            inbox = receiveACLMessage();
            System.out.println("Recibido");
            /* Imprimir para debug */
            System.out.println("Vehiculo " + getAid().toString() + " recibe: " + inbox.getContent());
            if(inbox.getPerformativeInt() == ACLMessage.FAILURE){
                System.out.println("Deberías avisar al profesor: mailto:l.castillo@decsai.ugr.es");
            }
            /**/


        } catch (InterruptedException e) {
            System.err.println("Error al recibir mensaje en receiveMessage");
        }


        System.out.println(getAid().toString() + " finalizado.");
    }
}