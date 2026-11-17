/** What the thread needs of a comment: its id and, for a session comment, the one it answers. */
export interface Threadable {
  id: string
  replyTo?: { id: string }
}

/**
 * Comments in reading order (D-128): each first comment, oldest first, followed by every answer
 * it got — answers to answers included — oldest first, one level in. An answer whose comment is
 * gone (deleted) stands on its own: it still says whom it answered.
 */
export function threadComments<T extends Threadable>(
  comments: readonly T[],
): { comment: T; reply: boolean }[] {
  const byId = new Map(comments.map((comment) => [comment.id, comment]))
  const rootOf = (comment: T): T => {
    const seen = new Set<string>()
    let current = comment
    while (current.replyTo !== undefined && !seen.has(current.id)) {
      seen.add(current.id)
      const parent = byId.get(current.replyTo.id)
      if (parent === undefined) break
      current = parent
    }
    return current
  }
  const answers = new Map<string, T[]>()
  for (const comment of comments) {
    const root = rootOf(comment)
    if (root !== comment) answers.set(root.id, [...(answers.get(root.id) ?? []), comment])
  }
  return comments
    .filter((comment) => rootOf(comment) === comment)
    .flatMap((root) => [
      { comment: root, reply: false },
      ...(answers.get(root.id) ?? []).map((comment) => ({ comment, reply: true })),
    ])
}
