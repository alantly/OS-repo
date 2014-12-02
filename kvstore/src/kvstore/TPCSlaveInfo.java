package kvstore;

import static kvstore.KVConstants.*;

import java.io.IOException;
import java.net.*;
import java.util.regex.*;

/**
 * Data structure to maintain information about SlaveServers
 */
public class TPCSlaveInfo {

    public long slaveID;
    public String hostname;
    public int port;

    /**
     * Construct a TPCSlaveInfo to represent a slave server.
     *
     * @param info as "SlaveServerID@Hostname:Port"
     * @throws KVException ERROR_INVALID_FORMAT if info string is invalid
     */
    public TPCSlaveInfo(String info) throws KVException {
        // implement me
        Pattern pSlaveID = Pattern.compile("[0-9]+@"); //number mix.
        Pattern pHostName = Pattern.compile("@[0-9a-zA-Z]+:"); //alpha/number mix.
        Pattern pPort = Pattern.compile(":[0-9]+"); //number mix.

        try {
            Matcher m = pSlaveID.matcher(info);
            m.find();
            this.slaveID = Long.valueOf(info.substring(m.start(),m.end()-1)).longValue();

            m = pHostName.matcher(info);
            m.find();
            this.hostname = info.substring(m.start()+1,m.end()-1);

            m = pPort.matcher(info);
            m.find();
            this.port = Integer.valueOf(info.substring(m.start()+1)).intValue();
        } catch (IllegalStateException e) {
            //no match found
            throw new KVException(KVConstants.ERROR_PARSER);
        }
    }

    public long getSlaveID() {
        return slaveID;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    /**
     * Create and connect a socket within a certain timeout.
     *
     * @return Socket object connected to SlaveServer, with timeout set
     * @throws KVException ERROR_SOCKET_TIMEOUT, ERROR_COULD_NOT_CREATE_SOCKET,
     *         or ERROR_COULD_NOT_CONNECT
     */
    public Socket connectHost(int timeout) throws KVException {
        // implement me
        try {
            Socket nSocket = new Socket(hostname, port);
            nSocket.setSoTimeout(timeout);
            return nSocket;
        } catch (IOException ioe) {
            throw new KVException(ERROR_COULD_NOT_CONNECT);
        }
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     * @param sock Socket to be closed
     */
    public void closeHost(Socket sock) {
        // implement me
        try {
            sock.close();
        }
        catch (IOException ioex) {}
    }
}