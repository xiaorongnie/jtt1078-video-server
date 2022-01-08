package com.transcodegroup.jtt1078.core.httpflv;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @RequestMapping(value = "/index")
    public String encrypt() {
        return new Date().toString();
    }

}
