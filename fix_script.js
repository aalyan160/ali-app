const fs = require('fs');
const path = 'app/src/main/java/com/example/MainActivity.kt';
let lines = fs.readFileSync(path, 'utf8').split('\n');

for (let i = 0; i < lines.length; i++) {
    let line = lines[i];
    
    // Fix ProfileStatBox syntax error
    if (line.includes('fun ProfileStatBox(') && line.includes('.height(52.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    
    // Fix CommitOptionButton syntax error
    if (line.includes('fun CommitOptionButton(') && line.includes('.height(52.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    
    // Fix TypingIndicator syntax error
    if (line.includes('fun TypingIndicator(') && line.includes('.height(52.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    
    // Fix Spacers that were incorrectly given 52.dp
    // Spacers usually have 16.dp or 8.dp or 24.dp. 16 is a safe default.
    if (line.includes('Spacer(modifier = Modifier.height(52.dp))')) {
        lines[i] = line.replace('Modifier.height(52.dp)', 'Modifier.height(16.dp)');
    }
    if (line.includes('Spacer(modifier = Modifier.height(52.dp).width(4.dp))')) {
        lines[i] = line.replace('Modifier.height(52.dp)', 'Modifier.height(16.dp)'); // or remove height
    }
    
    // Fix root container that got height 52
    // e.g. modifier = Modifier.height(52.dp).fillMaxSize()
    if (line.includes('.height(52.dp).fillMaxSize()')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    if (line.includes('.height(52.dp).size(280.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    if (line.includes('.height(52.dp).size(24.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    if (line.includes('.height(52.dp).size(56.dp)')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
    
    // Fix random modifiers that got height(52.dp) but were not buttons.
    // E.g. modifier: Modifier.height(52.dp) = Modifier
    if (line.includes('modifier: Modifier.height(52.dp) = Modifier')) {
        lines[i] = line.replace('.height(52.dp)', '');
    }
}

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log("Fixed broken modifiers syntax.");
