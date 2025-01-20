

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
//have to Generate RSA Key
public class server {
    private static final int PORT = 8082;
    private ServerSocket serverSocket;
    private SecretKey secretKey;
    private EncryptionAndDecryption encryptionAndDecryption;

    public server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Waiting for clients to connect...");
        encryptionAndDecryption = new EncryptionAndDecryption();

        try {
            secretKey = encryptionAndDecryption.Generatedkey();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Accept multiple client connections in a loop
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection established with client: " + socket.getInetAddress());

            // Handle each client in a new thread
            new Thread(() -> {
                ServerConnection serverConnection = new ServerConnection(socket,secretKey);
                serverConnection.start();
            }).start();
        }
    }

    public static void main(String[] args) {
        try {
            new server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
