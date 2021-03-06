package camara;

import corba.instantanea.*;
import corba.khepera.escenario.EscenarioD;
import corba.khepera.escenario.RectanguloD;
import corba.camara.*;
import corba.consola.ConsolaInt;
import corba.robot.*;
import khepera.escenario.Escenario;

import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

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

import java.util.Iterator;

public class CamaraIntServerImpl extends corba.camara.CamaraIntPOA implements javax.jms.MessageListener {

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
    Boolean background = true;
	
   private org.omg.PortableServer.POA poa_;
   private org.omg.CORBA.ORB orb_;

   LinkedBlockingQueue<String> listaRobots = new LinkedBlockingQueue<String>();
   //private LinkedBlockingQueue<String> buffRobots = new LinkedBlockingQueue<String>();
   LinkedBlockingQueue<String> listaConsolas = new LinkedBlockingQueue<String>();
   //private LinkedBlockingQueue<String> buffConsolas = new LinkedBlockingQueue<String>();
   private LinkedList<EstadoRobotD> listaEstados = new LinkedList<EstadoRobotD>();
   InstantaneaD instantanea;
   EscenarioD escenario = new EscenarioD();
   int lastIdRobot;
   int lastIdConsola;
   private IPYPortD ipyport;
   private int escmodifier=3;


    public CamaraIntServerImpl(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa, IPYPortD iport) 
    {
        orb_ = orb;
        poa_ = poa;
        ipyport = new IPYPortD(iport.ip, iport.port);

        lastIdRobot = 0;
        lastIdConsola = 0;
        
     // look up the ConnectionFactory
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
        	connection.setClientID("Camara"+Math.random()+"/"+destName);
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
        escenario.recs = new RectanguloD[1];
        escenario.nrecs = 1;
        escenario.color = 1;
        escenario.recs[0] = new RectanguloD(100,100, 0, 0, 0);
    }


    public org.omg.PortableServer.POA
    _default_POA()
    {
        if(poa_ != null)
            return poa_;
        else
            return super._default_POA();
    }

    //
    // IDL:corba/Camara/CamaraInt/SuscribirRobot:1.0
    //
    @SuppressWarnings("unchecked")
	public suscripcionD SuscribirRobot(String IORrob)
    {
    	suscripcionD ret = null;
    	listaRobots.add(IORrob);
    	ret = new suscripcionD(lastIdRobot++,ipyport, escenario);
    	return ret;
    }
    
    public void start(){
        new CamaraDifusion(ipyport).start();
    }

    //------------------------------------------------------------------------------
    // La clase anidada CamaraDifusion
    //------------------------------------------------------------------------------
    class CamaraDifusion extends Thread{
     //private Difusion difusion;
     
      //------------------------------------------------------------------------------
      public CamaraDifusion(IPYPortD iport){
         //difusion = new Difusion(iport);
    	  
      }

