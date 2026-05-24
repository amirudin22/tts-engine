const fs = require("fs")
const path = require("path")

const target = path.join(
  __dirname, "..",
  "node_modules/@capacitor/android/capacitor/build.gradle"
)

let content = fs.readFileSync(target, "utf8")
content = content.replace(/VERSION_21/g, "VERSION_17")
fs.writeFileSync(target, content)

console.log("✅ Capacitor Java version patched to 17")
