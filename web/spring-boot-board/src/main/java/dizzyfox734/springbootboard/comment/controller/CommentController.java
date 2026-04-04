package dizzyfox734.springbootboard.comment.controller;

import dizzyfox734.springbootboard.comment.controller.dto.CommentDto;
import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.service.CommentService;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 댓글 저장
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String create(Model model, @PathVariable("id") Integer postId,
                         @Valid CommentDto commentDto, BindingResult bindingResult,
                         Principal principal) {
        Post post = this.postService.getPost(postId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post);
            return "post/detail";
        }

        Integer commentId = this.commentService.create(postId, commentDto.getContent(), principal.getName());
        return String.format("redirect:/post/detail/%s#comment_%s", postId, commentId);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String modify(CommentDto commentDto, @PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getCommentForModify(id, principal.getName());
        commentDto.setContent(comment.getContent());
        return "comment/form";
    }

    /**
     * 댓글 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@Valid CommentDto commentDto, BindingResult bindingResult,
                         @PathVariable("id") Integer postId, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "comment/form";
        }

        Integer commentId = this.commentService.modify(postId, commentDto.getContent(), principal.getName());
        return String.format("redirect:/post/detail/%s#comment_%s", postId, commentId);
    }

    /**
     * 댓글 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String delete(Principal principal, @PathVariable("id") Integer commentId) {
        Comment comment = commentService.getComment(commentId);
        Integer postId = comment.getPost().getId();

        commentService.delete(commentId, principal.getName());

        return String.format("redirect:/post/detail/%s", postId);
    }
}