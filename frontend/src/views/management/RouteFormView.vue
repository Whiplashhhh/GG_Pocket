<script setup lang="ts">
import { useEventListener } from '@vueuse/core'
import { Camera } from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, useId, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'

import { errorMessageKey } from '@/api/errors'
import { useRouteDetail, useTopoConfig, type RouteDetail } from '@/api/topo'
import { RoutePhotoNotSaved, useSaveRoute, useSetters, type PhotoChange } from '@/api/topoAdmin'
import HoldColorChoice from '@/components/topo/HoldColorChoice.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import FormAlert from '@/components/ui/FormAlert.vue'
import GgButton from '@/components/ui/GgButton.vue'
import RouteLabel from '@/components/ui/RouteLabel.vue'
import SelectField from '@/components/ui/SelectField.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import TextField from '@/components/ui/TextField.vue'
import ToggleChip from '@/components/ui/ToggleChip.vue'
import { clubToday } from '@/lib/age'
import { focusFirstInvalid, serverFieldErrors, type FieldErrors, type Message } from '@/lib/forms'
import { PhotoError, prepareRoutePhoto } from '@/lib/photo'
import { ROUTE_STYLES } from '@/lib/route'
import {
  ADVICE_MAX,
  colorChoices,
  cordChoices,
  duplicateRouteForm,
  emptyRouteForm,
  gradeChoices,
  labelPreview,
  MAX_STYLES,
  ROUTE_FORM_ALIASES,
  ROUTE_FORM_FIELDS,
  ROUTE_NAME_MAX,
  routeSettings,
  sameRouteForm,
  SETTER_NOTE_MAX,
  toggleId,
  toggleStyle,
  toRouteForm,
  withMainColor,
  withSecondColor,
  type RouteForm,
  type RouteFormField,
} from '@/lib/routeForm'

import ManagementTitle from './ManagementTitle.vue'

/**
 * Spec §8.10: create a route, edit it, or duplicate one (`?depuis=<id>`: reopening on the same
 * cord). A live label shows what climbers will see. Leaving with unsaved changes asks first.
 */
const LIST = '/gestion/voies'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const editId = computed(() =>
  typeof route.params.id === 'string' && route.params.id !== '' ? route.params.id : undefined,
)
const modelId = computed(() =>
  editId.value === undefined && typeof route.query.depuis === 'string'
    ? route.query.depuis
    : undefined,
)
const sourceId = computed(() => editId.value ?? modelId.value)

const config = useTopoConfig()
const source = useRouteDetail(sourceId)
const setters = useSetters()
const save = useSaveRoute()

const form = ref<RouteForm>(emptyRouteForm(clubToday()))
const initial = ref<RouteForm>(emptyRouteForm(clubToday()))
const errors = ref<FieldErrors<RouteFormField>>({})
const general = ref<Message | null>(null)
/** Which form is loaded: the same view serves creation, duplication and edition. */
const loadedKey = ref<string | null>(null)
const formKey = computed(() => `${editId.value ?? ''}|${modelId.value ?? ''}`)
const sourceName = ref('')
const dismounted = ref(false)

type PhotoState =
  { kind: 'keep' } | { kind: 'replace'; photo: Blob; url: string } | { kind: 'remove' }
const photo = ref<PhotoState>({ kind: 'keep' })
const savedPhotoUrl = ref<string | undefined>(undefined)
const preparingPhoto = ref(false)
const photoError = ref<string | null>(null)
const photoInputId = useId()
const adviceId = useId()
const groupId = useId()

function setPhoto(next: PhotoState) {
  if (photo.value.kind === 'replace') URL.revokeObjectURL(photo.value.url)
  photo.value = next
}
onBeforeUnmount(() => {
  setPhoto({ kind: 'keep' })
})

function load(next: RouteForm, detail?: RouteDetail) {
  form.value = next
  initial.value = structuredClone(next)
  errors.value = {}
  sourceName.value = detail?.route.name ?? ''
  // A duplicate does not take the photo: the new holds are not the old ones.
  savedPhotoUrl.value = editId.value === undefined ? undefined : detail?.route.photoUrl
  dismounted.value = editId.value !== undefined && detail?.route.status === 'DISMOUNTED'
  setPhoto({ kind: 'keep' })
  photoError.value = null
}

