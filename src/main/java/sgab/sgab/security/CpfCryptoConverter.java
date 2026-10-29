package sgab.sgab.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Converter
public class CpfCryptoConverter implements AttributeConverter<String, String> {

    private static String secretKey;
    private static final String ALGORITHM = "AES";

    @Value("${api.security.cpf.secret}")
    public void setSecretKey(String key) {
        CpfCryptoConverter.secretKey = key;
    }

    @Override
    public String convertToDatabaseColumn(String cpf) {
        if (cpf == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secretKey.getBytes(), ALGORITHM));
            return Base64.getEncoder().encodeToString(cipher.doFinal(cpf.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar o CPF", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String cpfCriptografado) {
        if (cpfCriptografado == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getBytes(), ALGORITHM));
            return new String(cipher.doFinal(Base64.getDecoder().decode(cpfCriptografado)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar o CPF", e);
        }
    }
}
