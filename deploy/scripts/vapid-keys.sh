#!/usr/bin/env bash
# Prints a fresh VAPID key pair for push notifications (D-130), as the .env lines of an environment.
# One pair per environment. Changing it later stops the devices already subscribed until their
# members turn notifications on again: keep it with the other secrets of the .env.
set -euo pipefail

command -v openssl >/dev/null || { echo "openssl is needed" >&2; exit 1; }

b64url() { base64 | tr -d '\n=' | tr '/+' '_-'; }

der=$(mktemp)
trap 'rm -f "$der"' EXIT
# SEC1 DER of a P-256 key: 7 bytes of header, the 32-byte private scalar, then the parameters and
# the 65-byte public point at the very end.
openssl ecparam -name prime256v1 -genkey -noout -outform DER -out "$der"
[ "$(wc -c <"$der")" -eq 121 ] || { echo "Unexpected key format" >&2; exit 1; }

echo "GG_PUSH_PUBLIC_KEY=$(tail -c 65 "$der" | b64url)"
echo "GG_PUSH_PRIVATE_KEY=$(dd if="$der" bs=1 skip=7 count=32 2>/dev/null | b64url)"
