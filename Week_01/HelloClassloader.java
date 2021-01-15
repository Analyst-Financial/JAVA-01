package Week_01;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class HelloClassloader extends ClassLoader {

    public static void main(String[] args) {
        try {
            Object instance = new HelloClassloader().findClass("Hello.xlass").newInstance();
            instance.getClass().getMethod("hello").invoke(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] newByte = new byte[0];
        try {
            // 字节码读取
            InputStream ins = HelloClassloader.class.getClassLoader().getResourceAsStream(name);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len = 0;
            while ((len = ins.read()) != -1) {
                bos.write(len);
            }
            newByte = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 字节码转换
        for (int j = 0; j < newByte.length; j++) {
            newByte[j] = (byte) (255 - newByte[j]);
        }

        String className = name.substring(0, name.lastIndexOf("."));
        return defineClass(className, newByte, 0, newByte.length);
    }

    private static byte[] decode(String str) {
        return Base64.getDecoder().decode(str);
    }

}
