<script setup lang="ts">
import type { ChangelogItem } from '@/lib/changelog'

/** A bullet list of the CHANGELOG, nested items included; text only, never raw HTML. */
defineProps<{ items: ChangelogItem[] }>()
</script>

<template>
  <ul class="list-disc space-y-2 pl-5 marker:text-ink-muted">
    <li v-for="(item, index) in items" :key="index">
      <template v-for="(inline, part) in item.inlines" :key="part">
        <strong v-if="inline.kind === 'strong'" class="font-semibold">{{ inline.text }}</strong>
        <a
          v-else-if="inline.kind === 'link'"
          :href="inline.href"
          target="_blank"
          rel="noopener noreferrer"
          class="font-semibold underline"
          >{{ inline.text }}</a
        >
        <template v-else>{{ inline.text }}</template>
      </template>
      <ChangelogItems v-if="item.children.length > 0" :items="item.children" class="mt-2" />
    </li>
  </ul>
</template>
