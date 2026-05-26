const fs = require('fs');

const path = 'app/src/main/java/com/example/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

content = content.replace(/RoundedCornerShape\(\d+\.dp\)/g, "RoundedCornerShape(12.dp)");
content = content.replace(/\.padding\(20\.dp\)/g, ".padding(16.dp)");
content = content.replace(/\.padding\(24\.dp\)/g, ".padding(16.dp)");
content = content.replace(/start = 20\.dp, top = 20\.dp, end = 20\.dp/g, "start = 16.dp, top = 16.dp, end = 16.dp");

content = content.replace(/(Button\([\s\S]*?modifier\s*=\s*Modifier[^\n,]*\))/g, (match) => {
    if (match.includes('.height(')) {
        return match.replace(/\.height\([0-9]+\.dp\)/g, ".height(52.dp)");
    } else {
        return match.replace(/Modifier/, "Modifier.height(52.dp)");
    }
});

// Since buttons without modifiers won't match the regex above, let's catch standard buttons missing modifiers.
content = content.replace(/Button\([\s]*(onClick = [\s\S]*?)\n[ ]*([\s\S]{1,10})\)/g, (match, p1, p2) => {
    if (match.includes('modifier')) return match; 
    return `Button(\nmodifier = Modifier.fillMaxWidth().height(52.dp), \n${p1}\n  ${p2})`;
});

fs.writeFileSync(path, content, 'utf8');
console.log("Formatting done!");
