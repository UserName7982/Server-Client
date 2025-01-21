
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Scanner;
import javax.crypto.SecretKey;


public class ServerConnection {
    private Socket socket;
    private ObjectInputStream br;
    private ObjectOutputStream pr;
    private Message messages;
    private EncryptionAndDecryption encryptionAndDecryption;
    private SecretKey secretKey;
    private volatile boolean isRunning = true; // Flag to coordinate thread termination

    public ServerConnection(Socket socket, SecretKey secretKey, String encryptSecretKey, PrivateKey privateKey) {
        this.socket = socket;
        this.secretKey = secretKey;
        try {
            this.pr = new ObjectOutputStream(socket.getOutputStream());
            this.br = new ObjectInputStream(socket.getInputStream());

            // Initialize EncryptionAndDecryption and SecretKey
            encryptionAndDecryption = new EncryptionAndDecryption();
            
            System.out.println("SecretKey generated: " + secretKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Reader Thread
        new Thread(() -> {
            try {
                while (isRunning) {
                    try {
                        String encryptedMessage = (String) br.readObject();
                        try {
                            messages = (Message) encryptionAndDecryption.Decrypt(encryptedMessage, secretKey);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String input = messages.getMessage();

                        if (input != null) {
                            System.out.println("Client: " + input);
                            if (input.equalsIgnoreCase("exit")) {
                                System.out.println("Client closed the connection.");
                                stopConnection();
                                break;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error deserializing message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error reading from client: " + e.getMessage());
                }
            } finally {
                stopConnection();
            }
        }).start();

        // Writer Thread
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isRunning) {
                    String message = scanner.nextLine();
                    messages = new Message(message, socket.getLocalAddress().toString(),
                            socket.getInetAddress().toString());

                    try {
                        String encryptedMessage = encryptionAndDecryption.encrypt(messages, secretKey);
                        pr.writeObject(encryptedMessage);
                    } catch (Exception e) {
                        System.err.println("Error encrypting or sending message: " + e.getMessage());
                    }

                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Closing connection...");
                        stopConnection();
                        break;
                    }
                }
            }
        }).start();
    }

    // Gracefully stop the connection
    private void stopConnection() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    // public static void main(String[] args) {
    //     try {
    //         // Connect to the server
    //         Socket socket = new Socket("127.0.0.1", 8082);
    //         System.out.println("Connected to the server.");
    //         // Start communication with the server
    //         ServerConnection serverConnection = new ServerConnection(socket,new Secretkey);
    //         serverConnection.start();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
