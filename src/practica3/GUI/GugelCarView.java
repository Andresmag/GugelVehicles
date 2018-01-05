package practica3.GUI;

import es.upv.dsic.gti_ia.core.AgentID;
import practica3.SuperMente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class GugelCarView extends JFrame {
    private JButton buttonEjecutar;
    private JButton buttonSalir;
    private JLabel mapIndicator;
    private JPanel contentPane;
    private JPanel buttonsPanel;
    private JPanel informationPanel;
    private JLabel traceLabel;
    private JPanel canvasPanel;
    private TraceMap traceMap;
    private JButton buttonReiniciar;

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

        buttonReiniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onReiniciar();
            }
        });

        setContentPane(contentPane);
        setTitle("DBA Practica 3: GugelCar");
        setSize(650, 400);
        initComponents();
        setLocationRelativeTo(null);

        //Centrar en pantalla
       // Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
       // this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        this.mapaSeleccionado = mapaSeleccionado;
        this.nombreAgente = nombreAgente;

        mapIndicator.setText(mapaSeleccionado);

        try {
            superMente = new SuperMente(mapaSeleccionado, new AgentID("Supermente"),this);
        } catch (Exception e) {
            System.out.println("Error al inicializar Supermente en GugelCardView");
            e.printStackTrace();
        }
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
        buttonEjecutar.setEnabled(true);
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

    /**
     * Permite a supermente reiniciar la sesión,
     * por si se hubiera cerrado mal anteriormente.
     *
     * @Author Jose Luis Martínez Ortiz
     */
    private void onReiniciar(){
        superMente.reiniciarSesion();
    }

}
