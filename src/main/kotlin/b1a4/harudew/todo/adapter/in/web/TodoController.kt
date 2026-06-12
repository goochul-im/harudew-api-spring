package b1a4.harudew.todo.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.todo.adapter.dto.request.CreateTodoCalendarRequest
import b1a4.harudew.todo.adapter.dto.request.CreateTodoRequest
import b1a4.harudew.todo.adapter.dto.request.UpdateTodoCalendarContentRequest
import b1a4.harudew.todo.adapter.dto.request.UpdateTodoCalendarDateRequest
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarByMonthResponse
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarResponse
import b1a4.harudew.todo.adapter.dto.response.TodoResponse
import b1a4.harudew.todo.application.service.TodoService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/todos")
class TodoController(
    private val todoService: TodoService
) {

    @GetMapping("/calendar")
    fun monthlyStatus(
        @CurrentMember member: Member,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): List<TodoCalendarByMonthResponse> = todoService.monthlyStatus(member.id, year, month)

    @PostMapping("/calendar")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCalendar(
        @CurrentMember member: Member,
        @RequestBody request: CreateTodoCalendarRequest
    ): TodoCalendarResponse = todoService.createCalendar(member.id, request)

    @GetMapping("/calendar/date")
    fun byDate(
        @CurrentMember member: Member,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): List<TodoCalendarResponse> = todoService.findByDate(member.id, date)

    @PatchMapping("/calendar/content/{id}")
    fun updateContent(
        @CurrentMember member: Member,
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoCalendarContentRequest
    ): TodoCalendarResponse = todoService.updateContent(member.id, id, request.content)

    @PatchMapping("/calendar/date/{id}")
    fun updateDate(
        @CurrentMember member: Member,
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoCalendarDateRequest
    ): TodoCalendarResponse = todoService.updateDate(member.id, id, request.date)

    @PatchMapping("/calendar/{id}")
    fun toggle(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ): TodoCalendarResponse = todoService.toggle(member.id, id)

    @DeleteMapping("/calendar/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCalendar(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ) {
        todoService.delete(member.id, id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTodo(
        @CurrentMember member: Member,
        @RequestBody request: CreateTodoRequest
    ): TodoResponse = todoService.createTodo(member.id, request)

    @GetMapping
    fun findAll(@CurrentMember member: Member): List<TodoResponse> =
        todoService.findAll(member.id)

    @PatchMapping("/{id}")
    fun updateTodo(
        @CurrentMember member: Member,
        @PathVariable id: Long,
        @RequestBody request: CreateTodoRequest
    ): TodoCalendarResponse = todoService.updateContent(member.id, id, request.content ?: request.title)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTodo(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ) {
        todoService.delete(member.id, id)
    }
}
