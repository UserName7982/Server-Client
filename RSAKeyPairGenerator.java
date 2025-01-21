import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyPairGenerator {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RSAKeyPairGenerator() throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair =keyPairGenerator.genKeyPair();
        this.publicKey=keyPair.getPublic();
        this.privateKey=keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
}
