#!/bin/sh

HASH=$(git rev-parse --short HEAD)
BRANCH=$(git rev-parse --abbrev-ref HEAD)
NOW=$(TZ=Europe/Rome date +%FT%T%:z)

cat >version.ts <<EOL
// THIS FILE IS GENERATED AUTOMATICALLY DURING BUILD
// SHOULD NOT BE EDITED MANUALLY
import { version } from '../../../../../package.json';
export const versionInfo = {
  buildTime:'${NOW}',
  gitHash:'${HASH}',
  branchName:'${BRANCH}',
  version:version
};
EOL
