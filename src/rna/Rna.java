/*********************************************
 * RNA: Red Neuronal Artificial              *
 *                                           *
 * Jose Javier 2004                          *
 *********************************************/
package rna;
import java.awt.Image;
import java.lang.*;
import java.util.*;
import java.io.*;

//esta clase modela una red neuronal del tipo MLP (Multi-Layer Perceptron)
//MLP siempre es no recurrente y con funcion de activacion sigmoide
//el aprendizaje es por retro-propagacion
//este programa puede construir cualquier configuracion de MLP y salvarla en
//un fichero de properties
//tambien puede cargar una red neuronal ya calculada de un fichero de properties

public class Rna{

	//arquitectura de la red
	public int num_neuronas_entrada;
	public int num_neuronas_oculta;
	public int num_neuronas_salida;

	public Neurona[] capa_entrada;
	public Sinapsis[][] entrada_oculta;
	public Neurona[] capa_oculta;

	//para 4 capas
	public int num_capas_ocultas;
	public Sinapsis[][] oculta_oculta2;
	public Neurona[] capa_oculta2;
	public Sinapsis[][] oculta2_salida;
	// end 4 capas

	public Sinapsis[][] oculta_salida;
	public Neurona[] capa_salida;

	public double ritmo_aprendizaje=0.0001;
	public double aumento_ritmo=ritmo_aprendizaje/1000.0f;//0.0000001f;



	//pesos , pueden definirse inicialmente aunque 0 esta bien
	public double PESO_INICIAL=0;

	//variable auxiliar
	public double varianza=0;
	public static boolean TRAZAS_ACTIVAS=false;


	//-----------------------------------------------------------------  
	public void loadRna(String fichero_setup_rna) throws Exception
	{
		//esta funcion sera capaz de iniciar una red neuronal 
		//a partir de un fichero de pesos precalculado  

		//abrimos el fichero de properties de la RNA
		File propFile=new File(fichero_setup_rna);
		Properties prop=new Properties();
		try{
			prop.load(new FileInputStream(propFile));
		}catch(Exception e){System.out.println(e);
		throw e;
		//System.exit(0);
		}
		int num_capa_entrada=Integer.valueOf(prop.getProperty("NUM_CAPA_ENTRADA")).intValue();
		int num_capa_oculta=Integer.valueOf(prop.getProperty("NUM_CAPA_OCULTA")).intValue();
		int num_capa_salida=Integer.valueOf(prop.getProperty("NUM_CAPA_SALIDA")).intValue();

		//instanciamos las neuronas
		capa_entrada=new Neurona[num_capa_entrada+1];
		capa_entrada[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_entrada;i++) capa_entrada[i]=new Neurona("ENTRADA");

		//capa oculta
		capa_oculta=new Neurona[num_capa_oculta+1];
		capa_oculta[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_oculta;i++) capa_oculta[i]=new Neurona("OCULTA");


		//capa salida
		//ahora no hay BIAS pero uso el 1 para empezar a numerar, por coherencia
		capa_salida=new Neurona[num_capa_salida+1];
		for(int i=1;i<=num_capa_salida;i++) capa_salida[i]=new Neurona("SALIDA");

		//instanciamos las sinapsis con los pesos del fichero
		//capa_entrada--->capa_oculta
		entrada_oculta=new Sinapsis[(num_capa_entrada+1)][num_capa_oculta+1];
		for(int i=0;i<=num_capa_entrada;i++)  
		{
			for (int j=0;j<=num_capa_oculta;j++)
			{
				double w= Double.valueOf(prop.getProperty("PESO_ENTRADA_OCULTA["+i+"]["+j+"]")).doubleValue();
				entrada_oculta[i][j]=new Sinapsis(capa_entrada[i],capa_oculta[j],w);

				capa_entrada[i].sinapsis_salida.add(entrada_oculta[i][j]);
				capa_oculta[j].sinapsis_entrada.add(entrada_oculta[i][j]);
			}
		}
		//capa_oculta--->capa_salida
		oculta_salida=new Sinapsis[(num_capa_oculta+1)][num_capa_salida+1];
		for(int j=0;j<=num_capa_oculta;j++)   
		{
			for (int k=1;k<=num_capa_salida;k++)
			{
				//              double w=PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5
				double w= Double.valueOf(prop.getProperty("PESO_OCULTA_SALIDA["+j+"]["+k+"]")).doubleValue();

				oculta_salida[j][k]=new Sinapsis(capa_oculta[j],capa_salida[k],w);

				capa_oculta[j].sinapsis_salida.add(oculta_salida[j][k]);
				capa_salida[k].sinapsis_entrada.add(oculta_salida[j][k]);


			}
		}


	}   
	//--------------------------------------------------------- 
	public  void saveRna(String fichero_setup_rna)
	{
		//esta funcion salva e en un fichero los pesos de la 
		//rna actual para volver a recuperarla cuando queramos

		//creamos el fichero de properties de la RNA
		File propFile=new File(fichero_setup_rna);
		Properties prop=new Properties();

		prop.setProperty("NUM_CAPA_ENTRADA",(new Integer(capa_entrada.length-1)).toString());
		prop.setProperty("NUM_CAPA_OCULTA",(new Integer(capa_oculta.length-1)).toString());
		prop.setProperty("NUM_CAPA_SALIDA",(new Integer(capa_salida.length-1)).toString());

		//ahora almacenamos los pesos de las sinapsis

		//capa_entrada--->capa_oculta
		for(int i=0;i<capa_entrada.length;i++)  
		{
			for (int j=0;j<capa_oculta.length;j++)
			{
				prop.setProperty("PESO_ENTRADA_OCULTA["+i+"]["+j+"]",(new Double(entrada_oculta[i][j].w)).toString());

			}
		}
		//capa_oculta--->capa_salida
		for(int j=0;j<capa_oculta.length;j++)   
		{
			for (int k=1;k<capa_salida.length;k++)
			{
				prop.setProperty("PESO_OCULTA_SALIDA["+j+"]["+k+"]",(new Double(oculta_salida[j][k].w)).toString());


			}
		}
		try{
			prop.store(new FileOutputStream(propFile),"Configuracion de una red neuronal MLP");
		}catch(Exception e){System.out.println(e);System.exit(0);}


	}
	//--------------------------------------------------------      
	public void run(double[] entradas)
	{
		//esta funcion ejecuta la red neuronal 
		//las salidas se pueden recoger en las neuronas de la capa de salida
		//traza ("entradas:"+entradas.length);  
		for (int i=1;i<entradas.length;i++)
		{
			//     traza ("i:"+i);
			capa_entrada[i].in=entradas[i];  
		}
		for (int i=0;i<capa_entrada.length;i++)
		{
			capa_entrada[i].run();    
		}
		for (int j=0;j<capa_oculta.length;j++)
		{
			capa_oculta[j].run(); 
		}
		for (int k=1;k<capa_salida.length;k++)
		{
			capa_salida[k].run(); 
			traza ("salida:"+capa_salida[k].out);
		}

	}   

