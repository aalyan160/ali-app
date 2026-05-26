const fs = require('fs');
const path = 'app/src/main/java/com/example/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

// 3. Card corners 12dp rounded:
content = content.replace(/RoundedCornerShape\(24\.dp\)/g, "RoundedCornerShape(12.dp)");
content = content.replace(/RoundedCornerShape\(16\.dp\)/g, "RoundedCornerShape(12.dp)");
content = content.replace(/RoundedCornerShape\(22\.dp\)/g, "RoundedCornerShape(12.dp)");
content = content.replace(/RoundedCornerShape\(8\.dp\)/g, "RoundedCornerShape(12.dp)");

// 2. Buttons 52dp height
// Example modifying buttons to have consistent height.
// Let's replace Button( and \n modifier = Modifier... with height(52.dp)
// This might be tricky because Modifier could be on top or missing.
// A simpler way: we just find "Button(" or "OutlinedButton(" or "TextButton("? 
// The prompt says "Make sure all buttons have consistent height of 52dp"

fs.writeFileSync(path, content, 'utf8');
console.log("Updated borders.");
