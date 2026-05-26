const fs = require('fs');
const path = 'app/src/main/java/com/example/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

// 1. Screens consistent 16dp padding
// This usually means `.padding(20.dp)` -> `.padding(16.dp)` 
// and `.padding(24.dp)` -> `.padding(16.dp)`
// Let's specifically look for Modifier...padding(20.dp) and padding(24.dp)
content = content.replace(/\.padding\(20\.dp\)/g, ".padding(16.dp)");
content = content.replace(/\.padding\(24\.dp\)/g, ".padding(16.dp)");
// There's a case: .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp)
content = content.replace(/start = 20\.dp, top = 20\.dp, end = 20\.dp/g, "start = 16.dp, top = 16.dp, end = 16.dp");

// 2. Consistent button height 52dp
// We can find `Modifier` inside Button/OutlinedButton/TextButton bodies and add `.height(52.dp)`
// But a safer way is to just look for `.height(X.dp)` in Modifiers or just globally replace `.height(?.dp)` for specific button occurrences? No, there's `Button(` then inside `modifier = Modifier...`.
// If we replace `ButtonDefaults.buttonColors` maybe? No.
// Let's do a simple regex: find `modifier = Modifier` inside Buttons and add `.height(52.dp)`. But some already have `.height(48.dp)` or `.height(56.dp)`.
// Actually, Jetpack Compose Buttons wrap content by default. If they have `.height(...)`, we should change it. If not, we should add it.
content = content.replace(/(Button\(\s*[\s\S]*?modifier = Modifier[\s\S]*?\))/g, (match) => {
    // try to replace existing .height
    if (match.includes('.height(')) {
        return match.replace(/\.height\([0-9]+\.dp\)/g, ".height(52.dp)");
    } else {
        // inject .height(52.dp) right after Modifier
        return match.replace(/modifier = Modifier/, "modifier = Modifier.height(52.dp)");
    }
});
content = content.replace(/(OutlinedButton\(\s*[\s\S]*?modifier = Modifier[\s\S]*?\))/g, (match) => {
    if (match.includes('.height(')) {
        return match.replace(/\.height\([0-9]+\.dp\)/g, ".height(52.dp)");
    } else {
        return match.replace(/modifier = Modifier/, "modifier = Modifier.height(52.dp)");
    }
});
// Be careful with TextButton, usually it doesn't need 52dp unless explicitly requested? "Make sure all buttons have consistent height of 52dp" -> I'll apply to TextButton too just in case.
content = content.replace(/(TextButton\(\s*[\s\S]*?modifier = Modifier[\s\S]*?\))/g, (match) => {
    if (match.includes('.height(')) {
        return match.replace(/\.height\([0-9]+\.dp\)/g, ".height(52.dp)");
    } else {
        return match.replace(/modifier = Modifier/, "modifier = Modifier.height(52.dp)");
    }
});

// Also, some buttons might not even have a modifier argument! Let's find those:
content = content.replace(/Button\([\s\S]*?\)/g, (match) => {
    if (!match.includes('modifier = Modifier')) {
        return match.replace(/Button\(/, "Button(\nmodifier = Modifier.height(52.dp), ");
    }
    return match;
});

// 4. Font sizes: Titles 24.sp, Subtitles 16.sp, Body 14.sp
// These might use Text("...", fontSize = 20.sp) or MaterialTheme.typography.titleLarge
// Wait, the prompt says "Titles: 24sp, Subtitles: 16sp, Body: 14sp".
// We can just replace `fontSize = 20.sp` -> 24.sp etc? Let's check typical usage.

fs.writeFileSync(path, content, 'utf8');