	public static void main(String args[])
	{
		boolean comprobar = true;
		String ficheroEntrenamiento = "PruebaRestaCoches.csv";
		String ficheroProperties = "configCambioSigno.properties";
		String backPropagation = "BP-bateria";
		//Rna rna=new Rna(43,22,1,0);
		Rna rna=new Rna(4,4,1,0);
		
		if (comprobar == false){
		try{     rna.loadRna(ficheroProperties);
		}catch(Exception e){}
		
		//double[] primerElemento = {2,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0};
		//rna.run(primerElemento);
		boolean mutante = false;
		
		if (mutante == false){
		
		boolean estado = rna.converge(backPropagation, 500, ficheroEntrenamiento, 0.0000001f);
		rna.test(ficheroEntrenamiento);
		
		while (estado == false){
			estado = rna.converge(backPropagation, 500, ficheroEntrenamiento, 0.0000001f);
			rna.saveRna(ficheroProperties);
			rna.test(ficheroEntrenamiento);
		}
		}
		else {
			boolean estado = true;
			while (estado == true){
				estado = rna.converge("mutante", 100, ficheroEntrenamiento, 0.0000001f);
				rna.saveRna(ficheroProperties);
				rna.test(ficheroEntrenamiento);
			}
		}
		} else {
			try{     rna.loadRna(ficheroProperties);
			}catch(Exception e){}
			rna.test(ficheroEntrenamiento);
		}
	}
	//---------------------------------     
	public Rna( int num_capa_entrada, int num_capa_oculta, int num_capa_salida, float peso_ini)
	{

		num_neuronas_entrada=num_capa_entrada;
		num_neuronas_oculta=num_capa_oculta;
		num_neuronas_salida=num_capa_salida;

		num_capas_ocultas=1;
		PESO_INICIAL=peso_ini;

		//aqui se instancian las neuronas y las sinapsis entre ellas
		//conectando unas con otras. Es decir, se contruye 
		//la  RNA (red neuronal artificial)

		//añadiremos las neuronas tipo BIAS en la capa de entrada y en 
		//la oculta

		//capa de entrada
		capa_entrada=new Neurona[num_capa_entrada+1];
		capa_entrada[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_entrada;i++) capa_entrada[i]=new Neurona("ENTRADA");

		//capa oculta
		capa_oculta=new Neurona[num_capa_oculta+1];
		capa_oculta[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_oculta;i++) capa_oculta[i]=new Neurona("OCULTA");


		//capa salida
		//ahora no hay BIAS pero uso el 1 para empezar a numerar, por coherencia
		capa_salida=new Neurona[num_capa_salida+1];
		for(int i=1;i<=num_capa_salida;i++) capa_salida[i]=new Neurona("SALIDA");


		//ahora creamos las conexiones (sinapsis) entre neuronas
		//capa_entrada--->capa_oculta
		entrada_oculta=new Sinapsis[(num_capa_entrada+1)][num_capa_oculta+1];
		for(int i=0;i<=num_capa_entrada;i++)  
		{
			for (int j=0;j<=num_capa_oculta;j++)
			{
				double w= PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5
				entrada_oculta[i][j]=new Sinapsis(capa_entrada[i],capa_oculta[j],w);

				capa_entrada[i].sinapsis_salida.add(entrada_oculta[i][j]);
				capa_oculta[j].sinapsis_entrada.add(entrada_oculta[i][j]);
			}
		}
		//capa_oculta--->capa_salida
		oculta_salida=new Sinapsis[(num_capa_oculta+1)][num_capa_salida+1];
		for(int j=0;j<=num_capa_oculta;j++)   
		{
			for (int k=1;k<=num_capa_salida;k++)
			{
				double w=PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5

				oculta_salida[j][k]=new Sinapsis(capa_oculta[j],capa_salida[k],w);

				capa_oculta[j].sinapsis_salida.add(oculta_salida[j][k]);
				capa_salida[k].sinapsis_entrada.add(oculta_salida[j][k]);


			}
		}


		//-------------------------
	}
	
	
	
	//---------------------------------     
	//esta funcion ejecuta el algoritmo backpropagation con el
	//numero de epocas que queramos. Los patrones de ejemplo
	//los saca del fichero de entrada
	//al final imprime los pesos de la red calculados
	//si no se consigue llegar a un minimo local con error menor que
	//el solicitado, retorna false
	//el umbral es la condicion de comvergencia del algoritmo BP
	public boolean backPropagation_old(int max_epocas, String datos, float umbral)
	{
		//bucle until error<umbral or ejecuciones=epocas
		//osea, bucle para cada epoca
		int epocas=0;
		double error_actual=0;
		double error_anterior=0;
		double err_ant_ant=0;
		double varianza=0;

		int p=0; //numero de patrones en cada epoca

		while (epocas<max_epocas)
		{
			epocas+=1;      
			//traza ("epoca:"+epocas);  
			//cardinalidad de la epoca
			p=0;

			//inicializo Error (funcion de coste) para esta epoca

			error_actual=0;
			varianza=0;

			//abrimos el fichero de datos por epoca-esima vez
			SuperFile file_datos=new SuperFile();
			file_datos.openRead(datos); 
			p=0;//cardinalidad de la epoca



			double[] señal_error_salida=new double[capa_salida.length+1];
			double[][] grad_local_salida=new double[capa_oculta.length+1][capa_salida.length+1];

			double[] señal_error_oculta=new double[capa_oculta.length+1];
			double[][]grad_local_oculta=new double[capa_entrada.length+1][capa_oculta.length+1];



			//bucle para cada patron .
			String linea=file_datos.readLine();
			while (linea!=null)  
			{
				p=p+1;  
				//traza ("procesando linea:"+p);
				//leer linea until EOF . la llamamos patron u (u =0...p)
				//cargar las entradas
				double[] patron=extraerEntradas(linea);
				//cargar las salidas deseadas
				double[] salida_deseada=extraerSalidas(linea);
				//ejecutar red neuronal
				run(patron);
				//calcular las señales de error asociadas y los gradientes



				//para las sinapsis oculta-->salida:
				for (int k=1;k<capa_salida.length;k++)
				{  
					//para cada neurona de salida hay que calcular tantos
					//gradientes locales como sinapsis con las neuronas de la capa oculta
					señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out)* capa_salida[k].out*(1- capa_salida[k].out);

					for (int j=0;j<capa_oculta.length;j++)
					{

						grad_local_salida[j][k]+=ritmo_aprendizaje*señal_error_salida[k]*capa_oculta[j].out;
					}
				}

				//para las sinapsis entrada-->ocultas:
				for (int j=0;j<capa_oculta.length;j++)
				{  
					double suma=0;
					for (int k=1;k<capa_salida.length;k++) 
					{
						//      traza ("[k][j]"+k+" "+j+"");
						//      traza (""+oculta_salida[j][k].w);
						suma+=señal_error_salida[k]*oculta_salida[j][k].w;
					}
					señal_error_oculta[j]=suma*capa_oculta[j].out*(1-capa_oculta[j].out);
					for (int i=0;i<capa_entrada.length;i++)
					{
						grad_local_oculta[i][j]+=ritmo_aprendizaje*señal_error_oculta[j]*capa_entrada[i].out;

					}
				}


				//calcular el incremento parcial de los pesos y umbrales (gradientes)
				//ya esta
				//calculo el incremento del error actual    
				for (int k=1;k<capa_salida.length;k++)
				{
					error_actual+=0.5*Math.pow((salida_deseada[k]-capa_salida[k].out),2.0);
					varianza+=(150*salida_deseada[k]-150*capa_salida[k].out)*(150*salida_deseada[k]-150*capa_salida[k].out);
				}

				//end bucle  para cada patron
				linea=file_datos.readLine();

			}     
			//actualizar pesos y umbrales, en las sinapsis

			//actualizamos las sinapsis oculta-->salida
			for (int j=0;j<capa_oculta.length;j++)
			{
				for (int k=1;k<capa_salida.length;k++)
				{
					//traza ("j,k-->"+j+","+k);
					oculta_salida[j][k].w+=grad_local_salida[j][k];
				}
			}
			//actualizamos las sinapsis entrada--->oculta
			for (int i=0;i<capa_entrada.length;i++)
			{
				for (int j=0;j<capa_oculta.length;j++)
				{
					entrada_oculta[i][j].w+=grad_local_oculta[i][j];
				}
			}
			//calcular el error actual 

			//ya esta
			//impresion en pantalla de resultados provisionales

			//Comprobacion de condicion de salida
			//el problema es que no se determinar un umbral razonable a priori
			//para el error. Vamos a usar el valor promedio, pues no depende
			//del numero de patrones
			//osea , trabajamos con el valor promedio por epoca del error cuadrático medio
			//ojo:tb pienso que habría que dividir entre el numero de salidas, puesto que a
			//mas salidas, mayor es la funcion coste, aunque los libros de esta materia ni siquiera dividen
			//entre p (he encontrado un articulo donde si que lo hacen)

			if ((error_actual/p)<=umbral)  break;
			//  if ((int)(epocas/100)==epocas/100.0f) 
			//traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);
			this.varianza=varianza;
			//actualizamos el ritmo de aprendizaje
			//para implementar un ritmo de aprendizaje adaptativo
			//con este sistema obtengo el mayor ritmo de aprendizaje posible en cada momento
			//creciendo y decreciendo segun el error de la epoca
			if (error_anterior>=error_actual)//+0.0000001f) 
			{
				//ok veamos si podemos subir el ritmo
				if (err_ant_ant>=error_anterior)
					ritmo_aprendizaje +=aumento_ritmo;
				//      ritmo_aprendizaje +=ritmo_aprendizaje*0.1;//



				//0.0000001f;
				//ritmo_aprendizaje *=2;//+=0.000001f;

			}
			else 
			{//lo decremento, pero no puede hacerse negativo
				if (ritmo_aprendizaje>aumento_ritmo)    
					ritmo_aprendizaje -=aumento_ritmo;//0.0000001f;
				//          ritmo_aprendizaje -=ritmo_aprendizaje*0.1;//

			}
			err_ant_ant=error_anterior;
			error_anterior=error_actual;

			//end bucle 

		}  

