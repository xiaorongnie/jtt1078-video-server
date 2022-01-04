package cn.org.hentai.jtt1078.test;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedStreamTest {
    public static void main(String[] args) {
        try {
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String data = "12345";
                        while (true) {
                            pipedOutputStream.write(data.getBytes());
                            pipedOutputStream.flush();
                            System.out.println("pipedOutputStream.write " + data);
                            Thread.sleep(150000);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            thread1.start();

            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = new byte[10];
                        while (true) {
                            pipedInputStream.read(data);
                            System.out.println("pipedInputStream.read " + new String(data));
                            // Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            thread2.start();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}