import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * this class is using for the second and third algorithms.
 * A Factor is a kind of CPT of each Variable but init by evidences given in a Query
 * The Name of the Factor is determinate by the Variables names implicate in the "key" of the Factor
 */
public class Factor {
    HashMap<Set<String>,Double> CPTbyEvidences;
    Set<String> name;

    /**
     * empty constructor
     */
    public Factor() {
    }

    /**
     * Copy constructor
     * @param other factor
     */
    public Factor(Factor other) {
        this.CPTbyEvidences= new HashMap<>(other.CPTbyEvidences);
        this.name= new HashSet<>(other.name);
    }

    @Override
    public String toString() {
        return "Factor{" +
                "name=" + name +
                ", CPTbyEvidences=" + CPTbyEvidences +

                '}';
    }
}
