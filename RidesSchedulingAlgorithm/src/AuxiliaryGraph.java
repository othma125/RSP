
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class AuxiliaryGraph{
    private final int Length;
    private final int[] GiantTour;
    private final AuxiliaryGraphNode[] Nodes;
    private int ThreadsCounter;

    AuxiliaryGraph(int[] giant_tour) throws InterruptedException{
        this.GiantTour=giant_tour;
        this.ThreadsCounter=0;
        this.Length=HeuristicSolution.Data.RidesCounter;
        this.Nodes=new AuxiliaryGraphNode[this.Length+1]; 
        for(int i=0;i<this.Nodes.length;i++)
            this.Nodes[i]=new AuxiliaryGraphNode(i);
        this.setArcs();
    }

    AuxiliaryGraphNode getLastNode(){
        return this.Nodes[this.Length];
    }

    VehicleTrip[] getVehiclesSchedule(){
        return this.getLastNode().getVehiclesSchedule();
    }

    VehicleTrip getVehicleSchedule(int vehicle){
        return this.getVehiclesSchedule()[vehicle];
    }

    double getLabel() {
        return this.getLastNode().getLabel();
    }

    boolean isFeasible() {
        return this.getLastNode().isFeasible();
    }

    private void setArcs() throws InterruptedException{
        Thread t=new Thread(()->this.setEdges(this.Nodes[0]));
        this.ThreadsCounter++;
        t.start();
        t.join();
    }

    void StartThread(AuxiliaryGraphNode node,Vector<Thread> Threads){
        for(int i=(node.isFeasible())?node.Posterior.NodeIndex:0;i<node.NodeIndex;i++)
            if(this.Nodes[i].NodeProcessingWith<node.NodeIndex)
                return;
        boolean c;
        node.Lock.lock();
        try{
            c=this.ThreadsCounter==node.NodeIndex;
            if(c){
                this.ThreadsCounter++;
                if(!node.isFeasible()){
                    node.VehiclesSchedule=this.Nodes[node.NodeIndex-1].VehiclesSchedule;
                    node.Posterior=this.Nodes[node.NodeIndex-1].Posterior;
                    node.Label=this.Nodes[node.NodeIndex-1].Label;
                }
                Thread t=new Thread(()->this.setEdges(node));
                Threads.addElement(t);
                t.start();
            }
        }finally{
            node.Lock.unlock();
        }
        if(c && this.ThreadsCounter<this.Length)
            this.StartThread(this.Nodes[this.ThreadsCounter],Threads);
    }
    
    void setEdges(AuxiliaryGraphNode StartingNode){
        AuxiliaryGraphNode EndingNode;
        Vector<Thread> Threads=new Vector<>();
        int rides_counter=1;
        int current_ride;
        for(int NodeIndex=StartingNode.NodeIndex;rides_counter<=HeuristicSolution.Data.MaxRidesPerVehicle && NodeIndex<this.Length;NodeIndex++){
            EndingNode=this.Nodes[NodeIndex+1];
            current_ride=this.GiantTour[NodeIndex];
            int[] CurrentSequence=IntStream.range(StartingNode.NodeIndex,EndingNode.NodeIndex).map(ride_index->this.GiantTour[ride_index]).toArray();
            for(int vehicle=0;vehicle<HeuristicSolution.Data.VehiclesCounter;vehicle++){
                VehicleTrip VehicleTrip=StartingNode.getVehicleSchedule(vehicle);
                if(VehicleTrip!=null && (VehicleTrip.Length+rides_counter>HeuristicSolution.Data.MaxRidesPerVehicle || (rides_counter>1 && HeuristicSolution.Data.DropOffTime[VehicleTrip.getLastRide()]+HeuristicSolution.Data.TravelTime(VehicleTrip.RidesSequence,CurrentSequence)>HeuristicSolution.Data.PickupTime[CurrentSequence[0]])))
                    continue;
                VehicleTrip newVehicleTrip=new VehicleTrip(vehicle,(VehicleTrip==null)?CurrentSequence.clone():IntStream.range(0,VehicleTrip.Length+rides_counter).map(x->(x<VehicleTrip.Length)?VehicleTrip.RidesSequence[x]:CurrentSequence[x-VehicleTrip.Length]).toArray());
                if((VehicleTrip==null || HeuristicSolution.Data.DropOffTime[VehicleTrip.getLastRide()]+HeuristicSolution.Data.TravelTime(VehicleTrip.RidesSequence,CurrentSequence)<=HeuristicSolution.Data.PickupTime[CurrentSequence[0]]) && newVehicleTrip.IsFeasible())
                    EndingNode.UpdateLabel(StartingNode,newVehicleTrip,vehicle);
                for(int r=0;VehicleTrip!=null && rides_counter==1 && r<VehicleTrip.Length;r++){
                    int ride=VehicleTrip.RidesSequence[r];
                    if((r==0 || HeuristicSolution.Data.PickupTime[current_ride]>=HeuristicSolution.Data.DropOffTime[VehicleTrip.RidesSequence[r-1]]+HeuristicSolution.Data.TravelTime(VehicleTrip.RidesSequence[r-1],current_ride))
                            && HeuristicSolution.Data.PickupTime[ride]>=HeuristicSolution.Data.DropOffTime[current_ride]+HeuristicSolution.Data.TravelTime(current_ride,ride)){
                        int[] newSequence=new int[VehicleTrip.Length+1];
                        newSequence[VehicleTrip.Length]=current_ride;
                        System.arraycopy(VehicleTrip.RidesSequence,0,newSequence,0,VehicleTrip.Length);
                        new Motion(r,VehicleTrip.Length).Insertion(newSequence);
                        VehicleTrip VT=new VehicleTrip(vehicle,newSequence);
                        if(VT.IsFeasible())
                            EndingNode.UpdateLabel(StartingNode,VT,vehicle);
                    }
                }
            }
            StartingNode.NodeProcessingWith++;
            this.StartThread(EndingNode,Threads);
            rides_counter++;
            if(NodeIndex+1<this.Length && HeuristicSolution.Data.DropOffTime[current_ride]+HeuristicSolution.Data.TravelTime(current_ride,this.GiantTour[NodeIndex+1])>HeuristicSolution.Data.PickupTime[this.GiantTour[NodeIndex+1]])
                break;
        }
        StartingNode.NodeProcessingWith=this.Length;
        Threads.forEach(t->{
            try{
                t.join();
            }catch(InterruptedException ex){
                Logger.getLogger(HeuristicSolution.class.getName()).log(Level.SEVERE,null,ex);
            }
        });
    }
}