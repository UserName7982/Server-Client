
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.SecretKey;

public class client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8082;
    Message messages;
    private EncryptionAndDecryption encryptionAndDecryption;
    private SecretKey secretKey;

    public void startClient() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                ObjectInputStream serverInput = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream serverOutput = new ObjectOutputStream(socket.getOutputStream());
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server.");
            System.out.println("Genrating SecretKey");
            encryptionAndDecryption=new EncryptionAndDecryption();
            try {
                secretKey=encryptionAndDecryption.Generatedkey();
                System.out.println(secretKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            Thread readerThread = new Thread(() -> {
                try {
                    try {
                        String input;
                        while (true) {
                            String string = (String) serverInput.readObject();
                            try {
                                messages = (Message) encryptionAndDecryption.Decrypt(string, secretKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if ((input = messages.getMessage()) != null) {
                                System.out.println("Server: " + input);
                                if (input.equals("exit")) {
                                    socket.close();
                                    break;
                                }
                            }
                        }

                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            readerThread.start();
            new Thread(() -> {
                while (true) {
                    String message = scanner.nextLine();
                    messages = new Message(message, socket.getLocalAddress().toString(),
                            socket.getInetAddress().toString());
                    messages.setMessage(message);
                    messages.setSender(socket.getLocalAddress().toString());
                    messages.setRecipent(socket.getInetAddress().toString());
                    try {
                        String string = encryptionAndDecryption.encrypt(messages, secretKey);
                        serverOutput.writeObject(string);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if ("exit".equalsIgnoreCase(message)) {
                        System.out.println("Disconnected from server.");
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            });

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new client().startClient();
    }
}
