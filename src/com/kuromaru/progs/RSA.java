package com.kuromaru.progs;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.util.Base64;

public class RSA {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    public KeyPair pair;

    public RSA() throws NoSuchAlgorithmException, IOException
    {
        try
        {
            readKeypair();

        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            System.out.println("Pair not found...");
            System.out.println("Generating keypair...");
            generateKeypair();
        }
    }

    void generateKeypair() throws NoSuchAlgorithmException, IOException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        pair = generator.generateKeyPair();
        writeKeypair();
    }

    public void writeKeypair() throws IOException
    {
        FileOutputStream f = new FileOutputStream(new File("Keys.dat"));
        ObjectOutputStream o = new ObjectOutputStream(f);

        o.writeObject(pair);

        o.close();
        f.close();
        System.out.println("key pair generated...");
    }

    public void readKeypair() throws IOException, ClassNotFoundException
    {
        FileInputStream fi = new FileInputStream(new File("Keys.dat"));
        ObjectInputStream oi = new ObjectInputStream(fi);

        // Read objects
        pair = (KeyPair) oi.readObject();

        oi.close();
        fi.close();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
        System.out.println("keys read succesfull....");

    }

    public String encrypt(String message) throws Exception
    {
        byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }

    public String decrypt(String encryptedMessage) throws Exception
    {
        byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage,"UTF8");
    }

    private String encode(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }
    private byte[] decode(String data){
        return Base64.getDecoder().decode(data);
    }

}
