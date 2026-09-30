/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


public class JvnCoordImpl
              extends UnicastRemoteObject
							implements JvnRemoteCoord{


  /**
	 *
	 */
	private static final long serialVersionUID = 1L;
    private int nextObjectId =0;

    private Hashtable<Integer, ObjectInfo> objectsById;
    private Hashtable<String, ObjectInfo> objectsByName;

    private static class ObjectInfo{
        int joi;
        String jon;

        //Dernier Etat valide de l'objet
        Serializable state;
        // Client qui possede actuellement writing lock
        JvnRemoteServer writer;

        // Clients qui possèdent actuellement un verrou de lecture
        Set<JvnRemoteServer> readers;

        ObjectInfo(int joi, String jon, Serializable state) {
            this.joi = joi;
            this.jon = jon;
            this.state = state;
            this.writer = null;
            this.readers = new HashSet<>();
        }

    }

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		super();
        objectsById = new Hashtable<>();
        objectsByName = new Hashtable<>();
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    return nextObjectId++;
  }

  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  //Register an object after its creation by the client
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    if (jon==null || jo==null || js==null){
        throw new NullPointerException("Invalid parameter");
    }

    if(objectsByName.contains(jon)){
        throw new jvn.JvnException("jvn  with the name "+jon+" already registered");
    }

    int joi = jo.jvnGetObjectId();
    if(objectsById.contains(joi)){
          throw new jvn.JvnException("jvn  with the name "+joi+" already registered");
      }

    Serializable state = jo.jvnGetObjectState();

    ObjectInfo objectInfo = new ObjectInfo(joi, jon, state);

    objectsById.put(joi, objectInfo);
    objectsByName.put(jon, objectInfo);
  }

  /**
  * Get the reference of a JVN object managed by a given JVN server
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  //Recuperer la representation de l'objet a partir de son jon
  //Client demande au cordianteur de lui renvoyer la representation de l'objet a partir de son jon
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    ObjectInfo objectInfo = objectsById.get(jon);

    if(objectInfo==null){
        return null;
    }

    return new JvnObjectImpl(objectInfo.joi, objectInfo.state,null);
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
    ObjectInfo objectInfo = objectsById.get(joi);

    if(objectInfo==null){
        throw new JvnException("jvn  with the id "+joi+" does not exist");
    }

    if(objectInfo.writer != null && objectInfo.writer != js){
        Serializable newState = objectInfo.writer.jvnInvalidateWriterForReader(joi);
        objectInfo.state = newState;
        objectInfo.readers.add(objectInfo.writer);
        objectInfo.writer = null;
    }

    //Ajout du nouveau reader
    objectInfo.readers.add(js);

    //Envoie de l'objet actuel a C2
    return objectInfo.state;
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

       ObjectInfo objectInfo = objectsById.get(joi);
       if(objectInfo==null){
           throw new JvnException("jvn  with the id "+joi+" does not exist");
       }

       if(objectInfo.writer != null && objectInfo.writer != js){
           Serializable newState = objectInfo.writer.jvnInvalidateWriter(joi);

           objectInfo.state = newState;
           objectInfo.writer = null;
       }

       for(JvnRemoteServer reader : objectInfo.readers){

           if(reader != js){
               reader.jvnInvalidateReader(joi);
           }
       }

       objectInfo.readers.clear();

       objectInfo.writer = js;


    return objectInfo.state;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 if(js == null){
         return;
     }

     for(ObjectInfo objectInfo : objectsById.values()){
         if(objectInfo.writer != null && objectInfo.writer != js){

             if(objectInfo.writer == js){
                 Serializable newState = js.jvnInvalidateWriter(objectInfo.joi);
                 objectInfo.state = newState;
                 objectInfo.writer = null;
             }

             objectInfo.readers.remove(js);
         }
     }
    }
}

 
