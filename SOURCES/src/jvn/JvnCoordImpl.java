/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.io.Serializable;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

	
  private static final long serialVersionUID = 1L;


  /**
	 * this is therepresentation of each exposed object from the coordinator's prespective
	 */
  private class ObjectInCoord {
    Serializable object; // object itself
    JvnRemoteServer writer; // client with the permission to write
    Set<JvnRemoteServer> readers = new HashSet<JvnRemoteServer>(); // client(s) with the permission to read
  }


  private int nextId; // the object IDs
  private Hashtable<Integer, ObjectInCoord> objects; //each object representation paired with it's ID
  private Hashtable<String , Integer> names; // each object ID paired with the object's symbolic name

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		super();
        nextId = 1;
        objects = new Hashtable<Integer, ObjectInCoord>();
        names = new Hashtable<String, Integer>();
	}
	
	/**
	 * Starts the coordinator and binds it in the RMI registry.
	 */
	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
			// registry likely already running, ignore
		}
		try {
			JvnCoordImpl coord = new JvnCoordImpl();
			Naming.rebind("rmi://localhost/JvnCoord", coord);
			System.out.println("JvnCoord ready");
		} catch (Exception e) {
			System.out.println("JvnCoord problem : " + e.getMessage());
		}
	}


  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public synchronized int jvnGetObjectId()  throws java.rmi.RemoteException,jvn.JvnException {
      return nextId++;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)  throws java.rmi.RemoteException,jvn.JvnException{
    
        int id = jo.jvnGetObjectId();
            
        synchronized(this) {
        names.put(jon, id);
        
        if( !objects.containsKey(id)) {
            ObjectInCoord newObject = new ObjectInCoord();
            newObject.object = jo.jvnGetObjectState();
            newObject.writer = js;
            
            objects.put(id, newObject);

        }

        }

  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
    
        Integer id;
        ObjectInCoord objetInfo;

        synchronized (this) {
            id = names.get(jon);
            
            if (id == null) {
                return null;
            }

            objetInfo = objects.get(id);
            if (objetInfo == null) {
			return null;
		    }

            // we give him a no lock because the asker just lookedup the object, so they have no locks in it yet
            return new JvnObjectImpl(id, objetInfo.object, null, JvnObjectImpl.LockState.NL);

        }

  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
        
        ObjectInCoord objetInfo = objects.get(joi);
        if(objetInfo == null) {
            throw new JvnException("Unknown object id: " + joi);
        }

        synchronized(objetInfo) {
            if(objetInfo.writer != null && objetInfo.writer != js) {
                // if another client already has a write lock, then downgrade it to a reader, so both askers have a read lock
                Serializable newState = objetInfo.writer.jvnInvalidateWriterForReader(joi);
                objetInfo.object = newState;
                objetInfo.readers.add(objetInfo.writer);
                objetInfo.writer = null;
            }

            objetInfo.readers.add(js);
            return objetInfo.object;
        }

   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    
        ObjectInCoord objetInfo = objects.get(joi);
        if(objetInfo == null) {
            throw new JvnException("Unknown object id: " + joi);
        }

        synchronized(objetInfo) {
            if(objetInfo.writer != null && objetInfo.writer != js) {
                // if another client already has a write lock, then remove it, only the asker has a writer lok at the end
                Serializable newState = objetInfo.writer.jvnInvalidateWriter(joi);
                objetInfo.object = newState;
                objetInfo.writer = null;
            }

            for (JvnRemoteServer reader : new HashSet<JvnRemoteServer>(objetInfo.readers)) {
				if (reader != js) {
					reader.jvnInvalidateReader(joi);
				}
			}

            objetInfo.readers.clear();
            objetInfo.writer = js;
            return objetInfo.object;
        }

   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 synchronized (this) {
        for (ObjectInCoord objectInfo : objects.values()) {
				if (objectInfo.writer == js) {
					objectInfo.writer = null;
				}
				objectInfo.readers.remove(js);
			}
     }
    }
}

 
