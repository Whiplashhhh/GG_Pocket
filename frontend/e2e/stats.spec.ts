import { readFile } from 'node:fs/promises'

import type { Page } from '@playwright/test'

import type { FakeClub } from './fakeClub'
import { expect, test } from './fixtures'

/** The app reads "today" from the phone: the same day as the fake server (Tuesday 20 October). */
async function sameDay(page: Page, club: FakeClub) {
  await page.clock.setSystemTime(new Date(club.now))
}

async function openSection(page: Page, isMobile: boolean, name: string) {
  await page.goto('/gestion')
  if (isMobile) {
    await page.getByRole('link', { name }).click()
  } else {
    await page
      .getByRole('navigation', { name: 'Sections de la gestion' })
      .getByRole('link', { name })
      .click()
  }
  await expect(page.getByRole('heading', { level: 1, name })).toBeVisible()
}

/** October: Bye Bye Morice 3 times (2 topped, 1 clean) by 2 climbers, Iron Man once, Sec ! tried. */
function climbOctober(club: FakeClub) {
  const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
  const lea = club.addMember({ nickname: 'lea', firstName: 'Léa', lastName: 'Dupont' })
  club.climbs.add(tom, 1, [
    { route: 'Bye Bye Morice' },
    { route: 'Bye Bye Morice', topped: false },
    { route: 'Iron Man', clean: true },
  ])
  club.climbs.add(lea, 2, [{ route: 'Bye Bye Morice', clean: true }])
  // "Juste moi": counted all the same.
  club.climbs.add(lea, 3, [{ route: 'Sec !', topped: false }], { visibility: 'PRIVATE' })
  // September.
  club.climbs.add(tom, 25, [{ route: 'Réglisse' }])
}

test.describe('a climber', () => {
  test('cannot open the statistics', async ({ page }) => {
    await page.goto('/gestion/frequentation')
    await expect(page).toHaveURL(/\/actu$/)
  })
})

test.describe('a setter', () => {
  test.use({ signedInAs: 'SETTER' })

  test('reads the attendance of a month, sorts it and exports it', async ({
    page,
    club,
    isMobile,
  }) => {
    await sameDay(page, club)
    climbOctober(club)

    await openSection(page, isMobile, 'Fréquentation')
    await expect(page.getByRole('heading', { level: 2, name: /octobre 2026/i })).toBeVisible()
    await expect(page.getByLabel('Le mois en chiffres')).toHaveText(
      /ascensions\s*5\s*grimpeurs\s*2\s*de réussite\s*60\s%/,
    )

    const rows = isMobile
      ? page.getByRole('listitem').filter({ hasText: /ascension/ })
      : page.getByRole('table').getByRole('row')
    if (isMobile) {
      await expect(rows.first()).toContainText('Bye Bye Morice')
      await expect(rows.first()).toContainText('3 ascensions')
      await expect(rows.first()).toContainText('2 grimpeurs')
      await expect(rows.first()).toContainText('67 % topées')
    } else {
      // The header, then the most climbed first.
      await expect(rows.nth(1)).toContainText('Bye Bye Morice')
      await expect(rows.nth(1).getByRole('cell')).toHaveText([/.+/, '3', '2', '1', '2', /67\s%/])
    }

    await page.getByLabel('Trier par').selectOption('Le meilleur taux de réussite')
    await expect(page).toHaveURL(/tri=success/)
    await expect(rows.nth(isMobile ? 0 : 1)).toContainText('Iron Man')

    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Exporter en CSV' }).click()
    const file = await download
    expect(file.suggestedFilename()).toBe('gg-pocket-frequentation-2026-10.csv')
    const csv = (await readFile(await file.path(), 'utf8')).split('\r\n')
    expect(csv[0]).toBe(
      '﻿Corde;Secteur;Voie;Cotation;Prises;Ouverte le;Ascensions;Topées;Enchaînées;Grimpeurs;Réussite (%)',
    )
    expect(csv[1]).toMatch(/^3;[^;]+;Iron Man;6a\+;[^;]+;[\d-]+;1;1;1;1;100$/)
    expect(csv).toContainEqual(
      expect.stringMatching(/^12;[^;]+;Bye Bye Morice;6b;[^;]+;2026-05-16;3;2;1;2;67$/),
    )
    await expect(
      page.getByText('Fichier « gg-pocket-frequentation-2026-10.csv » prêt'),
    ).toBeVisible()

    await page.getByRole('button', { name: 'Mois précédent' }).click()
    await expect(page.getByRole('heading', { level: 2, name: /septembre 2026/i })).toBeVisible()
    await expect(page).toHaveURL(/mois=2026-09/)
    await expect(page.getByLabel('Le mois en chiffres')).toHaveText(
      /ascension\s*1\s*grimpeur\s*1\s*de réussite\s*100\s%/,
    )
  })

  test('reviews the topo: grades, cords, most and least climbed, oldest, loved', async ({
    page,
    club,
    isMobile,
  }) => {
    await sameDay(page, club)
    climbOctober(club)
    const onTheWall = club.topo.routes.filter((route) => route.status !== 'DISMOUNTED').length

    await openSection(page, isMobile, 'Bilan du topo')
    await expect(page.getByText(`${String(onTheWall)} voies au mur`)).toBeVisible()
    await expect(page.getByText('dont 2 en dernière chance')).toBeVisible()

    const grades = page.getByRole('region', { name: 'Par cotation' })
    await expect(grades.getByRole('listitem').first()).toContainText('4a')
    await expect(grades.getByRole('listitem').last()).toContainText('7c')

    const cords = page.getByRole('region', { name: 'Par corde' })
    await expect(cords.getByRole('listitem').filter({ hasText: 'Corde 12' }).first()).toContainText(
      '6b',
    )

    const most = page.getByRole('region', { name: 'Les plus grimpées' }).getByRole('listitem')
    await expect(most.first()).toContainText('Bye Bye Morice')
    await expect(most.first()).toContainText('3 ascensions')

    const least = page.getByRole('region', { name: 'Les moins grimpées' }).getByRole('listitem')
    await expect(least.first()).toContainText('Aucune ascension')

    const oldest = page.getByRole('region', { name: 'Les plus anciennes' })
    await expect(oldest.getByRole('listitem').first()).toContainText('Avant-bras en béton')
    await expect(oldest.getByRole('listitem').first()).toContainText('ouverte il y a 8 mois')

    const loved = page.getByRole('region', { name: 'Les coups de cœur' }).getByRole('listitem')
    await expect(loved.first()).toContainText('Bye Bye Morice')
  })
})
