package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;

public class JvnObjectImpl implements JvnObject {
    Serializable object;
    int id;
    JvnLock lockState;

    JvnServerImpl server;

    public JvnObjectImpl(Serializable object, Serializable id, JvnServerImpl server) {
        this.object = object;
        // after creation , the object is in write mode
        this.id = (int) id;
        this.lockState = JvnLock.W;
        this.server = server;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        // TODO document why this method is empty
        object = server.jvnLockRead(id);


        lockState = JvnLock.R;
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        // TODO document why this method is empty
        object = server.jvnLockWrite(id);
        lockState = JvnLock.W;
    }

    @Override
    public void jvnUnLock() throws JvnException {
        switch (lockState) {
            case R -> lockState = JvnLock.RC;
            case W -> lockState = JvnLock.WC;
            case RC, WC -> lockState = JvnLock.NL;
            default -> throw new JvnException("Object is not locked");
        }
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return object;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException, RemoteException {
        switch (lockState) {
            case R, RC -> lockState = JvnLock.NL;
            case RWC -> lockState = JvnLock.WC;
            case NL -> throw new JvnException("Object is not locked");
            default -> throw new JvnException("Invalid lock state");
        }


    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        switch (lockState) {
            case W, WC -> lockState = JvnLock.NL;
            case RWC -> lockState = JvnLock.RC;
            case NL -> throw new JvnException("Object is not locked");
            default -> throw new JvnException("Invalid lock state");
        }
        return object;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        // TODO : what is the purpose of this method ?
        return object;
    }
}
