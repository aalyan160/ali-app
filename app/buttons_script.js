const fs = require('fs');

// Note: we use absolute path /app/src/...
const path = '/app/src/main/java/com/example/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

// Ensure card corners are consistently 12dp rounded.
content = content.replace(/RoundedCornerShape\(\d+\.dp\)/g, "RoundedCornerShape(12.dp)");
// Ensure consistent 16dp horizontal padding.
// We'll just replace padding(20.dp) and padding(24.dp) globally since they are mostly screens.
content = content.replace(/\.padding\(20\.dp\)/g, ".padding(16.dp)");
content = content.replace(/\.padding\(24\.dp\)/g, ".padding(16.dp)");

// And special complex paddings:
content = content.replace(/start = 20\.dp, top = 20\.dp, end = 20\.dp/g, "start = 16.dp, top = 16.dp, end = 16.dp");

// Button height replacements
// We want all buttons to have 52dp height. 
// A button typically starts with `Button(` or `OutlinedButton(` or `TextButton(`, etc.
// But some `Button(` might not have `Modifier.height()`.
content = content.replace(/(Button\([\s\S]*?modifier\s*=\s*Modifier[^\n,]*\))/g, (match) => {
    if (match.includes('.height(')) {
        return match.replace(/\.height\([0-9]+\.dp\)/g, ".height(52.dp)");
    } else {
        return match.replace(/Modifier/, "Modifier.height(52.dp)");
    }
});

fs.writeFileSync(path, content, 'utf8');
console.log("Formatting done via node script!");
