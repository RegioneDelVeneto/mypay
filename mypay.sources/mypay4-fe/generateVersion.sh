#!/bin/sh

HASH=$(git rev-parse --short HEAD)
BRANCH=$(git rev-parse --abbrev-ref HEAD)
TAG=$(git --no-pager tag --points-at HEAD | tail -n 1)
NOW=$(TZ=Europe/Rome date +%FT%T%:z)

cat >version.ts <<EOL
// THIS FILE IS GENERATED AUTOMATICALLY DURING BUILD
// SHOULD NOT BE EDITED MANUALLY
import { version } from '../../../../../package.json';
export const versionInfo = {
  buildTime:'${NOW}',
  gitHash:'${HASH}',
  branchName:'${BRANCH}',
  tag:'${TAG}',
  version:version
};
EOL
