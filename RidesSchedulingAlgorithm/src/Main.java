
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class Main{

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException, Throwable{
        
//        HeuristicSolution.Data=new InputData("s1");
//        HeuristicSolution.Data=new InputData("s2");
//        HeuristicSolution.Data=new InputData("s3");
//        HeuristicSolution.Data=new InputData("s4");
//        HeuristicSolution.Data=new InputData("s5");
//        HeuristicSolution.Data=new InputData("s6");
//        HeuristicSolution.Data=new InputData("s7");
        HeuristicSolution.Data=new InputData("big1");
//        HeuristicSolution.Data=new InputData("big2");
//        HeuristicSolution.Data=new InputData("big3");
        
        HeuristicSolution s=HeuristicSolution.IterativeLocalSearch(10);
//        HeuristicSolution s=HeuristicSolution.GeneticAlgorithm(10);
        
        s.setOutputFile();            
        s.ShowSolution();
        if(HeuristicSolution.Data.VehiclesCounter<10)
            s.GanttDiagramm(); 
    }
}