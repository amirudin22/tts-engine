const fs = require("fs")
const path = require("path")

const targets = [
  // Capacitor library in node_modules
  path.join(__dirname, "..", "node_modules/@capacitor/android/capacitor/build.gradle"),
  // Generated app config (overwritten on cap sync)
  path.join(__dirname, "..", "android/app/capacitor.build.gradle"),
]

for (const target of targets) {
  try {
    let content = fs.readFileSync(target, "utf8")
    if (content.includes("VERSION_21")) {
      content = content.replace(/VERSION_21/g, "VERSION_17")
      fs.writeFileSync(target, content)
      console.log(`✅ Patched ${path.relative(__dirname + "/..", target)}`)
    } else {
      console.log(`ℹ️  Already patched: ${path.relative(__dirname + "/..", target)}`)
    }
  } catch {
    console.log(`ℹ️  Not found: ${target}`)
  }
}
