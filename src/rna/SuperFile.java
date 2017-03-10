package rna;

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//
//    Jose Javier 2000
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
import java.lang.*;
import java.util.*;
import java.io.*;

public class SuperFile{
	
public String nombre_file_output;//nombre del fichero de salida
public FileWriter file_output;   // fichero de salida	
public String nombre_file_input; //nombre del fichero 
public BufferedReader file_input;// fichero	input


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
public void openWrite(String nombre) 
{	
nombre_file_output=new String(nombre);
	try
		{
	    file_output=new FileWriter(nombre_file_output);
	  }catch (Exception e)
	  	{
	  		System.out.println("no se pudo crear el fichero");
	  		System.out.println(e);
	  		System.exit(0);
	  	}
}
public void openWrite(String nombre, boolean append) 
{	
nombre_file_output=new String(nombre);
	try
		{
	    file_output=new FileWriter(nombre_file_output,append);
	  }catch (Exception e)
	  	{
	  		System.out.println("no se pudo crear el fichero");
	  		System.out.println(e);
	  		System.exit(0);
	  	}
}


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
public void openRead(String nombre)
	{
	nombre_file_input=new String(nombre);
	//System.out.println("fichero :"+nombre);
	try
		{
	    file_input=new BufferedReader(new FileReader(nombre_file_input));
	    //System.out.println("file input preparado:"+file_input.ready());
		}
		catch (Exception e)
	  	{
	  		System.out.println("error al abrir el fichero "+nombre);
	  		System.out.println(e);
	  		System.exit(0);
	  	}
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
public String readLine()
	{
		String lee;
		try{
		lee=	file_input.readLine();
		}
	catch (Exception e)
	  	{
	  		return null;
	  	}
	  	return lee;
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void writeLine(String cadena)
	{
	try{
   file_output.write(cadena);
   file_output.write(13);
   file_output.write(10);
   file_output.flush();
}
catch(Exception e)
	{
	System.out.println("no se pudo escribir en el fichero");
	System.out.println(e);
	System.exit(0);	
	}
	
		
	}	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public void writeString(String cadena)
	{
	  try{
      file_output.write(cadena);
      file_output.flush();
    }
    catch(Exception e)
	  {
	    System.out.println("no se pudo escribir en el fichero");
	    System.out.println(e);
	    System.exit(0);	
	  }
		
	}	
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	public long getNumLines()
	{
	  long i=0;//contador	
	  String cad=new String("");
    while (cad!=null)
   	{
  		cad=readLine();
  		if (cad!=null) i+=1;
  	}
  	//System.out.println("leidas todas las lineas de "+this.nombre_file_input);
    closeRead();
    
    openRead(this.nombre_file_input);
	  return i;	
		
	}		
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	public void closeRead()
	{
		
		try{
		 file_input.close();
		}
	  catch (Exception e)
		{
	  		
		}
	  	
	}
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%	
	public void closeWrite()
	{
		
		try{
		 file_output.close();
		}
	  catch (Exception e)
	  {
	  		
	  }
	
	}
	//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
}