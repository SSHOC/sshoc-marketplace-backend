package eu.sshopencloud.marketplace.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class RootRedirectController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String toApiDocs(HttpServletRequest request) {
        return "redirect:/swagger-ui/index.html?url=" + request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort() +"/v3/api-docs";
    }
}
