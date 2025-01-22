import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class server {
    private static final int PORT = 8082;
    private ServerSocket serverSocket;
    private SecretKey secretKey;
    private EncryptionAndDecryption encryptionAndDecryption;

    public server() throws IOException, NoSuchAlgorithmException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Waiting for clients to connect...");
        encryptionAndDecryption = new EncryptionAndDecryption();

        // Generate a secret key for communication
        secretKey = encryptionAndDecryption.Generatedkey();

        // Accept multiple client connections
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection established with client: " + socket.getInetAddress());

            // Handle each client in a new thread
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private void handleClient(Socket socket) {
        try (
            ObjectInputStream br = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream ois = new ObjectOutputStream(socket.getOutputStream())
        ) {
            System.out.println("Thread: " + Thread.currentThread().getName());

            // Step 1: Receive client's public key
            System.out.println("Waiting to receive client's public key...");
            PublicKey clientPublicKey = (PublicKey) br.readObject();
            System.out.println("Received client's public key: " + clientPublicKey);

            // Step 2: Encrypt the secret key with client's public key
            System.out.println("Encrypting secret key...");
            String encryptSecretKey = encryptionAndDecryption.encryptSecretKey(secretKey, clientPublicKey);
            System.out.println("Encrypted secret key: " + encryptSecretKey);

            // Step 3: Send the encrypted secret key to the client
            System.out.println("Sending encrypted secret key to client...");
            ois.writeObject(encryptSecretKey);
            ois.flush();
            System.out.println("Encrypted secret key sent to client.");

            // Step 4: Continue handling client communication (if necessary)
            
            ServerConnection serverConnection = new ServerConnection(socket, secretKey);
            serverConnection.start();
            

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
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
