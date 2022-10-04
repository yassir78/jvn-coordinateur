/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class JvnCoordImpl
    extends UnicastRemoteObject
    implements JvnRemoteCoord {


  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Logger logger = Logger.getLogger(this.getClass().getName());
  private static JvnCoordImpl coord;
  private transient Registry registry;


  private Map<Integer, List<JvnRemoteServer>> readers;
  private Map<Integer, JvnRemoteServer> writers;

  private Map<Integer, JvnObject> objects;

  private Map<Integer, String> joiAndIdsMap;


  /**
   * Default constructor
   *
   * @throws JvnException
   **/
  private JvnCoordImpl() throws Exception {
    super();
    objects = new ConcurrentHashMap<>();
    readers = new ConcurrentHashMap<>();
    writers = new ConcurrentHashMap<>();
    registry = registry == null ? LocateRegistry.createRegistry(1099) : registry;
    registry.rebind("JvnCoord", this);

  }

  public static JvnCoordImpl getInstance() throws Exception {
    return coord == null ? new JvnCoordImpl() : coord;
  }

  /**
   * Allocate a NEW JVN object id (usually allocated to a newly created JVN object)
   *
   * @throws java.rmi.RemoteException,JvnException
   **/
  public int jvnGetObjectId()
      throws java.rmi.RemoteException, JvnException {
    return UUID.randomUUID().hashCode();
  }

  /**
   * Associate a symbolic name with a JVN object
   *
   * @param jon : the JVN object name
   * @param jo  : the JVN object
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the JVNServer
   * @throws java.rmi.RemoteException,JvnException
   **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    objects.put(jo.jvnGetObjectId(), jo);
    writers.put(jo.jvnGetObjectId(), js);
    joiAndIdsMap.put(jo.jvnGetObjectId(), jon);

  }

  /**
   * Get the reference of a JVN object managed by a given JVN server
   *
   * @param jon : the JVN object name
   * @param js  : the remote reference of the JVNServer
   * @throws java.rmi.RemoteException,JvnException
   **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    return objects.values().stream().filter(jvnObject -> {
      try {
        return joiAndIdsMap.get(jvnObject.jvnGetObjectId()).equals(jon);
      } catch (JvnException e) {
        throw new RuntimeException(e);
      }
    }).findFirst().orElseThrow(() -> new JvnException("Object not found"));

  }

  /**
   * Get a Read lock on a JVN object managed by a given JVN server
   *
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the server
   * @return the current JVN object state
   * @throws java.rmi.RemoteException, JvnException
   **/
  public Serializable jvnLockRead(int joi, JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    if (writers.containsKey(joi)) {
      JvnRemoteServer writer = writers.get(joi);
      objects.put(joi, (JvnObject) writer.jvnInvalidateWriterForReader(joi));
      readers.computeIfAbsent(joi, k -> new ArrayList<>()).add(writer);
      writers.remove(joi);
    }
    readers.computeIfAbsent(joi, k -> new ArrayList<>()).add(js);
    return objects.get(joi).jvnGetSharedObject();
  }

  /**
   * Get a Write lock on a JVN object managed by a given JVN server
   *
   * @param joi : the JVN object identification
   * @param js  : the remote reference of the server
   * @return the current JVN object state
   * @throws java.rmi.RemoteException, JvnException
   **/
  public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    readers.forEach((key, value) -> {
      if (key.equals(joi)) {
        value.forEach(jvnRemoteServer -> {
          try {
            if (!jvnRemoteServer.equals(js)) {
              jvnRemoteServer.jvnInvalidateReader(joi);
            }
            readers.get(joi).remove(jvnRemoteServer);
          } catch (RemoteException | JvnException e) {
            throw new RuntimeException(e);
          }
        });
      }
    });
    JvnRemoteServer writer = writers.get(joi);
    if (writer != null && !writer.equals(js)) {
      objects.put(joi, (JvnObject) writer.jvnInvalidateWriter(joi));
      writers.remove(joi);
    }
    writers.put(joi, js);
    return objects.get(joi).jvnGetSharedObject();
  }

  /**
   * A JVN server terminates
   *
   * @param js : the remote reference of the server
   * @throws java.rmi.RemoteException, JvnException
   **/
  public void jvnTerminate(JvnRemoteServer js)
      throws java.rmi.RemoteException, JvnException {
    readers.forEach((key, value) -> {
      value.forEach(jvnRemoteServer -> {
        if (jvnRemoteServer.equals(js)) {
          readers.get(key).remove(jvnRemoteServer);
        }
      });
    });
    writers.forEach((key, value) -> {
      if (value.equals(js)) {
        writers.remove(key);
      }
    });
  }

}

 
