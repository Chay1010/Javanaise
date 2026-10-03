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

        }


    }


    @Override
    public void jvnInvalidateReader() throws JvnException {
        waitWHileBusy(LockState.R);
        lock = LockState.NL;
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        waitWHileBusy(LockState.W);
        lock = LockState.NL;
        return object;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        waitWHileBusy(LockState.W);
        lock = LockState.RC;
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
}
