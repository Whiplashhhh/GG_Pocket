/**
 * The push part of the service worker (D-128, D-130), written at build time into `push-sw.js`
 * and loaded by the generated service worker (`importScripts`). The server sends data; the words
 * come from the i18n file, given here, so they stay with every other text of the app.
 */
export interface PushWorkerTexts {
  /** `push.messages`: one title per kind, with {nickname}, {emoji} and {day}. */
  messages: Record<string, string>
  /** `session.day`: « d’aujourd’hui », « d’hier », « du {date} ». */
  day: { today: string; yesterday: string; date: string }
  /** The emoji of each reaction. */
  emoji: Record<string, string>
  /** Title when the kind is unknown (a newer server). */
  fallback: string
}

export function pushWorkerSource(texts: PushWorkerTexts): string {
  return `/* GG Pocket: push notifications (generated, see src/lib/pushWorker.ts). */
const TEXTS = ${JSON.stringify(texts)};

function fill(template, values) {
  return template.replace(/\\{(\\w+)\\}/g, (match, name) => (name in values ? values[name] : ''));
}

function day(climbedOn) {
  if (!climbedOn) return '';
  const zone = { timeZone: 'Europe/Paris' };
  const today = new Intl.DateTimeFormat('en-CA', zone).format(new Date());
  const yesterday = new Intl.DateTimeFormat('en-CA', zone).format(new Date(Date.now() - 864e5));
  if (climbedOn === today) return TEXTS.day.today;
  if (climbedOn === yesterday) return TEXTS.day.yesterday;
  const date = new Intl.DateTimeFormat('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(climbedOn + 'T12:00:00Z'));
  return fill(TEXTS.day.date, { date: date.replace(/^1(?= )/, '1er') });
}

self.addEventListener('push', (event) => {
  let data;
  try {
    data = event.data.json();
  } catch {
    return;
  }
  const url = typeof data.url === 'string' && data.url.startsWith('/') ? data.url : '/';
  const title = fill(TEXTS.messages[data.kind] || TEXTS.fallback, {
    nickname: data.nickname || '',
    emoji: TEXTS.emoji[data.reaction] || '',
    day: day(data.climbedOn),
  });
  event.waitUntil(
    self.registration.showNotification(title.replace(/\\s+/g, ' ').trim(), {
      body: data.excerpt ? '« ' + data.excerpt + ' »' : '',
      icon: '/pwa-192.png',
      badge: '/favicon-48.png',
      lang: 'fr',
      tag: data.kind + ':' + url,
      data: { url },
    }),
  );
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const url = (event.notification.data && event.notification.data.url) || '/';
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
      const open = windows.find((client) => 'focus' in client);
      if (open) {
        open.postMessage({ type: 'gg-open', url });
        return open.focus();
      }
      return self.clients.openWindow(url);
    }),
  );
});
`
}

/** A message from the service worker asking the open app to show a page (a tapped notification). */
export function pageToOpen(message: unknown): string | undefined {
  if (typeof message !== 'object' || message === null) return undefined
  const { type, url } = message as { type?: unknown; url?: unknown }
  return type === 'gg-open' &&
    typeof url === 'string' &&
    url.startsWith('/') &&
    !url.startsWith('//')
    ? url
    : undefined
}
