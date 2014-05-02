package com.babyrhythm.controller;

import javax.servlet.http.HttpServletRequest;

import com.babyrhythm.mvc.Controller;
import com.babyrhythm.mvc.PathParser;
import com.babyrhythm.mvc.StaticFilesView;
import com.babyrhythm.mvc.View;

@SuppressWarnings("serial")
public class StaticFiles extends Controller {

    private String root;

    public StaticFiles(String root) {
        this.root = root;
    }

    @Override
    public View get(HttpServletRequest request, PathParser pathInfo) throws Exception {
        return new StaticFilesView(root+request.getPathInfo());
    }

}
