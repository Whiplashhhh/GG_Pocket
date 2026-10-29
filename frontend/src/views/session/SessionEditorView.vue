<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { computed, onMounted, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import {
  SessionPhotoNotSaved,
  useClimbedRoutes,
  useSaveSession,
  useSession,
  type SessionVisibility,
} from '@/api/sessions'
import { useDirectory, type MemberCard } from '@/api/social'
import { useRoutes, type RouteSummary } from '@/api/topo'
import type { PhotoChange } from '@/api/topoAdmin'
import AscentLineEditor from '@/components/session/AscentLineEditor.vue'
import BelayerPicker from '@/components/session/BelayerPicker.vue'
import FeelingSlider from '@/components/session/FeelingSlider.vue'
import SessionPhotoField from '@/components/session/SessionPhotoField.vue'
import SessionRoutePicker from '@/components/session/SessionRoutePicker.vue'
import SessionSavedMoment from '@/components/session/SessionSavedMoment.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import GgIconButton from '@/components/ui/GgIconButton.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import { draftKey, useSessionDraft } from '@/composables/useSessionDraft'
import { clubToday } from '@/lib/age'
import { canWait } from '@/lib/outbox'
import { canLogOn } from '@/lib/planning'
import {
  addAscent,
  dateBounds,
  duplicateAscent,
  emptySessionForm,
  formProblems,
  MAX_ASCENTS,
  MAX_COMMENT_LENGTH,
  moveAscent,
  removeAscent,
  removeLastOf,
  sessionToForm,
  toSessionRequest,
  type AscentLine,
  type SessionForm,
} from '@/lib/sessionForm'
import { useOutboxStore } from '@/stores/outbox'

/**
 * The session sheet (spec §8.3): a new session, or one of mine to change. Everything typed is
 * kept on the phone at once (draft), and a session that cannot leave waits for the network.
 */
const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const outbox = useOutboxStore()

const sessionId = computed(() =>
  typeof route.params.id === 'string' ? route.params.id : undefined,
)
const editing = computed(() => sessionId.value !== undefined)
const existing = useSession(sessionId)
const routes = useRoutes()
const climbed = useClimbedRoutes()
const save = useSaveSession()

const form = ref<SessionForm>(emptySessionForm())
const photo = ref<Blob | undefined>(undefined)
const removePhoto = ref(false)
const preparingPhoto = ref(false)
const draft = useSessionDraft(draftKey(sessionId.value), form, photo, removePhoto)
const loaded = ref(false)

const today = clubToday()
const bounds = computed(() => {
  const { min, max } = dateBounds(today)
  // A saved session may stay on its day, even older than a week (while its month is open).
  const saved = existing.data.value?.climbedOn
  return { min: saved !== undefined && saved < min ? saved : min, max }
})

let loading = false
async function load() {
  if (loading) return
  loading = true
  const restored = await draft.restore()
  if (!restored && editing.value && existing.data.value !== undefined) {
    form.value = sessionToForm(existing.data.value)
  }
  // From the planning: "Noter une séance ce jour-là" (?jour=YYYY-MM-DD, within the open week).
  const day = route.query.jour
  if (!editing.value && typeof day === 'string' && canLogOn(day, today)) {
    form.value = { ...form.value, climbedOn: day }
  }
  loaded.value = true
}

onMounted(() => {
  if (!editing.value) void load()
})
watch(
  () => existing.data.value,
  (session) => {
    if (session !== undefined && !loaded.value) void load()
  },
  { immediate: true },
)

async function startOver() {
  form.value =
    editing.value && existing.data.value !== undefined
      ? sessionToForm(existing.data.value)
      : emptySessionForm(today)
  photo.value = undefined
  removePhoto.value = false
  await draft.clear(true)
}

/** Routes on the wall, plus those of the session being changed (maybe dismounted since). */
const routeById = computed(() => {
  const map = new Map<string, RouteSummary>()
  for (const ascent of existing.data.value?.ascents ?? []) map.set(ascent.route.id, ascent.route)
  for (const item of routes.data.value ?? []) map.set(item.id, item)
  return map
})
/** The club, plus the belayers of the session being changed. */
const directory = useDirectory()
const belayerById = computed(() => {
  const map = new Map<string, { nickname: string; avatarUrl?: string }>()
  for (const ascent of existing.data.value?.ascents ?? []) {
    if (ascent.belayer) map.set(ascent.belayer.id, ascent.belayer)
  }
  for (const entry of directory.data.value?.members ?? []) map.set(entry.member.id, entry.member)
  return map
})
const pickingBelayer = ref(false)
const belayerLine = ref(0)

function chooseBelayer(index: number) {
  belayerLine.value = index
  pickingBelayer.value = true
}

function pickBelayer(member: MemberCard | null) {
  const line = form.value.ascents[belayerLine.value]
  if (line === undefined) return
  update(belayerLine.value, { ...line, belayerId: member?.id ?? null })
}

const recentIds = computed(() => (climbed.data.value ?? []).map((item) => item.routeId))
const climbedBefore = computed(() => new Set(recentIds.value))
const full = computed(() => form.value.ascents.length >= MAX_ASCENTS)

const announce = ref('')
const addedCounts = computed(() => {
  const counts = new Map<string, number>()
  for (const line of form.value.ascents)
    counts.set(line.routeId, (counts.get(line.routeId) ?? 0) + 1)
  return counts
})
function add(routeId: string) {
  form.value.ascents = addAscent(form.value.ascents, routeId, climbedBefore.value)
  const added = routeById.value.get(routeId)
  announce.value = t('session.added', {
    name: added?.name ?? '',
    n: form.value.ascents.length,
  })
}
function takeBack(routeId: string) {
  form.value.ascents = removeLastOf(form.value.ascents, routeId)
  announce.value = t('session.takenBack', {
    name: routeById.value.get(routeId)?.name ?? '',
    n: form.value.ascents.length,
  })
}
function update(index: number, line: AscentLine) {
  form.value.ascents = form.value.ascents.map((item, i) => (i === index ? line : item))
}
function duplicate(index: number) {
  form.value.ascents = duplicateAscent(form.value.ascents, index)
}
function move(index: number, delta: -1 | 1) {
  form.value.ascents = moveAscent(form.value.ascents, index, delta)
}
function remove(index: number) {
  form.value.ascents = removeAscent(form.value.ascents, index)
}

const visibilities = computed<{ value: SessionVisibility; label: string }[]>(() => [
  { value: 'FRIENDS', label: t('session.visibilityFriends') },
  { value: 'PRIVATE', label: t('session.visibilityPrivate') },
])
const commentId = useId()

const problem = ref<string | null>(null)
const moment = ref<'saved' | 'queued' | null>(null)
const MOMENT_MS = 1400

function closeTarget() {
  return editing.value ? `/seances/${sessionId.value ?? ''}` : '/actu'
}

function finish(kind: 'saved' | 'queued', target: string) {
  moment.value = kind
  window.setTimeout(() => void router.replace(target), MOMENT_MS)
}

async function queue() {
  await outbox.enqueue(toSessionRequest(form.value), photo.value)
  await draft.clear()
  finish('queued', '/actu')
}

async function submit() {
  problem.value = null
  const [first] = formProblems(form.value, bounds.value)
  if (first !== undefined) {
    problem.value = t(`session.problems.${first}`)
    return
  }
  const body = toSessionRequest(form.value)
  const change: PhotoChange =
    photo.value !== undefined
      ? { kind: 'replace', photo: photo.value }
      : removePhoto.value
        ? { kind: 'remove' }
        : { kind: 'keep' }
  if (!editing.value && !navigator.onLine) {
    await queue()
    return
  }
  try {
    const saved = await save.mutateAsync({ id: sessionId.value, body, photo: change })
    await draft.clear()
    finish('saved', editing.value ? `/seances/${saved.id}` : '/actu')
  } catch (error) {
    if (error instanceof SessionPhotoNotSaved) {
      await draft.clear()
      void router.replace({ path: `/seances/${error.saved.id}`, query: { photo: 'refusee' } })
    } else if (!editing.value && canWait(error)) {
      await queue()
    } else {
      problem.value = t(errorMessageKey(error))
    }
  }
}

const sending = computed(() => save.isPending.value || moment.value !== null)
const title = computed(() => (editing.value ? t('session.editTitle') : t('session.newTitle')))
</script>

<template>
  <SessionSavedMoment v-if="moment" :queued="moment === 'queued'" />
  <form class="flex min-h-dvh flex-col" novalidate @submit.prevent="submit">
    <header class="sticky top-0 z-20 flex items-center gap-2 border-b border-rule bg-bg px-2 py-2">
      <GgIconButton :to="closeTarget()" :label="t('session.close')">
        <X :size="26" aria-hidden="true" />
      </GgIconButton>
      <h1 class="min-w-0 flex-1 truncate text-xl font-title">{{ title }}</h1>
    </header>

    <div v-if="!loaded" class="flex flex-col gap-3 px-4 py-6" aria-hidden="true">
      <SkeletonBlock class="h-11 w-full" />
      <SkeletonBlock class="h-24 w-full" />
    </div>
    <FormAlert v-else-if="editing && existing.isError.value" class="m-4">
      {{ t(errorMessageKey(existing.error.value)) }}
    </FormAlert>

    <template v-else>
      <div
        v-if="draft.restored.value"
        class="flex flex-wrap items-center gap-2 bg-surface px-4 py-3"
      >
        <p role="status" class="flex-1 text-sm">{{ t('session.draftRestored') }}</p>
        <GgButton variant="ghost" @click="startOver">{{ t('session.startOver') }}</GgButton>
      </div>

      <div class="px-4 pt-4">
        <TextField
          v-model="form.climbedOn"
          type="date"
          :label="t('session.date')"
          :min="bounds.min"
          :max="bounds.max"
          :hint="t('session.dateHint')"
        />
      </div>

      <div v-if="routes.isPending.value" class="flex flex-col gap-2 px-4 py-4" aria-hidden="true">
        <SkeletonBlock v-for="index in 3" :key="index" class="h-12 w-full" />
      </div>
      <div v-else-if="routes.isError.value" class="flex flex-col items-start gap-3 px-4 py-4">
        <FormAlert
          >{{ t('routePicker.loadError') }} {{ t(errorMessageKey(routes.error.value)) }}</FormAlert
        >
        <GgButton variant="secondary" @click="routes.refetch()">{{ t('common.retry') }}</GgButton>
      </div>
      <SessionRoutePicker
        v-else
        :routes="routes.data.value ?? []"
        :recent-ids="recentIds"
        :added-counts="addedCounts"
        :disabled="full"
        @remove="takeBack"
        @add="add"
      />
      <p class="sr-only" role="status" aria-live="polite">{{ announce }}</p>

      <section class="border-t-2 border-ink" :aria-label="t('session.linesTitle')">
        <h2 class="px-4 pt-4 text-lg font-title">
          {{ t('session.linesCount', form.ascents.length) }}
        </h2>
        <p v-if="form.ascents.length === 0" class="px-4 py-3 text-ink-muted">
          {{ t('session.noLine') }}
        </p>
        <ol v-else>
          <AscentLineEditor
            v-for="(line, index) in form.ascents"
            :key="line.key"
            :line="line"
            :route="routeById.get(line.routeId)"
            :position="index + 1"
            :count="form.ascents.length"
            :can-duplicate="!full"
            :belayer="line.belayerId === null ? undefined : belayerById.get(line.belayerId)"
            @update="update(index, $event)"
            @choose-belayer="chooseBelayer(index)"
            @duplicate="duplicate(index)"
            @move="move(index, $event)"
            @remove="remove(index)"
          />
        </ol>
        <p v-if="full" class="px-4 py-3 text-sm text-ink-muted">{{ t('session.full') }}</p>
        <BelayerPicker
          v-model:open="pickingBelayer"
          :selected-id="form.ascents[belayerLine]?.belayerId ?? null"
          @pick="pickBelayer"
        />
      </section>

      <section
        class="flex flex-col gap-5 border-t-2 border-ink px-4 py-5"
        :aria-label="t('session.endTitle')"
      >
        <h2 class="text-lg font-title">{{ t('session.endTitle') }}</h2>
        <FeelingSlider v-model="form.feeling" :label="t('session.globalFeeling')" />
        <div class="flex flex-col gap-1.5">
          <label :for="commentId" class="font-semibold">
            {{ t('session.comment') }}
            <span class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
          </label>
          <textarea
            :id="commentId"
            v-model="form.comment"
            rows="3"
            :maxlength="MAX_COMMENT_LENGTH"
            :placeholder="t('session.commentPlaceholder')"
            :aria-describedby="`${commentId}-count`"
            class="w-full rounded-control border-2 border-rule bg-surface px-3 py-2 text-base text-ink focus:border-ink focus:outline-none"
          />
          <p :id="`${commentId}-count`" class="text-right text-sm text-ink-muted tabular-nums">
            {{
              t('routeForm.adviceCount', { count: form.comment.length, max: MAX_COMMENT_LENGTH })
            }}
          </p>
        </div>
        <SessionPhotoField
          v-model:photo="photo"
          v-model:remove="removePhoto"
          v-model:preparing="preparingPhoto"
          :saved-url="existing.data.value?.photoUrl"
        />
        <div class="flex flex-col gap-1">
          <span class="font-semibold" aria-hidden="true">{{ t('session.visibility') }}</span>
          <SegmentedControl
            v-model="form.visibility"
            :label="t('session.visibility')"
            :options="visibilities"
            size="lg"
            stretch
          />
          <p class="text-sm text-ink-muted">
            {{
              form.visibility === 'FRIENDS'
                ? t('session.visibilityFriendsHint')
                : t('session.visibilityPrivateHint')
            }}
          </p>
        </div>
      </section>

      <div
        class="sticky bottom-0 z-20 mt-auto flex flex-col gap-2 border-t border-rule bg-bg px-4 pt-3 pb-3"
      >
        <FormAlert v-if="problem">{{ problem }}</FormAlert>
        <GgButton type="submit" size="lg" block :disabled="sending || preparingPhoto">
          {{ save.isPending.value ? t('session.saving') : t('session.save') }}
        </GgButton>
      </div>
    </template>
  </form>
</template>
