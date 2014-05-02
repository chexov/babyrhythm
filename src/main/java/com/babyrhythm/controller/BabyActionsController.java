package com.babyrhythm.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import com.babyrhythm.files.FileUtil;
import com.babyrhythm.model.Activity;
import com.babyrhythm.mvc.Controller;
import com.babyrhythm.mvc.JsonView;
import com.babyrhythm.mvc.PathParser;
import com.babyrhythm.mvc.View;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BabyActionsController extends Controller {

    private Gson g = new GsonBuilder().create();
    private File fsRoot;

    public BabyActionsController(File fsRoot) {
        this.fsRoot = fsRoot;
    }

    @Override
    public View get(HttpServletRequest request, PathParser pathInfo) throws Exception {
        String babyName = pathInfo.cutNext();
        if (StringUtils.isBlank(babyName))
            return new JsonView(Error.MALFORMED_URL);
        List<File> files = FileUtil.find(new File(fsRoot, babyName), FileFilterUtils.suffixFileFilter(".json"));
        List<Activity> acts = new ArrayList<Activity>();
        for (File aFile : files) {
            String json = FileUtils.readFileToString(aFile);
            acts.add(g.fromJson(json, Activity.class));
        }

        Collections.sort(acts, new Comparator<Activity>() {

            @Override
            public int compare(Activity o1, Activity o2) {
                if (o1.timestamp < o2.timestamp)
                    return -1;

                if (o1.timestamp > o2.timestamp)
                    return 1;

                return 0;
            }
        });
        return new JsonView(acts);
    }

    @Override
    public View post(HttpServletRequest request, PathParser pathInfo) throws Exception {
        String body = getRequestBody(request);
        String babyName = pathInfo.cutNext();
        if (StringUtils.isBlank(babyName))
            return new JsonView(Error.MALFORMED_URL);

        if (StringUtils.isBlank(body))
            return new JsonView(Error.MISSING_POST_BODY);

        Activity a = g.fromJson(body, Activity.class);
        if (StringUtils.isBlank(a.type))
            return new JsonView(Error.MISSING_ACTIVITY_TYPE);

        a.babyName = babyName;
        if (a.timestamp == 0) {
            a.timestamp = System.currentTimeMillis();
        } else {
            a.uploadedAt = System.currentTimeMillis();
        }
        a.id = UUID.randomUUID().toString().toLowerCase();

        File savedTo = new File(fsRoot, babyName + "/" + a.timestamp + "_" + a.type + ".json");
        System.out.println(savedTo.getCanonicalPath());
        File parent = savedTo.getParentFile();
        if (!parent.exists())
            parent.mkdirs();

        FileUtils.writeStringToFile(savedTo, g.toJson(a));
        return new JsonView(a);
    }

    @Override
    public View delete(HttpServletRequest request) throws Exception {
        PathParser info = PathParser.pathInfoParser(request.getPathInfo());
        String babyName = info.cutNext();
        if (StringUtils.isBlank(babyName))
            return new JsonView(Error.MALFORMED_URL);

        String id = info.cutNext();
        if (StringUtils.isBlank(id))
            return new JsonView(Error.MALFORMED_URL);

        List<File> files = FileUtil.find(new File(fsRoot, babyName), FileFilterUtils.suffixFileFilter(".json"));
        for (File aFile : files) {
            String json = FileUtils.readFileToString(aFile);
            Activity a = g.fromJson(json, Activity.class);
            if (a.id.equals(id)) {
                if (aFile.delete()) {
                    return new JsonView(a);
                } else {
                    return new JsonView(Error.DELETING_ACTIVITY_FAILED);
                }
            }
        }

        return new JsonView(Error.ACTIVITY_WAS_NOT_LOCATED);
    }
    
    public static final String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sb.append(line);
        return sb.toString();
    }

    public static class Error {
        public static final Error MALFORMED_URL = new Error("Malformed URL");
        public static final Error MISSING_POST_BODY = new Error("No body for POST request was specified");
        public static final Error MISSING_ACTIVITY_TYPE = new Error("Activity \"type\" attribute was not specified");
        public static final Error ACTIVITY_WAS_NOT_LOCATED = new Error("Activity was not located");
        public static final Error DELETING_ACTIVITY_FAILED = new Error(
                "Activity deletion failed. Check the server file system.");

        private String message;
        private boolean error = true;

        private Error(String message) {
            this.message = message;
        }
    }

}
