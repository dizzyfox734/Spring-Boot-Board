package dizzyfox734.springbootboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/post/list";
    }
}
