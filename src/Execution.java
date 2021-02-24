import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Execution {

    public static void main(String[] args){
        Scanner scanner=new Scanner(System.in);
        System.out.println("The inputs options are: input0, input1, ... input8");
        System.out.println("Please insert the name of the bayesian network you want to run:  ");

        String fileName=scanner.nextLine();
        fileName+=".txt";

        ReadFile Network= new ReadFile();
        Network.initFile(fileName);
        Network.startReading();

        Graph myGraph= Network.getMyGraph();

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("output.txt"));

            for (Querie query: Network.getQueries()) {
                String answerLine;

                switch (query.mode){
                    case 1:
                        Mode1 mode1 =new Mode1(myGraph,query);
                        answerLine=mode1.CalculateQuery()+","+mode1.additions+","+mode1.multiplications;
                        break;
                    default:
                        Mode2 mode2 =new Mode2(myGraph,query);
                        answerLine=mode2.CalculateQuery()+","+mode2.additions+","+mode2.multiplications;
                        break;

                }
                writer.write(answerLine + "\n");
            }


            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Look at the output.txt file");
    }
}
