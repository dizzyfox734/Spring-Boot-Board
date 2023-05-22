package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.CommentDto;
import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.service.CommentService;
import dizzyfox734.springbootboard.service.PostService;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    /**
     * 댓글 저장
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String modify(CommentDto commentDto, @PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.findOne(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        commentDto.setContent(comment.getContent());
        return "comment/form";
    }

    /**
     * 댓글 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@Valid CommentDto commentDto, BindingResult bindingResult,
                               @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "comment/form";
        }
        Comment comment = this.commentService.findOne(id);

        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.commentService.modify(comment, commentDto.getContent());

        return String.format("redirect:/post/detail/%s", comment.getPost().getId());
    }

    /**
     * 댓글 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String delete(Principal principal, @PathVariable("id") Integer id) {
        Comment comment = this.commentService.findOne(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.commentService.delete(comment);

        return String.format("redirect:/post/detail/%s", comment.getPost().getId());
    }
}