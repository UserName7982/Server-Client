import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import javax.crypto.SecretKey;

public class client {
    private Message messages;
    private EncryptionAndDecryption encryptionAndDecryption;
    private SecretKey secretKey;
    private Socket socket;
    private volatile boolean isRunning = true; // Shared flag for thread termination

    public client(SecretKey secretKey, Socket socket) {
        this.secretKey = secretKey;
        this.socket = socket;
    }

    public void startClient() {
        try  {

            System.out.println("Running Client");

            // Reading thread
            Thread readerThread = new Thread(() -> {
                try (ObjectInputStream serverInput = new ObjectInputStream(socket.getInputStream())) {
                    while (isRunning && !socket.isClosed()) {
                        try {
                            String encryptedMessage = (String) serverInput.readObject(); // Read encrypted string
                            try {
                                messages = (Message) encryptionAndDecryption.Decrypt(encryptedMessage, secretKey);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            String receivedMessage = messages.getMessage();
                            System.out.println("Server: " + receivedMessage);

                            if ("exit".equalsIgnoreCase(receivedMessage)) {
                                System.out.println("Server disconnected.");
                                isRunning = false; // Signal other threads to stop
                                break;
                            }
                        } catch (ClassNotFoundException | IOException e) {
                            if (!socket.isClosed()) {
                                System.err.println("Error reading from server: " + e.getMessage());
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error initializing input stream: " + e.getMessage());
                }
            });
            readerThread.start();

            // Writing thread
            Thread writerThread = new Thread(() -> {
                while (isRunning && !socket.isClosed()) {
                    try(ObjectOutputStream serverOutput = new ObjectOutputStream(socket.getOutputStream());
                    Scanner scanner = new Scanner(System.in)) {
                        if (scanner.hasNextLine()) {
                            String message = scanner.nextLine();
                            messages = new Message(message, socket.getLocalAddress().toString(),
                                    socket.getInetAddress().toString());
                            String encryptedMessage;
                            try {
                                encryptedMessage = encryptionAndDecryption.encrypt(messages, secretKey);
                                serverOutput.writeObject(encryptedMessage);
                                serverOutput.flush();

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            if ("exit".equalsIgnoreCase(message)) {
                                System.out.println("Disconnecting...");
                                isRunning = false; // Signal other threads to stop
                                socket.close(); // Close the socket
                                break;
                            }
                        }
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("Error writing to server: " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            writerThread.start();

            // Wait for threads to finish
            readerThread.join();
            writerThread.join();

        } catch ( InterruptedException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close(); // Ensure the socket is closed
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
