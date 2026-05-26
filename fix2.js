const fs = require('fs');
const path = 'app/src/main/java/com/example/MainActivity.kt';
let lines = fs.readFileSync(path, 'utf8').split('\n');

// The offending insertion is:
// `modifier = Modifier.fillMaxWidth().height(52.dp), ` 
// on a line right above `onClick = `
// I can just remove that exact line whenever it's followed by `onClick =` that indicates my faulty insertion. But wait! I also inserted it as exactly:
// `Button(\nmodifier = Modifier.fillMaxWidth().height(52.dp), \n    onClick = ...`

for (let i = 0; i < lines.length - 1; i++) {
    if (lines[i].startsWith('modifier = Modifier.fillMaxWidth().height(52.dp), ') && 
        lines[i+1].trim().startsWith('onClick =')) {
        let contentAfterOnClick = lines[i+2] || "";
        // Wait, did it have `modifier =` later? Let's check for `modifier = ` in the subsequent lines until `)`
        let hasModifierLater = false;
        let pCnt = 1; 
        // simplistic check: if within 15 lines there's a modifier =
        for(let j=i+1; j < Math.min(lines.length, i+30); j++) {
            if (lines[j].includes('modifier = Modifier')) {
               hasModifierLater = true; break;
            }
        }
        
        if (hasModifierLater) {
            lines[i] = ""; // remove the duplicate one I inserted
        }
    }
}

fs.writeFileSync(path, lines.filter(l => l !== "").join('\n'), 'utf8');
