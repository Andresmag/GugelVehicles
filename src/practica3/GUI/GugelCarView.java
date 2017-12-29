package practica3.GUI;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import practica3.SuperMente;
import practica3.Vehiculo;
import practica3.Mensajes;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
import java.util.ArrayList;

import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

public class GugelCarView extends JFrame {
    private JButton buttonEjecutar;
    private JButton buttonSalir;
    private JLabel mapIndicator;
    private JPanel contentPane;
    private JPanel buttonsPanel;
    private JPanel informationPanel;
    private JPanel ScannerPanel;
    private JPanel RadarPanel;
    private JTextArea scannerTextArea;
    private JTextArea generalMsgTextArea;
    private JTextArea radarTextArea;
    private JLabel traceLabel;
    private JPanel canvasPanel;
    private TraceMap traceMap;

    private SuperMente superMente;

    private String mapaSeleccionado,nombreAgente;

    /**
     * Constructor
     *
     * @author David Vargas Carrillo, Jose Luis Martínez Ortiz
     * @param mapaSeleccionado para donde se va a ejecutar el agente.
     * @param nombreAgente nombre que identifica al agente
     */
    public GugelCarView(String mapaSeleccionado, String nombreAgente) {
        buttonEjecutar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEjecutar();
            }
        });

        buttonSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSalir();
            }
        });

        setContentPane(contentPane);
        setTitle("DBA Practica 3: GugelCar");
        setLocationRelativeTo(null);
        setSize(650, 400);
        initComponents();

        this.mapaSeleccionado = mapaSeleccionado;
        this.nombreAgente = nombreAgente;

        mapIndicator.setText(mapaSeleccionado);
    }

    /**
     * Constructor
     *
     * @author David Vargas Carrillo
     * @param mapa indicador del mapa actual (formato "mapX" o "mapXX")
     */
    public GugelCarView(String mapa) {
        setContentPane(contentPane);
        setTitle("DBA Practica 2: practica2.gugelcar.practica2.gugelcar.GugelCar");
        setSize(650, 400);
        setMapIndicator(mapa);
        initComponents();
    }

    /**
     * Inicializa los componentes de la clase
     *
     * @author David Vargas Carrillo
     */
    private void initComponents(){
        scannerTextArea.setEditable(false);
        radarTextArea.setEditable(false);
        generalMsgTextArea.setEditable(false);
        buttonEjecutar.setEnabled(true);


        DefaultCaret caret = (DefaultCaret) scannerTextArea.getCaret();
        caret.setUpdatePolicy(ALWAYS_UPDATE);

        DefaultCaret caret1 = (DefaultCaret) radarTextArea.getCaret();
        caret1.setUpdatePolicy(ALWAYS_UPDATE);
        DefaultCaret caret2 = (DefaultCaret) generalMsgTextArea.getCaret();
        caret2.setUpdatePolicy(ALWAYS_UPDATE);



        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

    }



    /**
     * Metodo SET para el indicador del mapa
     *
     * @author David Vargas Carrillo
     * @param mapInd String de la forma "mapX" o "mapXX" siendo X un entero
     */
    public void setMapIndicator(String mapInd) {
        mapIndicator.setText(mapInd);
    }

    /**
     * Imprimir texto en el panel Radar
     *
     * @author David Vargas Carrillo, Jose Luis Martínez Ortiz
     * @param radarText informacion recogida por el sensor radar
     */
    public void printToRadar(ArrayList<Integer> radarText) {
        String texto = "";
        for(Integer i:radarText)
            texto += i + "   ";
        radarTextArea.append("\n"+texto);
    }

    /**
     * Imprimir texto en el panel Scanner
     *
     * @author David Vargas Carrillo, Jose Luis Martínez Ortiz
     * @param scannerText informacion recogida por el sensor scanner
     */
    public void printToScanner(ArrayList<Float> scannerText) {
        String texto = "";
        for(Float i:scannerText)
            texto += i.toString().substring(0,i.toString().indexOf('.') + 2) + "      ";
        scannerTextArea.append("\n"+texto);

    }

    /**
     * Imprimir texto en el panel General Message
     *
     * @author David Vargas Carrillo
     * @param message mensaje que se quiere imprimir
     */
    public void printToGeneralMsg(String message) {

        generalMsgTextArea.append("\n"+message);
    }

    /**
     * Imprimir la imagen de la traza
     *
     * @author David Vargas Carrillo
     * @param path ruta de la imagen de traza
     */
    public void printTraceUI(String path) {
        traceLabel.setText(" ");
        traceLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(path)));

    }

    /**
     * Dibujar la percepción actual en el mapa de la practica2.GUI
     *
     * @author Diego Iáñez Ávila
     * @param x Posición x del agente
     * @param y Posición y del agente
     * @param radar Percepción del radar
     */
    public void updateMap(int x, int y, ArrayList<Integer> radar, int ancho, int inicio){
        traceMap.updateMap(x, y, radar, ancho, inicio);
    }

    /**
     * Accion del boton de Ejecutar
     *
     * @author David Vargas Carrillo, Jose Luis Martínez Ortiz
     */
    private void onEjecutar(){
        try {
            superMente = new SuperMente(mapaSeleccionado, new AgentID("Supermente"),this);

            System.out.println("\n\n-------------------------------\n");

            superMente.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonEjecutar.setEnabled(false);
    }

    /**
     * @author Jose Luis Martínez Ortiz
     * Permite que se vuelva a ejecutar el mapa.
     */
    public void enableEjecutar(){
        buttonEjecutar.setEnabled(true);
    }

    /**
     * Accion del boton de Salir
     *
     * @author David Vargas Carrillo
     */
    private void onSalir(){
        System.exit(0);
    }

}
