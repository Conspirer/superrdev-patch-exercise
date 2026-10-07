-- H2-compatible task search query
-- Used by the Spring Data repository layer
--
-- Parameters:
--   :term   — search term wrapped in wildcards, e.g. '%api%'
--   :status — status filter or NULL for all statuses
--
-- Note: AND binds tighter than OR, so the title/description match is
-- parenthesised. Without the parentheses archived rows leak into results
-- and the status filter is skipped for title matches.

SELECT *
FROM tasks
WHERE archived = FALSE
  AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term)
  AND (:status IS NULL OR status = :status)
ORDER BY created_at DESC, id DESC;
