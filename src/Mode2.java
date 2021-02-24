import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Mode2 {

    Graph myGraph;
    Querie q;
    int additions;
    int multiplications;
    DecimalFormat df = new DecimalFormat("#0.00000");

    public Mode2(Graph myGraph, Querie q) {
        this.myGraph = myGraph;
        this.q = q;
    }
    /**
     * @return Set of all missing Variables names in Query
     */
    public Set<String> findMissingVariable(){
        Set<String> VariablesInSample = new HashSet<>( myGraph.myVariables.keySet());
        Set<String> VariablesInQuery = q.e.variablesInQuery();
        VariablesInSample.removeAll(VariablesInQuery);
        return VariablesInSample;
    }

    /**
     * @return set of variables names that are Necessary for solving the query
     * A Necessary variable is define by an ancestor of the Variables in the query
     */
    public Set<String> Necessary(){
        Set<String> Necessary= new HashSet<>();

        Queue<Variable> queue = new LinkedList<>();

        for (String var:q.e.variablesInQuery()) {
            queue.add(myGraph.myVariables.get(var));
        }

        while (!queue.isEmpty()) {

            Variable NecessaryVar=queue.remove();
            queue.addAll(NecessaryVar.Parents);
            Necessary.add(NecessaryVar.name);

        }

        return Necessary;
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

    /**
     * given a query and a
     * @param variable the function
     * @return a Factor- CPTs of the variable init by query evidences
     */
    public Factor MakeFactor(Variable variable){
        Factor factor =new Factor();

        //first define the name of the factor  :
        // the PotentialFactorName are the MissingVariable names and this variable name
        Set<String> PotentialFactorName= new HashSet<>(findMissingVariable());
        PotentialFactorName.add(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('=')));

        //MeAndMyParentsNames is Set of this variable name and his parents names
        //(its actually the variables which appear in the CPts keys )
        Set<String> MeAndMyParentsNames= new HashSet<>(variable.MyParentsNames());
        MeAndMyParentsNames.add(variable.name);

        //and finally we define the factor name by the intersection of
        //MeAndMyParentsNames and PotentialFactorName
        // and the new Factors Keys will be all the permutation of the values of the factor name
        PotentialFactorName.retainAll(MeAndMyParentsNames);
        factor.name=PotentialFactorName;

        //QueryEvidences represent the intersection between MeAndMyParentsValues
        //and the evidences of the query
        Set<String> QueryEvidences =new HashSet<>(q.e.evidences);
        Set<String> MeAndMyParentsValues =variable.MyParentsValues();
        MeAndMyParentsValues.addAll(variable.values);
        QueryEvidences.retainAll(MeAndMyParentsValues);

        //so if the intersection between them is empty the Factor will be exactly the original CPTs
        if (QueryEvidences.isEmpty()){
            HashMap<Set<String>,Double> CPTbyEvidences= new HashMap<>();
            for (Event key:variable.getCPT().keySet()) {
                    Set<String> hashKey = new HashSet<>(key.evidences);
                    hashKey.add(key.hypothesis);
                    CPTbyEvidences.put(hashKey,variable.CPT.get(key));
            }
            factor.CPTbyEvidences=CPTbyEvidences;

        }else { //make the factor as the CPT init by the evidences in QueryEvidences

            HashMap<Set<String>,Double> CPTbyEvidences= new HashMap<>();

            for (Event key:variable.getCPT().keySet()) {
                Set<String> hashKey = new HashSet<>(key.evidences);// now the key is set of values(easier for Join )
                hashKey.add(key.hypothesis);
                if (hashKey.containsAll(QueryEvidences)) {
                    hashKey.removeAll(QueryEvidences); //we ignore the evidence in the new keys
                    CPTbyEvidences.put(hashKey,variable.CPT.get(key));
                }
            }

            factor.CPTbyEvidences=CPTbyEvidences;
        }

        return factor;

    }

    /**
     * given set of Necessary variables
     * @param Necessary
     * @return ArrayList of all factors to answer the query
     */
    public ArrayList<Factor> makeAllFactors(Set<String> Necessary){

        ArrayList<Factor> allFactors = new ArrayList<>();
        for (Variable var: myGraph.myVariables.values()) {
            if(Necessary.contains(var.name)){
                Factor factor =MakeFactor(var);
                if (factor.CPTbyEvidences.size()!=1){
                    allFactors.add( factor);
                }
            }
        }
        return allFactors;
    }


    /**
     * given two Factors
     * @param a
     * @param b
     * @return a new Factor how is a result of  Join between them
     */
    public Factor Join(Factor a, Factor b){

        //define the name of the new factor
        Set<String> newKeyForm=new HashSet<>(a.name);
        newKeyForm.addAll(b.name);

        int varInKey=newKeyForm.size();// size of values in the new keys as the number of variable in the new Factor

        Factor Join = new Factor();
        Join.name=newKeyForm;

        HashMap<Set<String>,Double> JoinCPTbyEvidences= new HashMap<>();

        //for each key in the first factor check if there is a match
        //between each key of the second factor (the match is explain in ThereIsMatch function )
        for (Set<String> aKey: a.CPTbyEvidences.keySet()) {
            for (Set<String> bKey: b.CPTbyEvidences.keySet()) {
                if (ThereIsMatch(aKey,bKey,varInKey)){ // is there is a match
                    Set<String> JoinKey=new HashSet<>(aKey);// make union between the two keys
                    JoinKey.addAll(bKey);
                    double JoinValue= a.CPTbyEvidences.get(aKey) * b.CPTbyEvidences.get(bKey);
                    JoinCPTbyEvidences.put(JoinKey,JoinValue);

                    multiplications++;
                }
            }
        }

        Join.CPTbyEvidences= JoinCPTbyEvidences;

        return Join;
    }

    /**
     * given two keys and a size of the expected key (varInKey)
     * @param keys1
     * @param keys2
     * @param varInKey as the number of variable in the new Factor
     * @return true if the size of the union between the two key
     * equals to the expected new key size
     */
    public boolean ThereIsMatch(Set<String> keys1 ,Set<String> keys2, int varInKey ){
        Set<String> union = new HashSet<>(keys1);
        union.addAll(keys2);
        return union.size()==varInKey;
    }

    /**
     * given a list of
     * @param factors of One Variable which appear in all of them  Join them using the order that we define in
     * @return Join them using the order that we define in sortFactors function
     */
    public Factor JoinAllFactors (ArrayList<Factor> factors){

        ArrayList<Set<String>> factorsNames= new ArrayList<>();// all the factorsNames
        for (Factor f: factors) {
            factorsNames.add(new HashSet<>(f.name));
        }

        //Sort the names using the order that we define in sortFactors function
        ArrayList<Set<String>> sortedFactorsNames= new ArrayList<>(factorsNames);
        sortFactors(sortedFactorsNames);

        //sort now the factors using the order of they names in sortedFactorsNames
        ArrayList<Factor> sortedFactor = new ArrayList<>();
        for (Set<String> name : sortedFactorsNames) {
            for (Factor f :factors) {
                if (f.name.equals(name)){
                    sortedFactor.add(new Factor(f));
                    factors.remove(f);
                    break;
                }
            }
        }

        //in each iteration Join the two first and add the result in the first place of the sortedFactor list
        while (sortedFactor.size()>= 2){
            Factor join = Join(sortedFactor.get(0),sortedFactor.get(1));
            sortedFactor.remove(0);
            sortedFactor.remove(0);
            sortedFactor.add(0,join);
        }

        return (sortedFactor.get(0));
    }
    /**
     * Given a
     * @param factor and
     * @param variable that we want to eliminate
     * @return new factor which the variable is not appear.
     * we remove him by summing the values (prob) of the keys how have the same Variable value (as true, false ect..)
     */
    public Factor SumOut(Factor factor,Variable variable ){
        Factor newFactor= new Factor();

        //define the new factor name
        Set<String> newFactorName= new HashSet<>(factor.name);
        newFactorName.remove(variable.name);
        newFactor.name=newFactorName;

        HashMap<Set<String>,Double> newFactorCPT= new HashMap<>();

        List<List<String>> listOfValues= new ArrayList<>();
        for (String varName:newFactorName) {
            listOfValues.add(myGraph.myVariables.get(varName).values);
        }

        // make cartesianProduct between all sets of values of the Variables in the new factor
        // and its will represent a new key in the new factor
        List<List<String>> cartesianProduct =cartesianProduct(listOfValues);

        // make sets from the lists (easier)
        List<Set<String>> newKeyList=new ArrayList<>();
        for (List<String> list:cartesianProduct) {
            Set<String> newKey = new HashSet<>(list);
            newKeyList.add(newKey);
        }

        // for each newKey chek if the ancient key contains the all newKey and sum
        for (Set<String> newKey:newKeyList) {
            for (Set<String> key: factor.CPTbyEvidences.keySet()) {
                if (key.containsAll(newKey)){

                    if(newFactorCPT.containsKey(newKey)){
                        double x = newFactorCPT.get(newKey) + factor.CPTbyEvidences.get(key);
                        newFactorCPT.put(newKey,x);
                        additions++;
                    }else {
                        newFactorCPT.put(newKey,factor.CPTbyEvidences.get(key));
                    }
                }
            }
        }
        newFactor.CPTbyEvidences=newFactorCPT;
        return newFactor;
    }

    /**
     * given a list of
     * @param hiddens variables sorted by sortFactors function and a ArrayList of
     * @param allFactors
     * @return HashMap when key is an hidden variable name and
     * values  are ArrayList of factors how accord to the hidden variable
     */
    public  HashMap<String,ArrayList<Factor>> accordFactorToHidden (ArrayList<String> hiddens, ArrayList<Factor> allFactors ){
        HashMap<String,ArrayList<Factor>> factorByHidden= new HashMap<>();

        for (String varName:hiddens) {
            ArrayList<Factor> hiddensFactors= new ArrayList<>();
            for (Factor factor:allFactors) {
                if (factor.name.contains(varName)){
                    hiddensFactors.add(factor); // maybe make new
                }
            }
            factorByHidden.put(varName,hiddensFactors);
            allFactors.removeAll(hiddensFactors);
        }
        return factorByHidden;
    }
    /**
     * given a list of
     * @param hiddens variables to eliminate and an
     * @param factorByHidden HashMap when key is an hidden variable name and
     * values  are ArrayList of factors how accord to the hidden variable
     * @return the "Final" factor of this query
     */
    public Factor EliminateHiddens (ArrayList<String> hiddens,HashMap<String,ArrayList<Factor>> factorByHidden){

        Factor FactorAfterElimination= new Factor();

        //eliminate all the hidden variables
        while (!hiddens.isEmpty()){
            //first JoinAllFactors how accord to the hidden variable
            FactorAfterElimination= JoinAllFactors(factorByHidden.get(hiddens.get(0)));

            //sum out
            FactorAfterElimination= SumOut(FactorAfterElimination,myGraph.myVariables.get(hiddens.get(0)));

            factorByHidden.remove(hiddens.get(0));
            hiddens.remove(0);

            if(FactorAfterElimination.CPTbyEvidences.size()==1) continue;

            for (String key :hiddens) {
                if (FactorAfterElimination.name.contains(key)){
                    factorByHidden.get(key).add(FactorAfterElimination);
                    break;
                }
            }

        }
        return FactorAfterElimination;
    }

    /**this is the final function of this class
     * @return the answer of the query
     */
    public String CalculateQuery (){
        //if the query has a knew answer
        if (myGraph.myVariables.get(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('='))).getCPT().containsKey(q.e))
            return df.format(myGraph.myVariables.get(q.e.hypothesis.substring(0,q.e.hypothesis.indexOf('='))).getCPT().get(q.e)) ;

        // Necessary vars to the query
        Set<String> Necessary= new HashSet<>(Necessary());

        // Make Factors for all Necessary vars
       ArrayList<Factor> allFactors = makeAllFactors(Necessary);

        // Hidden and Necessary variables
        ArrayList<String> hiddens = new ArrayList<>(findMissingVariable());
        hiddens.retainAll(Necessary);

        //sort the hidden Variables given the number of algorithms to apply  (2 or 3 )
        if (q.mode==2){
            sortHiddensAlgo2(hiddens);
        }else {
            // init Neighbors of each variable
            for (Variable var:myGraph.myVariables.values()) {
                var.initNeighbors();
            }
            // sort by number of Neighbors
            hiddens.sort(new sortHiddensAlgo3(myGraph));
            myGraph.ResetNeighbors(); // to future query
        }

         //put all hidden factors in hashmap by hiddens names
        HashMap<String,ArrayList<Factor>> factorByHidden= accordFactorToHidden(hiddens,allFactors);

        // eliminate all hiddens
        Factor FactorAfterElimination= EliminateHiddens(hiddens,factorByHidden);

       if (FactorAfterElimination.name!=null){
           allFactors.add(FactorAfterElimination);
       }

       // Join all the remaining Factors
       Factor finalFactor =JoinAllFactors(allFactors);

       //normalize
        double denominator=-1;
        for (double d:finalFactor.CPTbyEvidences.values()) {
            if (denominator==-1){
                denominator=d;
            }else{
                denominator+=d;
                additions++;
            }
        }


        Set<String> answerKey =new HashSet<String>();
        answerKey.add(q.e.hypothesis);
        return df.format(finalFactor.CPTbyEvidences.get(answerKey) / denominator);
    }

    /**
     * given an ArrayList of
     * @param hiddens variables Sort them in lexicographic order
     * to determinate the order of elimination
     */
    public void sortHiddensAlgo2(ArrayList<String> hiddens){
        hiddens.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int comp =o1.compareTo(o2);
               return Integer.compare( comp,0);
            }
        });
    }

    /**
     * given a list of
     * @param FactorsNames
     * sort them by table size to create as few rows as possible in the new table.
     * And if the size is equal then the sorting is done according to the ASCII value
     * of the names of the variables in the table
     */
    public void sortFactors ( ArrayList<Set<String>> FactorsNames){
        FactorsNames.sort(new Comparator<Set<String>>() {
            @Override
            public int compare(Set<String> o1, Set<String> o2) {
                if (o1.size()< o2.size())
                    return -1;
                else if (o1.size()> o2.size())
                    return  1;
                else {
                    int ASCIIo1=0, ASCIIo2=0;
                    for (String s1 : o1) {
                        for (int i=0 ; i<s1.length();i++)
                            ASCIIo1+=s1.charAt(i);
                    }
                    for (String s2 : o2) {
                        for (int i=0 ; i<s2.length();i++)
                            ASCIIo2+=s2.charAt(i);
                    }

                    return Integer.compare(ASCIIo1,ASCIIo2);
                }
            }
        });

    }

    /**
     * Inner Class implements Comparator
     * in order to sort the Hidden Variable to eliminate by number of neighbors 
     */
   class sortHiddensAlgo3 implements Comparator<String>{

        public Graph myGraph;

        public sortHiddensAlgo3(Graph myGraph) {
           this.myGraph = myGraph;
       }

       @Override
       public int compare(String o1, String o2) {
           return Integer.compare(myGraph.myVariables.get(o1).neighbors,myGraph.myVariables.get(o2).neighbors);
       }
   }

}
