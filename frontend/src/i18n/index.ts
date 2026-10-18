import { createI18n } from 'vue-i18n'

import fr from './fr.json'

export type MessageSchema = typeof fr

declare module 'vue-i18n' {
  // Typed keys: t('nav.topo') is checked by the compiler.
  // eslint-disable-next-line @typescript-eslint/no-empty-object-type -- module augmentation pattern of vue-i18n
  export interface DefineLocaleMessage extends MessageSchema {}
}

export const i18n = createI18n<[MessageSchema], 'fr', false>({
  legacy: false,
  locale: 'fr',
  fallbackLocale: 'fr',
  messages: { fr },
})
