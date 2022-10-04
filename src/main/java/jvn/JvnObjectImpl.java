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
    switch (lockState) {
      case RC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RWC;
      case NL -> {
        object = server.jvnLockRead(id);
        lockState = JvnLock.R;
      }
      default -> throw new JvnException("Read lock not possible");
    }
  }

  @Override
  public void jvnLockWrite() throws JvnException {
    switch (lockState) {
      case WC, RWC -> lockState = JvnLock.W;
      case NL, RC, R -> {
        object = server.jvnLockWrite(id);
        lockState = JvnLock.W;
      }
      default -> throw new JvnException("Write lock not possible");
    }
  }

  @Override
  public void jvnUnLock() throws JvnException {
    switch (lockState) {
      case R -> lockState = JvnLock.RC;
      case W -> lockState = JvnLock.WC;
      case RWC -> lockState = JvnLock.NL;
      default -> throw new JvnException("Unlock not possible");
    }
    lockState.notifyAll();
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
      case R, RWC -> {
        synchronized (lockState) {
          while (lockState == JvnLock.R) {
            try {
              lockState.wait();

            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          lockState = lockState.NL;
        }
      }
      case RC -> lockState = JvnLock.NL;
      case NL -> throw new JvnException("Object is not locked");
      default -> throw new JvnException("Invalid lock state");
    }


  }

  @Override
  public Serializable jvnInvalidateWriter() throws JvnException {
    switch (lockState) {
      case W, RWC -> {
        synchronized (lockState) {
          while (lockState == JvnLock.W || lockState == JvnLock.RWC) {
            try {
              lockState.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          lockState = JvnLock.NL;
        }
      }
      case WC -> lockState = JvnLock.NL;
      case NL -> throw new JvnException("Object is not locked");
      default -> throw new JvnException("Invalid lock state");
    }
    return this;
  }

  @Override
  public Serializable jvnInvalidateWriterForReader() throws JvnException {
    switch (lockState) {
      case RWC -> lockState = JvnLock.R;
      case WC -> lockState = JvnLock.RC;
      case W -> {
        synchronized (lockState) {
          while (lockState == JvnLock.W) {
            try {
              lockState.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          lockState = JvnLock.RC;
        }
      }
      default -> throw new JvnException("Invalid lock state");
    }
    return this;
  }

}
