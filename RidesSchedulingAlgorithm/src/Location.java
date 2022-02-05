/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class Location{
    public double Latitude;
    public double Longitude;
    
    public Location(double x,double y){
        this.Latitude=x;
        this.Longitude=y;
    }
    
    double getHaversineDistance(Location location){
         int R=6371; // Radious of the earth in kilometer
         double latDistance=toRad(location.Latitude-this.Latitude);
         double lonDistance=toRad(location.Longitude-this.Longitude);
         double a=Math.sin(latDistance/2)*Math.sin(latDistance/2)+Math.cos(toRad(this.Latitude))*Math.cos(toRad(location.Latitude))*Math.sin(lonDistance/2)*Math.sin(lonDistance/2);
         double c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
         return R*c;//distance in Kms
    }
    
    private static double toRad(double value){
         return value*Math.PI/180;
    }
}