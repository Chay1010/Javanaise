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
    private String jon;
    private Serializable object;
    private transient JvnLocalServer server;
    private enum LockState {
        NL,
        RC,
        WC,
        R,
        W,
        RWC
    }
    private LockState lockState = LockState.NL;
    private boolean lockAcquisitionInProgress = false;

    public JvnObjectImpl(int joi, String jon, Serializable object, JvnLocalServer server) {
        this.joi = joi;
        this.jon = jon;
        this.object = object;
        this.server = server;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        synchronized (this){
            if(lockState == LockState.RC){
                lockState = LockState.R;
                return;
            }

            if(lockState == LockState.WC){
                lockState = LockState.RWC;
                return;
            }

            if(lockState == LockState.R || lockState == LockState.W || lockState == LockState.RWC){
                throw new JvnException("Impossible de prendre un verrou READ : verrou déjà utilisé.");
            }

        }

    }

    @Override
    public void jvnLockWrite() throws JvnException {

    }

    @Override
    public void jvnUnLock() throws JvnException {

    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return 0;
    }

    @Override
    public Serializable jvnGetObjectState() throws JvnException {
        return null;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {

    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        return null;
    }
}
