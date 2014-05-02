package com.babyrhythm.files;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class FileUtil {
    public static List<File> find(File file) {
        return find(file, null);
    }

    public static List<File> find(File file, FileFilter filter) {
        List<File> result = new ArrayList<File>();
        LinkedList<File> stack = new LinkedList<File>();
        stack.push(file);
        while (!stack.isEmpty()) {
            File f = stack.pop();
            if (filter == null || filter.accept(f)) {
                result.add(f);
            }

            if (f.isDirectory() && f.exists()) {
                stack.addAll(asList(f.listFiles()));
            }
        }
        return result;
    }

    public final static File EOF = new File("EOF");
    
    public static BlockingQueue<File> findInThread(final File file, final FileFilter filter) {
        final BlockingQueue<File> result = new LinkedBlockingDeque<File>();
        Thread t = new Thread() {
            public void run() {
                Deque<File> stack = new LinkedList<File>();
                stack.push(file);
                while (!stack.isEmpty()) {
                    File f = stack.pop();
                    if (filter == null || filter.accept(f)) {
                        result.add(f);
                    }

                    if (f.isDirectory() && f.exists()) {
                        List<File> asList = new ArrayList<File>(asList(f.listFiles()));
                        Collections.reverse(asList);
                        for (File file2 : asList) {
                            stack.addFirst(file2);
                        }
                    }
                }
                result.add(EOF);
            }
        };
        t.setDaemon(true);
        t.start();
        return result;
    }
}
