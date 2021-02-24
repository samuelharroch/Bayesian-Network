import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/***
 * this class represent a Query or Event :
 * We have an hypothesis and evidences.
 *
 */

public class Event {
    ArrayList<String> evidences;
    String hypothesis;

    /**
     * constructor
     * @param hypothesis
     * @param evidences
     */
    public Event(String hypothesis ,ArrayList<String> evidences) {
        this.hypothesis = hypothesis;
        this.evidences=evidences;
    }

    /**
     * copy constructor
     * @param event
     */
    public Event(Event event) {
        this.evidences= new ArrayList<>(event.evidences);
        this.hypothesis= event.hypothesis;
    }

    /**
     * given a Event this method
     * @return the Variables names that appear in a Query/Event
     */
    public Set<String> variablesInQuery (){
        Set<String> Variables = new HashSet<>();

       if (hypothesis!=null)
           Variables.add( hypothesis.substring(0,hypothesis.indexOf('=')));

        for (String s:evidences) {
            Variables.add( s.substring(0,s.indexOf('=')));
        }
        return Variables;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        for (String s:evidences) {
            if(!event.evidences.contains(s))
                return false;
        }
        return Objects.equals(hypothesis, event.hypothesis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new HashSet<String>(evidences), hypothesis);
    }

    @Override
    public String toString() {
        return "Event{" +
                "evidences=" + evidences +
                ", hypothesis='" + hypothesis + '\'' +
                '}';
    }
}

