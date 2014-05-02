package com.babyrhythm.auth;

import static com.babyrhythm.Utils.existsReadableFile;
import static com.babyrhythm.Utils.fromJson;
import static com.babyrhythm.Utils.lowerCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.babyrhythm.Utils;

public class UserService {
    private final static Logger log = getLogger(UserService.class);
    private static final String USER_INFO_JS = "info.js";
    private final File dir;

    public UserService(File usersDir) {
        this.dir = usersDir;
        if (!this.dir.exists())
            this.dir.mkdir();
    }

    public BabyrhythmUser addUser(String email, String password, String name) {
        BabyrhythmUser user = new BabyrhythmUser(lowerCase(email), password, name, UUID.randomUUID().toString());
        user.setCreationDate(new Date());
        return writeUser(user);
    }

    public BabyrhythmUser getUser(String email) {
        return readUser(email);
    }

    public void saveUser(BabyrhythmUser user) {
        if (user.dateCreated == null) {
            log.warn(user.facebookId + " dateCreated == null");
            if (contains(user.facebookId)) {
                BabyrhythmUser old = getUser(user.facebookId);
                if (old.dateCreated != null) {
                    user.dateCreated = old.dateCreated;
                } else {
                    user.setCreationDate(new Date());
                }
            } else {
                user.setCreationDate(new Date());
            }
        }
        writeUser(user);
    }

    public boolean contains(String id) {
        return existsReadableFile(f(id));
    }

    private BabyrhythmUser readUser(String email) {
        try {
            File f = f(email);
            if (existsReadableFile(f)) {
                BabyrhythmUser u = fromJson(f, BabyrhythmUser.class);
                u.setLastModified(new Date(f.lastModified()));
                return u;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File f(String email) {
        File userDir = new File(dir, "user_" + email);
        return new File(userDir, USER_INFO_JS);
    }

    private BabyrhythmUser writeUser(BabyrhythmUser user) {
        try {
            File f = f(user.facebookId);
            if (!existsReadableFile(f)) {
                f.getParentFile().mkdirs();
            }
            Utils.atomicJsonWrite(f, user);
            return user;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BabyrhythmUser getUserByFacebook(String id) {
        if (isBlank(id))
            return null;

        for (File userDir : dir.listFiles()) {
            if (userDir.isDirectory() && !userDir.getName().startsWith(".")) {
                BabyrhythmUser user = Utils.fromJson(new File(userDir, USER_INFO_JS), BabyrhythmUser.class);
                if (id.equals(user.facebookId)) {
                    return user;
                }
            }
        }

        return null;
    }

    public List<String> listIds() {
        List<String> ids = new ArrayList<String>();
        for (File userDir : dir.listFiles()) {
            if (userDir.getName().startsWith("user_") && userDir.isDirectory() && !userDir.getName().startsWith(".")) {
                String id = userDir.getName().replaceFirst("user_", "");
                ids.add(id);
            }
        }

        return ids;
    }

    public List<String> listFacebookIds() {
        List<String> ids = new ArrayList<String>();
        for (File userDir : dir.listFiles()) {
            if (userDir.getName().startsWith("user_") && userDir.isDirectory() && !userDir.getName().startsWith(".")) {
                String id = userDir.getName().replaceFirst("user_", "");
                BabyrhythmUser u = readUser(id);
                if (u != null && isNotBlank(u.facebookId))
                    ids.add(u.facebookId);
            }
        }
        return ids;
    }

    public int size() {
        int users = 0;
        for (File userDir : dir.listFiles()) {
            if (userDir.getName().startsWith("user_") && userDir.isDirectory() && !userDir.getName().startsWith(".")) {
                users++;
            }
        }
        return users;
    }

    public int createdSince(Date date) {
        long mtime = date.getTime();
        int users = 0;
        for (File userDir : dir.listFiles()) {
            if (userDir.getName().startsWith("user_") && userDir.isDirectory() && !userDir.getName().startsWith(".")
                    && userDir.lastModified() >= mtime) {
                String id = userDir.getName().replaceFirst("user_", "");
                BabyrhythmUser user = readUser(id);
                if (user.dateCreated == null) {
                    log.warn("no dateCreated for user " + user.facebookId);
                }
                if (user.getCreationDate().getTime() >= mtime) {
                    users++;
                }
            }
        }
        return users;
    }

    public String getNameOrEmail(String from) {
        BabyrhythmUser user = getUser(from);
        return user != null && user.name != null ? user.name : from;
    }

}
