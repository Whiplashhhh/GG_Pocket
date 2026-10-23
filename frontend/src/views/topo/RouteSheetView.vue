<script setup lang="ts">
import { Check, Flag, Heart, Maximize2, Minimize2, X } from 'lucide-vue-next'
import {
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogOverlay,
  DialogPortal,
  DialogRoot,
  DialogTitle,
} from 'reka-ui'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { ApiError, errorMessageKey } from '@/api/errors'
import {
  useAddFavorite,
  useMyFavorites,
  useRemoveFavorite,
  useRouteDetail,
  useToggleCheck,
  type ClimbMode,
} from '@/api/topo'
import FavoriteReplaceDialog from '@/components/topo/FavoriteReplaceDialog.vue'
import RouteComments from '@/components/topo/RouteComments.vue'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import HoldSwatch from '@/components/ui/HoldSwatch.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { formatDate } from '@/lib/format'

/**
 * The route sheet (spec §8.4, wireframe « Fiche voie »): slides up over the list it was opened
 * from, which keeps its sort and filters. Deep-linkable. Ascent figures and « mon historique »
 * come with sessions (phase 4, D-080): nothing is shown for them until then.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const id = computed(() => String(route.params.id))
const detail = useRouteDetail(id)
const favorites = useMyFavorites()
const addFavorite = useAddFavorite()
const removeFavorite = useRemoveFavorite()
const toggleCheck = useToggleCheck()

const open = ref(true)
const expanded = ref(false)
const photoOpen = ref(false)
const replacing = ref(false)
const actionError = ref<unknown>(null)
const notice = ref('')

const summary = computed(() => detail.data.value?.route)
const dismounted = computed(() => summary.value?.status === 'DISMOUNTED')
const notFound = computed(
  () => detail.error.value instanceof ApiError && detail.error.value.code === 'NOT_FOUND',
)

const SETTERS = new Intl.ListFormat('fr', { type: 'conjunction' })

const holds = computed(() => {
  const [first, second] = summary.value?.holds ?? []
  if (!first) return ''
  const colors = second
    ? t('routeLabel.colorsJoin', { first: first.label, second: second.label })
    : first.label
  return t('routeSheet.holds', { colors })
})

const opened = computed(() => {
  const value = detail.data.value
  if (!value) return ''
  const names = [
    ...value.setters.map((setter) => setter.nickname),
    ...(value.setterNote ? [value.setterNote] : []),
  ]
  const date = formatDate(value.route.openedOn)
  return names.length > 0
    ? t('routeSheet.openedBy', { date, setters: SETTERS.format(names) })
    : t('routeSheet.openedOn', { date })
})

/** My other favourites on the wall: the ones I could give up for this route. */
const otherFavorites = computed(() =>
  (favorites.data.value ?? []).filter(
    (favorite) => favorite.favorite && favorite.status !== 'DISMOUNTED' && favorite.id !== id.value,
  ),
)

/** Back where the sheet was opened from; a deep link closes onto the list under it. */
function close() {
  const back: unknown = (window.history.state as { back?: unknown } | null)?.back
  if (typeof back === 'string') {
    router.back()
  } else {
    void router.replace({ path: route.matched[0]?.path ?? '/topo', query: route.query })
  }
}

watch(open, (isOpen) => {
  if (!isOpen) close()
})

function failed(error: unknown) {
  actionError.value = error
}

function toggleHeart() {
  const current = summary.value
  if (current === undefined) return
  actionError.value = null
  notice.value = ''
  if (current.favorite) {
    removeFavorite.mutate(current.id, { onError: failed })
    return
  }
  if (otherFavorites.value.length >= 2) {
    replacing.value = true
    return
  }
  addFavorite.mutate(
    { id: current.id },
    {
      onError: (error) => {
        // Another device used the last place in the meantime.
        if (error instanceof ApiError && error.code === 'FAVORITES_FULL') {
          void favorites.refetch().then(() => {
            replacing.value = true
          })
        } else {
          failed(error)
        }
      },
    },
  )
}

function release(replaceRouteId: string) {
  const current = summary.value
  replacing.value = false
  if (current === undefined) return
  addFavorite.mutate(
    { id: current.id, replaceRouteId },
    {
      onSuccess: () => {
        notice.value = t('routeSheet.heartReplaced', { name: current.name })
      },
      onError: failed,
    },
  )
}

function check(mode: ClimbMode) {
  const current = summary.value
  if (current === undefined) return
  actionError.value = null
  toggleCheck.mutate(
    { id: current.id, mode, checked: !current.checks.includes(mode) },
    { onError: failed },
  )
}

const CHECKS = [
  { mode: 'TOP_ROPE', label: 'routeSheet.checkTopRope' },
  { mode: 'LEAD', label: 'routeSheet.checkLead' },
] as const

