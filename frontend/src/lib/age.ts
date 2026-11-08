/** Spec §6: under 15, a parent or legal guardian must agree. Same rule as the backend (Age.java). */
export const PARENTAL_CONSENT_AGE = 15
/** D-119: under 18, a minor; "Afficher mon âge" is not offered (Age.java). */
export const ADULT_AGE = 18

const DATE = /^\d{4}-\d{2}-\d{2}$/

/** Today in Gravelines, as YYYY-MM-DD. */
export function clubToday(now: Date = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Paris' }).format(now)
}

/** Whole years between a birth date and a day, both as YYYY-MM-DD. */
export function yearsOld(birthDate: string, today: string): number {
  const [by = 0, bm = 0, bd = 0] = birthDate.split('-').map(Number)
  const [ty = 0, tm = 0, td = 0] = today.split('-').map(Number)
  const hadBirthday = tm > bm || (tm === bm && td >= bd)
  return ty - by - (hadBirthday ? 0 : 1)
}

export function needsParentalConsent(birthDate: string, today: string = clubToday()): boolean {
  return DATE.test(birthDate) && yearsOld(birthDate, today) < PARENTAL_CONSENT_AGE
}

/** False while the date is not typed in full: nothing to hide yet. */
export function isMinor(birthDate: string, today: string = clubToday()): boolean {
  return DATE.test(birthDate) && yearsOld(birthDate, today) < ADULT_AGE
}
