import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class EncryptionAndDecryption {

    // Generate a new AES key
    public SecretKey Generatedkey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // AES-128
        return keyGenerator.generateKey();
    }

    // Encrypt a Message object
    public String encrypt(Message message, SecretKey secretKey) throws Exception {
        // Serialize the Message object to bytes
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream)) {
            objectOutputStream.writeObject(message);
        }
        byte[] serializedBytes = byteStream.toByteArray();

        // Generate a random IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[cipher.getBlockSize()];
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        // Encrypt the serialized bytes
        byte[] encryptedBytes = cipher.doFinal(serializedBytes);

        // Encode the IV and encrypted data to Base64 and concatenate
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

        return ivBase64 + ":" + encryptedBase64;
    }

    public String encryptSecretKey(SecretKey secretKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(cipher.ENCRYPT_MODE, publicKey);
        byte[] doFinal = cipher.doFinal(secretKey.getEncoded());
        return Base64.getEncoder().encodeToString(doFinal);
    }

    public SecretKey DecryptSecretkey(String encryptedString, PrivateKey privateKey) throws Exception {
        byte[] Byte=Base64.getDecoder().decode(encryptedString);
        Cipher sCipher = Cipher.getInstance("RSA");
        sCipher.init(sCipher.DECRYPT_MODE, privateKey);
        byte[] doFinal = sCipher.doFinal(Byte);
        return new javax.crypto.spec.SecretKeySpec(doFinal, "AES");
    }

    // Decrypt a String to a Message object
    public Message Decrypt(String encryptedString, SecretKey secretKey) throws Exception {
        // Split the encrypted string into IV and data
        String[] parts = encryptedString.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted string format");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

        // Initialize cipher with the IV
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // Decrypt the data
        byte[] serializedBytes = cipher.doFinal(encryptedBytes);

        // Deserialize the bytes back into a Message object
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedBytes))) {
            return (Message) objectInputStream.readObject();
        }
    }
}
