package practica3;

import java.awt.geom.Point2D;

/**
 * Clase para contener la información de cada celda de la matriz del mapa.
 * Utilizada para la obtención de la ruta de un vehículo al objetivo de forma óptima.
 *
 * @author Jose Luis Martínez Ortiz
 *
 */
public class Nodo{

    // Atributos públicos de la clase
    public Point2D.Double punto;    // Indica la posición en el mapa
    public double g_coste;  //coste real
    public double f_coste;  //coste heuristico estimado
    public String accion;   // Acción para llegar a dicha posición
    public Nodo anterior;   // Nodo anterior

    public Nodo(Point2D.Double punto, double g_coste, Point2D.Double goal, String accion, Nodo anterior) {
        this.punto = punto;
        this.g_coste = g_coste;
        this.f_coste = punto.distance(goal) + g_coste;
        this.accion = accion;
        this.anterior = anterior;
    }

    public Nodo() {
        punto = new Point2D.Double();
        g_coste = 0;
        f_coste = 0;
        accion = "";
        anterior = null;
    }

    /**
     * Posición X del Nodo en el mapa.
     * @return la coordenada X en el mapa del nodo.
     */
    public double x(){
        return punto.getX();
    }

    /**
     * Posición Y del Nodo en el mapa
     * @return la coordenada Y en el mapa del nodo.
     */
    public double y(){
        return punto.getY();
    }

    // TODO Revisar que contains de LinkedList funciona con equals

    /**
     * Compara si un nodo está en la misma posición del nodo n
     * @param n nodo a comprar con la posición
     * @return true si el nodo n está en la misma posición que el actual.
     */
    @Override
    public boolean equals(Object n){
        boolean resultado = false;
        if(x() == ((Nodo) n).x() && y() == ((Nodo) n).y())
            resultado = true;
        return resultado;
    }

}
