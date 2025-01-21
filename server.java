
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

public class server {
    private static final int PORT = 8082;
    private ServerSocket serverSocket;
    private SecretKey secretKey;
    private EncryptionAndDecryption encryptionAndDecryption;
    private String encryptSecretKey;
    public server() throws IOException, NoSuchAlgorithmException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Waiting for clients to connect...");
        encryptionAndDecryption = new EncryptionAndDecryption();
        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        PublicKey publicKey = rsaKeyPairGenerator.getPublicKey();
        PrivateKey privateKey = rsaKeyPairGenerator.getPrivateKey();

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
                try {
                   ObjectInputStream br=new ObjectInputStream((socket.getInputStream()));
                    
                    PublicKey ClientpublicKey=(PublicKey)br.readObject();
                    encryptSecretKey = encryptionAndDecryption.encryptSecretKey(secretKey, ClientpublicKey);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ServerConnection serverConnection = new ServerConnection(socket, secretKey, encryptSecretKey,
                        privateKey);
                serverConnection.start();
            }).start();
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        try {
            new server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
