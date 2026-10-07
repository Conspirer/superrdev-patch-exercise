# Notes

## Summary of changes

1. **SQL operator precedence** (repository query, `db/queries`, Oracle package). `AND` binds tighter than `OR`, so archived tasks leaked into search and the status filter was ignored for title matches (and entirely when the search was blank). Parenthesised the OR; added `id` as an ORDER BY tie-breaker.
2. **Artificial `Thread.sleep`** in the controller. Blank and short queries slept up to 1 s. Removed.
3. **Validation.** Unknown `status`, `page < 1`, and `pageSize` outside 1–100 return 400 instead of 500.
4. **Pagination in the database.** It loaded every match and sliced it in Java; now uses `Pageable` with a count query. Same response shape.
5. **`useTasks` hook.** Loading never cleared on error (stuck spinner), errors never reset, and stale responses could overwrite newer ones. Added an `AbortController` and fixed the state handling.
6. **UX.** Search is debounced (300 ms); page resets to 1 when the search or filter changes.
7. **Locale-safe case conversion** (`Locale.ROOT`). On a Turkish-locale server, `in_progress` uppercased to `İN_PROGRESS`.
8. SLF4J instead of `System.out`; 6 regression tests in `TaskControllerTest`.

## Not changed

- LIKE wildcards (`%`, `_`) in input aren't escaped. Low impact.
- Search ranking, timezones: features, not bugs.
- Enums for status/priority in the entity. A larger refactor.

## Biggest remaining risk

`LIKE '%term%'` can't use an index, so every search is a full table scan; at scale this needs full-text search. Also, the H2 console is enabled with no auth.

## Assumptions

Archived tasks never appear in search; the repository query is the source of truth for the SQL files.

## Tools / AI

Used Claude Code for some reading of the codebase, reproduce each bug with curl first, draft the fixes and tests, and check the UI in headless Chromium. I reviewed every change and wrote the handwritten notes myself.