watch(
  [formKey, () => source.data.value],
  ([key, detail]) => {
    if (loadedKey.value === key) return
    if (sourceId.value === undefined) {
      load(emptyRouteForm(clubToday()))
    } else if (detail?.route.id === sourceId.value) {
      load(
        editId.value === undefined ? duplicateRouteForm(detail, clubToday()) : toRouteForm(detail),
        detail,
      )
    } else {
      return
    }
    general.value = null
    loadedKey.value = key
  },
  { immediate: true },
)

const loaded = computed(() => loadedKey.value === formKey.value)
const title = computed(() => {
  if (!loaded.value) return t('routesAdmin.title')
  if (editId.value !== undefined) return t('routeForm.editTitle', { name: sourceName.value })
  if (modelId.value !== undefined) return t('routeForm.duplicateTitle', { name: sourceName.value })
  return t('routeForm.createTitle')
})

const grades = computed(() => (config.data.value ? gradeChoices(config.data.value) : []))
const cords = computed(() =>
  config.data.value ? cordChoices(config.data.value, initial.value.cordId) : [],
)
const colors = computed(() =>
  config.data.value ? colorChoices(config.data.value, initial.value.holdColorIds) : [],
)
const secondColors = computed(() =>
  colors.value.filter((color) => color.id !== form.value.holdColorIds[0]),
)
const preview = computed(() =>
  config.data.value ? labelPreview(form.value, config.data.value) : null,
)
const photoUrl = computed(() => {
  if (photo.value.kind === 'replace') return photo.value.url
  return photo.value.kind === 'remove' ? undefined : savedPhotoUrl.value
})

const mainColor = computed({
  get: () => form.value.holdColorIds[0] ?? '',
  set: (id: string) => {
    form.value.holdColorIds = withMainColor(form.value.holdColorIds, id)
  },
})
const secondColor = computed({
  get: () => form.value.holdColorIds[1] ?? '',
  set: (id: string) => {
    form.value.holdColorIds = withSecondColor(form.value.holdColorIds, id === '' ? undefined : id)
  },
})

const dirty = computed(
  () => loaded.value && (!sameRouteForm(form.value, initial.value) || photo.value.kind !== 'keep'),
)

async function photoChosen(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (file === undefined) return
  photoError.value = null
  preparingPhoto.value = true
  try {
    const prepared = await prepareRoutePhoto(file)
    setPhoto({ kind: 'replace', photo: prepared, url: URL.createObjectURL(prepared) })
  } catch (failure) {
    photoError.value =
      failure instanceof PhotoError ? t(`photo.${failure.reason}`) : t('errors.UNKNOWN')
  } finally {
    preparingPhoto.value = false
  }
}

function removePhoto() {
  setPhoto(savedPhotoUrl.value === undefined ? { kind: 'keep' } : { kind: 'remove' })
}

/** Set while the form itself navigates (after saving): nothing is lost then. */
let leavingOnPurpose = false

function submit() {
  general.value = null
  const result = routeSettings(form.value, clubToday())
  if ('errors' in result) {
    errors.value = result.errors
    focusFirstInvalid()
    return
  }
  errors.value = {}
  const current = photo.value
  const change: PhotoChange =
    current.kind === 'replace' ? { kind: 'replace', photo: current.photo } : { kind: current.kind }
  const creating = editId.value === undefined
  save.mutate(
    { id: editId.value, settings: result.settings, photo: change },
    {
      onSuccess: (detail) => {
        leavingOnPurpose = true
        void router.push({
          path: LIST,
          state: {
            notice: t(creating ? 'routeForm.created' : 'routeForm.updated', {
              name: detail.route.name,
            }),
          },
        })
      },
      onError: (error) => {
        if (error instanceof RoutePhotoNotSaved) {
          void photoNotSaved(error)
          return
        }
        const split = serverFieldErrors(error, ROUTE_FORM_FIELDS, ROUTE_FORM_ALIASES)
        errors.value = split.fields
        general.value = split.general
        focusFirstInvalid()
      },
    },
  )
}

/** The route is saved but not its photo: carry on editing the saved route, and say so. */
async function photoNotSaved(error: RoutePhotoNotSaved) {
  const id = error.saved.route.id
  if (editId.value !== id) {
    leavingOnPurpose = true
    await router.replace(`/gestion/voies/${id}/modifier`)
    leavingOnPurpose = false
  }
  load(toRouteForm(error.saved), error.saved)
  loadedKey.value = formKey.value
  general.value = {
    key: 'routeForm.photoFailed',
    params: { reason: t(errorMessageKey(error.cause)) },
  }
}

