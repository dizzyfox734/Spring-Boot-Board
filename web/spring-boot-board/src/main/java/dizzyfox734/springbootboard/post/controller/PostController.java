package dizzyfox734.springbootboard.post.controller;

import dizzyfox734.springbootboard.comment.controller.dto.CommentDto;
import dizzyfox734.springbootboard.post.controller.dto.PostDto;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequestMapping("/post")
@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Post> paging = postService.findPosts(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "post/list";
    }

    @GetMapping("/detail/{postId}")
    public String detail(Model model,
                         @PathVariable("postId") Integer postId,
                         CommentDto commentDto) {
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);

        return "post/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String create(PostDto postDto, Model model) {
        model.addAttribute("isModify", false);
        return "post/form";
    }

    /**
     * 포스트 저장
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@Valid PostDto postDto,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isModify", false);
            return "post/form";
        }

        postService.create(postDto.getTitle(), postDto.getContent(), principal.getName());

        return "redirect:/post/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{postId}")
    public String modify(PostDto postDto,
                         @PathVariable("postId") Integer postId,
                         Principal principal,
                         Model model) {
        Post post = postService.getPostForModify(postId, principal.getName());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        model.addAttribute("isModify", true);
        return "post/form";
    }

    /**
     * 포스트 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{postId}")
    public String modify(@Valid PostDto postDto,
                         BindingResult bindingResult,
                         Principal principal,
                         @PathVariable("postId") Integer postId,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isModify", true);
            return "post/form";
        }

        postService.modify(postId, postDto.getTitle(), postDto.getContent(), principal.getName());
        return String.format("redirect:/post/detail/%s", postId);
    }

    /**
     * 포스트 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{postId}")
    public String delete(Principal principal,
                         @PathVariable("postId") Integer postId) {
        postService.delete(postId, principal.getName());

        return "redirect:/post/list";
    }
}
