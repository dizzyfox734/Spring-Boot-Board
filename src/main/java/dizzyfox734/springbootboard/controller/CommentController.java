package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.CommentDto;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.service.CommentService;
import dizzyfox734.springbootboard.service.PostService;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    /**
     * 답변 저장
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String create(Model model, @PathVariable("id") Integer id, @Valid CommentDto commentDto,
                               BindingResult bindingResult,Principal principal) {
        Post post = this.postService.findOne(id);
        User user = this.userService.getUser(principal.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post);
            return "post/detail";
        }
        this.commentService.create(post, commentDto.getContent(), user);
        return String.format("redirect:/post/detail/%s", id);
    }
}