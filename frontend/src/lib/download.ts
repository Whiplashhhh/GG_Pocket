/** "gg-pocket-mes-donnees-2026-10-26.json": the day of the export, in the member's time zone. */
export function myDataFileName(day: Date): string {
  const pad = (value: number) => String(value).padStart(2, '0')
  const date = `${String(day.getFullYear())}-${pad(day.getMonth() + 1)}-${pad(day.getDate())}`
  return `gg-pocket-mes-donnees-${date}.json`
}

/** Hands a file over to the browser, which saves it (or offers to, on a phone). */
export function saveFile(file: Blob, name: string): void {
  const url = URL.createObjectURL(file)
  const link = document.createElement('a')
  link.href = url
  link.download = name
  link.rel = 'noopener'
  document.body.append(link)
  link.click()
  link.remove()
  // Safari reads the file after the click returns: let it finish before releasing it.
  setTimeout(() => {
    URL.revokeObjectURL(url)
  }, 30_000)
}
