import { flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import type { BadgeCode } from '@/lib/badges'
import { mountWithApp } from '@/test/render'

import SessionSavedMoment from './SessionSavedMoment.vue'

vi.mock('@/api/client', () => ({
  api: { GET: vi.fn(() => new Promise(() => undefined)) },
}))

function moment(props: { queued: boolean; unlocked?: BadgeCode[] }) {
  return mountWithApp(SessionSavedMoment, {
    props,
    global: { plugins: [[VueQueryPlugin, { queryClient: new QueryClient() }]] },
  })
}

describe('SessionSavedMoment', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('stamps the session and leaves on its own without a badge', () => {
    const wrapper = moment({ queued: false })
    expect(wrapper.text()).toContain('Séance au carnet')

    vi.advanceTimersByTime(1400)

    expect(wrapper.emitted('done')).toHaveLength(1)
  })

  it('then shows each badge unlocked, one at a time, until the climber moves on', async () => {
    const wrapper = moment({
      queued: false,
      unlocked: ['TOPPED_1', 'TOPPED_5', 'GRADE_5B', 'FLASH'],
    })

    vi.advanceTimersByTime(1100)
    await flushPromises()
    expect(wrapper.text()).toContain('Badge débloqué')
    expect(wrapper.text()).toContain('1 sur 3')
    expect(wrapper.text()).toContain('Voies topées')
    expect(wrapper.text()).toContain('Niveau 2')
    expect(wrapper.emitted('done')).toBeUndefined()

    await wrapper.get('button').trigger('click')
    expect(wrapper.text()).toContain('Première cotation')
    await wrapper.get('button').trigger('click')
    expect(wrapper.text()).toContain('Le flash')
    expect(wrapper.get('button').text()).toBe('Continuer')

    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('done')).toHaveLength(1)
  })

  it('never waits on badges for a session still in the outbox', () => {
    const wrapper = moment({ queued: true })
    expect(wrapper.text()).toContain('En attente de réseau')

    vi.advanceTimersByTime(1400)

    expect(wrapper.emitted('done')).toHaveLength(1)
  })
})