      //------------------------------------------------------------------------------
      public void run(){
        corba.instantanea.EstadoRobotDHolder st;
        String ior=null;
        LinkedList<String> listaFallos = new LinkedList<String>();

		
         while(true){
           listaEstados.clear();
           listaFallos.clear();
           
           for (Iterator<String> i = listaRobots.iterator(); i.hasNext();){
             try {
            	 ior = i.next();
                //EJERCICIO: invocar via CORBA el metodo ObtenerEstado y anyadir
               //el estado del robot correspondiente a la lista de estados
            	 
            	 //org.omg.CORBA.Object ncobj=orb_.resolve_initial_references("NameService");
     			 //NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);
    			 
            	 org.omg.CORBA.Object obj = orb_.string_to_object(ior);
    			 
     			 RobotSeguidorInt status = corba.robot.RobotSeguidorIntHelper.narrow(obj);
     			st = new EstadoRobotDHolder();
     			
     			 status.ObtenerEstado(st);
     			if(escmodifier<3)status.ModificarEscenario(escenario);
     			EstadoRobotD toad = new EstadoRobotD(new String(st.value.nombre),st.value.id,new String(st.value.IORrob),
						null,//TODO: testear si esto tira con "status"
						st.value.puntrob,st.value.posObj,
						st.value.idLider);
     			
     			listaEstados.add(toad);
     			 
             } catch (Exception  e){
                 System.out.println("Detectado fallo Robot: " + ior );
                 e.printStackTrace();
                 listaFallos.add(ior);
               
            } 
          }
           
           //i = bufferConsolas.iterator();
           for (Iterator<String> i = listaConsolas.iterator(); i.hasNext();){
             try {
            	 ior = i.next();
                //EJERCICIO: invocar via CORBA el metodo ObtenerEstado y anyadir
               //el estado del robot correspondiente a la lista de estados          
            	 
            	 // org.omg.CORBA.Object ncobj=orb_.resolve_initial_references("NameService");
     			 // NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);
            	 org.omg.CORBA.Object obj = orb_.string_to_object(ior);
    			 
     			 ConsolaInt status = corba.consola.ConsolaIntHelper.narrow(obj);
     			 if(!status.estoyviva()) throw new Exception();
     			if(escmodifier<3)status.ModificarEscenario(escenario);
     			 
             } catch (Exception  e){
                 System.out.println("Detectado fallo Consola: " + ior );
                 e.printStackTrace();
                 listaFallos.add(ior);
            } 
          }
           
           Object i1[] = listaFallos.toArray();
           for (int j=0; j<i1.length;j++ )
               BajaRobot((String) i1[j]);
           
           //EJERCICIO: crear una instantanea a partir de la lista de estados de los robots. 
           instantanea = new InstantaneaD((EstadoRobotD[]) listaEstados.toArray(new EstadoRobotD[0])); 
           
           //EJERCICIO: difundir la instantanea 
           //Difusion dif = new Difusion(ipyport);
           //dif.sendObject(instantanea);
           try {
        	ObjectMessage snd = pub_session.createObjectMessage(instantanea);
			publisher.publish(dest,snd);
			if(escmodifier<3)escmodifier++;
		} catch (JMSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	public void BajaConsola(String arg0) {
		// TODO Auto-generated method stub
		listaConsolas.remove(arg0);
	}


	@Override
	public void BajaRobot(String arg0) {
		// TODO Auto-generated method stub
		if(!listaRobots.remove(arg0))BajaConsola(arg0);
	}


	@Override
	public void ModificarEscenario(EscenarioD arg0) {
		escenario = arg0;
		ListaSuscripcionD lista = ObtenerLista();
		String ior;
		escmodifier=0;
		/*for(int i = 0;i<(lista.IORconsolas.length+lista.IORrobots.length);i++){
			if(i<lista.IORconsolas.length){
				ior = lista.IORconsolas[i];
       	 		org.omg.CORBA.Object ncobj=orb_.resolve_initial_references("NameService");
       	 		NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);
				org.omg.CORBA.Object obj = nc.resolve_str(ior);
			 
				ConsolaInt status = corba.consola.ConsolaIntHelper.narrow(obj);
				status.ModificarEscenario(arg0);
			}else{
				ior = lista.IORrobots[i-lista.IORrobots.length];
           	 	org.omg.CORBA.Object ncobj=orb_.resolve_initial_references("NameService");
    			NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);
    			org.omg.CORBA.Object obj = nc.resolve_str(ior);
   			 
    			RobotSeguidorInt status = corba.robot.RobotSeguidorIntHelper.narrow(obj);
    			status.ModificarEscenario(arg0);
			}
		}*/
	}


	@Override
	public EscenarioD ObtenerEscenario() {
		// TODO Auto-generated method stub
		return escenario;
	}


	@Override
	public IPYPortD ObtenerIPYPortDifusion() {
		// TODO Auto-generated method stub
		return ipyport;
	}


	@Override
	public InstantaneaD ObtenerInstantanea() {
		// TODO Auto-generated method stub
		return instantanea;
	}


	@Override
	public ListaSuscripcionD ObtenerLista() {
		// TODO Auto-generated method stub
		ListaSuscripcionD lista = new ListaSuscripcionD();
		lista.IORconsolas = listaConsolas.toArray(new String[0]);
		lista.IORrobots = listaRobots.toArray(new String[0]);
		return lista;
	}


	@Override
	public suscripcionD SuscribirConsola(String IORrob) {
		suscripcionD ret = null;
    	if(!listaConsolas.contains(IORrob)) {
    		listaConsolas.add(IORrob);
    	}
   		lastIdConsola++;
    	ret = new suscripcionD(lastIdConsola,ipyport, escenario);
    	return ret;
	}


	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		/*if(background){
			ObjectMessage obj = (ObjectMessage) arg0;
			try {
				instantanea = (InstantaneaD) obj.getObject();
				listaRobots.clear();
				for(int i = 0;i<instantanea.estadorobs.length;i++){
					listaRobots.add(instantanea.estadorobs[i].IORrob);
					
				}
				lastIdRobot = listaRobots.size();
				System.out.println("Instantanea received");
			} catch (JMSException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} *///esto es para cuando haya que implementar integridad
	}
}
