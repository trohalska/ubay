package world.ucode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import world.ucode.models.Bid;
import world.ucode.models.Lot;
import world.ucode.models.User;
import world.ucode.services.LotService;
import world.ucode.services.UserService;
import world.ucode.utils.CreateJSON;

import java.util.List;
import java.util.Set;

@Controller
public class ViewProfileController {
    @Autowired
    private CreateJSON createJSON;
    @Autowired
    UserService userService;
    @Autowired
    private LotService lotService;

    @RequestMapping(value = "/viewProfile", method = RequestMethod.GET)
    public ModelAndView viewProfile(@RequestParam String login) {
        ModelAndView mav = new ModelAndView();
        List<Lot> lots;
        try {
            if (login != null && !login.equals("")) {
                ObjectMapper mapper = new ObjectMapper();
                User user = userService.findUserByLogin(login);
                String json = mapper.writeValueAsString(user);
                mav.addObject("user", json);

                lots = lotService.findAllLotsByUser(login);
                Set<Bid> bids = userService.findBidsByBidder(login);
                JSONArray jsonLotsSeller = createJSON.mainShowLotsJSON(lots);
                JSONArray jsonLotsBidder = createJSON.profileBidderShowLotsJSON(bids);
                mav.addObject("lots", user.getUserRole().equals("seller") ? jsonLotsSeller : jsonLotsBidder);
            }
            mav.setViewName("/viewProfile");
            return mav;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad JSON");
            mav.setViewName("/errors/error");
            return mav;
        }
    }
}
