import { useI18n } from 'vue-i18n'

import { badgeFamily, badgeMark, type BadgeCode } from '@/lib/badges'

/** The name and the condition of a badge, from the i18n (tiers share one text per family). */
export function useBadgeText() {
  const { t } = useI18n()

  function text(code: BadgeCode, part: 'name' | 'condition'): string {
    const family = badgeFamily(code)
    if (family === 'REPEATABLE') return t(`badges.repeatable.${code}.${part}`)
    const mark = badgeMark(code) ?? ''
    if (family === 'FIRST_GRADE') return t(`badges.tiers.FIRST_GRADE.${part}`, { grade: mark })
    return t(`badges.tiers.${family}.${part}`, Number(mark))
  }

  return {
    name: (code: BadgeCode) => text(code, 'name'),
    condition: (code: BadgeCode) => text(code, 'condition'),
  }
}
