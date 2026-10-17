package jvn;

import java.io.Serializable;

/**
 * @author chaym
 * JnvObejctImpl va implementer l'interface JvnObject et il va memorider
 * - l'ID de Sentence
 * - Sentence lui-même
 * - l'état du verrou
 **/
public class JvnObjectImpl implements JvnObject{

	private static final long serialVersionUID = 1L;
	
    private final int joi; //Jvn Object Identifier
    private Serializable object;
    private transient JvnServerImpl server;
    public enum LockState {
        NL,
        R,
        W,
        RC,
        WC,
        RWC
    }
    private LockState lock;

    public JvnObjectImpl(int joi , Serializable object, JvnServerImpl server, LockState lock) {
        this.joi = joi;

        this.object = object;
        this.server = server;
        this.lock = lock;
    }
    
	public void setServer(JvnServerImpl server) {
		this.server = server;
	}

    @Override
    public int jvnGetObjectId() throws JvnException {
        return joi;
    }

    @Override
    public Serializable jvnGetObjectState() throws JvnException {
        return object;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        synchronized (this){

            switch (lock) {

                case LockState.NL :
                    Serializable newState = server.jvnLockRead(joi);
                    object = newState;
                    lock = LockState.R;
                    break;

                case LockState.RC :
                    lock = LockState.R;
                    break;
            

                case LockState.WC :
                    lock = LockState.RWC;
                    break;

                case LockState.R :
			    case LockState.W :
			    case LockState.RWC :
                    // we do nothing since we already have the required rights
                    break;
            }

        }

    }

    @Override
    public void jvnLockWrite() throws JvnException {
        synchronized (this){

            switch (lock) {

                case LockState.RC :
                case LockState.NL :
                    Serializable newState = server.jvnLockWrite(joi);
                    object = newState;
                    lock = LockState.W;
                    break;
            
			    case LockState.RWC :
                case LockState.WC :
                    lock = LockState.W;
                    break;

			    case LockState.W :
                    // we already have the right to write
                    break;

            }

        }

    }

    @Override
    public void jvnUnLock() throws JvnException {
       synchronized (this){

            switch (lock) {
                case LockState.R :
                    lock = LockState.RC;
                    break;
			    case LockState.W :
                    lock = LockState.WC;
                    break;
			    case LockState.RWC :
                    lock = LockState.WC;
                    break;
            }
            notifyAll();
        }


    }


    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        waitWHileBusy(LockState.R);
        lock = LockState.NL;
        notifyAll();
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        waitWHileBusy(LockState.W);
        lock = LockState.NL;
        notifyAll();
        return object;
        
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        waitWHileBusy(LockState.W);
        lock = LockState.RC;
        notifyAll();
        return object;
    }

    public void waitWHileBusy (LockState busy) {
        while(lock == busy) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Indique si l'objet est en cours d'utilisation
    //R,W,RWC correspondent a une verrou actif
    public synchronized boolean isBusy(){
        return lock == LockState.R || lock == LockState.W || lock == LockState.RWC;
    }

    //Revoie l'etat metier de l'objet
    //La mehode est synchrose pour lire l'etat local
    //sous le meme moniteur que les operation sur le verrou
    public synchronized Serializable getObjectStateForFlush(){
        return object;
    }
}

