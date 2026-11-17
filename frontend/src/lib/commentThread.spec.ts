import { describe, expect, it } from 'vitest'

import { threadComments } from './commentThread'

const plain = (id: string) => ({ id })
const answer = (id: string, to: string) => ({ id, replyTo: { id: to } })

const shape = (comments: Parameters<typeof threadComments>[0]) =>
  threadComments(comments).map(({ comment, reply }) => `${reply ? '  ' : ''}${comment.id}`)

describe('threadComments', () => {
  it('keeps plain comments in their order', () => {
    expect(shape([plain('a'), plain('b')])).toEqual(['a', 'b'])
  })

  it('puts the answers under their comment, answers to answers included, oldest first', () => {
    expect(
      shape([plain('a'), plain('b'), answer('c', 'a'), answer('d', 'c'), answer('e', 'b')]),
    ).toEqual(['a', '  c', '  d', 'b', '  e'])
  })

  it('lets an answer to a deleted comment stand on its own', () => {
    expect(shape([plain('a'), answer('b', 'gone'), answer('c', 'b')])).toEqual(['a', 'b', '  c'])
  })
})
