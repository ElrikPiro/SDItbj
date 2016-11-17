package robot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Time;
import java.util.Properties;

import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
// import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.ThreadPolicyValue;
/*MODIFICADO*/
import corba.camara.*;
/*FIN MODIFICADO*/

public class Server_AOM {
	  /*MODIFICADO*/	
	  static CamaraInt camara;
	  static int ok=0;
	  /*FIN MODIFICADO*/
	public static void main(String[] args) {

		Properties props = System.getProperties();
		props.setProperty("org.omg.CORBA.ORBClass", "com.sun.corba.se.internal.POA.POAORB");
		props.setProperty("org.omg.CORBA.ORBSingletonClass", "com.sun.corba.se.internal.corba.ORBSingleton");
		// Solo si se cambia el host 
		props.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");
		// Solo si se cambia el port 
		props.put("org.omg.CORBA.ORBInitialPort", "1050");
		try {
			// Initialize the ORB.
			org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, props);

			// get a reference to the root POA
			org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");
			POA poaRoot = POAHelper.narrow(obj);

			// Create policies for our persistent POA
			org.omg.CORBA.Policy[] policies = {
					// poaRoot.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
					poaRoot.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
					poaRoot.create_thread_policy(ThreadPolicyValue.ORB_CTRL_MODEL) 
			};

			// Create myPOA with the right policies
			POA poa = poaRoot.create_POA("RobotSeguidorIntServerImpl_poa",	poaRoot.the_POAManager(), policies);

			// Create the servant
			RobotSeguidorIntServerImpl servant = new RobotSeguidorIntServerImpl();

			// Activate the servant with the ID on myPOA
			byte[] objectId = "Robot".getBytes();
			poa.activate_object_with_id(objectId, servant);
			
			// Activate the POA manager
			poaRoot.the_POAManager().activate();

			// Get a reference to the servant and write it down.
			obj = poa.servant_to_reference(servant);

			// ---- Uncomment below to enable Naming Service access. ----
			 org.omg.CORBA.Object ncobj = orb.resolve_initial_references("NameService");
			 NamingContextExt nc = NamingContextExtHelper.narrow(ncobj);

			//PrintWriter ps = new PrintWriter(new FileOutputStream(new File("server.ior")));
			//ps.println(orb.object_to_string(obj));
			//ps.close();

			/*MODIFICADO*/			
		    do{
		        try{
		        	//EJERCICIO:Conectar con el servidor de nombre y obtener una referencia 
		        	//a la **camara*
		        	org.omg.CORBA.Object camobj = nc.resolve_str("Camara");
					camara = CamaraIntHelper.narrow(camobj);

		          System.out.println("Identificador: " + servant.toString());
			         //EJERCICIO: convertir la referencia al robot en un IOR en formato String 
		         
		          if (args.length>0) servant.minombre = args[0]; else servant.minombre="Robot"+Math.random();
		          servant.orb = orb;
		          servant.camara = camara;
		          servant.miIOR = orb.object_to_string(obj);
		          ok=1;
		        } catch(Exception ex) {
		          System.out.println("El robot no se registro bien en la camara. Reintentando...");
		          ex.printStackTrace();
		          //System.exit(0);
		        }
		      } while(ok==0);

		      servant.start();
			  /*FIN MODIFICADO*/
	
			
			System.out.println("CORBA Server ready...");

			// Wait for incoming requests
			orb.run();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
