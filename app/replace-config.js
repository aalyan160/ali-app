module.exports = {
  files: 'app/src/main/java/com/example/MainActivity.kt',
  from: [
    /Button\(\s*onClick = ([^{]*?\{[^}]*\}),\s*colors = ([^,]*),\s*modifier = Modifier\.fillMaxWidth\(\)\s*/g,
    /Button\(\s*onClick = ([^{]*?\{[^}]*\}),\s*colors = ([^,]*),\s*modifier = Modifier\.fillMaxWidth\(\)\.height\([0-9]+\.dp\)\s*/g,
    /Button\(\s*onClick = ([^{]*?\{[^}]*\}),\s*modifier = Modifier\.fillMaxWidth\(\)\s*/g,
  ],
  to: [
    'Button(\nonClick = $1,\ncolors = $2,\nmodifier = Modifier.fillMaxWidth().height(52.dp)\n',
    'Button(\nonClick = $1,\ncolors = $2,\nmodifier = Modifier.fillMaxWidth().height(52.dp)\n',
    'Button(\nonClick = $1,\nmodifier = Modifier.fillMaxWidth().height(52.dp)\n',
  ]
};
