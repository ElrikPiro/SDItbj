package camara;

import corba.instantanea.*;
import corba.khepera.escenario.EscenarioD;
import corba.camara.*;
import corba.robot.*;
import java.util.LinkedList;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Iterator;

public class CamaraIntServerImpl extends corba.camara.CamaraIntPOA {

   private org.omg.PortableServer.POA poa_;
   private org.omg.CORBA.ORB orb_;

   private LinkedList<String> listaRobots = new LinkedList<String>();
   private LinkedList<EstadoRobotD> listaEstados = new LinkedList<EstadoRobotD>();
   InstantaneaD instantanea;
   private int nrobots;
   private IPYPortD ipyport;

    public

    CamaraIntServerImpl(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa, IPYPortD iport) 
    {
        orb_ = orb;
        poa_ = poa;
        ipyport = new IPYPortD(iport.ip, iport.port);
        
        nrobots = 0;
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
    	int indexRobot = listaRobots.indexOf(IORrob);
    	if(indexRobot==-1) {listaRobots.add(IORrob);indexRobot=listaRobots.indexOf(IORrob);}
    	//ret = new suscripcionD(indexRobot,ipyport, null);
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
        corba.instantanea.EstadoRobotDHolder st = new EstadoRobotDHolder();
        String ior=null;
        LinkedList listaFallos = new LinkedList();

         while(true){
           listaEstados.clear();
           listaFallos.clear();
           Iterator<String> i = listaRobots.iterator();
           for (; i.hasNext(); ){
             try {
                //EJERCICIO: invocar via CORBA el metodo ObtenerEstado y anyadir
               //el estado del robot correspondiente a la lista de estados          
            	 ior = (String) i.next();
            	 org.omg.CORBA.Object ncobj=orb_.resolve_initial_references("NameService");
     			 NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);
    			 org.omg.CORBA.Object obj = nc.resolve_str(ior);
    			 
     			 RobotSeguidorInt status = corba.robot.RobotSeguidorIntHelper.narrow(obj);
     			 status.ObtenerEstado(st);
     			 listaEstados.add(st.value);
     			 
             } catch (Exception  e){
                 System.out.println("Detectado fallo Robot: " + ior );
                 listaFallos.add(ior);
               
            } 
          }
           
           Object i1[] = listaFallos.toArray();
           for (int j=0; j<i1.length;j++ )
               listaRobots.remove(i1[j]);
           
           //EJERCICIO: crear una instantanea a partir de la lista de estados de los robots. 
           instantanea = new InstantaneaD((EstadoRobotD[]) listaEstados.toArray(new EstadoRobotD[0])); 
           
           //EJERCICIO: difundir la instantanea 
           //Difusion dif = new Difusion(ipyport);
           //dif.sendObject(instantanea);
           
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
		
	}


	@Override
	public void BajaRobot(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void ModificarEscenario(EscenarioD arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public EscenarioD ObtenerEscenario() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IPYPortD ObtenerIPYPortDifusion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public InstantaneaD ObtenerInstantanea() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ListaSuscripcionD ObtenerLista() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public suscripcionD SuscribirConsola(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
