package eu.sshopencloud.marketplace.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class RootRedirectController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String toApiDocs() {
        return "redirect:/swagger-ui/index.html?url=http://localhost:8080/v3/api-docs";
    }
}
