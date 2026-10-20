"""CHANGELOG.md for the release workflows (spec §15.2, step 6 and 7).

    python3 changelog.py release 0.2.0 2026-10-20   moves "Non publié" under "## 0.2.0 — 2026-10-20"
    python3 changelog.py notes 0.2.0                 prints that section (GitHub Release notes)
    python3 changelog.py bump 0.1.3 minor            prints the next version (0.2.0)
"""

import re
import sys
from pathlib import Path

CHANGELOG = Path(__file__).resolve().parents[2] / "CHANGELOG.md"
UNRELEASED = "## Non publié"


def sections(text: str) -> list[tuple[int, str]]:
    return [(m.start(), m.group(0)) for m in re.finditer(r"^## .*$", text, re.MULTILINE)]


def section_body(text: str, heading: str) -> str:
    starts = sections(text)
    for index, (start, title) in enumerate(starts):
        if title.strip() == heading:
            end = starts[index + 1][0] if index + 1 < len(starts) else len(text)
            return text[start + len(title) : end].strip("\n")
    sys.exit(f"No section '{heading}' in CHANGELOG.md")


def release(version: str, date: str) -> None:
    text = CHANGELOG.read_text(encoding="utf-8")
    if not re.search(r"^- ", section_body(text, UNRELEASED), re.MULTILINE):
        sys.exit("The 'Non publié' section is empty: nothing to release.")
    if re.search(rf"^## {re.escape(version)} ", text, re.MULTILINE):
        sys.exit(f"CHANGELOG.md already has a {version} section.")
    # The entries stay where they are: a fresh, empty "Non publié" goes above their new title.
    start = text.index(f"{UNRELEASED}\n")
    after = start + len(UNRELEASED)
    text = text[:start] + f"{UNRELEASED}\n\n## {version} — {date}" + text[after:]
    CHANGELOG.write_text(text, encoding="utf-8")


def notes(version: str) -> None:
    text = CHANGELOG.read_text(encoding="utf-8")
    for _, title in sections(text):
        if title.startswith(f"## {version} "):
            print(section_body(text, title.strip()))
            return
    sys.exit(f"No {version} section in CHANGELOG.md")


def bump(current: str, part: str) -> None:
    match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)", current.strip())
    if not match:
        sys.exit(f"VERSION is not X.Y.Z: {current!r}")
    major, minor, patch = map(int, match.groups())
    if part == "major":
        print(f"{major + 1}.0.0")
    elif part == "minor":
        print(f"{major}.{minor + 1}.0")
    elif part == "patch":
        print(f"{major}.{minor}.{patch + 1}")
    else:
        sys.exit(f"Unknown bump {part!r} (patch, minor or major)")


if __name__ == "__main__":
    command, *arguments = sys.argv[1:]
    {"release": release, "notes": notes, "bump": bump}[command](*arguments)
