package com.babyrhythm;

import java.io.File;
import java.io.IOException;

//import org.j4web.server.VelocityConfigurator;
//import org.j4web.web.controllers.RedirectController;


import javax.servlet.http.HttpServletRequest;

import com.babyrhythm.auth.AuthService;
import com.babyrhythm.auth.UserService;
import com.babyrhythm.controller.AStaticFile;
import com.babyrhythm.controller.BabyActionsController;
import com.babyrhythm.controller.FacebookLoginController;
import com.babyrhythm.controller.LogController;
import com.babyrhythm.controller.StaticFiles;
import com.babyrhythm.controller.StorageController;
import com.babyrhythm.controller.TestController;
import com.babyrhythm.mvc.Controller;
import com.babyrhythm.mvc.PathParser;
import com.babyrhythm.mvc.StaticFilesView;
import com.babyrhythm.mvc.View;

public class Server {

        private HttpServer server;
        private Config conf;

        public Server(Config conf) throws IOException {
            this.server = new HttpServer(conf.port, "/");
            this.conf = conf;
        }

        private void init() throws IOException {
//            VelocityConfigurator.configure(new File("/tmp"), "pages/");
            server.add("/favicon.ico", new AStaticFile("/static/img/babyrhythm-blue.jpg"));
            server.add("/static/*", new StaticFiles("/static"));
            server.add("/babyactions/*", new BabyActionsController(new File(conf.fsRoot,"babies")));
            server.add("/storage", new StorageController());
            server.add("/test", new TestController());
            UserService us = new UserService(new File(conf.fsRoot, "users"));
            server.add("/fb", new FacebookLoginController(us, new AuthService(us), "497207593664605", "64fa9962a12964ca167a44978af636e5", "http://192.168.1.200:2424"));
            server.add("/", new LogController());
            server.add("/m/*", new Controller(){
                @Override
                public View get(HttpServletRequest request, PathParser pathInfo) throws Exception {
                        return new StaticFilesView("/pages/mlog.html");
                }
            });
        }

        /**
         * @param args
         * @throws IOException
         */
        public static void main(String[] args) throws IOException {
            Server server = new Server(Config.parseDefault());
            server.init();
            server.start();
            server.join();
        }

        public void start() {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void join() {
            try {
                server.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

}
