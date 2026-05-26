const fs = require('fs');
const path = 'app/src/main/java/com/example/MainActivity.kt';
let lines = fs.readFileSync(path, 'utf8').split('\n');

// Delete line 4312 (1-indexed) which is 4311 (0-indexed)
// We just check if it contains the exact string we mistakenly added.
if (lines[4311].includes('modifier = Modifier.fillMaxWidth().height(52.dp),')) {
   lines.splice(4311, 1);
}

fs.writeFileSync(path, lines.join('\n'), 'utf8');
