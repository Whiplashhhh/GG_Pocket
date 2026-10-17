import { mount, type ComponentMountingOptions } from '@vue/test-utils'
import type { Component } from 'vue'

import { i18n } from '@/i18n'

/** Mounts a component with the app's i18n, as in the real app. */
export function mountWithApp<C extends Component>(
  component: C,
  options: ComponentMountingOptions<C> = {},
) {
  return mount(component, {
    ...options,
    global: { ...options.global, plugins: [i18n, ...(options.global?.plugins ?? [])] },
  })
}
