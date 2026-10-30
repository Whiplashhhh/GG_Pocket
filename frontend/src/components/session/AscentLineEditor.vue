<script setup lang="ts">
import { ArrowDown, ArrowUp, ChevronRight, Copy, EllipsisVertical, Trash2 } from 'lucide-vue-next'
import {
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuPortal,
  DropdownMenuRoot,
  DropdownMenuTrigger,
} from 'reka-ui'
import { computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'

import type { ClimbMode, RouteSummary } from '@/api/topo'
import RouteSummaryLabel from '@/components/topo/RouteSummaryLabel.vue'
import CheckboxField from '@/components/ui/CheckboxField.vue'
import SegmentedControl from '@/components/ui/SegmentedControl.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { setClean, setFalls, setTopped, type AscentLine } from '@/lib/sessionForm'

import FallsStepper from './FallsStepper.vue'
import FeelingSlider from './FeelingSlider.vue'

/**
 * One ascent of the session sheet (docs/DESIGN.md, « Nouvelle séance »): mode, topped, clean,
 * falls, first try, feeling, belayer. Big targets: hands are full of chalk.
 */
const props = defineProps<{
  line: AscentLine
  /** Undefined while the routes load, or for a route no longer listed. */
  route: RouteSummary | undefined
  position: number
  count: number
  canDuplicate: boolean
  /** Who line.belayerId is; undefined when nobody is named (or no longer in the club). */
  belayer: { nickname: string; avatarUrl?: string } | undefined
}>()
const emit = defineEmits<{
  update: [line: AscentLine]
  chooseBelayer: []
  duplicate: []
  move: [delta: -1 | 1]
  remove: []
}>()

const { t } = useI18n()
const labelId = useId()

type YesNo = 'yes' | 'no'
const yesNo = computed<{ value: YesNo; label: string }[]>(() => [
  { value: 'yes', label: t('session.yes') },
  { value: 'no', label: t('session.no') },
])
const modes = computed<{ value: ClimbMode; label: string }[]>(() => [
  { value: 'TOP_ROPE', label: t('session.topRope') },
  { value: 'LEAD', label: t('session.lead') },
])

const mode = computed({
  get: () => props.line.mode,
  set: (value: ClimbMode) => {
    emit('update', { ...props.line, mode: value })
  },
})
const topped = computed({
  get: (): YesNo => (props.line.topped ? 'yes' : 'no'),
  set: (value: YesNo) => {
    emit('update', setTopped(props.line, value === 'yes'))
  },
})
const clean = computed({
  get: (): YesNo => (props.line.clean ? 'yes' : 'no'),
  set: (value: YesNo) => {
    emit('update', setClean(props.line, value === 'yes'))
  },
})
const firstTry = computed({
  get: () => props.line.firstTry,
  set: (value: boolean) => {
    emit('update', { ...props.line, firstTry: value })
  },
})
const feeling = computed({
  get: () => props.line.feeling,
  set: (value: number | null) => {
    emit('update', { ...props.line, feeling: value })
  },
})

const menuItem =
  'flex min-h-12 cursor-pointer items-center gap-3 px-4 font-semibold outline-none data-disabled:cursor-not-allowed data-disabled:opacity-45 data-highlighted:bg-surface-sunken'
</script>

<template>
  <li class="flex flex-col gap-3 border-b border-rule px-4 py-4" :aria-labelledby="labelId">
    <div class="flex items-start gap-3">
      <span
        class="mt-2 w-6 shrink-0 text-right font-bold text-ink-muted tabular-nums"
        aria-hidden="true"
        >{{ t('session.position', { n: position }) }}</span
      >
      <div class="flex min-w-0 flex-1">
        <RouteSummaryLabel v-if="route" :route="route" :label-id="labelId" />
        <span v-else :id="labelId" class="mt-2 text-ink-muted">{{
          t('session.unknownRoute')
        }}</span>
      </div>
      <DropdownMenuRoot :modal="false">
        <DropdownMenuTrigger
          class="inline-flex size-12 shrink-0 items-center justify-center rounded-control hover:bg-surface-sunken"
          :aria-label="t('session.lineMenu', { n: position })"
        >
          <EllipsisVertical :size="22" aria-hidden="true" />
        </DropdownMenuTrigger>
        <DropdownMenuPortal>
          <DropdownMenuContent
            align="end"
            :side-offset="4"
            class="z-50 min-w-56 border-2 border-ink bg-surface py-1 shadow-lg"
          >
            <DropdownMenuItem
              :class="menuItem"
              :disabled="!canDuplicate"
              @select="emit('duplicate')"
            >
              <Copy :size="20" aria-hidden="true" />{{ t('session.duplicate') }}
            </DropdownMenuItem>
            <DropdownMenuItem
              :class="menuItem"
              :disabled="position === 1"
              @select="emit('move', -1)"
            >
              <ArrowUp :size="20" aria-hidden="true" />{{ t('session.moveUp') }}
            </DropdownMenuItem>
            <DropdownMenuItem
              :class="menuItem"
              :disabled="position === count"
              @select="emit('move', 1)"
            >
              <ArrowDown :size="20" aria-hidden="true" />{{ t('session.moveDown') }}
            </DropdownMenuItem>
            <DropdownMenuItem :class="[menuItem, 'text-danger']" @select="emit('remove')">
              <Trash2 :size="20" aria-hidden="true" />{{ t('session.remove') }}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenuPortal>
      </DropdownMenuRoot>
    </div>

    <div class="grid grid-cols-2 gap-x-3 gap-y-3 sm:pl-9">
      <div class="flex flex-col gap-1">
        <span class="font-semibold" aria-hidden="true">{{ t('session.mode') }}</span>
        <SegmentedControl
          v-model="mode"
          :label="t('session.mode')"
          :options="modes"
          size="lg"
          stretch
        />
      </div>
      <div class="flex flex-col gap-1">
        <span class="font-semibold" aria-hidden="true">{{ t('session.topped') }}</span>
        <SegmentedControl
          v-model="topped"
          :label="t('session.topped')"
          :options="yesNo"
          size="lg"
          stretch
        />
      </div>
      <div class="flex flex-col gap-1">
        <span class="font-semibold" aria-hidden="true">{{ t('session.clean') }}</span>
        <SegmentedControl
          v-model="clean"
          :label="t('session.clean')"
          :options="yesNo"
          size="lg"
          stretch
          :disabled="!line.topped"
        />
      </div>
      <FallsStepper
        :falls="line.falls"
        :disabled="line.clean"
        @change="emit('update', setFalls(line, $event))"
      />
      <div class="col-span-2 flex flex-col gap-1 sm:col-span-1">
        <CheckboxField v-model="firstTry">{{ t('session.firstTry') }}</CheckboxField>
      </div>
      <div class="col-span-2 sm:col-span-1">
        <FeelingSlider v-model="feeling" :label="t('session.feeling')" />
      </div>
      <div class="col-span-2 flex flex-col gap-1">
        <span class="font-semibold" aria-hidden="true">{{ t('session.belayer') }}</span>
        <button
          type="button"
          class="flex min-h-12 items-center gap-3 rounded-control border-2 border-rule bg-surface px-3 py-1.5 text-left hover:border-ink"
          :aria-label="
            t('session.belayerButton', {
              n: position,
              name: belayer?.nickname ?? t('session.belayerNobody'),
            })
          "
          @click="emit('chooseBelayer')"
        >
          <UserAvatar
            v-if="belayer"
            :first-name="belayer.nickname"
            last-name=""
            :seed="belayer.nickname"
            :src="belayer.avatarUrl"
            :size="32"
          />
          <span class="flex-1 truncate" :class="belayer ? 'font-semibold' : 'text-ink-muted'">{{
            belayer?.nickname ?? t('session.belayerNobody')
          }}</span>
          <ChevronRight :size="20" aria-hidden="true" />
        </button>
      </div>
    </div>
  </li>
</template>
