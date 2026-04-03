package dizzyfox734.springbootboard.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
