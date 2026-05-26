const fs = require('fs');

const path = 'app/src/main/java/com/example/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

// 1. Padding 16dp: replace padding(20.dp) to padding(16.dp), padding(24.dp) to padding(16.dp).
// But let's be careful. The prompt says "Make sure all screens have consistent 16dp padding on left and right".
// "Screens" usually have `Modifier.fillMaxSize().padding(...)`. Let's look for padding values. 
// Or better, let's just do a regex replace over all rounded corners first.

// 3. Card corners 12dp rounded:
// We can replace RoundedCornerShape(24.dp) and RoundedCornerShape(16.dp) with RoundedCornerShape(12.dp)
content = content.replace(/RoundedCornerShape\(24\.dp\)/g, "RoundedCornerShape(12.dp)");
content = content.replace(/RoundedCornerShape\(16\.dp\)/g, "RoundedCornerShape(12.dp)");

// Let's write back
fs.writeFileSync(path, content, 'utf8');
console.log("Updated borders.");
