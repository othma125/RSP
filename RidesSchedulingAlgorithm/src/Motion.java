
import java.util.Vector;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
public class Motion {
    private final int Id1,Id2;
    
    Motion(int a,int b){
        this.Id1=a;
        this.Id2=b;
    }
    
    void Swap(int[] array){
        if(this.Id1==this.Id2)
           return;
        int aux=array[this.Id1];
        array[this.Id1]=array[this.Id2];
        array[this.Id2]=aux;
    }  
    
    void Swap(Vector v){
        if(this.Id1==this.Id2)
            return;
        Object aux=v.elementAt(this.Id1);
        v.set(this.Id1,v.elementAt(this.Id2));
        v.set(this.Id2,aux);
    }  
    
    void _2opt(int[] array){
        if(this.Id1<this.Id2)
            for(int k=this.Id1,l=this.Id2;k<l;k++,l--)
                new Motion(k,l).Swap(array);
    }
    
    void Swap(HeuristicSolution[] array){
        if(this.Id1==this.Id2)
            return;
        HeuristicSolution aux=array[this.Id1];
        array[this.Id1]=array[this.Id2];
        array[this.Id2]=aux;   
    }
    
    void Insertion(int[] array){
        if(this.Id1<this.Id2){
            int aux=array[this.Id2];
            for(int k=this.Id2;k>this.Id1;k--)
                array[k]=array[k-1];        
            array[this.Id1]=aux;
        }
    }
}