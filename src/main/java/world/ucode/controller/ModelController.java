package world.ucode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import world.ucode.models.Lot;
import world.ucode.models.User;
import world.ucode.services.UserService;

@Controller
public class ModelController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "/index";
    }
    @RequestMapping(value="/hallo{id}",  produces = "text/plain;charset=UTF-8", method = RequestMethod.GET)
    public String developer(@RequestParam int id, ModelMap model) {
        model.addAttribute("id", id);
        System.out.println(id);
        database();
        return "/id";
    }

    private void database() {
        UserService userService = new UserService();

        User user = new User("tro", "qwerty", 0);
        userService.saveUser(user);

        Lot ferrari = new Lot("Ferrari", 12000);
        ferrari.setSeller(user);
        user.addLot(ferrari);
        Lot ford = new Lot("Ford", 600);
        ford.setSeller(user);
        user.addLot(ford);
        userService.updateUser(user);
    }
}