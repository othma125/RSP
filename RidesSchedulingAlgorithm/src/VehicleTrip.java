/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class VehicleTrip {
    final int Vehicle;
    final int Length;
    final int[] RidesSequence;
    double WastedTime;

    VehicleTrip(int vehicle,int[] sequence) {
        this.Vehicle=vehicle;
        this.RidesSequence=sequence;
        this.Length=this.RidesSequence.length;
    }
    
    boolean IsFeasible(){
        if(this.Length>HeuristicSolution.Data.MaxRidesPerVehicle)
            return false;
        int ride=this.RidesSequence[0];
        double time=HeuristicSolution.Data.WorkingDayStartTime;
        time+=HeuristicSolution.Data.FromGarageTravelTime(ride,this.Vehicle);
        this.WastedTime=3*HeuristicSolution.Data.FromGarageTravelTime(ride,this.Vehicle)*HeuristicSolution.Data.VehicleSpeed;
        for(int r=0;r<this.Length;r++){
            ride=this.RidesSequence[r];
            if(time<=HeuristicSolution.Data.PickupTime[ride]){
                if(r>0)
                    this.WastedTime+=HeuristicSolution.Data.PickupTime[ride]-time;
                time=HeuristicSolution.Data.DropOffTime[ride];
            }
            else
                return false;
            if(r+1<this.Length){
                time+=HeuristicSolution.Data.TravelTime(ride,this.RidesSequence[r+1]);
                this.WastedTime+=3*HeuristicSolution.Data.TravelTime(ride,this.RidesSequence[r+1])*HeuristicSolution.Data.VehicleSpeed;
            }
            else{
                time+=HeuristicSolution.Data.ToGarageTravelTime(ride,this.Vehicle);
                this.WastedTime+=3*HeuristicSolution.Data.ToGarageTravelTime(ride,this.Vehicle)*HeuristicSolution.Data.VehicleSpeed;
            }
        }
        if(time>HeuristicSolution.Data.WorkingDayEndTime)
            return false;
        return true;
    }
    
    @Override
    public String toString(){
        String str="";
        for(int ride:this.RidesSequence)
            str+=(1+ride)+" ";
        return this.Length+" "+str;
    }
    
    int getFirstRide(){
        return this.RidesSequence[0];
    }
    
    int getLastRide(){
        return this.RidesSequence[this.Length-1];
    }
}
