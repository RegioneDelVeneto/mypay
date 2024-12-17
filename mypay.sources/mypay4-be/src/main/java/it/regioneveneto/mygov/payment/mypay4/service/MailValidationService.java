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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
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

  private static final String KEY_ALGORITHM = "RSA";
  private static final String CIPHER = "RSA";
  private static final String CIPHER_NO_PADDING = "RSA/ECB/NoPadding";
  private static final String SIG_ALGORITHM = "SHA256withRSA";

  public static final String MAIL_VALIDATION_STATUS_VALID = "VALID";
  public static final String MAIL_VALIDATION_STATUS_WRONG_PIN = "WRONG_PIN";
  public static final String MAIL_VALIDATION_STATUS_INVALID = "INVALID";
  public static final String MAIL_VALIDATION_STATUS_EXPIRED = "EXPIRED";
  public static final String MAIL_VALIDATION_STATUS_CAPTCHA = "CAPTCHA";


  @Value("${mail-validation.rsa.pub}")
  private String rsaPub;
  @Value("${mail-validation.rsa.prv}")
  private String rsaPrv;

  @Autowired
  private MailService mailService;

  private PublicKey publicKey;
  private PrivateKey privateKey;

  @Getter
  private String wildcartMailValidationToken;

  @PostConstruct
  private void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
    this.publicKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(rsaPub)));
    this.privateKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(rsaPrv)));
    this.wildcartMailValidationToken = UUID.randomUUID().toString();
  }

  public String encrypt(String toEncrypt, boolean padding) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(padding ? CIPHER : CIPHER_NO_PADDING);
    cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
    byte[] result = cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeBase64String(result);
  }

  public String decrypt(String toDecrypt, boolean padding) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(padding ? CIPHER : CIPHER_NO_PADDING);
    cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
    byte[] encrypted = Base64.decodeBase64(toDecrypt);
    return new String(cipher.doFinal(encrypted));
  }

  private String sign(String toSign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature sign = Signature.getInstance(SIG_ALGORITHM);
    sign.initSign(this.privateKey);
    sign.update(toSign.getBytes(StandardCharsets.UTF_8));
    byte[] signature = sign.sign();
    return Base64.encodeBase64String(signature);
  }

  private boolean verifySignature(String signed, String signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature sign = Signature.getInstance(SIG_ALGORITHM);
    sign.initVerify(this.publicKey);
    sign.update(signed.getBytes(StandardCharsets.UTF_8));
    return sign.verify(Base64.decodeBase64(signature));
  }

  public MailValidationRequest requestMailValidation(String emailAddress, String nomeEnte){
    try {
      //check if a non-expired validationRequest already exists

      long now = System.currentTimeMillis();
      String salt = UUID.randomUUID().toString();
      String encryptedSalt = encrypt(salt+"|"+now, true);
      String hash = DigestUtils.sha384Hex(emailAddress + "|" + salt);
      int pinInteger = Math.abs(hash.hashCode()% 1000000) ; //6 digit numeric pin
      String pinString = String.format("%06d", pinInteger);

      mailService.sendMailAddressValidationMail(emailAddress, pinString, nomeEnte);

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

  public boolean verifyMailValidationToken(String validatedMailToken, String emailAddress){
    if(StringUtils.isBlank(emailAddress))
      return true;
    else if(StringUtils.equals(validatedMailToken, this.wildcartMailValidationToken))
      return true;
    else if(StringUtils.isBlank(validatedMailToken)) {
      log.info("empty validatedMailToken for email address [{}]",emailAddress);
      return false;
    }
    try{
      String[] parts = StringUtils.split(validatedMailToken, "|", 2);
      long now = Long.parseLong(parts[0]);
      String signature = parts[1];
      String signed = emailAddress+"|"+now;
      if(System.currentTimeMillis()-now > 1000*3600) {
        log.info("expired mailValidationToken [{}] for email address [{}]", validatedMailToken, emailAddress);
        return false;
      } else if(!this.verifySignature(signed, signature)){
        log.info("invalid mailValidationToken [{}] for email address [{}]", validatedMailToken, emailAddress);
        return false;
      }
      return true;
    }catch(Exception e){
      log.info("invalid mailValidationToken [{}] for email address [{}]", validatedMailToken, emailAddress, e);
      return false;
    }
  }

  public MailValidationResponse verifyMailValidation(MailValidationRequest req){
    try {
      String saltAndNow = decrypt(req.getEncryptedSalt(), true);
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
      String correctPin = String.format("%06d", Math.abs(hash.hashCode()% 1000000) );
      //verify pin
      if(!req.getPin().equals(correctPin)) {
        log.warn("mail validation request failed; email: {} - valid pin: {} - input pin: {}", req.getEmail(), correctPin, req.getPin());
        return MailValidationResponse.builder()
            .email(req.getEmail()).validationStatus(MAIL_VALIDATION_STATUS_WRONG_PIN)
            .build();
      }

      String validatedMailToken = now+"|"+sign(req.getEmail()+"|"+now);

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

  public String generatePaymentId(Long mygovDovutoId){
    try {
      String payload = mygovDovutoId.toString();
      String salt = Long.toHexString(System.currentTimeMillis());
      String encryptedSalt = encrypt(salt, false);
      String hash = DigestUtils.sha1Hex(payload + "|" + encryptedSalt);
      return payload + "-" + salt + "-" + hash.substring(0, 9);
    } catch (Exception e) {
      log.error("error generating paymentId", e);
      throw new MyPayException("Errore di sistema durante la generazione del paymentId");
    }
  }

  public Long parsePaymentId(String paymentId, Long maxDurationMillis){
    try {
      String[] paymentIdParts = paymentId.split("-");
      if(paymentIdParts.length!=3)
        throw new MyPayException("invalid paymentId [format]");
      if(maxDurationMillis!=null){
        long duration = System.currentTimeMillis() - Long.parseLong(paymentIdParts[1], 16);
        if(duration > maxDurationMillis)
          throw new MyPayException("invalid paymentId [expired]");
      }
      String encryptedSalt = encrypt(paymentIdParts[1], false);
      String hash = DigestUtils.sha1Hex(paymentIdParts[0] + "|" + encryptedSalt);
      if(!StringUtils.equals(hash.substring(0, 9), paymentIdParts[2]))
        throw new MyPayException("invalid paymentId [hash]");
      return Long.parseLong(paymentIdParts[0]);
    } catch (Exception e) {
      log.error("error parsing paymentId", e);
      throw new MyPayException("Errore di sistema durante la verifica del paymentId");
    }
  }

  @SuppressWarnings({"java:S106"})
  public static void main(String[] args) {
    try {
      // generate key
      KeyGenerator kg = KeyGenerator.getInstance("AES");
      kg.init(128); // Determine the key length
      byte[] keyBytes = kg.generateKey().getEncoded();
      System.out.println("AES: "+Base64.encodeBase64String(keyBytes));


      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
      RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();

      String rsaPub = Base64.encodeBase64String(rsaPublicKey.getEncoded());

      System.out.println(KEY_ALGORITHM+" pub: "+rsaPub);
      System.out.println(KEY_ALGORITHM+" prv: "+Base64.encodeBase64String(rsaPrivateKey.getEncoded()));

      MailValidationService instance = new MailValidationService();
      instance.publicKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(rsaPub)));
      instance.privateKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded()));

      String testData = "156874";
      String encrypted = instance.encrypt(testData, true);
      System.out.println("encrypted: "+encrypted);

      String salt = Long.toString(RandomUtils.nextLong(100000000L,999999999L));
      String encryptedSalt = instance.encrypt(salt, true);
      String hash = DigestUtils.sha1Hex(testData + "|" + salt);
      System.out.println("encrypted hash: "+testData + "-" + salt + "-" + hash.substring(0,9));

      Long mygovDovutoId = 1847290L;
      String paymentId = instance.generatePaymentId(mygovDovutoId);
      System.out.println("paymentId: "+paymentId);
      System.out.println("check mygovDovutoId: "+instance.parsePaymentId(paymentId, null));


      String decrypted = instance.decrypt(encrypted, true);
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
