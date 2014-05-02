package com.babyrhythm;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.apache.commons.lang3.StringUtils.stripStart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class Utils {
    public static void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Uncaught exception in: " + t);
                System.out.println(e);
                e.printStackTrace();
                System.exit(123);
            }
        });
    }

    public static File tildeExpand(String path) {
        if (path.startsWith("~")) {
            path = path.replaceFirst("~", SystemUtils.getUserHome().getAbsolutePath());
        }
        return new File(path);
    }

    public static Properties loadProperties(File file) throws IOException {
        Reader reader = null;
        try {
            Properties props = new Properties();
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                props.load(reader);
            }
            return props;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public static <T> List<T> intersection(List<T> a, List<T> b) {
        List<T> A = new ArrayList<T>(a);
        A.retainAll(b);
        return A;
    }

    public static Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        return builder.create();
    }

    public static String toGson(Object src) {
        return createGson().toJson(src);
    }

    public static String gsonToString(Object o) {
        GsonBuilder builder = new GsonBuilder();
        return builder.create().toJson(o);
    }

    public static <T> T fromJson(File file, Class<T> classOf) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return createGson().fromJson(IOUtils.toString(is), classOf);
        } catch (Exception e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static boolean existsWritableDir(File d) {
        return d.isDirectory() && d.canWrite();
    }

    public static boolean existsReadableFile(File f) {
        return f.isFile() && f.canRead();
    }

    public static void copy(InputStream in, File file) throws FileNotFoundException, IOException {
        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        try {
            IOUtils.copy(in, output);
        } finally {
            output.close();
        }
    }

    public static void printHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String string = (String) headerNames.nextElement();
            String header = request.getHeader(string);
            System.out.println(string + ": " + header);
        }
    }

    public static void printParams(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String string = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(string);
            System.out.println(string + " = " + (parameterValues == null ? "" : Arrays.asList(parameterValues)));
        }
    }

    public static <T> List<T> defaultList(List<T> list) {
        return (List<T>) (list == null ? Collections.emptyList() : list);
    }

    public static <T> T fromRequest(HttpServletRequest request, Class<T> typeOfT) {
        try {
            return createGson().fromJson(IOUtils.toString(request.getInputStream()), typeOfT);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static java.util.Comparator<File> newestFirstComparator = new Comparator<File>() {
        public int compare(File o1, File o2) {
            long mtime1 = o1.lastModified();
            long mtime2 = o2.lastModified();
            if (mtime2 == mtime1)
                return 0;
            return mtime2 > mtime1 ? 1 : -1;
        }
    };

    public static File tmpFile(File target) {
        return new File(target.getParentFile(), "." + target.getName() + "." + UUID.randomUUID());
    }

    private final static ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    static Lock getModificationLock(String r) {
        if (!locks.containsKey(r)) {
            locks.putIfAbsent(r, new ReentrantLock());
        }
        return locks.get(r);
    }

    public static void lockFile(File f) {
        getModificationLock(f.getAbsolutePath()).lock();
    }

    public static void unlockFile(File f) {
        getModificationLock(f.getAbsolutePath()).unlock();
    }

    private static String ip(String ip) {
        if ("0:0:0:0:0:0:0:1%0".equals(ip) || "fe80:0:0:0:0:0:0:1%1".equals(ip)) {
            // looks better
            ip = "127.0.0.1";
        }
        return ip;
    }

    public static BufferedReader reader(File csv) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(csv))));
    }

    public static String unquote(String str) {
        return stripEnd(stripStart(str, "\""), "\"");
    }

    public static boolean isOdd(int i) {
        return (i & 1) == 1;
    }

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }

    public static long unsigned(int i) {
        return i & 0xffffffffL;
    }

    public static boolean atomicJsonWrite(File f, Object o) throws IOException {
        File tmpFile = Utils.tmpFile(f);
        writeStringToFile(tmpFile, Utils.toGson(o));
        return tmpFile.renameTo(f);
    }

    public static File forceIsDir(File dir) throws IOException {
        if (!dir.isDirectory()) {
            if (dir.exists()) {
                FileUtils.forceDelete(dir);
            }
            FileUtils.forceMkdir(dir);
        }
        return dir;
    }
    
    public static String lowerCase(String str){
        return str == null? null : str.toLowerCase();
    }

    public static File forceEmptyDir(File dir) throws IOException {
        dir = forceIsDir(dir);
        FileUtils.cleanDirectory(dir);
        return dir;
    }

    public static String urlencode(String niceMessageId) {
        try {
            return URLEncoder.encode(niceMessageId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean strEquals(String s1, String s2) {
        if (s1 != null && s2 != null)
            return (s1.equals(s2));
        return false;
    }
}
