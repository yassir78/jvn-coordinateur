/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class JvnServerImpl
    extends UnicastRemoteObject
    implements JvnLocalServer, JvnRemoteServer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  // A JVN server is managed as a singleton
  private static JvnServerImpl js = null;
  private JvnRemoteCoord coord;
  private Registry registry;


  /**
   * Default constructor
   *
   * @throws JvnException
   **/
  private JvnServerImpl() throws Exception {
    super();
    registry = LocateRegistry.getRegistry(1099);
    coord = (JvnRemoteCoord) registry.lookup("JvnCoord");

  }

  /**
   * Static method allowing an application to get a reference to a JVN server instance
   *
   * @throws JvnException
   **/
  public static JvnServerImpl jvnGetServer() {
    if (js == null) {
      try {
        js = new JvnServerImpl();
      } catch (Exception e) {
        return null;
      }
    }
    return js;
  }

  /**
   * The JVN service is not used anymore
   *
   * @throws JvnException
   **/
  public void jvnTerminate()
      throws JvnException {
    try {
      coord.jvnTerminate(this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }

  }

  /**
   * creation of a JVN object
   *
   * @param o : the JVN object state
   * @throws JvnException
   **/
  public JvnObject jvnCreateObject(Serializable o)
      throws JvnException {
    try {
      var id = coord.jvnGetObjectId();
      var jvnObject = new JvnObjectImpl(id, o, this);
      return jvnObject;

    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

  }


  /**
   * Associate a symbolic name with a JVN object
   *
   * @param jon : the JVN object name
   * @param jo  : the JVN object
   * @throws JvnException
   **/
  public void jvnRegisterObject(String jon, JvnObject jo)
      throws JvnException {
    // to be completed
    try {
      coord.jvnRegisterObject(jon, jo, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }

  /**
   * Provide the reference of a JVN object beeing given its symbolic name
   *
   * @param jon : the JVN object name
   * @return the JVN object
   * @throws JvnException
   **/
  public JvnObject jvnLookupObject(String jon)
      throws JvnException {
    try {
      return coord.jvnLookupObject(jon, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }

  /**
   * Get a Read lock on a JVN object
   *
   * @param joi : the JVN object identification
   * @return the current JVN object state
   * @throws JvnException
   **/
  public Serializable jvnLockRead(int joi)
      throws JvnException {
    try {
      return coord.jvnLockRead(joi, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }

  }

  /**
   * Get a Write lock on a JVN object
   *
   * @param joi : the JVN object identification
   * @return the current JVN object state
   * @throws JvnException
   **/
  public Serializable jvnLockWrite(int joi)
      throws JvnException {
    try {
      return coord.jvnLockWrite(joi, this);
    } catch (RemoteException e) {
      throw new JvnException(e.getMessage());
    }
  }


  /**
   * Invalidate the Read lock of the JVN object identified by id called by the JvnCoord
   *
   * @param joi : the JVN object id
   * @return void
   * @throws java.rmi.RemoteException,JvnException
   **/
  public void jvnInvalidateReader(int joi)
      throws java.rmi.RemoteException, JvnException {
    JvnObjectImpl obj = (JvnObjectImpl) jvnLookupObject(String.valueOf(joi));
    obj.jvnInvalidateReader();
  }

  /**
   * Invalidate the Write lock of the JVN object identified by id
   *
   * @param joi : the JVN object id
   * @return the current JVN object state
   * @throws java.rmi.RemoteException,JvnException
   **/
  public Serializable jvnInvalidateWriter(int joi)
      throws java.rmi.RemoteException, JvnException {
    JvnObjectImpl obj = (JvnObjectImpl) jvnLookupObject(String.valueOf(joi));
    return obj.jvnInvalidateWriter();
  }

  ;

  /**
   * Reduce the Write lock of the JVN object identified by id
   *
   * @param joi : the JVN object id
   * @return the current JVN object state
   * @throws java.rmi.RemoteException,JvnException
   **/
  public Serializable jvnInvalidateWriterForReader(int joi)
      throws java.rmi.RemoteException, JvnException {
    JvnObjectImpl obj = (JvnObjectImpl) jvnLookupObject(String.valueOf(joi));
    return obj.jvnInvalidateWriterForReader();
  }

  ;

}

 
