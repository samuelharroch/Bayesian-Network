import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class is using for answer a Query by the simple Algorithms of bayes rule
 * developing by the total probability rule and the Chain rule.
 */

public class Mode1 {

    Graph myGraph;
    Querie q;
    int additions;
    int multiplications;
    DecimalFormat df = new DecimalFormat("#0.00000");

    /**
     * constructor
     * @param myGraph the Network
     * @param q Query
     */
    public Mode1(Graph myGraph, Querie q) {
        this.myGraph = myGraph;
        this.q = q;
    }

    /**
     *
     * @return Set of all missing Variables names in Query
     *
     */
    public  Set<String> findMissingVariable(){
        Set<String> VariablesInSample = new HashSet<>( myGraph.myVariables.keySet());
        Set<String> VariablesInQuery = q.e.variablesInQuery();
        VariablesInSample.removeAll(VariablesInQuery);
        return VariablesInSample;
    }

    /**
     * help function to make cartesian multiplication
     * over all values of missing Variables
     * @param lists
     * @return List<List<String>>of all possible permutations
     */
    public List<List<String>> cartesianProduct(List<List<String>> lists) {
        List<List<String>> resultLists = new ArrayList<List<String>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<String>());
            return resultLists;
        } else {
            List<String> firstList = lists.get(0);
            List<List<String>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
            for (String condition : firstList) {
                for (List<String> remainingList : remainingLists) {
                    ArrayList<String> resultList = new ArrayList<String>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    /**the function  makes cartesian multiplication
     * over all values of missing Variables
     * @return List<List<String>>of all possible permutations
     */
    public List<List<String>> makeCartesianProduct (){
        Set<String> missingVariables= findMissingVariable();
        List<List<String>> list= new ArrayList<>();

        for (String s: missingVariables) {
            list.add(myGraph.myVariables.get(s).values);
        }
        return cartesianProduct(list);
    }

    /**
     * given the
     * @param BayesNumerator of our Query this method
     * add all the permutations of the missing variables values to the Event of our query and
     * @return Arraylist of Events as the Total Probability rule development of the given query
     */
    public ArrayList<Event> MakeTotalProbability(Event BayesNumerator ){
        ArrayList<Event> TotalProbability= new ArrayList<>();
        List<List<String>> missingVariables = makeCartesianProduct();

        for (List list: missingVariables) {
            Event e =new Event(BayesNumerator);
            e.evidences.addAll(list);
            TotalProbability.add(e);
        }
        return TotalProbability;

    }

    /**
     * given an
     * @param event which all variables appear ,
     * @return ArrayList of new Events- when each event is representing by
     * each(Hypothesis) Variables given his parents (evidences)
     */
    public  ArrayList<Event> ChainRule (Event event){
        ArrayList<Event> ConditionalIndependence = new ArrayList<>();

        //loop over all variables given a specific values for each one
        for (String evidence: event.evidences) {
            String hypothesis = evidence; // now the variable his hypothesis

            ArrayList<String> Parents= new ArrayList<>();// this list will keep all the parent of the present Hypothesis

            for (String PotentialParent : event.evidences){
                //if he is my parent
                if(myGraph.myVariables.get(hypothesis.substring(0,hypothesis.indexOf('='))).IsMyParent(( PotentialParent.substring(0,PotentialParent.indexOf('=')))))
                    Parents.add(PotentialParent);
            }
            ConditionalIndependence.add(new Event(hypothesis,Parents));
        }
        return ConditionalIndependence;
    }

    /**
     *
     * @param q Query
     * @return the BayesNumerator of the answer of the  query
     */
    public double ComputeBayesNumerator (Querie q){
        ArrayList<String> arrayList= new ArrayList<>(q.e.evidences);
        arrayList.add(q.e.hypothesis);

        //  P(B|A) = P(A,B)/P(A)
        Event BayesNumerator= new Event(null, arrayList); // = P(A,B)

        //given the query MakeTotalProbability
        ArrayList<Event> TotalProbability =MakeTotalProbability(BayesNumerator);


        ArrayList< ArrayList<Event>> FullBayesNumerator=new ArrayList<>();

        //and make ChainRule
        for ( Event event : TotalProbability) {
            FullBayesNumerator.add(ChainRule(event));
        }
        // in the end of the loop we have in FullBayesNumerator all the events that we need to answers to the query
        //we just need to find the values and compute them:
        double ans=-1;
        for (ArrayList<Event> eventArrayList:FullBayesNumerator) {
            double m=-1;

            for (Event event:eventArrayList) {
                if (m==-1)
                    m = myGraph.myVariables.get( event.hypothesis.substring(0,event.hypothesis.indexOf('='))).CPT.get(event);
                else {
                    m *= myGraph.myVariables.get( event.hypothesis.substring(0,event.hypothesis.indexOf('='))).CPT.get(event);
                    multiplications++;
                }

            }
            if (ans==-1)
                ans=m;
            else {
                ans+=m;
                additions++;
            }

        }

        return ans ;
    }

    /**
     * this is the final method of the Algorithms
     * @return
     */
    public String CalculateQuery(){

        //if the query has a knew answer
        if (myGraph.myVariables.get(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('='))).getCPT().containsKey(q.e))
            return df.format(myGraph.myVariables.get(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('='))).getCPT().get(q.e)) ;

        //we keep all the values of the hypothesis Variables and remove the given value
        Set<String> Values =
                new HashSet<>(new ArrayList<>(myGraph.myVariables.get(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('='))).values) );
        Values.remove(q.e.hypothesis);

        double denominator = 0;

        // for each value we compute the BayesNumerator of our query for normalization
        for (String value:Values) {
            Querie querie = new Querie(new Event(value,q.e.evidences),0);// this is our query except the hypo

            denominator+= ComputeBayesNumerator(querie);
            additions++;
        }
        double numerator = ComputeBayesNumerator(q);

        return df.format(numerator/(numerator+denominator));

    }

}