		//si no ha convergido, retornamos false
		//traza ("Error de epoca " +epocas+":"+error_actual);
		traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);

		if ((error_actual/p)>umbral)  return false;
		return true;

	}   //end backpropagation
	//---------------------------------
	//esta funcion sirve para probar la red neuronal contra un fichero de datos 
	//para test
	public void test(String datos)
	{
		double error_actual[]= {0, 0};
		int p=0; //numero de patrones del fichero de test
		int p_exito1;//patrones con respuesta neuronal adecuada de la salida 1
		int p_exito2;//patrones con respuesta neuronal adecuada de la salida 2
		double margen=0.01;
		//inicializo Error (funcion de coste) para esta epoca(epoca= todo el fichero)
		//error_actual={0, 0};

		//abrimos el fichero de datos 
		SuperFile file_datos=new SuperFile();
		file_datos.openRead(datos); 
		p=0;//cardinalidad de la epoca
		p_exito1=0;
		p_exito2=0;


		double[] señal_error_salida=new double[capa_salida.length+1];
		//double[][] grad_local_salida=new double[capa_oculta.length+1][capa_salida.length+1];

		double[] señal_error_oculta=new double[capa_oculta.length+1];
		//double[][]grad_local_oculta=new double[capa_entrada.length+1][capa_oculta.length+1];


		//bucle para cada patron .
		String linea=file_datos.readLine();
		while (linea!=null)  
		{
			p=p+1;  
			System.out.println("procesando linea:"+p+"    "+linea);
			//leer linea until EOF . la llamamos patron u (u =0...p)
			//cargar las entradas
			double[] patron=extraerEntradas(linea);
			//cargar las salidas deseadas
			double[] salida_deseada=extraerSalidas(linea); //Aqui tendrá las dos salidas
			//ejecutar red neuronal
			run(patron);

			System.out.println("-------------------");
			System.out.println("patron:"+p);
			//traza("patron[1]"+patron[0]);
			System.out.print("se esperaba:");
			for (int x=1;x<salida_deseada.length;x++)
			{
				System.out.print("s"+x+":"+salida_deseada[x]+" , ");    
			}
			System.out.println("se ha obtenido:");
			for (int x=1;x<salida_deseada.length;x++)
			{
				System.out.print("s"+x+":"+capa_salida[x].out+" , ");   
			}

			//solo vale si hay una sola salida
			if (Math.abs(capa_salida[1].out-salida_deseada[1]) <margen){
				p_exito1++;
				System.out.println("");
			}
			else{
				System.out.print("*************** ups! "+Math.abs(capa_salida[1].out-salida_deseada[1]));
				//System.out.println("        "+salida_deseada.length);
			}
			if (salida_deseada.length == 3){
			if (Math.abs(capa_salida[2].out-salida_deseada[2]) <margen){
				p_exito2++;
				System.out.println("");
			}
			else{
				System.out.print("  *************** ups! "+Math.abs(capa_salida[2].out-salida_deseada[2]));
				
			}
			}
			System.out.println("");

			//calcular las señales de error asociadas 

			//para las sinapsis oculta-->salida:
			for (int k=1;k<capa_salida.length;k++)
			{  
				//para cada neurona de salida hay que calcular tantos
				//gradientes locales como sinapsis con las neuronas de la capa oculta
				señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out)* capa_salida[k].out*(1- capa_salida[k].out);

			}


			//para las sinapsis entrada-->ocultas:
			for (int j=0;j<capa_oculta.length;j++)
			{  
				double suma=0;
				for (int k=1;k<capa_salida.length;k++) 
				{
					suma+=señal_error_salida[k]*oculta_salida[j][k].w;
				}
				señal_error_oculta[j]=suma*capa_oculta[j].out*(1-capa_oculta[j].out);
			}


			//calcular el incremento parcial de los pesos y umbrales (gradientes)
			//ya esta
			//calculo el incremento del error actual    
			for (int k=1;k<capa_salida.length;k++)
			{
				//error_actual+=0.5*Math.pow((salida_deseada[k]-capa_salida[k].out),2.0);
				error_actual[k-1]+=Math.abs(salida_deseada[k]-capa_salida[k].out);
			}

			//end bucle  para cada patron
			linea=file_datos.readLine();

		}     
		System.out.println("--------------------------------------");
		System.out.println("resumen de resultados:");
		System.out.println("ejemplos:"+p+"    exito salida 1:"+p_exito1+ "    exito salida 2:"+p_exito2+"    margen"+margen);
		System.out.println("Error de la epoca test: S1:"+error_actual[0]+" S2:"+error_actual[1]);
		System.out.println("---------------------------------------");


	}
	//-------------------------------
	//esta funcion es de apoyo. Sirve para extraer los valores de las entradas
	//de los patrones de ejemplo para entrenar a la RNA


	//considero que el primer parametro de la linea no es una entrada sino
	//un numero de orden. ejemplo:
	// 01->,3,2,0,8,3,3,10,2,7,7,2,1,3,1
	public double[] extraerEntradas(String linea)
	{
		double[] entradas=new double[capa_entrada.length];
		int i=1;
		StringTokenizer tok=new StringTokenizer(linea,",");
		//saco el primer elemento:
		if (tok.hasMoreTokens()) tok.nextToken();

		while((tok.hasMoreTokens()) && (i<capa_entrada.length))
		{

			entradas[i]=Double.valueOf(tok.nextToken()).doubleValue();
			 //traza("entrada["+i+"]:"+entradas[i]);

			i++;
		}
		return entradas;

	}
	//-------------------------------
	//esta funcion es de apoyo. Sirve para extraer los valores de las salidas deseadas
	//de los patrones de ejemplo para entrenar a la RNA

	public double[] extraerSalidas(String linea)
	{
		//saco las entradas y luego las salidas, para quedarme solo
		//con las salidas

		double[] salidas=new double[capa_salida.length];
		
		int i=1;
		StringTokenizer tok=new StringTokenizer(linea,",");
		//traza ("capa entrada:"+capa_entrada.length);
		//saco el primer elemento:
		if (tok.hasMoreTokens()) tok.nextToken();

		while((tok.hasMoreTokens()) && (i<capa_entrada.length+capa_salida.length))
		{//traza ("i"+i);
			if (i<capa_entrada.length) 
			{
				tok.nextToken();
				i++;
			}
			if (i>=capa_entrada.length)
			{
				String cad=new String(    tok.nextToken());
				//    traza ("ii"+(i-capa_entrada.length+1)+"-->"+cad);

				salidas[i-capa_entrada.length+1]=Double.valueOf(cad).doubleValue(); //pej "8.76"
				//traza ("salida["+(i-capa_entrada.length+1)+"]:"+salidas[i-capa_entrada.length+1]);
				i++;
			}
		}
		return salidas  ;

	}
	//------------------------------------------
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
	//--------------------------------------------------
	//--------------------------------------------------
	//---------------------------------     
	public Rna(int num_capa_entrada, int num_capa_oculta1,int num_capa_oculta2,int num_capa_salida, float peso_ini)
	{
		num_capas_ocultas=2;
		PESO_INICIAL=peso_ini;

		//aqui se instancian las neuronas y las sinapsis entre ellas
		//conectando unas con otras. Es decir, se contruye 
		//la  RNA (red neuronal artificial)

		//añadiremos las neuronas tipo BIAS en la capa de entrada y en 
		//la oculta

		//capa de entrada
		capa_entrada=new Neurona[num_capa_entrada+1];
		capa_entrada[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_entrada;i++) capa_entrada[i]=new Neurona("ENTRADA");

		//capa oculta1
		capa_oculta=new Neurona[num_capa_oculta1+1];
		capa_oculta[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_oculta1;i++) capa_oculta[i]=new Neurona("OCULTA");

		//capa oculta2
		capa_oculta2=new Neurona[num_capa_oculta2+1];
		capa_oculta2[0]=new Neurona("BIAS");
		for(int i=1;i<=num_capa_oculta2;i++) capa_oculta2[i]=new Neurona("OCULTA");



		//capa salida
		//ahora no hay BIAS pero uso el 1 para empezar a numerar, por coherencia
		capa_salida=new Neurona[num_capa_salida+1];
		for(int i=1;i<=num_capa_salida;i++) capa_salida[i]=new Neurona("SALIDA");


		//ahora creamos las conexiones (sinapsis) entre neuronas
		//capa_entrada--->capa_oculta1
		entrada_oculta=new Sinapsis[(num_capa_entrada+1)][num_capa_oculta1+1];
		for(int i=0;i<=num_capa_entrada;i++)  
		{
			for (int j=0;j<=num_capa_oculta1;j++)
			{
				double w= PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5
				entrada_oculta[i][j]=new Sinapsis(capa_entrada[i],capa_oculta[j],w);

				capa_entrada[i].sinapsis_salida.add(entrada_oculta[i][j]);
				capa_oculta[j].sinapsis_entrada.add(entrada_oculta[i][j]);
			}
		}
		//capa_oculta--->capa_oculta2
		oculta_oculta2=new Sinapsis[(num_capa_oculta1+1)][num_capa_oculta2+1];
		for(int j=0;j<=num_capa_oculta1;j++)  
		{
			for (int k=0;k<=num_capa_oculta2;k++)
			{
				double w=PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5

				//traza("j:"+j+",   k:"+k);

				oculta_oculta2[j][k]=new Sinapsis(capa_oculta[j],capa_oculta2[k],w);

				capa_oculta[j].sinapsis_salida.add(oculta_oculta2[j][k]);
				capa_oculta2[k].sinapsis_entrada.add(oculta_oculta2[j][k]);


			}
		}



		//capa_oculta2--->capa_salida
		oculta2_salida=new Sinapsis[(num_capa_oculta2+1)][num_capa_salida+1];
		for(int j=0;j<=num_capa_oculta2;j++)  
		{
			for (int k=1;k<=num_capa_salida;k++)
			{
				double w=PESO_INICIAL*Math.random()-(PESO_INICIAL/2); //entre -5 y 5

				oculta2_salida[j][k]=new Sinapsis(capa_oculta2[j],capa_salida[k],w);

				capa_oculta2[j].sinapsis_salida.add(oculta2_salida[j][k]);
				capa_salida[k].sinapsis_entrada.add(oculta2_salida[j][k]);


			}
		}


		//-------------------------
	}
	//---------------------------------     
	public void run2(double[] entradas)
	{
		//esta funcion jecuta la red neuronal 
		//las salidas se pueden recoger en las neuronas de la capa de salida
		//traza ("entradas:"+entradas.length);  
		for (int i=1;i<entradas.length;i++)
		{
			//     traza ("i:"+i);
			capa_entrada[i].in=entradas[i];  
		}
		for (int i=0;i<capa_entrada.length;i++)
		{
			capa_entrada[i].run();    
		}
		for (int j=0;j<capa_oculta.length;j++)
		{
			capa_oculta[j].run(); 
		}

		for (int j=0;j<capa_oculta2.length;j++)
		{
			capa_oculta2[j].run();    
		}

		for (int k=1;k<capa_salida.length;k++)
		{
			capa_salida[k].run(); 
		}

	}   
	//---------------------------------------
	//---------------------------------
	public boolean backPropagationLinea(int max_epocas, String datos, float umbral)
	{
		boolean aleatorio=false;
		//traza ("enter in BP modo linea");
		//bucle until error<umbral or ejecuciones=epocas
		//osea, bucle para cada epoca
		int epocas=0;
		double error_actual=0;
		double error_anterior=0;
		double err_ant_ant=0;
		double varianza=0;

		int p=0; //numero de patrones en cada epoca





		//necesitamos leer el fichero y meterlo en un array para poder desordenar a cada vez
		SuperFile file_datos=new SuperFile();
		file_datos.openRead(datos); 
		String linea=file_datos.readLine();
		int cardinalidad=0;
		while (linea!=null)  
		{
			cardinalidad+=1;
			linea=file_datos.readLine();
		}
		file_datos.closeRead(); 

		//ahora construyo un array
		String[] lineas=new String[cardinalidad];
		file_datos.openRead(datos); 
		linea=file_datos.readLine();
		int indice=0;
		while (linea!=null)  
		{
			lineas[indice]=linea; 
			indice+=1;
			linea=file_datos.readLine();
		}
		file_datos.closeRead(); 
		//ya tengo todas las lineas en el array lineas[]
		// traza("ya tengo todas las lineas en el array lineas[]");



		while (epocas<max_epocas)
		{
			epocas+=1;      
			//traza ("epoca:"+epocas);  
			//cardinalidad de la epoca
			p=0;

			//inicializo Error (funcion de coste) para esta epoca

			error_actual=0;
			varianza=0;

			//abrimos el fichero de datos por epoca-esima vez
			//    SuperFile file_datos=new SuperFile();
			//   file_datos.openRead(datos); 




			double[] señal_error_salida=new double[capa_salida.length+1];
			double[][] grad_local_salida=new double[capa_oculta.length+1][capa_salida.length+1];

			double[] señal_error_oculta=new double[capa_oculta.length+1];
			double[][]grad_local_oculta=new double[capa_entrada.length+1][capa_oculta.length+1];

			double[] delta_entrada=new double[capa_entrada.length+1];   
			double[] delta_oculta=new double[capa_oculta.length+1];
			double[] delta_salida=new double[capa_salida.length+1];


			//bucle para cada patron .
			//String linea=file_datos.readLine();
			//en capa epoca vamos a recorrer de un modo distinto

			//busco la potencia de 2 mas cercana y superior
			int potencia2=0;
			for (int p2=0;p2<23;p2++)
			{
				int numero=(int)Math.pow(2,p2);
				if (numero>=cardinalidad)
				{
					potencia2=numero;
					break;
				}
			}

			int num_aleatorio=(int) (Math.random()*potencia2);
			int indice_normal=0;
			int indice_aleatorio=0;
			for (int x=0;x<cardinalidad;x++)
			{
				indice_aleatorio=indice_normal ^num_aleatorio;
				if (indice_aleatorio< cardinalidad) break;
				indice_normal++;
			}


			//int num_normal=0;
			//indice_aleatorio=num_normal ^ num_aleatorio;
			linea=lineas[indice_aleatorio];

			if (!aleatorio)linea=lineas[p];



			while (linea!=null)  
			{
				p=p+1;  
				//traza ("procesando linea:"+p);
				//leer linea until EOF . la llamamos patron u (u =0...p)
				//cargar las entradas
				double[] patron=extraerEntradas(linea);
				//cargar las salidas deseadas
				double[] salida_deseada=extraerSalidas(linea);
				//ejecutar red neuronal
				run(patron);
				//calcular las señales de error asociadas y los gradientes



				//para las sinapsis oculta-->salida:

				for (int k=1;k<capa_salida.length;k++)
				{  
					//para cada neurona de salida hay que calcular tantos
					//gradientes locales como sinapsis con las neuronas de la capa oculta
					//sigmo 
					señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out)*capa_salida[k].derivada ;//capa_salida[k].out*(1- capa_salida[k].out);

					//señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out);//tanh
					//N delta_salida[k]=señal_error_salida[k]*capa_salida[k].derivada;//tanh

					// sigmo
					for (int j=0;j<capa_oculta.length;j++)
					{                    
						// sigmoide: 
						grad_local_salida[j][k]+=ritmo_aprendizaje*señal_error_salida[k]*capa_oculta[j].out;


					}

				}

				for (int j=0;j<capa_oculta.length;j++)
				{  
					delta_oculta[j]=0;
					double suma=0;
					for (int k=1;k<capa_salida.length;k++) 
					{

						//sigmo 
						suma+=señal_error_salida[k]*oculta_salida[j][k].w;
						//n delta_oculta[j]+=delta_salida[k]*oculta_salida[j][k].w*capa_oculta[j].derivada;

					}
					//sigmo 
					señal_error_oculta[j]=suma*capa_oculta[j].derivada;//capa_oculta[j].out*(1-capa_oculta[j].out);


					//señal_error_oculta[j]=suma;

					// sigmo
					for (int i=0;i<capa_entrada.length;i++)
					{
						//sigmo 
						grad_local_oculta[i][j]+=ritmo_aprendizaje*señal_error_oculta[j]*capa_entrada[i].out;



					}


				}



				//calcular el incremento parcial de los pesos y umbrales (gradientes)
				//ya esta
				//calculo el incremento del error actual    
				for (int k=1;k<capa_salida.length;k++)
				{
					error_actual+=0.5*Math.pow((salida_deseada[k]-capa_salida[k].out),2.0);
					varianza+=(150*salida_deseada[k]-150*capa_salida[k].out)*(150*salida_deseada[k]-150*capa_salida[k].out);
				}

				//end bucle  para cada patron

				//int aleat=(int) (Math.random()*10.0f);
				//for (int aa=0;aa<aleat;aa++)  


				//          linea=file_datos.readLine();


				if (p==cardinalidad) linea=null;//con esto salgo del bucle
				else{


					for (int x=0;x<cardinalidad;x++)
					{
						indice_normal++;
						indice_aleatorio=indice_normal ^num_aleatorio;
						if (indice_aleatorio< cardinalidad) break;

					}


					//indice_aleatorio=(int) (Math.random()*(float)cardinalidad);
					//indice_aleatorio+=1;
					linea=lineas[indice_aleatorio];
					if (!aleatorio)linea=lineas[p];

				}


				//}//end for bateria


				//actualizar pesos y umbrales, en las sinapsis

				//actualizamos las sinapsis oculta-->salida
				for (int j=0;j<capa_oculta.length;j++)
				{
					for (int k=1;k<capa_salida.length;k++)
					{
						//traza ("j,k-->"+j+","+k);
						//sigmo 
						oculta_salida[j][k].w+=grad_local_salida[j][k];
						//oculta_salida[j][k].w+=ritmo_aprendizaje*delta_salida[k]*capa_oculta[j].out;

					}
				}
				//actualizamos las sinapsis entrada--->oculta
				for (int i=0;i<capa_entrada.length;i++)
				{
					for (int j=0;j<capa_oculta.length;j++)
					{
						//sigmo 
						entrada_oculta[i][j].w+=grad_local_oculta[i][j];
						//entrada_oculta[i][j].w+=ritmo_aprendizaje*delta_oculta[j]*capa_entrada[i].out;

					}
				}

			}//linea


			//calcular el error actual 

			//ya esta
			//impresion en pantalla de resultados provisionales

			//Comprobacion de condicion de salida
			//el problema es que no se determinar un umbral razonable a priori
			//para el error. Vamos a usar el valor promedio, pues no depende
			//del numero de patrones
			//osea , trabajamos con el valor promedio por epoca del error cuadrático medio
			//ojo:tb pienso que habría que dividir entre el numero de salidas, puesto que a
			//mas salidas, mayor es la funcion coste, aunque los libros de esta materia ni siquiera dividen
			//entre p (he encontrado un articulo donde si que lo hacen)

			if ((error_actual/p)<=umbral)  break;
			//  if ((int)(epocas/100)==epocas/100.0f) 
			//          traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);
			this.varianza=varianza;
			//actualizamos el ritmo de aprendizaje
			//para implementar un ritmo de aprendizaje adaptativo
			//con este sistema obtengo el mayor ritmo de aprendizaje posible en cada momento
			//creciendo y decreciendo segun el error de la epoca
			if (error_anterior>=error_actual)//+0.0000001f) 
			{
				//ok veamos si podemos subir el ritmo
				if (err_ant_ant>=error_anterior)
					ritmo_aprendizaje +=aumento_ritmo;
				//      ritmo_aprendizaje +=ritmo_aprendizaje*0.1;//

				//aumento_ritmo=ritmo_aprendizaje/100.0f;
				//   aumento_ritmo=      aumento_ritmo*2;

				//0.0000001f;
				//ritmo_aprendizaje *=2;//+=0.000001f;

			}
			else 
			{//lo decremento, pero no puede hacerse negativo
				if (ritmo_aprendizaje>aumento_ritmo)    
				{ritmo_aprendizaje -=aumento_ritmo;}//0.0000001f;
				//else aumento_ritmo=aumento_ritmo/2;   
				//          ritmo_aprendizaje -=ritmo_aprendizaje*0.1;//
				//aumento_ritmo=ritmo_aprendizaje/100.0f;
				//aumento_ritmo=      aumento_ritmo/2;


			}
			err_ant_ant=error_anterior;
			error_anterior=error_actual;




			//end bucle 

		}  
		//aumento_ritmo=ritmo_aprendizaje/100.0f;



		traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);

		//si no ha convergido, retornamos false
		//traza ("Error de epoca " +epocas+":"+error_actual);

		if ((error_actual/p)>umbral)  return false;
		return true;

	}   //end backpropagation
	//---------------------------------
	public boolean converge ( String metodo, int iteraciones, String file_samples, float umbral_error)
	{
		//metodos:
		// BP-bateria: el backpropagation en modo bateria
		// BP-linea: backpropagation en modo linea
		// mutante: mutaciones para evitar minimo local. Es cosecha propia

		if (metodo.equals("BP-bateria")) return backPropagation(iteraciones, file_samples,umbral_error);
		if (metodo.equals("BP-linea")) return backPropagationLinea(iteraciones, file_samples,umbral_error);
		if (metodo.equals("mutante")) return mutante(iteraciones, file_samples,umbral_error);

		return true;

	}//end converge
	//------------------------------------------    
	public boolean mutante(int max_iteraciones, String file_samples, float umbral_error)
	{
		float varianza_min=1000000000;//varianza minima 10M
		float varianza=0;        
		float old_varianza=-1;       
		float error_actual=0.0f;

		double iteraciones=-1;
		int num_mejoras=0;
		int MAX_ITERACIONES=max_iteraciones;
		boolean fool_vars_done=true;//false;//para identificar foolvars. 
		int u=0;//para identificar foolvars
		float RND=100.0f;    
		float RNDA=5.0f; 

		//guardo los pesos actuales y busco el maximo, para el RND
		int total_pesos=0;
		int k=Math.max(num_neuronas_entrada,num_neuronas_oculta);
		int k2=Math.max(num_neuronas_oculta,num_neuronas_salida);


		double[][][] old_value=new double[2][k][k2];
		double max=-10000;

		for (int i=0;i<num_neuronas_entrada;i++)
		{
			for (int j=0;j<num_neuronas_oculta;j++)
			{
				old_value[0][i][j]=entrada_oculta[i][j].w;
				if (old_value[0][i][j]>max) max=old_value[0][i][j];

				total_pesos+=1;
			}  
		}      
		for (int i=0;i<num_neuronas_oculta;i++)
		{
			for (int j=1;j<=num_neuronas_salida;j++)
			{
				old_value[1][i][j]=oculta_salida[i][j].w;
				if (old_value[1][i][j]>max) max=old_value[1][i][j];
				total_pesos+=1;
			}  
		}      

		//ya tengo todos los pesos en old_value

		//numero de mutaciones
		//si son muchas, seguro que no mejora, y si son pocas, puedo encajarme en un
		//minimo local

		int num_muta;
		//num_muta=(int)(Math.random()*(k/5))+1; //al menos 1 max k/5 , por ejemplo

		//ahora calculamos la varianza sin mutar
		error_actual=getError(file_samples);
		float error_new=error_actual;
		int[] index_victima=new int[1000];
		float error_ini=getError(file_samples);

		//bucle
		int capa=-1;
		for (int i=0;i<max_iteraciones;i++)
		{
			//mutamos
			//al menos 1 max k/5 , por ejemplo
			num_muta=(int)(Math.random()*(2/*total_pesos/5*/))+1;
			//elijo la victimas
			for (int v=0;v<num_muta;v++) 
			{
				capa=(int) (Math.random()+0.5f);
				if (capa==0) 
				{

					//elegimos neurona de entrada y oculta
					int v1=(int)(Math.random()*num_neuronas_entrada);
					int v2=(int)(Math.random()*num_neuronas_oculta);
					//traza("v1="+v1+",v2="+v2);

					entrada_oculta[v1][v2].w=(Math.random()-0.5)*max;

				}

				if (capa==1) 
				{
					//elegimos neurona de entrada y oculta
					int v1=(int)(Math.random()*num_neuronas_oculta);
					int v2=(int)(Math.random()*num_neuronas_salida+1);

					//traza("v1="+v1+",v2="+v2);
					oculta_salida[v1][v2].w=(Math.random()-0.5)*max;

				}
			}//num mutaciones

			//recalculamos nueva varianza
			error_new=getError(file_samples);

			//si es peor, desago la mutacion
			if (error_new>=error_actual)
			{
				//----------------------------------------------
				for (int ii=0;ii<num_neuronas_entrada;ii++)
				{
					for (int jj=0;jj<num_neuronas_oculta;jj++)
					{
						entrada_oculta[ii][jj].w=old_value[0][ii][jj];
					}   
				}     
				for (int ii=0;ii<num_neuronas_oculta;ii++)
				{
					for (int jj=1;jj<=num_neuronas_salida;jj++)
					{
						oculta_salida[ii][jj].w=old_value[1][ii][jj];
					}  
				} 


				//-----------------------------------------------   

			}
			//si es mejor, notifico el cambio
			else
			{
				num_mejoras+=1; 
				error_actual=error_new;
				//traza(" mejoras: "+num_mejoras+ ", num_muta:"+num_muta+" capa:"+capa+" error:"+error_new);    
				//y actualizo old values
				for (int ii=0;ii<num_neuronas_entrada;ii++)
				{
					for (int jj=0;jj<num_neuronas_oculta;jj++)
					{
						old_value[0][ii][jj]=entrada_oculta[ii][jj].w;
						if (old_value[0][ii][jj]>max) max=old_value[0][ii][jj];
					}  
				}       
				for (int ii=0;ii<num_neuronas_oculta;ii++)
				{
					for (int jj=1;jj<=num_neuronas_salida;jj++)
					{
						old_value[1][ii][jj]=oculta_salida[ii][jj].w;
						if (old_value[1][ii][jj]>max) max=old_value[1][ii][jj];
					}  
				}        

			}//else

		}//end for iteraciones
		traza(" mejoras: "+num_mejoras+" error_ini:"+error_ini+"  errorfinal:"+error_actual);   

		return true;

	}   //end funcion
	//-----------------------------------------
	public float getError(String datos)
	{
		double error_actual=0;
		int p=0; //numero de patrones del fichero de test
		int p_exito;//patrones con respuesta neuronal adecuada
		double margen=0.001;
		//inicializo Error (funcion de coste) para esta epoca(epoca= todo el fichero)
		error_actual=0;

		//abrimos el fichero de datos 
		SuperFile file_datos=new SuperFile();
		file_datos.openRead(datos); 
		p=0;//cardinalidad de la epoca
		p_exito=0;


		double[] señal_error_salida=new double[capa_salida.length+1];
		//double[][] grad_local_salida=new double[capa_oculta.length+1][capa_salida.length+1];

		double[] señal_error_oculta=new double[capa_oculta.length+1];
		//double[][]grad_local_oculta=new double[capa_entrada.length+1][capa_oculta.length+1];


		//bucle para cada patron .
		String linea=file_datos.readLine();
		while (linea!=null)  
		{
			p=p+1;  
			//traza ("procesando linea:"+p+"    "+linea);
			//leer linea until EOF . la llamamos patron u (u =0...p)
			//cargar las entradas
			double[] patron=extraerEntradas(linea);
			//cargar las salidas deseadas
			double[] salida_deseada=extraerSalidas(linea);
			//ejecutar red neuronal
			run(patron);

			//traza ("-------------------");
			//traza ("patron:"+p);
			//traza("patron[1]"+patron[0]);
			//traza ("se esperaba:");
			for (int x=1;x<salida_deseada.length;x++)
			{
				//   System.out.print("s"+x+":"+salida_deseada[x]+" , ");  
			}
			//traza ("se ha obtenido:");
			for (int x=1;x<salida_deseada.length;x++)
			{
				//    System.out.print("s"+x+":"+capa_salida[x].out+" , "); 
			}

			//solo vale si hay una sola salida
			if (Math.abs(capa_salida[1].out-salida_deseada[1]) <margen) p_exito++;
			else {}
			//System.out.print("*************** ups! "+Math.abs(capa_salida[1].out-salida_deseada[1])); 

			//calcular las señales de error asociadas 

			//para las sinapsis oculta-->salida:
			for (int k=1;k<capa_salida.length;k++)
			{  
				//para cada neurona de salida hay que calcular tantos
				//gradientes locales como sinapsis con las neuronas de la capa oculta
				señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out)* capa_salida[k].out*(1- capa_salida[k].out);

			}


			//para las sinapsis entrada-->ocultas:
			for (int j=0;j<capa_oculta.length;j++)
			{  
				double suma=0;
				for (int k=1;k<capa_salida.length;k++) 
				{
					suma+=señal_error_salida[k]*oculta_salida[j][k].w;
				}
				señal_error_oculta[j]=suma*capa_oculta[j].out*(1-capa_oculta[j].out);
			}


			//calcular el incremento parcial de los pesos y umbrales (gradientes)
			//ya esta
			//calculo el incremento del error actual    
			for (int k=1;k<capa_salida.length;k++)
			{
				error_actual+=0.5*Math.pow((salida_deseada[k]-capa_salida[k].out),2.0);
			}

			//end bucle  para cada patron
			linea=file_datos.readLine();

		}    

		return (float)error_actual;  
		//traza("--------------------------------------");
		//traza("resumen de resultados:");
		//traza("ejemplos:"+p+"    exito:"+p_exito+"    margen"+margen);
		//traza ("Error de la epoca test:"+error_actual);
		//traza("---------------------------------------");


	}
	//--------------------------------------------------------------------------
	public boolean backPropagation(int max_epocas, String datos, float umbral)
	{
		//bucle until error<umbral or ejecuciones=epocas
		//osea, bucle para cada epoca
		int epocas=0;
		double error_actual=0;
		double error_anterior=0;
		double err_ant_ant=0;
		double varianza=0;

		int p=0; //numero de patrones en cada epoca

		while (epocas<max_epocas)
		{
			epocas+=1;      
			//traza ("epoca:"+epocas);  
			//cardinalidad de la epoca
			p=0;

			//inicializo Error (funcion de coste) para esta epoca

			error_actual=0;
			varianza=0;

			//abrimos el fichero de datos por epoca-esima vez
			SuperFile file_datos=new SuperFile();
			file_datos.openRead(datos); 
			p=0;//cardinalidad de la epoca



			double[] señal_error_salida=new double[capa_salida.length+1];
			double[][] grad_local_salida=new double[capa_oculta.length+1][capa_salida.length+1];

			double[] señal_error_oculta=new double[capa_oculta.length+1];
			double[][]grad_local_oculta=new double[capa_entrada.length+1][capa_oculta.length+1];



			//bucle para cada patron .
			String linea=file_datos.readLine();
			while (linea!=null)  
			{
				p=p+1;  
				//traza ("procesando linea:"+p);
				//leer linea until EOF . la llamamos patron u (u =0...p)
				//cargar las entradas
				double[] patron=extraerEntradas(linea);
				//cargar las salidas deseadas
				double[] salida_deseada=extraerSalidas(linea);
				
				//ejecutar red neuronal
				run(patron);
				//calcular las señales de error asociadas y los gradientes



				//para las sinapsis oculta-->salida:
				for (int k=1;k<capa_salida.length;k++)
				{  
					//para cada neurona de salida hay que calcular tantos
					//gradientes locales como sinapsis con las neuronas de la capa oculta
					señal_error_salida[k]=(salida_deseada[k]-capa_salida[k].out)* capa_salida[k].derivada;

					for (int j=0;j<capa_oculta.length;j++)
					{

						grad_local_salida[j][k]+=ritmo_aprendizaje*señal_error_salida[k]*capa_oculta[j].out;
					}
				}

				//para las sinapsis entrada-->ocultas:
				for (int j=0;j<capa_oculta.length;j++)
				{  
					double suma=0;
					for (int k=1;k<capa_salida.length;k++) 
					{
						//      traza ("[k][j]"+k+" "+j+"");
						//      traza (""+oculta_salida[j][k].w);
						suma+=señal_error_salida[k]*oculta_salida[j][k].w;
					}
					señal_error_oculta[j]=suma*capa_oculta[j].derivada;//capa_oculta[j].out*(1-capa_oculta[j].out);
					for (int i=0;i<capa_entrada.length;i++)
					{
						grad_local_oculta[i][j]+=ritmo_aprendizaje*señal_error_oculta[j]*capa_entrada[i].out;

					}
				}


				//calcular el incremento parcial de los pesos y umbrales (gradientes)
				//ya esta
				//calculo el incremento del error actual    
				for (int k=1;k<capa_salida.length;k++)
				{
					error_actual+=0.5*Math.pow((salida_deseada[k]-capa_salida[k].out),2.0);
					varianza+=(150*salida_deseada[k]-150*capa_salida[k].out)*(150*salida_deseada[k]-150*capa_salida[k].out);
				}

				//end bucle  para cada patron
				linea=file_datos.readLine();

			}     
			//actualizar pesos y umbrales, en las sinapsis

			//actualizamos las sinapsis oculta-->salida
			for (int j=0;j<capa_oculta.length;j++)
			{
				for (int k=1;k<capa_salida.length;k++)
				{
					//traza ("j,k-->"+j+","+k);
					oculta_salida[j][k].w+=grad_local_salida[j][k];
				}
			}
			//actualizamos las sinapsis entrada--->oculta
			for (int i=0;i<capa_entrada.length;i++)
			{
				for (int j=0;j<capa_oculta.length;j++)
				{
					entrada_oculta[i][j].w+=grad_local_oculta[i][j];
				}
			}
			//calcular el error actual 

			//ya esta
			//impresion en pantalla de resultados provisionales

			//Comprobacion de condicion de salida
			//el problema es que no se determinar un umbral razonable a priori
			//para el error. Vamos a usar el valor promedio, pues no depende
			//del numero de patrones
			//osea , trabajamos con el valor promedio por epoca del error cuadrático medio
			//ojo:tb pienso que habría que dividir entre el numero de salidas, puesto que a
			//mas salidas, mayor es la funcion coste, aunque los libros de esta materia ni siquiera dividen
			//entre p (he encontrado un articulo donde si que lo hacen)

			if ((error_actual/p)<=umbral)  break;
			//  if ((int)(epocas/100)==epocas/100.0f) 
			//traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);
			this.varianza=varianza;
			//actualizamos el ritmo de aprendizaje
			//para implementar un ritmo de aprendizaje adaptativo
			//con este sistema obtengo el mayor ritmo de aprendizaje posible en cada momento
			//creciendo y decreciendo segun el error de la epoca
			if (error_anterior>=error_actual)//+0.0000001f) 
			{
				//ok veamos si podemos subir el ritmo
				if (err_ant_ant>=error_anterior)
					ritmo_aprendizaje +=aumento_ritmo;
				//      ritmo_aprendizaje +=ritmo_aprendizaje*0.1;//

				//aumento_ritmo=      aumento_ritmo*2;

				//0.0000001f;
				//ritmo_aprendizaje *=2;//+=0.000001f;

			}
			else 
			{//lo decremento, pero no puede hacerse negativo
				if (ritmo_aprendizaje>aumento_ritmo)    
					ritmo_aprendizaje -=aumento_ritmo;//0.0000001f;
				//          ritmo_aprendizaje -=ritmo_aprendizaje*0.1;//
				//aumento_ritmo=      aumento_ritmo/2;
			}
			err_ant_ant=error_anterior;
			error_anterior=error_actual;

			//end bucle 

		}  

		//si no ha convergido, retornamos false
		//traza ("Error de epoca " +epocas+":"+error_actual);
		traza ("v:"+varianza+"  Error de epoca " +epocas+":"+error_actual+ "  ritmo:"+ritmo_aprendizaje);

		if ((error_actual/p)>umbral)  return false;
		return true;

	}   //end backpropagation
	//---------------------------------
	//------------------------------------------    
	public boolean fast(int max_iteraciones, String file_samples, float mipaso,double maxpaso)
	{

		double paso=(double) mipaso;
		float varianza_min=1000000000;//varianza minima 10M
		float varianza=0;        
		float old_varianza=-1;       
		float error_actual=0.0f;

		double iteraciones=-1;
		int num_mejoras=0;
		//ahora calculamos la varianza sin mutar
		error_actual=getError(file_samples);
		float error_new=error_actual;
		float error_ini=getError(file_samples);

		//bucle
		int capa=0;
		for (int it=0;it<max_iteraciones;it++){
			//para cada neurona de cada capa voy cambiando w y busco el minimo error 
			for (int i=0;i<num_neuronas_entrada;i++)
			{

				for (int j=0;j<num_neuronas_oculta;j++)
				{
					double w_min=entrada_oculta[i][j].w;  
					double w=(-1)*maxpaso;    
					while (w<maxpaso)
					{
						w=w+paso;
						error_new=getError(file_samples);
						entrada_oculta[i][j].w=w;
						if (error_new<error_actual)
						{
							error_actual=error_new;
							w_min=w;
							num_mejoras+=1;
						}           
					}//while
					entrada_oculta[i][j].w=w_min;  
					traza(" mejoras: "+num_mejoras+" error_ini:"+error_ini+"  errorfinal:"+error_actual);  

				}//for j
			}   //for i
			capa=1;

			//hacemos lo mismo
			//para cada neurona de cada capa voy cambiando w y busco el minimo error 
			for (int i=0;i<num_neuronas_oculta;i++)
			{

				for (int j=1;j<num_neuronas_salida;j++)
				{
					double w_min=oculta_salida[i][j].w;   
					double w=w=(-1)*maxpaso;  
					while (w<10)
					{
						w=w+paso;
						error_new=getError(file_samples);
						entrada_oculta[i][j].w=w;
						if (error_new<error_actual)
						{
							error_actual=error_new;

							w_min=w;
							num_mejoras+=1;
						}           
					}//while
					oculta_salida[i][j].w=w_min; 
					traza(" mejoras: "+num_mejoras+" error_ini:"+error_ini+"  errorfinal:"+error_actual);  

				}//for j
			}   //for i



		}//end for iteraciones
		traza(" mejoras: "+num_mejoras+" error_ini:"+error_ini+"  errorfinal:"+error_actual);   

		return true;

	}   //end funcion

	public double[] extraeModeloSalida(String datos){
		double[]salidas = new double[num_neuronas_salida+1];
		SuperFile file_datos=new SuperFile();
		file_datos.openRead(datos);
		String linea=file_datos.readLine();
		while (linea!=null)  
		{
			double[] patron=extraerEntradas(linea);
			run(patron);
			for (int x=1;x<salidas.length;x++)
			{
				salidas[x]=capa_salida[x].out;   
			}


			linea=file_datos.readLine();
		}

		return salidas;

	}
	//-----------------------------------------

}//end class Rna