const toggleClasses = (pressed: boolean, tone: 'brand' | 'success') => [
  'inline-flex min-h-12 items-center gap-2 rounded-control border-2 px-4 text-left font-semibold transition-colors duration-150',
  pressed
    ? tone === 'brand'
      ? 'border-brand bg-brand text-on-brand'
      : 'border-success bg-success text-bg'
    : 'border-ink text-ink hover:bg-surface-sunken',
]
</script>

<template>
  <DialogRoot v-model:open="open">
    <DialogPortal>
      <DialogOverlay class="fixed inset-0 z-40 bg-[rgb(23_20_15/0.45)]" />
      <DialogContent
        class="fixed inset-x-0 bottom-0 z-50 flex flex-col bg-surface shadow-sheet outline-none data-[state=open]:animate-sheet-up sm:inset-x-auto sm:left-1/2 sm:w-[36rem] sm:-translate-x-1/2"
        :class="expanded ? 'top-0' : 'max-h-[92dvh] rounded-t-sheet'"
      >
        <div class="flex items-center justify-between px-2 pt-2">
          <GgIconButton
            :label="t(expanded ? 'routeSheet.shrink' : 'routeSheet.expand')"
            @click="expanded = !expanded"
          >
            <Minimize2 v-if="expanded" :size="20" aria-hidden="true" />
            <Maximize2 v-else :size="20" aria-hidden="true" />
          </GgIconButton>
          <span class="h-1.5 w-12 rounded-full bg-rule" aria-hidden="true" />
          <DialogClose
            class="inline-flex size-11 items-center justify-center rounded-control text-ink hover:bg-surface-sunken"
            :aria-label="t('routeSheet.close')"
          >
            <X :size="24" aria-hidden="true" />
          </DialogClose>
        </div>

        <div class="flex-1 overflow-y-auto pb-[max(1.5rem,env(safe-area-inset-bottom))]">
          <template v-if="detail.isPending.value">
            <DialogTitle class="sr-only">{{ t('routeSheet.title') }}</DialogTitle>
            <DialogDescription class="sr-only" role="status">
              {{ t('common.loading') }}
            </DialogDescription>
            <div class="flex flex-col gap-4 pt-2" aria-hidden="true">
              <SkeletonBlock class="aspect-[4/3] w-full rounded-none" />
              <SkeletonBlock class="mx-4 h-16" />
              <SkeletonBlock class="mx-4 h-4 w-1/2" />
            </div>
          </template>

          <div v-else-if="!summary" class="flex flex-col items-start gap-3 px-4 py-6">
            <DialogTitle class="text-xl font-title text-balance">
              {{ notFound ? t('routeSheet.notFoundTitle') : t('routeSheet.loadError') }}
            </DialogTitle>
            <DialogDescription v-if="notFound" class="text-ink-muted">
              {{ t('routeSheet.notFoundBody') }}
            </DialogDescription>
            <FormAlert v-else>{{ t(errorMessageKey(detail.error.value)) }}</FormAlert>
            <GgButton v-if="!notFound" variant="secondary" @click="detail.refetch()">
              {{ t('common.retry') }}
            </GgButton>
          </div>

          <template v-else>
            <button
              v-if="summary.photoUrl"
              type="button"
              class="mt-1 block aspect-[4/3] w-full overflow-hidden bg-surface-sunken"
              :aria-label="t('routeSheet.openPhoto', { name: summary.name })"
              @click="photoOpen = true"
            >
              <img
                :src="summary.photoUrl"
                alt=""
                class="size-full object-cover"
                :class="{ grayscale: dismounted }"
              />
            </button>
            <div
              v-else
              class="mt-1 flex h-28 items-center justify-center gap-3 border-y border-rule bg-inserts"
              :class="{ 'opacity-60 grayscale': dismounted }"
              aria-hidden="true"
            >
              <HoldSwatch
                v-for="(hold, index) in summary.holds"
                :key="hold.id"
                :color="hold"
                :size="52"
                :class="index === 0 ? '-rotate-12' : 'rotate-12'"
              />
            </div>

            <div class="px-4 pt-7">
              <DialogTitle class="flex">
                <RouteSummaryLabel :route="summary" size="lg" />
              </DialogTitle>
              <DialogDescription class="mt-5 text-lg font-semibold">
                {{ summary.sectorName }} ·
                {{ t('routeSheet.cord', { number: summary.cordNumber }) }}
              </DialogDescription>
              <p class="mt-1 text-ink-muted">{{ holds }}</p>
              <p class="text-ink-muted">{{ opened }}</p>

              <ul
                v-if="summary.styles.length > 0"
                class="mt-3 flex flex-wrap gap-1.5"
                :aria-label="t('routeSheet.styles')"
              >
                <li
                  v-for="style in summary.styles"
                  :key="style"
                  class="rounded-control bg-surface-sunken px-2 py-0.5 text-sm font-semibold"
                >
                  {{ t(`routeStyles.${style}`) }}
                </li>
              </ul>

              <p
                v-if="summary.status === 'LAST_CHANCE'"
                class="mt-4 flex gap-2 border-l-4 border-brand py-1 pl-3 font-semibold text-brand-ink"
              >
                <Flag :size="20" class="mt-0.5 shrink-0" aria-hidden="true" />
                {{
                  summary.plannedDismountOn
                    ? t('routeSheet.lastChancePlanned', {
                        date: formatDate(summary.plannedDismountOn),
                      })
                    : t('routeSheet.lastChanceSoon')
                }}
              </p>
              <p
                v-if="dismounted"
                class="mt-4 border-l-4 border-ink-muted bg-surface-sunken px-3 py-2 font-semibold"
              >
                {{
                  t('routeSheet.dismounted', {
                    date: formatDate(summary.dismountedAt ?? summary.openedOn),
                  })
                }}
              </p>

              <section
                v-if="detail.data.value?.advice"
                class="mt-5 border-l-4 border-brand py-1 pl-3"
                aria-labelledby="conseil-ouvreur"
              >
                <h3 id="conseil-ouvreur" class="font-title">{{ t('routeSheet.advice') }}</h3>
                <p class="mt-1 whitespace-pre-line">
                  {{ t('routeSheet.adviceQuote', { advice: detail.data.value.advice }) }}
                </p>
              </section>

              <p class="mt-5 flex items-center gap-1.5 text-ink-muted">
                <Heart :size="16" aria-hidden="true" />
                {{ t('routeSheet.favorites', summary.favoriteCount) }}
              </p>

              <div v-if="!dismounted" class="mt-3 flex flex-col items-stretch gap-2 sm:items-start">
                <button
                  type="button"
                  :aria-pressed="summary.favorite"
                  :disabled="addFavorite.isPending.value || removeFavorite.isPending.value"
                  :class="toggleClasses(summary.favorite, 'brand')"
                  @click="toggleHeart"
                >
                  <Heart
                    :size="20"
                    :fill="summary.favorite ? 'currentColor' : 'none'"
                    aria-hidden="true"
                  />
                  {{ t('routeSheet.heart') }}
                </button>
                <div
                  role="group"
                  :aria-label="t('routeSheet.checks')"
                  class="flex flex-col items-stretch gap-2 sm:items-start"
                >
                  <button
                    v-for="item in CHECKS"
                    :key="item.mode"
                    type="button"
                    :aria-pressed="summary.checks.includes(item.mode)"
                    :class="toggleClasses(summary.checks.includes(item.mode), 'success')"
                    @click="check(item.mode)"
                  >
                    <Check
                      :size="20"
                      :stroke-width="3"
                      :class="{ 'opacity-0': !summary.checks.includes(item.mode) }"
                      aria-hidden="true"
                    />
                    {{ t(item.label) }}
                  </button>
                </div>
              </div>
              <p v-if="notice" role="status" class="mt-3 font-semibold text-success">
                {{ notice }}
              </p>
              <div v-if="actionError" class="mt-3">
                <FormAlert>{{ t(errorMessageKey(actionError)) }}</FormAlert>
              </div>
            </div>

            <RouteComments
              class="mt-8 border-t border-rule pt-6"
              :route-id="summary.id"
              :read-only="dismounted"
              :count="summary.commentCount"
            />

            <FavoriteReplaceDialog
              v-model:open="replacing"
              :route-name="summary.name"
              :favorites="otherFavorites"
              @release="release"
            />

            <DialogRoot v-if="summary.photoUrl" v-model:open="photoOpen">
              <DialogPortal>
                <DialogOverlay class="fixed inset-0 z-60 bg-[rgb(10_9_8/0.92)]" />
                <DialogContent
                  class="fixed inset-0 z-60 flex items-center justify-center p-2 outline-none"
                  :aria-describedby="undefined"
                >
                  <DialogTitle class="sr-only">
                    {{ t('routeSheet.photoTitle', { name: summary.name }) }}
                  </DialogTitle>
                  <img
                    :src="summary.photoUrl"
                    :alt="t('routeSheet.photoTitle', { name: summary.name })"
                    class="max-h-full max-w-full object-contain"
                  />
                  <DialogClose
                    class="absolute top-[max(0.5rem,env(safe-area-inset-top))] right-2 inline-flex size-11 items-center justify-center rounded-control bg-[rgb(10_9_8/0.6)] text-[#f3efe6]"
                    :aria-label="t('routeSheet.closePhoto')"
                  >
                    <X :size="24" aria-hidden="true" />
                  </DialogClose>
                </DialogContent>
              </DialogPortal>
            </DialogRoot>
          </template>
        </div>
      </DialogContent>
    </DialogPortal>
  </DialogRoot>
</template>
