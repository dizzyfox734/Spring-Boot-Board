package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.CommentDto;
import dizzyfox734.springbootboard.controller.dto.PostDto;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.service.PostService;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/post")
@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Page<Post> paging = this.postService.getList(page);
        model.addAttribute("paging", paging);

        return "post/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, CommentDto commentDto) {
        Post post = this.postService.findOne(id);
        model.addAttribute("post", post);

        return "post/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String create(PostDto postDto) {
        return "post/form";
    }

    /**
     * 포스트 저장
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@Valid PostDto postDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        User user = this.userService.getUser(principal.getName());
        this.postService.create(postDto.getTitle(), postDto.getContent(), user);

        return "redirect:/post/list";
    }

    /**
     * 포스트 수정
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String modify(PostDto postDto, @PathVariable("id") Integer id, Principal principal) {
        Post post = this.postService.findOne(id);
        if(!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        return "post/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@Valid PostDto postDto, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        Post post = this.postService.findOne(id);
        if (!post.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.postService.modify(post, postDto.getTitle(), postDto.getContent());
        return String.format("redirect:/post/detail/%s", id);
    }
}
