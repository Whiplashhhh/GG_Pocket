package fr.gravelinesgrimp.pocket.topo.api;

import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.topo.application.TodoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * "À faire": the routes on the wall I cannot climb yet. Ticking is in {@link FavoriteController}.
 */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class TodoController {

    private final TodoService service;

    TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping("/me/todo")
    @Operation(
            operationId = "getMyTodo",
            summary = "Routes on the wall with what I know of them, per mode")
    TodoResponse getMine(@Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return TodoResponse.from(service.of(me.memberId()));
    }
}
