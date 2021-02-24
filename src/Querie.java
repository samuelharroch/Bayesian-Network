import java.util.HashSet;
import java.util.Set;

/**
 * this Class "extend" the Event class for Queries in the end of the input file
 */

public class Querie {
    Event e;
    int mode;

    /**
     * Constructor
     * @param e the Event
     * @param mode Algorithms
     */
    public Querie(Event e, int mode) {
        this.e = e;
        this.mode = mode;
    }



    @Override
    public String toString() {
        return "Querie{" + e +
                ", mode=" + mode +
                '}';
    }
}
