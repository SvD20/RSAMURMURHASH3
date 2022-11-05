package edu.bsuir.kioki.rsa;

import edu.bsuir.kioki.murmurhash3.MurMurHash3;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class RSA {

    private final static String FIRSTFILE = "resources/firstfile";
    private final static String SECONDFILE = "resources/secondfile";
    private final static String EDS = "resources/eds";


    private BigInteger prime_p, prime_q, val_n, phi_n, val_e, val_d;
    private BigInteger one = BigInteger.ONE; //константа, равная 1
    private int maximumLength = 1024; // максимальный размер ключа в битах
    private SecureRandom random;//генератор случайных значений

    public void KeyGeneration() {
        random = new SecureRandom();
        prime_p = BigInteger.probablePrime(maximumLength, random);//первое простое число p
        prime_q = BigInteger.probablePrime(maximumLength, random);//второе простое число q
        val_n = prime_p.multiply(prime_q);// произведение простых чисел r
        phi_n = prime_p.subtract(one).multiply(prime_q.subtract(one));// вычисляем функцию Эйлера
        do {
             val_e = BigInteger.probablePrime(maximumLength, random);//генерируем случайное число размером 1024 бит,
            // которое будет открытой экспонентой e в соответствии с критерием ниже
        }
        while (phi_n.gcd(val_e).compareTo(one) < 0 && val_e.compareTo(phi_n) > 0);//gcd ищет НОД; здесь находится критерий
            // для целого значения открытой экспоненты e
             val_d = val_e.modInverse(phi_n);// вычисляем значение секретной экспоненты d; d -
        //мультипликативная инверсная по модулю функции Эйлера для e
    }

    public byte[] Encryption(byte[] plainMessage, BigInteger e, BigInteger n){ // передаем в функцию шифрования
        //хэш, открытую экспоненту и произведение простых p и q, последние два параметра есть ключ шифрования (открытый)
        BigInteger encryptedMessage = (new BigInteger(plainMessage)).modPow(e, n);
        return encryptedMessage.toByteArray(); //получаем эцп
    }

    public byte[] Decryption(byte[] encryptedMessage, BigInteger d, BigInteger n){// передаем в функцию расшифровки
        //эцп, закрытую экспоненту и произведение простых p и q, последние два параметра есть ключ дешифрования (закрытый)
        BigInteger decryptedMessage = (new BigInteger(encryptedMessage)).modPow(d, n);
        return decryptedMessage.toByteArray();//получаем хэш
    }

    public static void main (String [] arguments) throws IOException {
        int input;
        RSA rsa = new RSA();
        rsa.KeyGeneration();
        String plainMessage = readStringFromFile(FIRSTFILE);
        long hash = MurMurHash3.hash64(plainMessage.getBytes());
        byte[] encryptedMessage = rsa.Encryption(longToBytes(hash), rsa.val_e, rsa.val_n);
        writeByteInFile(EDS,encryptedMessage);
        System.out.println("Электронная цифровая подпись для содержимого первого файла создана...");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        input =Integer.parseInt(String.valueOf(bufferedReader.read()));
        byte[] decryptedMessage = rsa.Decryption(readByteFromFile(EDS), rsa.val_d, rsa.val_n);
        System.out.println("Проверяем ЭЦП для содержимого первого файла...");
        if(MurMurHash3.hash64(readStringFromFile(FIRSTFILE).getBytes()) == bytesToLong(decryptedMessage)){
            System.out.println("Подпись верна");
        }
        else{
            System.out.println("Подпись неверна");
        }
        System.out.println("Проверяем ЭЦП для содержимого второго файла...");
        if(MurMurHash3.hash64(readStringFromFile(SECONDFILE).getBytes()) == bytesToLong(decryptedMessage)){
            System.out.println("Подпись верна");
        }
        else{
            System.out.println("Подпись неверна");
        }
    }


    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    private static void writeByteInFile(String filepath, byte[] buffer) throws FileNotFoundException {
        try(FileOutputStream fos=new FileOutputStream(filepath))
        {
            fos.write(buffer, 0, buffer.length);
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }

    private static byte[] readByteFromFile(String filepath){
        byte[] buffer = null;
        try(FileInputStream fin=new FileInputStream(filepath))
        {
            buffer = new byte[fin.available()];
            fin.read(buffer, 0, fin.available());
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        return buffer;
    }

    private static String readStringFromFile(String filepath) throws IOException {
        File file = new File(filepath);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);
        return  reader.readLine();
    }
}

