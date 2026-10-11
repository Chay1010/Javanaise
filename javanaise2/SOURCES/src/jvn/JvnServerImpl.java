/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 * 
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.Hashtable;


public class JvnServerImpl
              extends UnicastRemoteObject //Cela signifie que cette instance peut être utilisée comme objet distant RMI.
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;

	//Reference vers le coordinateur
	private JvnRemoteCoord coord;

	/*
	 * Liste des objets connus par ce serveur.
	 * clé   : ID de l'objet
	 * valeur : JvnObject correspondant
	 */
	private Hashtable<Integer, JvnObjectImpl> cachedObjects;


  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		// to be completed
		cachedObjects = new Hashtable<>();

		//connercter le JvnServeImpl au JvnCoordimpl grace a java Rmi;
		//Trouve-moi l'objet distant qui est enregistré sous ce nom.
		String coordHost = System.getProperty("coord.host", "localhost");
		coord = (JvnRemoteCoord) Naming.lookup("rmi://" + coordHost + ":1099/JvnCoord");
	}

	/**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.out.println("JvnServerImpl problem : " + e.getMessage());
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
        try {
            coord.jvnTerminate(js);
        } catch (RemoteException e) {
            throw new JvnException("RemoteException in jvnTerminate: " + e.getMessage());
        }
		cachedObjects.clear();
    }
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws JvnException {
		int id;
        try {
			id = coord.jvnGetObjectId();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        JvnObjectImpl newObject = new JvnObjectImpl(id, o, js, JvnObjectImpl.LockState.W);
		cachedObjects.put(id, newObject);
		return newObject;
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
        try {
            coord.jvnRegisterObject(jon,jo,js);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
		JvnObject jvnSearchedObject;
        try {
            jvnSearchedObject = coord.jvnLookupObject(jon, this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

		if(jvnSearchedObject == null) {
			return null;
		}

		JvnObjectImpl joi = (JvnObjectImpl) jvnSearchedObject;
		joi.setServer(this);
		cachedObjects.put(joi.jvnGetObjectId(), joi);

		return joi;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		try {
			return coord.jvnLockRead(joi, js);
		} catch (RemoteException e) {
			throw new JvnException("RemoteException in jvnLockRead: " + e.getMessage());
		}

	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		try {
			return coord.jvnLockWrite(joi, js);
		} catch (RemoteException e) {
			throw new JvnException("RemoteException in jvnLockWrite: " + e.getMessage());
		}
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		
		JvnObjectImpl obj = cachedObjects.get(joi);
		
		if (obj != null) {
			obj.jvnInvalidateReader();
		}
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		
		JvnObjectImpl obj = cachedObjects.get(joi);
		
		if (obj != null) {
			return obj.jvnInvalidateWriter();
		}

		return null;
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		
		JvnObjectImpl obj =  cachedObjects.get(joi);
		
		if (obj != null) {
			return obj.jvnInvalidateWriterForReader();
		}

		return null;
	 };

}

 
