
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.RefineryUtilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class HeuristicSolution{
    static InputData Data;
    private static ReentrantLock GlobalLock=new ReentrantLock();
    private AuxiliaryGraph AuxiliaryGraph;
    double Fitness;
    int[] GiantTour;
    VehicleTrip[] VehiclesSchedule;

    static HeuristicSolution IterativeLocalSearch(int Max_Iterations) throws InterruptedException{
        System.out.println("Testing file = "+HeuristicSolution.Data.FileName+".in ");
        System.out.println("Solution approach = Iterative local search");
        System.out.println();
        long RunStartTime=System.currentTimeMillis();
        HeuristicSolution newSolution,BestSolution;
        do{
            BestSolution=new HeuristicSolution();
        }while(!BestSolution.isFeasible());
        System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(BestSolution.Fitness)+" after "+(System.currentTimeMillis()-RunStartTime)+" ms");
        int iteration=0;
        while(iteration<Max_Iterations){
            do{
                newSolution=new HeuristicSolution();
            }while(!BestSolution.isFeasible());
            newSolution.LocalSearch();
            if(newSolution.Fitness>BestSolution.Fitness){
                BestSolution=newSolution;
                System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(BestSolution.Fitness)+" after "+(System.currentTimeMillis()-RunStartTime)+" ms");
            }
            iteration++;
        }
        return BestSolution;
    }
    
    static HeuristicSolution GeneticAlgorithm(int Max_Iterations) throws InterruptedException, Throwable{
        System.out.println("Testing file = "+HeuristicSolution.Data.FileName+".in ");
        System.out.println("Solution approach = Genetic Algorithm");
        System.out.println();
        long RunStartTime=System.currentTimeMillis();
        int PopulationSize=20;
        HeuristicSolution[] population=HeuristicSolution.InitialPopulation(PopulationSize);
        System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(population[0].Fitness)+" after "+(System.currentTimeMillis()-RunStartTime)+" ms");
        Vector<Thread> Threads=new Vector<>();
        for(int iteration=0;iteration<Max_Iterations;iteration++)
            HeuristicSolution.Crossover(population,Threads,RunStartTime);
        Threads.forEach(t->{
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return population[0];
    }
    
    static void Crossover(HeuristicSolution[] population,Vector<Thread> Threads,long RunStartTime) throws InterruptedException, Throwable{
        int half=population.length/2,i=(int)(Math.random()*half),j;
        if(Math.random()<0.7d)
            do{
                j=(int)(Math.random()*half);
            }while(i==j);
        else
            j=half+(int)(Math.random()*(population.length-half));
        HeuristicSolution.Crossover(population,Math.random()<0.1d,RunStartTime,Threads,population[i],population[j]);
    }
    
    private static void Crossover(HeuristicSolution[] population,boolean mutation,long RunStartTime,Vector<Thread> Threads,HeuristicSolution ... parents) throws InterruptedException, Throwable{
        HeuristicSolution SecondChild;
        Thread t;
        boolean crossover_rate=Math.random()<0.8d;
        int n=(int)(HeuristicSolution.Data.RidesCounter*Math.random());
        if(Math.random()<0.7d){
            t=new Thread(()->{
                try{
                    HeuristicSolution FirstChild=(crossover_rate)?parents[0].Crossover(parents[1],mutation,n):new HeuristicSolution();
                    FirstChild.LocalSearch();
                    HeuristicSolution.UpdatePopulation(population,FirstChild,RunStartTime);
                }catch(InterruptedException ex){
                    Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE,null,ex);
                }catch(Throwable ex){
                    Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE,null,ex);
                }
            });
            t.start();
            Threads.addElement(t);
            SecondChild=parents[1].Crossover(parents[0],mutation,n);
            SecondChild.LocalSearch();
        }
        else{
            int p=n+(int)((HeuristicSolution.Data.RidesCounter-n)*Math.random());
            t=new Thread(()->{
                try{
                    HeuristicSolution FirstChild=(crossover_rate)?parents[0].Crossover(parents[1],mutation,n,p):new HeuristicSolution();
                    FirstChild.LocalSearch();
                    HeuristicSolution.UpdatePopulation(population,FirstChild,RunStartTime);
                }catch(InterruptedException ex){
                    Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE,null,ex);
                }catch(Throwable ex){
                    Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE,null,ex);
                }
            });
            t.start();
            Threads.addElement(t);
            SecondChild=parents[1].Crossover(parents[0],mutation,n,p);
            SecondChild.LocalSearch();
        }
        HeuristicSolution.UpdatePopulation(population,SecondChild,RunStartTime);
    }
    
    private HeuristicSolution Crossover(HeuristicSolution Parent,boolean mutation,int ... cut_points) throws InterruptedException{
        int n=(cut_points.length==0)?0:cut_points[0];
        int p=cut_points[(cut_points.length==1)?0:1];
        Vector<Integer> GT=new Vector<>();
        int i=0;
        for(int j=n;j<p;j++)
            GT.addElement(Parent.GiantTour[j]);
        for(int j=p;j<this.GiantTour.length;j++)
            if(!GT.contains(this.GiantTour[j]))
                if(GT.size()<this.GiantTour.length-n)
                    GT.addElement(this.GiantTour[j]);
                else{
                    GT.insertElementAt(this.GiantTour[j],i);
                    i++;
                }
        for(int j=0;j<p;j++)
            if(!GT.contains(this.GiantTour[j]))
                if(GT.size()<this.GiantTour.length-n)
                    GT.addElement(this.GiantTour[j]);
                else{
                    GT.insertElementAt(this.GiantTour[j],i);
                    i++;
                }
        return new HeuristicSolution(GT,mutation);
    }  
    
    private static void Mutation(int[] GiantTour){
        int x=(int)(Math.random()*GiantTour.length);
        int y=(int)(Math.random()*GiantTour.length);
        new Motion(Math.min(x,y),Math.max(x,y))._2opt(GiantTour);
    }
    
    private HeuristicSolution(Vector<Integer> GT,boolean mutation) throws InterruptedException{
        this.GiantTour=GT.stream().flatMapToInt(x->IntStream.of(x)).toArray();
        if(mutation)
            HeuristicSolution.Mutation(this.GiantTour);
        this.Split();
    }
    
    static void UpdatePopulation(HeuristicSolution[] population,HeuristicSolution newSolution,long RunStartTime) throws Throwable{
        HeuristicSolution.GlobalLock.lock();
        try{
            if(newSolution.isFeasible() && newSolution.Fitness>population[population.length-1].Fitness){
                int half=population.length/2;
                int i=half+(int)(Math.random()*(population.length-half));
                population[i].finalize();
                population[i]=newSolution;
                if(newSolution.Fitness>population[0].Fitness)
                    System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(newSolution.Fitness)+" after "+(System.currentTimeMillis()-RunStartTime)+" ms");
                Arrays.sort(population,(s1,s2)->s1.Compare(s2));
            }
            else
                newSolution.finalize();
        }finally{
            HeuristicSolution.GlobalLock.unlock();
        }
    }
    
    static HeuristicSolution[] InitialPopulation(int PopulationSize) throws InterruptedException, Throwable{
        HeuristicSolution[] population=new HeuristicSolution[PopulationSize];
        System.out.println("Initial population");
        int i=0;
        while(i<PopulationSize){
            do{
                if(population[i]!=null)
                    population[i].finalize();
                population[i]=new HeuristicSolution();
            }while(!population[i].isFeasible());
            System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(population[i].Fitness));
            i++;
        }
        Arrays.sort(population,(s1,s2)->s1.Compare(s2));
        return population;
    }

    void LocalSearch() throws InterruptedException{
        if(!this.isFeasible())
            return;
        this.LocalSearch(0,1);
        this.GiantTour=this.getGiantTour();
    }
    
    void LocalSearch(int i,int j){
        try{
            if(this.VehiclesSchedule[i]!=null && this.VehiclesSchedule[j]!=null){
                VehicleTrip vt1=new VehicleTrip(i,this.VehiclesSchedule[j].RidesSequence);
                VehicleTrip vt2=new VehicleTrip(j,this.VehiclesSchedule[i].RidesSequence);
                if(vt1.IsFeasible() && vt2.IsFeasible()
                        && vt1.WastedTime+vt2.WastedTime<this.VehiclesSchedule[i].WastedTime+this.VehiclesSchedule[j].WastedTime){
                    this.Fitness-=vt1.WastedTime+vt2.WastedTime;
                    this.Fitness+=this.VehiclesSchedule[i].WastedTime+this.VehiclesSchedule[j].WastedTime;
                    this.VehiclesSchedule[i]=vt1;
                    this.VehiclesSchedule[j]=vt2;
                    if(j+1<this.VehiclesSchedule.length)
                        this.LocalSearch(i,j+1);
                    else if(i+2<this.VehiclesSchedule.length)
                        this.LocalSearch(i+1,i+2);
                    return;
                }
            }
            else if(this.VehiclesSchedule[i]==null && this.VehiclesSchedule[j]!=null){
                VehicleTrip vt=new VehicleTrip(i,this.VehiclesSchedule[j].RidesSequence);
                if(vt.IsFeasible() && vt.WastedTime<this.VehiclesSchedule[j].WastedTime){
                    this.Fitness+=this.VehiclesSchedule[j].WastedTime-vt.WastedTime;
                    this.VehiclesSchedule[i]=vt;
                    this.VehiclesSchedule[j]=null;
                    if(j+1<this.VehiclesSchedule.length)
                        this.LocalSearch(i,j+1);
                    else if(i+2<this.VehiclesSchedule.length)
                        this.LocalSearch(i+1,i+2);
                    return;
                }
            }
            else if(this.VehiclesSchedule[i]!=null && this.VehiclesSchedule[j]==null){
                VehicleTrip vt=new VehicleTrip(j,this.VehiclesSchedule[i].RidesSequence);
                if(vt.IsFeasible() && vt.WastedTime<this.VehiclesSchedule[i].WastedTime){
                    this.Fitness+=this.VehiclesSchedule[i].WastedTime-vt.WastedTime;
                    this.VehiclesSchedule[i]=null;
                    this.VehiclesSchedule[j]=vt;
                    if(j+1<this.VehiclesSchedule.length)
                        this.LocalSearch(i,j+1);
                    else if(i+2<this.VehiclesSchedule.length)
                        this.LocalSearch(i+1,i+2);
                    return;
                }
            }
            if(j+1<this.VehiclesSchedule.length)
                this.LocalSearch(i,j+1);
            else if(i+2<this.VehiclesSchedule.length)
                this.LocalSearch(i+1,i+2);
        }catch(java.lang.StackOverflowError e){}
    }
    
    public HeuristicSolution() throws InterruptedException{
        this.GiantTour=HeuristicSolution.getRandomGiantTour();
        this.Split();
    }
    
    private static int[] getRandomGiantTour() throws InterruptedException{
        int[] array=IntStream.range(0,HeuristicSolution.Data.RidesCounter).toArray();
        IntStream.range(0,HeuristicSolution.Data.RidesCounter).forEach(i->new Motion(i,(int)(Math.random()*array.length)).Swap(array));
        return array;
    }
    
    private void Split() throws InterruptedException{
        this.AuxiliaryGraph=new AuxiliaryGraph(this.GiantTour);
        this.Fitness=this.AuxiliaryGraph.getLabel();
        this.VehiclesSchedule=this.AuxiliaryGraph.getVehiclesSchedule();
    }

    void ShowSolution(){
        System.out.println();
        System.out.println(HeuristicSolution.TwoDigitAfterFloatingPoint(this.Fitness));
        for(VehicleTrip vehicle_trip:this.AuxiliaryGraph.getVehiclesSchedule())
            System.out.println((vehicle_trip==null)?"0":vehicle_trip.toString());
    }

    void setOutputFile() throws IOException{
        String FileName=HeuristicSolution.Data.FileName;
        try(BufferedWriter OutputFile=new BufferedWriter(new FileWriter("outputs\\"+FileName+"("+Double.toString(HeuristicSolution.TwoDigitAfterFloatingPoint(this.Fitness))+").txt"))){
            OutputFile.write(Double.toString(HeuristicSolution.TwoDigitAfterFloatingPoint(this.Fitness)));
            OutputFile.newLine();
            for(VehicleTrip vehicle_trip:this.AuxiliaryGraph.getVehiclesSchedule()){
                OutputFile.write((vehicle_trip==null)?"0":vehicle_trip.toString());
                OutputFile.newLine();
            }
        }
    }
    
    void GanttDiagramm(){
        GanttDemoCollection demo=new GanttDemoCollection("Gantt Diagram",this);
        demo.pack();
        demo.setExtendedState(Frame.MAXIMIZED_BOTH);
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);          
    }
    
    IntervalCategoryDataset createDataset(){
        TaskSeriesCollection collection=new TaskSeriesCollection();
        int ride,vehicle;
        for(VehicleTrip vehicle_trip:this.AuxiliaryGraph.getVehiclesSchedule()){
            if(vehicle_trip==null)
                continue;
            vehicle=vehicle_trip.Vehicle;
            ride=vehicle_trip.RidesSequence[0];
            TaskSeries ts=new TaskSeries("G->R"+(1+ride));
            ts.add(new Task("Vehicle "+(1+vehicle),new Date(HeuristicSolution.Data.PickupTime[ride]-(int)(HeuristicSolution.Data.FromGarageTravelTime(ride,vehicle))),new Date(HeuristicSolution.Data.PickupTime[ride]))); 
            collection.add(ts);
            for(int r=0;r<vehicle_trip.Length;r++){
                ride=vehicle_trip.RidesSequence[r];
                TaskSeries ts1=new TaskSeries("R"+(1+ride));
                ts1.add(new Task("Vehicle "+(1+vehicle),new Date(HeuristicSolution.Data.PickupTime[ride]),new Date(HeuristicSolution.Data.DropOffTime[ride]))); 
                collection.add(ts1);
                if(r+1<vehicle_trip.Length){
                    TaskSeries ts2=new TaskSeries("R"+(1+ride)+"->R"+(1+vehicle_trip.RidesSequence[r+1]));
                    ts2.add(new Task("Vehicle "+(1+vehicle),new Date(HeuristicSolution.Data.DropOffTime[ride]),new Date(HeuristicSolution.Data.DropOffTime[ride]+(int)(HeuristicSolution.Data.TravelTime(ride,vehicle_trip.RidesSequence[r+1]))))); 
                    collection.add(ts2);
                }
                else{
                    TaskSeries ts2=new TaskSeries("R"+(1+ride)+"->G");
                    ts2.add(new Task("Vehicle "+(1+vehicle),new Date(HeuristicSolution.Data.DropOffTime[ride]),new Date(HeuristicSolution.Data.DropOffTime[ride]+(int)(HeuristicSolution.Data.ToGarageTravelTime(ride,vehicle))))); 
                    collection.add(ts2);
                }
            }
        }
        return collection;
    }
    
    int[] getRidesAssignmentArray(){
        int[] array=new int[HeuristicSolution.Data.RidesCounter];
        for(VehicleTrip trip:this.VehiclesSchedule){
            if(trip==null)
                continue;
            for(int ride:trip.RidesSequence)
                array[ride]=trip.Vehicle;
        }
        return array;
    }

    int[] getGiantTour(){
        int i=0;
        int[] giant_tour=new int[this.GiantTour.length];
        Vector<Integer> v=new Vector<>();
        for(VehicleTrip trip:this.VehiclesSchedule){
            if(trip==null)
                continue;
            IntStream.of(trip.RidesSequence).forEach(r->v.addElement(r));
            System.arraycopy(trip.RidesSequence,0,giant_tour,i,trip.RidesSequence.length);
            i+=trip.RidesSequence.length;
        }
        int ride;
        for(int j=0;i<this.GiantTour.length && j<this.GiantTour.length;j++){
            ride=giant_tour[j];
            if(v.contains(ride))
                continue;
            giant_tour[i]=ride;
            i++;
        }
        return giant_tour;
    }
    
    public static double TwoDigitAfterFloatingPoint(double x){
        double y=100*x;
        return ((int)y)/100d;
    }

    private boolean isFeasible(){
        return this.AuxiliaryGraph.isFeasible();
    }
    
    int Compare(HeuristicSolution s){
        return (int)(s.Fitness*100d-this.Fitness*100d);
    }
}
