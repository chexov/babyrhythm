package com.babyrhythm.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.Gson;

public class ActivityTest {

    @Test
    public void test() {
        Activity a = (new Gson()).fromJson("{\"type\":\"ololo\"}", Activity.class);
        System.out.println(a.timestamp);
    }

}
