package robot;

import corba.instantanea.*;
import corba.khepera.escenario.EscenarioD;
import corba.khepera.robot.PosicionD;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.NamingException;

import corba.camara.*;

/**
 * This class is the implementation object for your IDL interface.
 *
 * Let the Eclipse complete operations code by choosing 'Add unimplemented methods'.
 */
public class RobotSeguidorIntServerImpl extends corba.robot.RobotSeguidorIntPOA implements javax.jms.MessageListener {

    org.omg.CORBA.ORB orb;
    CamaraInt camara;

    String minombre;
    int miid;
    String miIOR;
    
    private InstantaneaD instantanea;
    
    Context context = null;
    TopicConnectionFactory factory = null;
    TopicConnection connection = null;
    String factoryName = "ConnectionFactory";
    Topic dest = null;
    TopicSession sus_session = null;
    TopicSession pub_session = null;
    TopicSubscriber subscriber = null;
    TopicPublisher publisher = null;
    String destName = "228.7.7.7_4001";
    
	/**
	 * Constructor for RobotSeguidorIntServerImpl 
	 */
	public RobotSeguidorIntServerImpl() {
		try {
        	factory = (TopicConnectionFactory) context.lookup(factoryName);
		
        // look up the Destination
        	dest = (Topic) context.lookup("dynamicTopics/"+destName);
        // create the connection
        	connection = factory.createTopicConnection();
        	connection.setClientID(minombre+"/"+destName);
        // setId
        // create the sessions
        	sus_session = connection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        	pub_session = connection.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        // create the publisher
        	publisher = pub_session.createPublisher(dest);
        // create the receiver (take into account if should be durable)
        	subscriber = sus_session.createSubscriber(dest);
        // set message listener
        	subscriber.setMessageListener(this);
        // start the connection, to enable message receipt
        	connection.start();
        } catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
}
