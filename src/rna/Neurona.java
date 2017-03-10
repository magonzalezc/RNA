package rna;
import java.lang.*;
import java.util.*;
import java.io.*;



public class Neurona{
	
public double out;//salida de la neurona (0..1)
public double derivada;//derivada de la funcion de activacion dadas sus entradas

public int tipofact;//tipo de funcion de activacion
	//0 sigmoide
	//1 tanh
	//2 gausiana
		
	
//solo si es de tipo ENTRADA se usa este parametro
public double in; //entrada de la neurona


public Vector sinapsis_entrada;
public Vector sinapsis_salida;
	
private String tipo;
	
private double potencial_post_sinaptico;
private static boolean TRAZAS_ACTIVAS=true;
//-----------------------------------------	
//constructor
public Neurona(String tipo){
	//tipo: "ENTRADA", "OCULTA", "FINAL", "BIAS"
	this.tipo=new String(tipo);
	sinapsis_entrada=new Vector();
	sinapsis_salida=new Vector();
	
	tipofact=0;
	//tipofact=(int)(Math.random()*1.0 + 0.5);
	//traza("tipo:"+tipofact);
	

}//end constructor
//-----------------------------------------	

public void run(){
	//esta funcion calcula la salida (miembro out)
	if (tipo.equals("ENTRADA"))
	{
	out=in;	
	derivada=1;
	}
	else if (tipo.equals("OCULTA"))
	{
	fCombinacion();
	fActivacion();	
	}
	else if (tipo.equals("BIAS"))
	{
		
		in=1.0;
		out=in;
		derivada=1;
	}
	else if (tipo.equals("SALIDA"))
	{
	fCombinacion();
	fActivacion();	
	}
	//traza ("run tipo:"+tipo+" out:"+out);
}
//-----------------------------------------	
//esta funcion calcula el potencial postsinaptico
//a traves de la regla de propagacion (tb llamada funcion de combinacion)

private void fCombinacion(){

potencial_post_sinaptico=0;
for (Iterator it=sinapsis_entrada.iterator();it.hasNext();)
	{
		Sinapsis s=(Sinapsis)it.next();
		potencial_post_sinaptico+=s.pre.out*s.w;
	
	}
	
}//end fCombinacion
//-----------------------------------------	

private void fActivacion(){
	  //sigmoide
	  if (tipofact==0)
	{
	 out=1/(1.0+Math.exp(-1*	potencial_post_sinaptico));
	 derivada=(1.0-out)*out;
	}
 else if (tipofact==2)
	{
  // gausiana   
   out=1/Math.exp(potencial_post_sinaptico);
   derivada=(-1)*out;;
	}
 else if (tipofact==1)
	{
   
    // tanh
    //-----
	
	double a=Math.exp(potencial_post_sinaptico);
     double b=1.0/Math.exp(potencial_post_sinaptico);
	out=(a-b)/(a+b);
     derivada= (1.0-out*out);
     out=(out+1.0)/2.0;
     
	}
	
	 
	}
//--------------------------------------------------------		
public static  void traza(String cad)
	{
	if (TRAZAS_ACTIVAS)
		{	
	System.out.println("");	
	GregorianCalendar calendar=new GregorianCalendar();
	String hh=new String(""+calendar.get(Calendar.HOUR));
	String mm=new String(""+calendar.get(Calendar.MINUTE));
  String ss=new String(""+calendar.get(Calendar.SECOND));

	System.out.print("["+hh+":"+mm+":"+ss+"]"+"  "+cad);
	System.out.flush();	
	}//end traza
	}

	
}//end class neurona