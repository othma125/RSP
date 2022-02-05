
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class InputData{
    public final String FileName;
    public final int RidesCounter;
    public final int VehiclesCounter;
    public final int MaxRidesPerVehicle=4;
    public final int WorkingDayStartTime=360;
    public final int WorkingDayEndTime=1200;
    public final double VehicleSpeed=60/60;//expressed in Km/minutes
    private final Location[] LocationsSet;
    public final int[] PickupTime;
    public final int[] DropOffTime;
    private final double[][] DistanceMatrix;
    
    public InputData(String file_name){
        this.FileName=file_name;
        Scanner scanner=null;
        try{
            scanner=new Scanner(new FileInputStream("inputs\\"+this.FileName+".in"));
        }catch(FileNotFoundException e){
            System.out.println(this.FileName+" file is missing");
            System.exit(0); 
        }
        String line=scanner.nextLine();
        StringTokenizer stk=new StringTokenizer(line);
        this.RidesCounter=Integer.valueOf(stk.nextToken());
        this.VehiclesCounter=Integer.valueOf(stk.nextToken());
        this.LocationsSet=new Location[2*this.RidesCounter+this.VehiclesCounter];
        this.PickupTime=new int[this.RidesCounter];
        this.DropOffTime=new int[this.RidesCounter];
        int i=0;
        while(i<this.RidesCounter){
            line=scanner.nextLine();
            stk=new StringTokenizer(line);
            this.LocationsSet[2*i]=new Location(Double.valueOf(stk.nextToken()),Double.valueOf(stk.nextToken()));//pickup location
            this.PickupTime[i]=Integer.valueOf(stk.nextToken());
            this.LocationsSet[2*i+1]=new Location(Double.valueOf(stk.nextToken()),Double.valueOf(stk.nextToken()));//drop-off location
            this.DropOffTime[i]=Integer.valueOf(stk.nextToken());
            i++;
        }
        i=0;
        while(i<this.VehiclesCounter){
            line=scanner.nextLine();
            stk=new StringTokenizer(line);
            this.LocationsSet[2*this.RidesCounter+i]=new Location(Double.valueOf(stk.nextToken()),Double.valueOf(stk.nextToken()));
            i++;
        }
        scanner.close();
        this.DistanceMatrix=new double[2*this.RidesCounter+this.VehiclesCounter][2*this.RidesCounter+this.VehiclesCounter];
    }
    
    double TravelTime(int ride1,int ride2){
        return this.getTravelTime(2*ride1+1,2*ride2);
    }
    
    double TravelTime(int[] trip1,int[] trip2){
        return this.TravelTime(trip1[trip1.length-1],trip2[0]);
    }
    
    double FromGarageTravelTime(int ride,int vehicle){
        return this.getTravelTime(2*this.RidesCounter+vehicle,2*ride);
    }
    
    double ToGarageTravelTime(int ride,int vehicle){
        return this.getTravelTime(2*ride+1,2*this.RidesCounter+vehicle);
    }
    
    private double getTraveledDistance(int location1,int location2){
        if(this.DistanceMatrix[location1][location2]==0d)
            this.DistanceMatrix[location1][location2]=this.DistanceMatrix[location2][location1]=this.LocationsSet[location1].getHaversineDistance(this.LocationsSet[location2]);
        return this.DistanceMatrix[location1][location2];
    }
    
    private double getTravelTime(int location1,int location2){
        return this.getTraveledDistance(location1,location2)/this.VehicleSpeed;
    }
}