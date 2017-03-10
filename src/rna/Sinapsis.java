package rna;
import java.lang.*;
import java.util.*;
import java.io.*;


public class Sinapsis{
//esta clase modela la sinapsis entre dos neuronas de cualquier tipo
double w; //peso sinaptico
Neurona pre;// presinaptica;
Neurona post;//postsinaptica;


//------------------------------------------

//constructor
public Sinapsis(Neurona pre, Neurona post,double w)
	{
		this.w=w;
		this.pre=pre;
		this.post=post;
		
	}
//------------------------------------------
	

}//end class sinapsis