

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * THis class read the the input file , init the Variable of the networks and the Queries
 */

public class ReadFile {

    File input;
    Graph myGraph;
    ArrayList<Querie> Queries;

    public ReadFile() {
    }

    public void initFile(String path){
        input = new File(path);
    }

    /***
     * This method read each line of the file and use all the methods
     * according to the line (or "paragraph")
     */
    public void startReading(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("inputs/"+input));
            String line;

            myGraph= new Graph();
            Queries= new ArrayList<>();

            if (!br.readLine().contains("Network")){

                System.out.println("the format of the file is not compatible");
                return;
            }

            while ((line=br.readLine())!= null){
                if (line.contains("Var ")){

                    Variable var = new Variable();
                    CreateVariable(line,br,var);

                    line=br.readLine();
                    while (!(line=br.readLine()).trim().isEmpty()){

                        CreateCPT_line(line,var, var.Parents, var.values);
                    }

                    myGraph.myVariables.put(var.name, var);
                }
                if(line.contains("P(")){

                    CreateQueries(line);
                }
            }
            br.close();

        } catch (FileNotFoundException e) {
            System.out.println("Can't find the file");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Can't read the file");
            e.printStackTrace();
        }
    }

    /**This method take care of init the name, the values and the parents
     * of the Variable.
     * @param line
     * @param var
     * @param br
     */
    public  void CreateVariable(String line, BufferedReader br, Variable var) throws IOException {

        String name= line.substring("Var ".length());
        var.setName(name.trim());

        line=br.readLine();
        String [] arrValues= line.substring("Values: ".length()).split(",");
        for (int i=0;i< arrValues.length;i++){
            arrValues[i]= var.name+"="+arrValues[i].trim();
        }
        ArrayList<String> values= new ArrayList<String>();
        Collections.addAll(values, arrValues);
        var.setValues(values);

        line=br.readLine();
        String [] arrParents= line.substring("Parents: ".length()).split(",");
        ArrayList<Variable> parents= new ArrayList<Variable>();
        for (String parent:arrParents) {
            if (parent.equals("none")) break;
            parents.add(myGraph.myVariables.get(parent.trim()));
        }
        var.setParents(parents);

    }

    /**This method create the queries using
     * String for hypothesis and  ArrayList<String> for the  evidences
     * @param line
     */
    public void CreateQueries (String line ){

        int mode =Integer.parseInt(line.substring(line.lastIndexOf(',')+1).trim());

        String event = line.substring("P(".length(),line.indexOf(')'));

        String hypothesis= event.substring(0,event.indexOf('|')).trim();

        String [] arrEvidences = event.substring(event.indexOf('|')+1).split(",");

        ArrayList<String> evidences= new ArrayList<>();
        Collections.addAll(evidences, arrEvidences);

        Event e = new Event(hypothesis,evidences);

        Queries.add(new Querie(e,mode));
    }

    /**This method init CPTs of each Variables using HashMap<Event/Double>
     * when event is a type of query.
     * @param line
     * @param var
     * @param  values
     * @param parents
     * */
    public void CreateCPT_line(String line , Variable var, ArrayList<Variable> parents,  ArrayList<String> values){

            String [] lineCPT =line.split(",");
            ArrayList<String> evidences= new ArrayList<>();
            int i=0;

            for (Variable parent:parents) {
                evidences.add(parent.name+"="+lineCPT[i++].trim());
            }

            i++;
            double prob=0;
            double complement=1;

            for (String val: values) {
                String hypothesis=val;
                Event e = new Event(hypothesis,evidences);

                if(i<lineCPT.length){
                    prob = Double.parseDouble(lineCPT[i].trim());
                    complement-=prob;
                }
                else
                    prob=complement;
                var.CPT.put(e,prob);
                i+=2;
            }

    }

    public Graph getMyGraph() {
        return myGraph;
    }

    public ArrayList<Querie> getQueries() {
        return Queries;
    }

}

