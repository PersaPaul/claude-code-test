# Todo

A small todo app: **Spring Boot 4** REST API + **Lit 3** frontend.

```
backend/    Spring Boot 4.1.2, Java 21 — REST API on :8080, in-memory store
frontend/   Lit 3 + Vite — dev server on :5173, proxies /api to the backend
```

## Requirements

- **JDK 21+** (the Maven wrapper downloads Maven itself, so no Maven install needed)
- **Node 20+**

Homebrew's `openjdk@21` is keg-only and is not on `PATH` by default. Either add
it per-shell:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
```

or symlink it system-wide:

```bash
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

## Running it

Two terminals.

```bash
# 1. backend
cd backend
./mvnw spring-boot:run

# 2. frontend
cd frontend
npm install
npm run dev
```

Then open <http://localhost:5173>.

The Vite dev server proxies `/api` to `localhost:8080`, so the browser stays on
one origin and there is no CORS hop in development.

## Tests

```bash
cd backend && ./mvnw test      # MockMvc tests over the REST API
cd frontend && npm run build   # type-free, but catches import/syntax errors
```

## API

Base path `/api/todos`. A todo is
`{ id, title, completed, createdAt }`.

| Method   | Path         | Body                              | Response                  |
| -------- | ------------ | --------------------------------- | ------------------------- |
| `GET`    | `/`          | — (query params below)            | `200` a page of todos     |
| `POST`   | `/`          | `{ "title": "..." }`              | `201` the created todo    |
| `PATCH`  | `/{id}`      | `{ "title"?, "completed"? }`      | `200` the updated todo    |
| `DELETE` | `/{id}`      | —                                 | `204`                     |
| `DELETE` | `/`          | —                                 | `200 { "deleted": n }`    |

### Listing is paginated

`GET /api/todos` takes `filter` (`all` \| `active` \| `completed`,
case-insensitive, default `all`), `page` (0-based, default `0`) and `size`
(1–100, default `10`), and returns:

```json
{
  "items": [ /* this page */ ],
  "page": 0,
  "size": 10,
  "totalItems": 14,
  "totalPages": 2,
  "activeCount": 14,
  "completedCount": 0
}
```

`totalItems` counts todos matching `filter`, while `activeCount` and
`completedCount` span **every** todo — the footer's "items left" has to stay
correct while the client holds only one page. `totalPages` is never below 1, so
an empty result still reads "1 of 1".

A `page` past the end clamps to the last page rather than returning an empty
list, so deleting the final item on a page can't strand the client on a blank
view. Out-of-range `page`/`size` or an unknown `filter` give `400`.

`PATCH` applies only the fields present, so toggling sends just
`{"completed": true}`. Errors come back as RFC 7807 problem details: `400` for a
blank or over-long title, `404` for an unknown id.

```bash
curl -X POST localhost:8080/api/todos \
  -H 'Content-Type: application/json' -d '{"title":"Buy milk"}'
```

## Layout

```
backend/src/main/java/com/example/todo/
  domain/   Todo (record), TodoRepository (in-memory), TodoService
  web/      TodoController, TodoRequests (validated DTOs),
            ApiExceptionHandler (problem details), WebConfig (dev CORS)

frontend/src/
  api.js               fetch wrapper; unwraps problem-detail errors
  components/
    todo-app.js        root: owns state, talks to the API, filters, paging
    todo-form.js       new-todo input → `todo-add`
    todo-list.js       keyed list via lit's repeat()
    todo-item.js       row: toggle / rename / delete, events carry the id
    todo-pager.js      prev/next → `page-change`; hides itself on one page
```

State lives in `todo-app`; the other components are presentational and
communicate upward with custom events. Because the client holds only one page,
filtering and counting are the server's job — every mutation reloads the
current page instead of patching the list locally. Page size is
`TodoApp.PAGE_SIZE` (10).

## Notes

- Todos are held in memory, so they reset when the backend restarts. Swap
  `TodoRepository` for a Spring Data repository to persist them; the paging
  contract is deliberately `Pageable`-shaped to make that swap easy.
- Double-click a todo's text to rename it; `Enter` saves, `Escape` cancels.
