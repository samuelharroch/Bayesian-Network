import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/***
 * This class represent a Variable in Bayesian Network
 */

public class Variable {
    String name;
    ArrayList<String> values;
    ArrayList<Variable> Parents;
    HashMap<Event,Double> CPT;
    int neighbors;


    ////////////////////////////////Getters and Setters////////////////////////

    public Variable() {
        CPT=new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Variable> getParents() {
        return Parents;
    }

    public void setParents(ArrayList<Variable> parents) {
        Parents = parents;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public HashMap<Event, Double> getCPT() {
        return CPT;
    }

    public void setCPT(HashMap<Event, Double> CPT) {
        this.CPT = CPT;
    }

////////////////////////////////////////////////////////////////////////////////////

    /**
     * Given a Variable name (representing by string) this method
     * @return true if the given Variable is a parents of this Variable
     * */
    public boolean IsMyParent(String PotentialParent){
        for (Variable parent : Parents) {
            if (parent.getName().equals(PotentialParent))
                return true;
        }
        return false;
    }

    /**
     * @return Set of the Parents names of this Variable
     */
    public Set<String> MyParentsNames (){
        Set<String> MyParentsNames = new HashSet<>();
        for (Variable parent: Parents) {
            MyParentsNames.add(parent.name);
        }
        return MyParentsNames;
    }
    /**
     * @return Set of the Parents values of this Variable
     */
    public Set<String> MyParentsValues (){
        Set<String> MyParentsValues = new HashSet<>();
        for (Variable parent: Parents) {
            MyParentsValues.addAll(parent.values);
        }
        return MyParentsValues;
    }

    /***
     * update the number of neighbors of this Variable
     * using for heuristic function for Variable elimination in Mode2
     */
    public void initNeighbors(){
        for (Variable parent : Parents) {
            parent.neighbors++;
            this.neighbors++;
        }
    }


    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                "\n, values=" + values +
             //   "\n, Parents=" + Parents +
                "\n, CPT=" + CPT +
                "}\n";
    }
}
