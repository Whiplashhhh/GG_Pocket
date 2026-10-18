import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'

import { mountWithApp } from '@/test/render'

import TabBar from './TabBar.vue'

function makeRouter() {
  const Empty = { template: '<div />' }
  return createRouter({
    history: createMemoryHistory(),
    routes: ['/actu', '/topo', '/a-faire', '/planning', '/podium'].map((path) => ({
      path,
      component: Empty,
    })),
  })
}

describe('TabBar', () => {
  it('offers exactly the five tabs, in order', async () => {
    const router = makeRouter()
    await router.push('/actu')
    const wrapper = mountWithApp(TabBar, { global: { plugins: [router] } })

    const links = wrapper.findAll('a')
    expect(links.map((link) => link.text())).toEqual([
      'Actu',
      'Topo',
      'À faire',
      'Planning',
      'Podium',
    ])
    expect(links.map((link) => link.attributes('href'))).toEqual([
      '/actu',
      '/topo',
      '/a-faire',
      '/planning',
      '/podium',
    ])
  })

  it('marks the current tab for assistive technologies', async () => {
    const router = makeRouter()
    await router.push('/planning')
    const wrapper = mountWithApp(TabBar, { global: { plugins: [router] } })

    const current = wrapper
      .findAll('a')
      .filter((link) => link.attributes('aria-current') === 'page')
    expect(current.map((link) => link.text())).toEqual(['Planning'])
  })
})
