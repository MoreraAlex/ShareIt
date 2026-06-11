package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentCreateRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public CommentResponse mapCommentToCommentResponse(Comment comment, String authorName) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setAuthorName(authorName);
        response.setItemId(comment.getItemId());
        response.setText(comment.getText());
        response.setCreated(comment.getCreated());
        return response;
    }

    public Comment mapCommentCreateRequestToComment(
            CommentCreateRequest request,
            User author,
            Long itemId,
            LocalDateTime created
    ) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setItemId(itemId);
        comment.setText(request.getText());
        comment.setCreated(created);
        return comment;
    }

}
