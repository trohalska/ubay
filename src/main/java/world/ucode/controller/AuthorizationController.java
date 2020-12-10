package world.ucode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthorizationController {
    @RequestMapping(value = "/authorization", method = RequestMethod.GET)
    public String signin() {
        return "/authorization";
    }
}
