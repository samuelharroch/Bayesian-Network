import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is using for keeping all the Variables (using HashMap)
 */
public class Graph {
    HashMap<String,Variable> myVariables;

    public Graph() {
        this.myVariables = new HashMap<>();
    }

    public void ResetNeighbors(){
        for (Variable variable: myVariables.values()) {
            variable.neighbors=0;
        }
    }

    @Override
    public String toString() {
        return "Graph{" +
                "myVariables=" + myVariables +
                '}';
    }

}
