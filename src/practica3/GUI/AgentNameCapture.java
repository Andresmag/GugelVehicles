package practica2.GUI;

import javax.swing.*;
import java.awt.event.*;

public class AgentNameCapture extends JDialog {
    private JPanel contentPane;
    private JButton loginButton;
    private JButton buttonCancel;
    private JFormattedTextField nombreAgenteField;
    private JComboBox<String> mapSelectionBox;

    private String nombreAgente;            // Nombre del agente
    private String mapaSeleccionado;        // Mapa seleccionado (1..10)

    /**
     * Constructor
     *
     * @author David Vargas Carrillo
     */
    public AgentNameCapture() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // Adicion de los mapas disponibles
        mapSelectionBox.addItem("map1"); mapSelectionBox.addItem("map2");
        mapSelectionBox.addItem("map3"); mapSelectionBox.addItem("map4");
        mapSelectionBox.addItem("map5"); mapSelectionBox.addItem("map6");
        mapSelectionBox.addItem("map7"); mapSelectionBox.addItem("map8");
        mapSelectionBox.addItem("map9"); mapSelectionBox.addItem("map10");
        mapSelectionBox.addItem("map11");

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Dimension de la ventana y centrado
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setTitle("Inicio");
    }

    /**
     * Accion a ejecutar cuando se pulsa el boton Login
     *
     * @author David Vargas Carrillo
     */
    private void onOK() {
        nombreAgente = nombreAgenteField.getText();
        mapaSeleccionado = (String) mapSelectionBox.getSelectedItem();
        dispose();
    }

    /**
     * Sale del programa al pulsar el boton Exit
     *
     * @author David Vargas Carrillo
     */
    private void onCancel() {
        System.exit(0);
    }

    /**
     * Metodo GET para obtener el mapa seleccionado
     *
     * @author David Vargas Carrillo
     * @return String con el mapa seleccionado, con el formato "mapX"
     */
    public String getMapaSeleccionado() {
        return mapaSeleccionado;
    }

    /**
     * Metodo GET para obtener el nombre del agente
     *
     * @author David Vargas Carrillo
     * @return String con el nombre del agente escrito
     */
    public String getNombreAgente() {
        return nombreAgente;
    }
}
