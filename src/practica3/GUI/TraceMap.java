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
    private int minX, minY, maxX, maxY;
    private ArrayList<Integer> radar;

    private int mapSize = 10001;
    // Para reducir el tamaño de la imagen. Con tamaño completo tarda demasiado en dibujarla.
    private int mapReduction = 9000;
    private int viewportSize = 500;

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila
     */
    public TraceMap(){
        map = new BufferedImage(mapSize - mapReduction, mapSize - mapReduction, BufferedImage.TYPE_INT_RGB);
        imageGraphics = map.createGraphics();
        imageGraphics.setColor(Color.GRAY);
        imageGraphics.fillRect(0, 0, mapSize, mapSize);

        setSize(viewportSize, viewportSize);
        setBackground(Color.GRAY);
        repaint();

        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = 0;
        maxY = 0;
    }

    /**
     * Actualizar el mapa
     *
     * @author Diego Iáñez Ávila
     * @param x posición X del agente
     * @param y posición Y del agente
     * @param radar percepción del radar
     */
    public void updateMap(int x, int y, ArrayList<Integer> radar){
        agentX = x - mapReduction / 2;
        agentY = y - mapReduction / 2;

        minX = Integer.min(agentX, minX);
        minY = Integer.min(agentY, minY);
        maxX = Integer.max(agentX, maxX);
        maxY = Integer.max(agentY, maxY);

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

        int x = -minX + (viewportSize/2 - (maxX - minY)/2);
        int y = -minY + (viewportSize/2 - (maxY - minY)/2);

        g.drawImage(map, x, y, this);
    }

    private void repaintMap(){
        if (radar != null){
            int x = agentX - 2;
            int y = agentY - 2;
            int value;
            Color color;

            for (int i = 0; i < 5; ++i){
                for (int j = 0; j < 5; ++j){
                    value = radar.get(i * 5 + j);

                    if (i == 2 && j == 2) {
                        color = Color.GREEN;
                    } else {
                        switch (value) {
                            case 0:
                                color = Color.WHITE;
                                break;

                            case 1:
                                color = Color.BLACK;
                                break;

                            case 2:
                                color = Color.RED;
                                break;

                            default:
                                color = Color.BLUE;
                        }
                    }

                    imageGraphics.setColor(color);
                    imageGraphics.fillRect(x + j, y + i, 1, 1);
                }
            }
        }
    }
}
