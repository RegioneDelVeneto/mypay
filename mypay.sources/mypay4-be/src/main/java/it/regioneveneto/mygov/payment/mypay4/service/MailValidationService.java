/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.service;

import it.regioneveneto.mygov.payment.mypay4.dto.MailValidationRequest;
import it.regioneveneto.mygov.payment.mypay4.dto.MailValidationResponse;
import it.regioneveneto.mygov.payment.mypay4.exception.MyPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Service
@Slf4j
public class MailValidationService {

  public final static String MAIL_VALIDATION_STATUS_VALID = "VALID";
  public final static String MAIL_VALIDATION_STATUS_WRONG_PIN = "WRONG_PIN";
  public final static String MAIL_VALIDATION_STATUS_INVALID = "INVALID";
  public final static String MAIL_VALIDATION_STATUS_EXPIRED = "EXPIRED";
  public final static String MAIL_VALIDATION_STATUS_CAPTCHA = "CAPTCHA";


  @Value("${mail-validation.rsa.pub}")
  private String rsaPub;
  @Value("${mail-validation.rsa.prv}")
  private String rsaPrv;

  @Autowired
  private MailService mailService;

  private PublicKey publicKey;
  private PrivateKey privateKey;

  @PostConstruct
  private void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
    this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(rsaPub)));
    this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(rsaPrv)));
  }

  private String encrypt(String toEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
    byte[] result = cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8.name()));
    return Base64.encodeBase64String(result);
  }

  private String decrypt(String toDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
    byte[] encrypted = Base64.decodeBase64(toDecrypt);
    return new String(cipher.doFinal(encrypted));
  }

  private String sign(String toSign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
    final Signature sign = Signature.getInstance("SHA256withRSA");
    sign.initSign(this.privateKey);
    sign.update(toSign.getBytes(StandardCharsets.UTF_8.name()));
    byte[] signature = sign.sign();
    return Base64.encodeBase64String(signature);
  }

  private boolean verifySignature(String signed, String signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
    final Signature sign = Signature.getInstance("SHA256withRSA");
    sign.initVerify(this.publicKey);
    sign.update(signed.getBytes(StandardCharsets.UTF_8.name()));
    return sign.verify(Base64.decodeBase64(signature));
  }

  public MailValidationRequest requestMailValidation(String emailAddress){
    try {
      //check if a non-expired validationRequest already exists

      long now = System.currentTimeMillis();
      String salt = UUID.randomUUID().toString();
      String encryptedSalt = encrypt(salt+"|"+now);
      String hash = DigestUtils.sha384Hex(emailAddress + "|" + salt);
      int pinInteger = Math.abs(hash.hashCode()) % 1000000; //6 digit numeric pin
      String pinString = String.format("%06d", pinInteger);

      mailService.sendMailAddressValidationMail(emailAddress, pinString);

      return MailValidationRequest.builder()
          .email(emailAddress)
          .encryptedSalt(encryptedSalt)
          //.pin(pinString) // uncomment this if you want PIN to be visible in carrello page
                            // (for example, during tests on environment where sending mail is not enabled)
          .build();
    } catch (Exception e) {
      log.error("error requesting mail validation", e);
      throw new MyPayException("Errore di sistema durante la generazione della verifica mail");
    }
  }

  public MailValidationResponse verifyMailValidation(MailValidationRequest req){
    try {
      String saltAndNow = decrypt(req.getEncryptedSalt());
      String salt = saltAndNow.substring(0, saltAndNow.lastIndexOf('|'));
      long now = Long.parseLong(saltAndNow.substring(salt.length()+1));
      //check not expired
      if(System.currentTimeMillis()-now > 1000*3600) { // 1 hours
        log.warn("mail validation request expired; email: {} - delay: {}s", req.getEmail(), (System.currentTimeMillis()-now)/1000);
        return MailValidationResponse.builder()
            .email(req.getEmail()).validationStatus(MAIL_VALIDATION_STATUS_EXPIRED)
            .build();
      }
      String hash = DigestUtils.sha384Hex(req.getEmail() + "|" + salt);
      String correctPin = String.format("%06d", Math.abs(hash.hashCode()) % 1000000);
      //verify pin
      if(!req.getPin().equals(correctPin)) {
        log.warn("mail validation request failed; email: {} - valid pin: {} - input pin: {}", req.getEmail(), correctPin, req.getPin());
        return MailValidationResponse.builder()
            .email(req.getEmail()).validationStatus(MAIL_VALIDATION_STATUS_WRONG_PIN)
            .build();
      }

      String validatedMailToken = sign(req.getEmail()+"|"+now);

      return MailValidationResponse.builder()
          .email(req.getEmail()).validationStatus(MAIL_VALIDATION_STATUS_VALID)
          .validatedToken(validatedMailToken)
          .expiration(now+1000*3600) // 1 hours
          .build();
    } catch (Exception e) {
      log.error("error verifying mail validation", e);
      return MailValidationResponse.builder()
          .email(req.getEmail()).validationStatus(MAIL_VALIDATION_STATUS_INVALID)
          .build();
    }
  }

  public static void main(String[] args) {
    try {
      // generate key
      KeyGenerator kg = KeyGenerator.getInstance("AES");
      kg.init(128); // Determine the key length
      byte[] keyBytes = kg.generateKey().getEncoded();
      System.out.println("AES: "+Base64.encodeBase64String(keyBytes));


      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(512);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
      RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();

      String rsaPub = Base64.encodeBase64String(rsaPublicKey.getEncoded());

      System.out.println("RSA pub: "+rsaPub);
      System.out.println("RSA prv: "+Base64.encodeBase64String(rsaPrivateKey.getEncoded()));

      MailValidationService instance = new MailValidationService();
      instance.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(rsaPub)));
      instance.privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded()));

      String testData = "Lorem ipsum dolor sit amet";
      String encrypted = instance.encrypt(testData);
      System.out.println("encrypted: "+encrypted);
      String decrypted = instance.decrypt(encrypted);
      System.out.println("decrypted: "+decrypted);
      String signature = instance.sign(testData);
      System.out.println("signature: "+signature);
      boolean signatureVerified = instance.verifySignature(testData, signature);
      System.out.println("signatureVerified: "+signatureVerified);
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
