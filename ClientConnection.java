import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        Socket socket = null;
        ObjectInputStream serverInput = null;
        ObjectOutputStream serverOutput = null;

        try {
            // Initialize socket connection
            socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("Connected to the server.");

            // Initialize streams
            serverOutput = new ObjectOutputStream(socket.getOutputStream());
            serverInput = new ObjectInputStream(socket.getInputStream());

            // Generate RSA key pair
            rsaKeyPairGenerator = new RSAKeyPairGenerator();
            publicKey = rsaKeyPairGenerator.getPublicKey();
            privateKey = rsaKeyPairGenerator.getPrivateKey();
            encryptionAndDecryption = new EncryptionAndDecryption();

            // Send public key to server
            serverOutput.writeObject(publicKey);
            serverOutput.flush();

            // Receive and decrypt the secret key
            String encryptedString = (String) serverInput.readObject();
            System.out.println("Encrypted Secret Key: " + encryptedString);

            secretKey = encryptionAndDecryption.DecryptSecretkey(encryptedString, privateKey);
            System.out.println("Decryption is done.");

            // Start client logic
            client clientInstance = new client(secretKey, socket);
            clientInstance.startClient();

        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
         } 
         
            // Ensure resources are closed when the client stops
            finally{
            try {
                if (serverInput != null) serverInput.close();
                if (serverOutput != null) serverOutput.close();
                if (socket != null && !socket.isClosed()) {
                    System.out.println("Closing socket...");
                    socket.close();}
            } catch (Exception e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new ClientConnection();
    }
}
