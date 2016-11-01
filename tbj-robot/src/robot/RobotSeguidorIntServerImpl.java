package robot;

import corba.instantanea.*;
import corba.khepera.escenario.EscenarioD;
import corba.khepera.robot.PosicionD;
import corba.camara.*;

/**
 * This class is the implementation object for your IDL interface.
 *
 * Let the Eclipse complete operations code by choosing 'Add unimplemented methods'.
 */
public class RobotSeguidorIntServerImpl extends corba.robot.RobotSeguidorIntPOA {

    org.omg.CORBA.ORB orb;
    CamaraInt camara;

    String minombre;
    int miid;
    String miIOR;
    
    private InstantaneaD instantanea;
    
	/**
	 * Constructor for RobotSeguidorIntServerImpl 
	 */
	public RobotSeguidorIntServerImpl() {
	}

	public void ObtenerEstado(EstadoRobotDHolder est) {
		// TODO Auto-generated method stub

		//EJERCICIO: componer la instantanea a partir de EstadoRobotD y retornarla
	        //return _r;
		//EstadoRobotD _r = new EstadoRobotD(minombre,miid,miIOR, null, null, null, miid);
	        //est.value = _r; // new corba.instantanea.EstadoRobotD();
		
	}
	
    public void start(){
        new RobotDifusion().start();
    }

    //------------------------------------------------------------------------------
    // La clase anidada RobotDifusion
    //------------------------------------------------------------------------------

    class RobotDifusion extends Thread{

      //private Difusion difusion;
      private EstadoRobotD sr;
      private suscripcionD sus;

      public void run(){
      //EJERCICIO: suscribir el robot en la camara
    	  sus = camara.SuscribirRobot(miIOR);
      //EJERCICIO: crear la difusion
    	  //difusion = new Difusion(sus.iport);
    	  miid=sus.id;

        while(true){
           //EJERCICIO: recibir instantanea
        	//instantanea = (InstantaneaD) difusion.receiveObject();
           //EJERCICIO: iterar sobre la lista de estados, imprimiendo el nombre de
           //todos los robots cuyo estado figura en la instantanea.
        	for(int v = 0; v<instantanea.estadorobs.length;v++){
	            sr = instantanea.estadorobs[v];
        		System.out.println("Robot " + sr.id + " : " + sr.nombre);
        	}
	          
          try{
            Thread.sleep(400);
          }catch(InterruptedException e){
            e.printStackTrace();
          }
        }
      }
    }

	@Override
	public void ModificarEscenario(EscenarioD arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ModificarLider(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ModificarObjetivo(PosicionD arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ModificarPosicion(PosicionD arg0) {
		// TODO Auto-generated method stub
		
	}
}