// Unsaved changes: ask before leaving the screen, and before closing the tab.
const leaveOpen = ref(false)
let leaveConfirmed = false
let decideLeave: ((leave: boolean) => void) | null = null

onBeforeRouteLeave(() => {
  if (leavingOnPurpose || !dirty.value) return true
  leaveConfirmed = false
  leaveOpen.value = true
  return new Promise<boolean>((resolve) => {
    decideLeave = resolve
  })
})

function confirmLeave() {
  leaveConfirmed = true
}

// The dialog closes on both buttons; the confirm button says it before the close is handled.
watch(leaveOpen, (open) => {
  if (open) return
  decideLeave?.(leaveConfirmed)
  decideLeave = null
})

useEventListener(window, 'beforeunload', (event: BeforeUnloadEvent) => {
  if (dirty.value && !leavingOnPurpose) event.preventDefault()
})

function errorText(field: RouteFormField): string | undefined {
  const message = errors.value[field]
  return message && t(message.key, message.params ?? {})
}
</script>

<template>
  <ManagementTitle :back="LIST" :back-label="t('routeForm.back')">{{ title }}</ManagementTitle>

  <div
    v-if="sourceId !== undefined && source.isError.value && !loaded"
    class="flex flex-col items-start gap-3 px-4 py-4 lg:px-0"
  >
    <FormAlert>{{ t(errorMessageKey(source.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="source.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <div v-else-if="config.isError.value" class="flex flex-col items-start gap-3 px-4 py-4 lg:px-0">
    <FormAlert>{{ t(errorMessageKey(config.error.value)) }}</FormAlert>
    <GgButton variant="secondary" @click="config.refetch()">{{ t('common.retry') }}</GgButton>
  </div>
  <div
    v-else-if="!loaded || config.isPending.value"
    class="flex flex-col gap-4 px-4 py-4 lg:px-0"
    aria-hidden="true"
  >
    <SkeletonBlock class="h-16 w-full max-w-md" />
    <SkeletonBlock v-for="index in 4" :key="index" class="h-12 w-full max-w-xl" />
  </div>

  <div
    v-else
    class="flex flex-col gap-6 px-4 pt-2 pb-8 lg:grid lg:grid-cols-[minmax(0,36rem)_minmax(0,1fr)] lg:items-start lg:gap-10 lg:px-0 lg:pt-4"
  >
    <!-- The label as it will be taped at the foot of the route. -->
    <section
      class="flex flex-col gap-3 lg:sticky lg:top-24 lg:order-last"
      :aria-label="t('routeForm.preview')"
    >
      <p class="text-sm font-semibold text-ink-muted" aria-hidden="true">
        {{ t('routeForm.preview') }}
      </p>
      <div v-if="preview" class="pt-2">
        <RouteLabel
          :cord="preview.cord"
          :name="form.name.trim() || t('routeForm.previewName')"
          :grade="preview.grade"
          :holds="preview.holds"
          size="lg"
        />
      </div>
      <p v-else class="text-ink-muted">{{ t('routeForm.previewEmpty') }}</p>
      <img
        v-if="photoUrl"
        :src="photoUrl"
        :alt="t('routeForm.photoAlt')"
        class="hidden max-h-96 w-full rounded-control bg-surface-sunken object-contain lg:block"
      />
    </section>

    <form class="flex flex-col gap-6" novalidate @submit.prevent="submit">
      <p v-if="modelId !== undefined" class="text-ink-muted">
        {{ t('routeForm.duplicateIntro', { name: sourceName }) }}
      </p>
      <FormAlert v-if="dismounted" tone="info">{{ t('routeForm.dismountedNotice') }}</FormAlert>

      <TextField
        v-model="form.name"
        :label="t('routeForm.name')"
        :hint="t('routeForm.nameHint')"
        :maxlength="ROUTE_NAME_MAX"
        :error="errorText('name')"
      />

      <div class="grid gap-6 sm:grid-cols-2">
        <SelectField
          v-model="form.gradeId"
          :label="t('routeForm.grade')"
          :error="errorText('gradeId')"
        >
          <option value="" disabled>{{ t('routeForm.gradePlaceholder') }}</option>
          <option v-for="grade in grades" :key="grade.id" :value="grade.id">
            {{ grade.code }}
          </option>
        </SelectField>
        <SelectField
          v-model="form.cordId"
          :label="t('routeForm.cord')"
          :error="errorText('cordId')"
        >
          <option value="" disabled>{{ t('routeForm.cordPlaceholder') }}</option>
          <optgroup v-for="group in cords" :key="group.sectorId" :label="group.sectorName">
            <option v-for="cord in group.cords" :key="cord.id" :value="cord.id">
              {{
                cord.active
                  ? t('routeForm.cordOption', { number: cord.number })
                  : t('routeForm.cordOptionInactive', { number: cord.number })
              }}
            </option>
          </optgroup>
        </SelectField>
      </div>

      <HoldColorChoice
        v-model="mainColor"
        :legend="t('routeForm.mainColor')"
        :colors="colors"
        :error="errorText('holdColorIds')"
      />
      <HoldColorChoice
        v-if="mainColor !== ''"
        v-model="secondColor"
        :legend="t('routeForm.secondColor')"
        :hint="t('routeForm.secondColorHint')"
        :none-label="t('routeForm.noSecondColor')"
        :colors="secondColors"
      />

      <TextField
        v-model="form.openedOn"
        type="date"
        :label="t('routeForm.openedOn')"
        :hint="t('routeForm.openedOnHint')"
        :max="clubToday()"
        :error="errorText('openedOn')"
      />

      <fieldset
        tabindex="-1"
        :data-invalid="errors.setters ? 'true' : undefined"
        :aria-describedby="errors.setters ? `${groupId}-setters-error` : `${groupId}-setters-hint`"
        class="flex flex-col gap-1.5 outline-none"
      >
        <legend class="font-semibold">{{ t('routeForm.setters') }}</legend>
        <p v-if="!errors.setters" :id="`${groupId}-setters-hint`" class="text-sm text-ink-muted">
          {{ t('routeForm.settersHint') }}
        </p>
        <div v-if="setters.isPending.value" class="flex gap-2" aria-hidden="true">
          <SkeletonBlock v-for="index in 3" :key="index" class="h-11 w-24" />
        </div>
        <FormAlert v-else-if="setters.isError.value">
          {{ t(errorMessageKey(setters.error.value)) }}
        </FormAlert>
        <p v-else-if="(setters.data.value ?? []).length === 0" class="text-ink-muted">
          {{ t('routeForm.settersEmpty') }}
        </p>
        <div v-else class="mt-1 flex flex-wrap gap-2">
          <ToggleChip
            v-for="setter in setters.data.value"
            :key="setter.id"
            :pressed="form.setterIds.includes(setter.id)"
            @toggle="form.setterIds = toggleId(form.setterIds, setter.id)"
          >
            {{ setter.nickname }}
          </ToggleChip>
        </div>
        <p
          v-if="errors.setters"
          :id="`${groupId}-setters-error`"
          class="text-sm font-semibold text-danger"
        >
          {{ errorText('setters') }}
        </p>
        <div class="mt-3">
          <TextField
            v-model="form.setterNote"
            :label="t('routeForm.setterNote')"
            :hint="t('routeForm.setterNoteHint')"
            :maxlength="SETTER_NOTE_MAX"
            :error="errorText('setterNote')"
            optional
          />
        </div>
      </fieldset>

      <fieldset
        tabindex="-1"
        :data-invalid="errors.styles ? 'true' : undefined"
        :aria-describedby="`${groupId}-styles-hint`"
        class="flex flex-col gap-1.5 outline-none"
      >
        <legend class="font-semibold">
          {{ t('routeForm.styles') }}
          <span class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
        </legend>
        <p
          :id="`${groupId}-styles-hint`"
          class="text-sm"
          :class="errors.styles ? 'font-semibold text-danger' : 'text-ink-muted'"
        >
          {{
            errors.styles
              ? errorText('styles')
              : t('routeForm.stylesHint', { count: form.styles.length, max: MAX_STYLES })
          }}
        </p>
        <div class="mt-1 flex flex-wrap gap-2">
          <ToggleChip
            v-for="style in ROUTE_STYLES"
            :key="style"
            :pressed="form.styles.includes(style)"
            :disabled="!form.styles.includes(style) && form.styles.length >= MAX_STYLES"
            @toggle="form.styles = toggleStyle(form.styles, style)"
          >
            {{ t(`routeStyles.${style}`) }}
          </ToggleChip>
        </div>
      </fieldset>

      <div class="flex flex-col gap-1.5">
        <label :for="adviceId" class="font-semibold">
          {{ t('routeForm.advice') }}
          <span class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
        </label>
        <textarea
          :id="adviceId"
          v-model="form.advice"
          rows="4"
          :maxlength="ADVICE_MAX"
          :aria-invalid="errors.advice ? true : undefined"
          :aria-describedby="`${adviceId}-hint`"
          class="w-full rounded-control border-2 border-rule bg-surface px-3 py-2 text-base text-ink focus:border-ink focus:outline-none aria-invalid:border-danger"
        />
        <p
          :id="`${adviceId}-hint`"
          class="flex justify-between gap-3 text-sm"
          :class="errors.advice ? 'font-semibold text-danger' : 'text-ink-muted'"
        >
          <span>{{ errors.advice ? errorText('advice') : t('routeForm.adviceHint') }}</span>
          <span class="shrink-0 tabular-nums">{{
            t('routeForm.adviceCount', { count: form.advice.length, max: ADVICE_MAX })
          }}</span>
        </p>
      </div>

      <section class="flex flex-col gap-3" :aria-labelledby="`${photoInputId}-title`">
        <h2 :id="`${photoInputId}-title`" class="font-semibold">
          {{ t('routeForm.photo') }}
          <span class="font-normal text-ink-muted">({{ t('common.optional') }})</span>
        </h2>
        <p class="text-sm text-ink-muted">{{ t('routeForm.photoHint') }}</p>
        <img
          v-if="photoUrl"
          :src="photoUrl"
          :alt="t('routeForm.photoAlt')"
          class="max-h-72 w-full rounded-control bg-surface-sunken object-contain lg:hidden"
        />
        <p v-if="photo.kind === 'remove'" role="status" class="text-ink-muted">
          {{ t('routeForm.photoWillGo') }}
        </p>
        <p v-if="preparingPhoto" role="status" class="text-ink-muted">
          {{ t('routeForm.photoPreparing') }}
        </p>
        <FormAlert v-if="photoError">{{ photoError }}</FormAlert>
        <div class="flex flex-wrap gap-2">
          <!-- A label styled as a button: the native file picker opens, camera included on phones. -->
          <label
            :for="photoInputId"
            class="inline-flex min-h-11 cursor-pointer items-center gap-2 rounded-control border-2 border-ink px-4 font-semibold text-ink hover:bg-surface-sunken has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-focus"
            :class="{ 'pointer-events-none opacity-60': preparingPhoto }"
          >
            <Camera :size="20" aria-hidden="true" />
            {{ photoUrl ? t('routeForm.photoChange') : t('routeForm.photoChoose') }}
            <input
              :id="photoInputId"
              type="file"
              accept="image/*"
              class="sr-only"
              :disabled="preparingPhoto"
              @change="photoChosen"
            />
          </label>
          <GgButton v-if="photoUrl" variant="ghost" @click="removePhoto">
            {{ t('routeForm.photoRemove') }}
          </GgButton>
          <GgButton
            v-if="photo.kind === 'remove'"
            variant="ghost"
            @click="setPhoto({ kind: 'keep' })"
          >
            {{ t('routeForm.photoRestore') }}
          </GgButton>
        </div>
      </section>

      <FormAlert v-if="general">{{ t(general.key, general.params ?? {}) }}</FormAlert>
      <div class="flex flex-col-reverse gap-2 sm:flex-row">
        <GgButton variant="secondary" :to="LIST">{{ t('common.cancel') }}</GgButton>
        <GgButton type="submit" :disabled="save.isPending.value || preparingPhoto || dismounted">
          {{ save.isPending.value ? t('routeForm.saving') : t('routeForm.save') }}
        </GgButton>
      </div>
    </form>
  </div>

  <ConfirmDialog
    v-model:open="leaveOpen"
    :title="t('routeForm.leaveTitle')"
    :body="t('routeForm.leaveBody')"
    :confirm-label="t('routeForm.leaveConfirm')"
    danger
    @confirm="confirmLeave"
  />
</template>
