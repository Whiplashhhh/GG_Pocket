import { useI18n } from 'vue-i18n'

import { badgeOf, isMaxLevel, isTiered, tierMark, type Badge, type BadgeCode } from '@/lib/badges'

/** The names, descriptions and tier conditions of the badges, from the i18n. */
export function useBadgeText() {
  const { t } = useI18n()

  return {
    name: (badge: Badge) => t(`badges.names.${badge}`),
    description: (badge: Badge) => t(`badges.descriptions.${badge}`),
    /** What a screen reader hears for a medal: the badge and its level. */
    label(badge: Badge, level: number): string {
      const name = t(`badges.names.${badge}`)
      if (level <= 0) return t('badges.srNotYet', { name })
      if (!isTiered(badge)) return t('badges.srTimes', { name, n: level }, level)
      return isMaxLevel(badge, level)
        ? t('badges.srLevelMax', { name })
        : t('badges.srLevel', { name, n: level })
    },
    /** What a tier asks: "toper 10 voies, tous modes confondus." */
    tierCondition(code: BadgeCode): string {
      const badge = badgeOf(code)
      const mark = tierMark(code)
      return badge === 'FIRST_GRADE'
        ? t('badges.tiers.FIRST_GRADE', { grade: mark })
        : t(`badges.tiers.${badge}`, Number(mark))
    },
  }
}
