package kvstore;

import static kvstore.KVConstants.DEL_REQ;
import static kvstore.KVConstants.ERROR_COULD_NOT_CONNECT;
import static kvstore.KVConstants.ERROR_COULD_NOT_CREATE_SOCKET;
import static kvstore.KVConstants.ERROR_INVALID_KEY;
import static kvstore.KVConstants.ERROR_INVALID_VALUE;
import static kvstore.KVConstants.GET_REQ;
import static kvstore.KVConstants.PUT_REQ;
import static kvstore.KVConstants.RESP;
import static kvstore.KVConstants.SUCCESS;
import static kvstore.KVConstants.ERROR_NO_SUCH_KEY;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Client API used to issue requests to key-value server.
 */
public class KVClient implements KeyValueInterface {

    public String server;
    public int port;

    /**
     * Constructs a KVClient connected to a server.
     *
     * @param server is the DNS reference to the server
     * @param port is the port on which the server is listening
     */
    public KVClient(String server, int port) {
        this.server = server;
        this.port = port;
    }

    /**
     * Creates a socket connected to the server to make a request.
     *
     * @return Socket connected to server
     * @throws KVException if unable to create or connect socket
     */
    public Socket connectHost() throws KVException {
        Socket nSocket = null;
        try {
            nSocket = new Socket(this.server, this.port);
        }
        catch (IOException ioe) {
            throw new KVException(KVConstants.ERROR_COULD_NOT_CREATE_SOCKET);
        }
        return nSocket;
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     * @param  sock Socket to be closed
     */
    public void closeHost(Socket sock) {
        // implement me
        try {
            sock.close();
        }
        catch (IOException ioex) {}
    }

    /**
     * Issues a PUT request to the server.
     *
     * @param  key String to put in server as key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void put(String key, String value) throws KVException {
        // implement me
        if (key == null || key.equals(""))
            throw new KVException(KVConstants.ERROR_INVALID_KEY);
        else if (value == null || value.equals(""))
            throw new KVException(KVConstants.ERROR_INVALID_VALUE);

        Socket nSocket = null;
        KVMessage msg = null; 
        KVMessage response = null;
        
        try {
            nSocket = connectHost();
            msg = new KVMessage(KVConstants.PUT_REQ);
            msg.setValue(value);
            msg.sendMessage(nSocket);
            response = new KVMessage(nSocket);
            closeHost(nSocket);
        }
        catch (KVException kve) {
            throw kve; // Not sure this is going to work
        } 

        String responseMessage = response.getMessage();
        if (responseMessage != null) {
            if (!responseMessage.equals(KVConstants.SUCCESS))
            throw new KVException(response.getMessage());
        }
    }

    /**
     * Issues a GET request to the server.
     *
     * @param  key String to get value for in server
     * @return String value associated with key
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public String get(String key) throws KVException {
        // implement me
        // To receive a response use input stream of the socket
        // with the kvmessage constructor that uses input stream
        if (key == null || key.equals(""))
            throw new KVException(KVConstants.ERROR_INVALID_KEY);

        Socket nSocket = null;
        KVMessage msg = null;
        KVMessage response = null;

        try {
            nSocket = connectHost();
            msg = new KVMessage(KVConstants.GET_REQ);
            msg.sendMessage(nSocket);
            response = new KVMessage(nSocket);
            closeHost(nSocket);
        }
        catch (KVException kve) {
            throw new KVException(KVConstants.ERROR_NO_SUCH_KEY);
        }
        
        if (response.getValue() == null)
            throw new KVException(KVConstants.ERROR_NO_SUCH_KEY);

        String responseMessage = response.getMessage();
        if (responseMessage != null) {
            if (!responseMessage.equals(KVConstants.SUCCESS))
            throw new KVException(response.getMessage());
        }
        
        return response.getValue();
    }

    /**
     * Issues a DEL request to the server.
     *
     * @param  key String to delete value for in server
     * @throws KVException if the request was not successful in any way
     */
    @Override
    public void del(String key) throws KVException {
        // implement me
        if (key == null || key.equals(""))
            throw new KVException(KVConstants.ERROR_INVALID_KEY);

        Socket nSocket = null;
        KVMessage msg = null;
        KVMessage response = null;

        try {
            nSocket = connectHost();
            msg = new KVMessage(KVConstants.DEL_REQ);
            msg.sendMessage(nSocket);
            response = new KVMessage(nSocket);
            closeHost(nSocket);
        }
        catch (KVException kve) {
            throw new KVException(KVConstants.ERROR_NO_SUCH_KEY);
        }

        String responseMessage = response.getMessage();
        if (responseMessage != null) {
            if (!responseMessage.equals(KVConstants.SUCCESS))
            throw new KVException(response.getMessage());
        }
    }
}
