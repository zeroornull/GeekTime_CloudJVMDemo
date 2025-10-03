package de.tuotuo.cloudjvmdemo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class CustomClassLoader extends ClassLoader {

    private String rootDir;

    private HashMap<String, Class<?>> loadedClasses = new HashMap<>();

    public CustomClassLoader(String rootDir) {
        this.rootDir = rootDir;
        loadedClasses = new HashMap<>();
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        //
        Class<?> loadedClass = loadedClasses.get(className);
        //
        if (loadedClass != null) {
            return loadedClass;
        }
        //
        byte[] classBytes = getClassBytes(className);
        if (classBytes == null) {
            throw new ClassNotFoundException();
        }
        //
        synchronized (loadedClasses) {
            loadedClass = defineClass(className, classBytes, 0, classBytes.length);
            loadedClasses.put(className, loadedClass);
        }
        return loadedClass;
    }

    private byte[] getClassBytes(String className) {
        //
        String classPath = rootDir + '/' +
            className.replace('.', '/') + ".class";
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(classPath);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            //
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            //
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //
            try {
                if (fis != null) {
                    fis.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // 创建新的CustomClassLoader实例
        CustomClassLoader customClassLoader = new CustomClassLoader("/path/to/classes");
        try {
            // 通过自定义的类加载器加载一个类，输出其类名
            Class<?> sampleClass = customClassLoader.loadClass("com.example.SampleClass");
            System.out.println("Class loaded successfully: " + sampleClass.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
