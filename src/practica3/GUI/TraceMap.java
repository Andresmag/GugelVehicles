package practica3.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TraceMap extends JPanel {

    private BufferedImage map;
    private Graphics2D imageGraphics;

    private int agentX;
    private int agentY;
    private ArrayList<Integer> radar;

    private int mapSize = 1000;
    private int viewportSize = 500;

    private int ancho = 0;
    private int inicio = 0;

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila
     */
    public TraceMap(){
        map = new BufferedImage(mapSize, mapSize, BufferedImage.TYPE_INT_RGB);
        imageGraphics = map.createGraphics();
        imageGraphics.setColor(Color.GRAY);
        imageGraphics.fillRect(0, 0, mapSize, mapSize);

        setSize(viewportSize, viewportSize);
        setBackground(Color.GRAY);
        repaint();
    }

    /**
     * Actualizar el mapa
     *
     * @author Diego Iáñez Ávila
     * @param x posición X del agente
     * @param y posición Y del agente
     * @param radar percepción del radar
     */
    public void updateMap(int x, int y, ArrayList<Integer> radar, int ancho, int inicio){
        agentX = x;
        agentY = y;

        this.ancho = ancho;
        this.inicio = inicio;

        this.radar = radar;

        repaint();
    }

    /**
     * Dibujar la traza
     *
     * @author Diego Iáñez Ávila
     * @param g
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        repaintMap();

        g.drawImage(map, 0, 0, this);
    }

    private void repaintMap(){
        if (radar != null){
            int x = agentX - inicio;
            int y = agentY - inicio;
            int value;
            Color color;

            for (int i = 0; i < ancho; ++i){
                for (int j = 0; j < ancho; ++j){
                    value = radar.get(i * ancho + j);

                    switch (value) {
                        case 0:
                            color = Color.WHITE;
                            break;

                        case 1:
                            color = Color.BLACK;
                            break;

                        case 2:
                            color = Color.DARK_GRAY;
                            break;

                        case 3:
                            color = Color.RED;
                            break;

                        case 4:
                            color = Color.YELLOW;
                            break;

                        default:
                            color = Color.BLUE;
                    }

                    imageGraphics.setColor(color);
                    imageGraphics.fillRect(x + j, y + i, 1, 1);
                }
            }
        }
    }
}
