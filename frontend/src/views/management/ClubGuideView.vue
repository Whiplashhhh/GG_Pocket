<script setup lang="ts">
import guide from 'virtual:club-guide'
import { useI18n } from 'vue-i18n'

import ManagementTitle from './ManagementTitle.vue'

/**
 * The club guide (docs/GUIDE_CLUB.md, D-110), as the version running here describes it. Its HTML
 * comes from the repository at build time, never from members: safe to render as is.
 */
const { t } = useI18n()

/** In-page jumps without touching the route; the heading takes the focus for screen readers. */
function jump(id: string) {
  const heading = document.getElementById(id)
  if (heading === null) return
  const smooth = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  heading.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto', block: 'start' })
  heading.focus({ preventScroll: true })
}
</script>

<template>
  <ManagementTitle>{{ t('clubGuide.title') }}</ManagementTitle>
  <p class="px-4 pt-1 pb-4 text-ink-muted lg:px-0">{{ t('clubGuide.intro') }}</p>

  <div class="lg:flex lg:items-start lg:gap-10">
    <nav
      :aria-label="t('clubGuide.contents')"
      class="mx-4 mb-6 rounded-control bg-surface-sunken p-4 lg:sticky lg:top-24 lg:order-last lg:mx-0 lg:w-64 lg:shrink-0"
    >
      <p class="mb-2 font-semibold" aria-hidden="true">{{ t('clubGuide.contents') }}</p>
      <ol>
        <li v-for="section in guide.sections" :key="section.id">
          <a
            :href="`#${section.id}`"
            class="flex min-h-11 items-center underline-offset-2 hover:underline"
            @click.prevent="jump(section.id)"
            >{{ section.title }}</a
          >
        </li>
      </ol>
    </nav>
    <!-- eslint-disable-next-line vue/no-v-html -- built from docs/GUIDE_CLUB.md, raw HTML refused -->
    <article class="guide min-w-0 flex-1 px-4 pb-10 lg:max-w-3xl lg:px-0" v-html="guide.html" />
  </div>
</template>

<style scoped>
.guide :deep(h2) {
  margin-top: 2.5rem;
  margin-bottom: 0.75rem;
  font-size: 1.25rem;
  font-weight: 750;
  font-stretch: 112%;
  letter-spacing: -0.01em;
  scroll-margin-top: 6rem;
}

.guide :deep(h3) {
  margin-top: 1.75rem;
  margin-bottom: 0.5rem;
  font-size: 1.0625rem;
  font-weight: 700;
  scroll-margin-top: 6rem;
}

.guide :deep(h2:focus),
.guide :deep(h3:focus) {
  outline: none;
}

.guide :deep(p),
.guide :deep(ul),
.guide :deep(ol),
.guide :deep(.guide-table) {
  margin-bottom: 0.875rem;
}

.guide :deep(ul) {
  padding-left: 1.25rem;
  list-style: disc;
}

.guide :deep(ol) {
  padding-left: 1.25rem;
  list-style: decimal;
}

.guide :deep(li + li) {
  margin-top: 0.375rem;
}

.guide :deep(li::marker) {
  color: var(--gg-ink-muted);
}

.guide :deep(strong) {
  font-weight: 650;
}

.guide :deep(a) {
  color: var(--gg-brand-ink);
  font-weight: 600;
  text-decoration: underline;
}

.guide :deep(hr) {
  margin-block: 2rem;
  border-color: var(--gg-rule);
}

.guide :deep(.guide-table) {
  overflow-x: auto;
}

.guide :deep(table) {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9375rem;
}

.guide :deep(th),
.guide :deep(td) {
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid var(--gg-rule);
  text-align: left;
  vertical-align: top;
}

.guide :deep(th) {
  background: var(--gg-surface-sunken);
}
</style>
