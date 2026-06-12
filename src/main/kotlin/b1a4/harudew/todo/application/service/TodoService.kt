package b1a4.harudew.todo.application.service

import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.todo.adapter.dto.request.CreateTodoCalendarRequest
import b1a4.harudew.todo.adapter.dto.request.CreateTodoRequest
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarByMonthResponse
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarResponse
import b1a4.harudew.todo.adapter.dto.response.TodoResponse
import b1a4.harudew.todo.adapter.out.persistence.TodoCalendarJpaRepository
import b1a4.harudew.todo.adapter.out.persistence.entity.TodoCalendarEntity
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class TodoService(
    private val todoCalendarJpaRepository: TodoCalendarJpaRepository,
    private val memberJpaRepository: MemberJpaRepository
) {

    fun monthlyStatus(memberId: String, year: Int, month: Int): List<TodoCalendarByMonthResponse> {
        val yearMonth = YearMonth.of(year, month)
        return todoCalendarJpaRepository.findByMemberAndDateBetween(
            memberId,
            yearMonth.atDay(1),
            yearMonth.atEndOfMonth()
        )
            .groupBy { it.date }
            .map { (date, todos) ->
                val completed = todos.count { it.isComplete }
                TodoCalendarByMonthResponse(
                    date = date,
                    todoTotalCount = todos.size,
                    completedCount = completed,
                    isAllCompleted = todos.isNotEmpty() && completed == todos.size
                )
            }
            .sortedBy { it.date }
    }

    fun findByDate(memberId: String, date: LocalDate): List<TodoCalendarResponse> =
        todoCalendarJpaRepository.findByMemberAndDate(memberId, date).map { it.toResponse() }

    fun createCalendar(memberId: String, request: CreateTodoCalendarRequest): TodoCalendarResponse {
        val member = memberJpaRepository.getReferenceById(memberId)
        return todoCalendarJpaRepository.save(
            TodoCalendarEntity(
                member = member,
                content = request.content,
                date = request.date
            )
        ).toResponse()
    }

    fun updateContent(memberId: String, id: Long, content: String): TodoCalendarResponse {
        val todo = findOwned(memberId, id)
        todo.content = content
        return todoCalendarJpaRepository.save(todo).toResponse()
    }

    fun updateDate(memberId: String, id: Long, date: LocalDate): TodoCalendarResponse {
        val todo = findOwned(memberId, id)
        todo.date = date
        return todoCalendarJpaRepository.save(todo).toResponse()
    }

    fun toggle(memberId: String, id: Long): TodoCalendarResponse {
        val todo = findOwned(memberId, id)
        todo.isComplete = !todo.isComplete
        return todoCalendarJpaRepository.save(todo).toResponse()
    }

    fun delete(memberId: String, id: Long) {
        todoCalendarJpaRepository.delete(findOwned(memberId, id))
    }

    fun createTodo(memberId: String, request: CreateTodoRequest): TodoResponse {
        val content = request.content ?: request.title
        val date = request.date ?: LocalDate.now()
        val created = createCalendar(memberId, CreateTodoCalendarRequest(content, date))
        return TodoResponse(created.id, title = content, content = content, isComplete = created.isComplete, date = created.date)
    }

    fun findAll(memberId: String): List<TodoResponse> =
        todoCalendarJpaRepository.findByMemberAndDateBetween(
            memberId,
            LocalDate.now().minusYears(10),
            LocalDate.now().plusYears(10)
        ).map {
            TodoResponse(
                id = requireNotNull(it.id),
                title = it.content,
                content = it.content,
                isComplete = it.isComplete,
                date = it.date
            )
        }

    private fun findOwned(memberId: String, id: Long): TodoCalendarEntity =
        todoCalendarJpaRepository.findOwnedById(id, memberId)
            .orElseThrow { NoSuchElementException("todo를 찾을 수 없습니다. id=$id") }
}
