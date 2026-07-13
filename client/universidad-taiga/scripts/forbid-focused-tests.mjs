import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const roots = ['src'];
const focusedTestPattern = /\b(?:fdescribe|fit)\s*\(|\b(?:describe|it|test)\.only\s*\(/;

function* files(directory) {
  for (const entry of readdirSync(directory)) {
    const path = join(directory, entry);
    if (statSync(path).isDirectory()) {
      yield* files(path);
    } else if (path.endsWith('.spec.ts')) {
      yield path;
    }
  }
}

const focusedTests = roots.flatMap((root) => [...files(root)]).filter((file) =>
  focusedTestPattern.test(readFileSync(file, 'utf8')),
);

if (focusedTests.length) {
  console.error(`Focused tests are forbidden:\n${focusedTests.join('\n')}`);
  process.exit(1);
}
