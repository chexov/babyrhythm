package com.babyrhythm.controller;

import javax.servlet.http.HttpServletRequest;

import com.babyrhythm.mvc.Controller;
import com.babyrhythm.mvc.PathParser;
import com.babyrhythm.mvc.StaticFilesView;
import com.babyrhythm.mvc.View;

public class AStaticFile extends Controller {
    private String path;

    public AStaticFile(String resourcePath) {
        this.path = resourcePath;
    }

    @Override
    public View get(HttpServletRequest request, PathParser pathInfo) throws Exception {
        return new StaticFilesView(path);
    }
}
