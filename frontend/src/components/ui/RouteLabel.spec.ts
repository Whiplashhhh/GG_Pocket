import { describe, expect, it } from 'vitest'

import { mountWithApp } from '@/test/render'

import RouteLabel from './RouteLabel.vue'

const yellow = { label: 'jaune', hex: '#f5c518' }
const blue = { label: 'bleu', hex: '#1f5fbf' }

describe('RouteLabel', () => {
  it('shows grade, name and cord', () => {
    const wrapper = mountWithApp(RouteLabel, {
      props: { cord: 12, name: 'Bye Bye Morice', grade: '6b', holds: [yellow] },
    })
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('Bye Bye Morice')
    expect(wrapper.text()).toContain('6b')
  })

  it('gives screen readers one complete sentence', () => {
    const wrapper = mountWithApp(RouteLabel, {
      props: { cord: 12, name: 'Bye Bye Morice', grade: '6b', holds: [yellow, blue] },
    })
    expect(wrapper.get('.sr-only').text()).toBe(
      '6b, Bye Bye Morice, couleur jaune et bleu, corde 12',
    )
  })

  it('marks a route in last chance, visibly and for screen readers', () => {
    const wrapper = mountWithApp(RouteLabel, {
      props: {
        cord: 15,
        name: 'Tonton Talon',
        grade: '6a+',
        holds: [yellow],
        status: 'LAST_CHANCE',
      },
    })
    expect(wrapper.get('.sr-only').text()).toMatch(/, dernière chance$/)
    expect(wrapper.text()).toContain('Dernière chance')
  })

  it('stamps a dismounted route', () => {
    const wrapper = mountWithApp(RouteLabel, {
      props: { cord: 26, name: 'Schtroumpfs', grade: '5b', holds: [blue], status: 'DISMOUNTED' },
    })
    expect(wrapper.get('.sr-only').text()).toMatch(/, démontée$/)
    expect(wrapper.text()).toContain('Démontée')
  })

  it('draws one hold per colour', () => {
    const wrapper = mountWithApp(RouteLabel, {
      props: { cord: 3, name: 'Iron Man', grade: '6a+', holds: [yellow, blue] },
    })
    expect(wrapper.findAll('svg')).toHaveLength(2)
  })
})
