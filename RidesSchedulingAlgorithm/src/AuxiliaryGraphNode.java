
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class AuxiliaryGraphNode{
    public final int NodeIndex;
    public int NodeProcessingWith;
    public double Label;
    public AuxiliaryGraphNode Posterior;
    public VehicleTrip[] VehiclesSchedule;
    public final ReentrantLock Lock;
    
    AuxiliaryGraphNode(int NodeIndex){
        this.NodeIndex=NodeIndex;
        this.NodeProcessingWith=NodeIndex;
        this.Label=Double.NEGATIVE_INFINITY;
        this.Lock=new ReentrantLock();
        if(this.NodeIndex==0){
            this.VehiclesSchedule=new VehicleTrip[HeuristicSolution.Data.VehiclesCounter];
            this.Posterior=null;
            this.Label=0d;
        }
    }
    
    void UpdateLabel(AuxiliaryGraphNode Posterior,VehicleTrip RA,int vehicle){
        this.Lock.lock();
        try{
            double label=Posterior.Label;
            label-=60*((Posterior.VehiclesSchedule[vehicle]!=null)?Posterior.VehiclesSchedule[vehicle].Length:0d);
            label+=(Posterior.VehiclesSchedule[vehicle]!=null)?Posterior.VehiclesSchedule[vehicle].WastedTime:0d;
            label+=60*RA.Length;
            label-=RA.WastedTime;
            if(label>this.Label){
                this.Label=label;
                this.Posterior=Posterior;
                this.VehiclesSchedule=new VehicleTrip[HeuristicSolution.Data.VehiclesCounter];
                IntStream.range(0,HeuristicSolution.Data.VehiclesCounter).forEach(i->this.VehiclesSchedule[i]=(i==vehicle)?RA:this.Posterior.VehiclesSchedule[i]);
            }
        }
        finally{
            this.Lock.unlock();
        }
    }
    
    boolean isFeasible(){
        return this.Posterior!=null;
    }

    VehicleTrip[] getVehiclesSchedule(){
        return this.VehiclesSchedule;
    }

    VehicleTrip getVehicleSchedule(int vehicle){
        return this.getVehiclesSchedule()[vehicle];
    }

    double getLabel() {
        return this.Label;
    }
}