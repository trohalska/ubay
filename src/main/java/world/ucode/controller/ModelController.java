package world.ucode.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import world.ucode.models.Bid;
import world.ucode.models.Lot;
import world.ucode.models.Search;
import world.ucode.models.User;
import world.ucode.services.BidService;
import world.ucode.services.LotService;
import world.ucode.utils.CreateJSON;
import world.ucode.utils.SendMail;
import world.ucode.utils.Token;
import world.ucode.services.UserService;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Controller
@ControllerAdvice
public class ModelController {
    ModelAndView mav = new ModelAndView();
    SendMail sendMail = new SendMail();
    LotService lotService = new LotService();
    BidService bidService = new BidService();
    CreateJSON createJSON = new CreateJSON();
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    UserService userService = context.getBean("userService", UserService.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "/index";
    }

    /**
     * ----------------------- helpful functional
     */
    private ModelAndView pageModelAndView(String login, String page) {
        ModelAndView mav = new ModelAndView();
        System.out.println(login);
        try {
            ObjectMapper mapper = new ObjectMapper();
            User user = userService.findUserByLogin(login);
            String json = mapper.writeValueAsString(user);
            mav.addObject("user", json);
            mav.setViewName(page);
            return mav;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad JSON");
            mav.setViewName("/errors/error");
            return mav;
        }
    }
    private ModelAndView pageModelAndView(int lotId, String page) {
        ModelAndView mav = new ModelAndView();
        System.out.println(lotId);
        try {
            ObjectMapper mapper = new ObjectMapper();
//            Lot lot = userService.findLotById(lotId);
//            String json = mapper.writeValueAsString(lot);
//            mav.addObject("lot", json);
            mav.setViewName(page);
            return mav;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad JSON");
            mav.setViewName("/errors/error");
            return mav;
        }
    }

    // -----------------------
    @RequestMapping(value = "/authorization", method = RequestMethod.GET)
    public String signin(ModelMap model) throws UnknownHostException {
//        model.addAttribute("form", new User());
        return "/authorization";
    }

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public ModelAndView main(ModelMap model) {
        if (!model.containsAttribute("search")) {
            model.addAttribute("search", new Search());
        }
        ModelAndView mav = new ModelAndView();
        try {
            ObjectMapper mapper = new ObjectMapper();
//            Lot lot = userService.findLotById(Integer.parseInt(lotId));
//            String json = mapper.writeValueAsString(lot);

            List<Lot> lots = lotService.findAllLots();
            JSONArray json = createJSON.mainShowLotsJSON(lots);

            mav.addObject("lots", json);
            mav.setViewName("/main");

//            List<Lot> lotss = userService.findUser("3").getLots();
//            for (Lot lot:lotss) {
//                System.out.println(lot.getTitle());
//            }
//            List<Bid> bids = userService.findUser("4").getBids();
//            for (Bid bid:bids) {
//                System.out.println(bid.getPrice());
//            }


            return mav;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad JSON");
            mav.setViewName("/errors/error");
            return mav;
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(ModelMap model) {
        return "/profile";
    }

    /**
     * requires unique user login, which profile needs to be showed.
     * */
    @RequestMapping(value = "/viewProfile", method = RequestMethod.GET)
    public ModelAndView viewProfile(@RequestParam String login) {
        return pageModelAndView(login, "/viewProfile");
    }

    /**
     * requires unique seller login (feedbacks about what seller).
     * */
    @RequestMapping(value = "/feedbacks", method = RequestMethod.GET)
    public ModelAndView feedbacks(@RequestParam String login) {
        return pageModelAndView(login, "/feedbacks");
    }

    /**
     * requires unique lot id (to what lot was added feedback).
     * */
    @RequestMapping(value = "/addFeedback", method = RequestMethod.GET)
    public ModelAndView addFeedback(@RequestParam String lotId) {
        return pageModelAndView(Integer.parseInt(lotId), "/addFeedback");
    }
    /**
     * requires unique lot id (that auction show).
     * */
    @RequestMapping(value = "/auction", method = RequestMethod.GET)
    public ModelAndView auction(@RequestParam String lotId) {
        ModelAndView mav = new ModelAndView();
        try {
            Lot lot = userService.findLotById(Integer.parseInt(lotId));
            User user = lot.getSeller();
            JSONObject json = createJSON.auctionJSON(user, lot);
            mav.addObject("lot", json);
            mav.setViewName("/auction");
            return mav;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad JSON");
            mav.setViewName("/errors/error");
            return mav;
        }
    }

    /**
     * requires unique seller login (what seller added lot).
     * */
    @RequestMapping(value = "/addLot", method = RequestMethod.GET)
    public ModelAndView addLot(@RequestParam String login) {
        return pageModelAndView(login, "/addLot");
    }

    public static Timestamp addDays(Timestamp date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);// w ww.  j ava  2  s  .co m
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return new Timestamp(cal.getTime().getTime());

    }

    @RequestMapping(value = "/addLot", method = RequestMethod.POST)
    public ModelAndView addLot(Lot lot) throws JsonProcessingException {
        User user = userService.findUser("3");
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        lot.setStartTime(curTime);
        lot.setFinishTime(addDays(curTime, lot.getDuration()));
        lot.setActive(true);
        user.addLot(lot);
        lotService.saveLot(lot);
        mav.setViewName("/profile");
        return mav;
    }

    @RequestMapping(value = "/newBit", method = RequestMethod.POST)
    public ModelAndView newBid(Bid bid) throws JsonProcessingException {
        System.out.println(bid.getPrice());
        User user = userService.findUser("4");
        bid.setLot(lotService.findLot(10));
        bid.setActive(true);
        user.addBid(bid);
        bidService.saveBid(bid);
        mav.setViewName("redirect:/main");
        return mav;
    }

    @RequestMapping(value = "confirmation{token}", method = RequestMethod.GET)
    public ModelAndView confirmation(@RequestParam("token") String token){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.validateToken(token);
        user.setVerification("verificated");
        userService.updateUser(user);
        modelAndView.setViewName("redirect:/");
        return modelAndView;
    }

    @RequestMapping(value = "/authorization", method = RequestMethod.POST)
    public ModelAndView signin_post(User user, ModelMap model) throws Exception {
        ModelAndView mav = new ModelAndView();
        ObjectMapper mapper = new ObjectMapper();
//        try {
            if (user.getType().equals("signin")) {
                User newUser = userService.validateUser(user);
                String json = mapper.writeValueAsString(newUser);
                mav.addObject("user", json);
                mav.setViewName("/profile");
            } else {
                Token token = new Token();
                user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                user.setToken(token.getJWTToken(user.getLogin()));
                sendMail.sendMail(user);
                userService.saveUser(user);
                String json = mapper.writeValueAsString(user);
                mav.addObject("user",json);
                mav.setViewName("/main");
            }
            return mav;
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("NON authorized or incorrect mail");
//            mav.setViewName("/authorization");
//            return mav;
//        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search_post(@ModelAttribute("search") Search search, Model model) {
        System.out.println(search.getTitle());
        System.out.println(search.getStartPrice());
        System.out.println(search.getDuration());
        System.out.println(search.getStartTime());
        System.out.println(search.getDescription());
        return "redirect:/main";
    }
    // -----------------------
    @RequestMapping(value = "/errors/404", method = RequestMethod.GET)
    public String error404() {
        return "/errors/404";
    }

    @RequestMapping(value = "/errors/error", method = RequestMethod.GET)
    public String exceptions() {
        return "/errors/error";
    }
}