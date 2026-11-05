#!/usr/bin/env bash
# Points the edge proxy (deploy/edge/compose.yml) at the web image of a release (D-112): its Caddy
# is rebuilt with a fixed Go and scanned by Trivy in CI, unlike the stock caddy image, which the
# server would otherwise keep forever (compose only pulls a missing image). Run on the bundle by
# release.yml before it ships, and by the CI stack test on its own web image.
#
# Usage: pin-edge-image.sh <image>   e.g. ghcr.io/whiplashhhh/gg-pocket-web:1.0.0
set -euo pipefail

image=${1:?usage: pin-edge-image.sh <image>}
[[ $image =~ ^[a-z0-9./_-]+:[A-Za-z0-9._-]+$ ]] || { echo "pin-edge-image: invalid image '$image'" >&2; exit 1; }
compose="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/edge/compose.yml"
grep -q '^    image: ' "$compose" || { echo "pin-edge-image: no image line in $compose" >&2; exit 1; }
sed -i "s|^    image: .*|    image: $image|" "$compose"
echo "Edge proxy image: $image"
