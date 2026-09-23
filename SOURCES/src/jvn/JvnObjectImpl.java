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

    private int idObject; //Jvn Object Identifier
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

    @Override
    public void jvnLockRead() throws JvnException {

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
