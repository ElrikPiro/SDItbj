package robot;

import corba.instantanea.*;
import corba.khepera.escenario.EscenarioD;
import corba.khepera.robot.PosicionD;
import corba.robot.RobotSeguidorInt;
import khepera.control.Braitenberg;
import khepera.control.Destino;
import khepera.control.Trayectoria;
import khepera.escenario.Escenario;
import khepera.robot.IzqDer;
import khepera.robot.KheperaInt;
import khepera.robot.Polares;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

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
    int milider=miid;
    String miIOR;
    PosicionD inicio = new PosicionD(10,10);
    PosicionD objetivo = new PosicionD(50,50);
    EscenarioD esc;
    khepera.robot.RobotKhepera robotillo;
    Trayectoria tra;
    Destino dst = new Destino();
    Braitenberg bra = new Braitenberg();
    
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
    String destName = "228.7.7.7_7010";
    
	/**
	 * Constructor for RobotSeguidorIntServerImpl 
	 */
	public RobotSeguidorIntServerImpl() {
		try {
			Properties env = new Properties( );
            // ActiveMQ
            env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            
            context = new InitialContext(env);
			
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

		est.value = new corba.instantanea.EstadoRobotD(new String(this.minombre),miid,miIOR,
						this._this(),
						robotillo.posicionRobot(),robotillo.posicionRobot().centro,
						milider);
//		est.value.id = miid;
//		est.value.idLider = milider;
//		est.value.IORrob = miIOR;
//		est.value.posObj = robotillo.posicionRobot().centro;
//		est.value.puntrob = robotillo.posicionRobot();
//		est.value.refrob = this._this();
//		est.value.nombre = this.minombre;
		
	        //est.value = _r; // new corba.instantanea.EstadoRobotD();
		
	}
	
    public void start(){
    	Escenario e = new Escenario(camara.ObtenerEscenario());
		robotillo = new khepera.robot.RobotKhepera(inicio, e, 0);
        new RobotDifusion().start();
    }

    //------------------------------------------------------------------------------
    // La clase anidada RobotDifusion
    //------------------------------------------------------------------------------

    class RobotDifusion extends Thread{

      //private Difusion difusion;
      
      private suscripcionD sus;

      public void run(){
      //EJERCICIO: suscribir el robot en la camara
    	  sus = camara.SuscribirRobot(miIOR);
      //EJERCICIO: crear la difusion
    	  //difusion = new Difusion(sus.iport);
    	  miid=sus.id;
    	  esc=sus.esc;
  		robotillo = new khepera.robot.RobotKhepera(robotillo.posicionRobot().centro, new Escenario(esc), 0);		
   	  
    	  
    	  Polares posActual;
    	  PuntosRobotD puntosAct;
    	  IzqDer nv, nv2;
    	  
    	  do{}while(instantanea==null);
    	  while(true){
    		  posActual = robotillo.posicionPolares();
    		  puntosAct = robotillo.posicionRobot();
    		  
    		  tra = new Trayectoria(posActual,objetivo);
    		  float[] ls = robotillo.leerSensores();
    		  nv = dst.calcularVelocidad(tra);
    		  nv2 = bra.calcularVelocidad(ls);
    		  
    		  nv.izq += nv2.izq/90; nv.der+=nv2.der/90;
    		  robotillo.fijarVelocidad(nv.izq, nv.der);
    		  robotillo.avanzar();
    		  try{Thread.sleep(400);}catch(Exception e){}
    	  }
      }
    }

	@Override
	public void ModificarEscenario(EscenarioD arg0) {
		esc=arg0;
		robotillo = new khepera.robot.RobotKhepera(robotillo.posicionRobot().centro, new Escenario(esc), 0);		
	}

	@Override
	public void ModificarLider(int arg0) {
		milider=arg0;
	}

	@Override
	public void ModificarObjetivo(PosicionD arg0) {
		objetivo = arg0;
		milider=miid;
	}

	@Override
	public void ModificarPosicion(PosicionD arg0) {
		robotillo.fijarPosicion(arg0);
	}

	@Override
	public void onMessage(Message arg0) {
		EstadoRobotD sr;
		boolean imiin = false;
		ObjectMessage obj = (ObjectMessage) arg0;
		try {
			instantanea = (InstantaneaD) obj.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	for(int v = 0; v<instantanea.estadorobs.length;v++){
            sr = instantanea.estadorobs[v];
            imiin = (sr.nombre == minombre) || imiin;//esto servira mas adelante para la tolerancia a fallos
            if(milider!=miid && sr.id==milider) objetivo = sr.puntrob.centro;
    	}
    	
	}
}
