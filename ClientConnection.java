import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

public class ClientConnection {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8082;
    private RSAKeyPairGenerator rsaKeyPairGenerator;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private EncryptionAndDecryption encryptionAndDecryption;
    private SecretKey secretKey;


    public ClientConnection() {

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                ObjectOutputStream serverOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream serverInput = new ObjectInputStream(socket.getInputStream())) {
            System.out.println("Connected to the server.");
            try {
                rsaKeyPairGenerator = new RSAKeyPairGenerator();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            publicKey = rsaKeyPairGenerator.getPublicKey();
            serverOutput.writeObject(publicKey);
            serverOutput.flush();
            privateKey = rsaKeyPairGenerator.getPrivateKey();
            encryptionAndDecryption = new EncryptionAndDecryption();
            try {
                String EncryptedString = (String) serverInput.readObject();
                try {
                    secretKey = encryptionAndDecryption.DecryptSecretkey(EncryptedString, privateKey);
                    System.out.println("Decryption is done: ");
                    serverOutput.close();
                    serverInput.close();
                    client Clients=new client(secretKey,socket);
                    Clients.startClient();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } 
    }
    public static void main(String[] args) {
        new ClientConnection();
    }
}
